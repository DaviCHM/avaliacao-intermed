package com.example.demo.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.demo.model.PrioridadeTarefa;
import com.example.demo.model.Tarefa;

@Component
public class NotificacaoObserver implements TarefaObserver {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoObserver.class);

    @Override
    public void onTarefaCriada(Tarefa tarefa) {
        if (tarefa.getPrioridade() == PrioridadeTarefa.ALTA) {
            log.warn("ALERTA: tarefa de prioridade ALTA criada -> id={}, titulo={}", tarefa.getId(), tarefa.getTitulo());
        }
    }

    @Override
    public void onTarefaExcluida(Tarefa tarefa) {
        // Notificacao de prioridade ALTA aplica-se apenas a criacao de tarefas.
    }
}
