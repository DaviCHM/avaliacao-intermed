package com.example.demo.observer;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.example.demo.model.Auditoria;
import com.example.demo.model.Tarefa;
import com.example.demo.model.TipoOperacao;
import com.example.demo.repository.AuditoriaRepository;

@Component
public class AuditoriaObserver implements TarefaObserver {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaObserver(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public void onTarefaCriada(Tarefa tarefa) {
        registrar(tarefa, TipoOperacao.CREATE);
    }

    @Override
    public void onTarefaExcluida(Tarefa tarefa) {
        registrar(tarefa, TipoOperacao.DELETE);
    }

    private void registrar(Tarefa tarefa, TipoOperacao tipoOperacao) {
        Auditoria auditoria = Auditoria.builder()
                .tarefaId(tarefa.getId())
                .tipoOperacao(tipoOperacao)
                .timestamp(LocalDateTime.now())
                .build();

        auditoriaRepository.save(auditoria);
    }
}
