package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.TarefaRequestDTO;
import com.example.demo.dto.TarefaResponseDTO;
import com.example.demo.exception.RecursoNaoEncontradoException;
import com.example.demo.model.StatusTarefa;
import com.example.demo.model.Tarefa;
import com.example.demo.observer.TarefaObserver;
import com.example.demo.repository.TarefaRepository;

@Service
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final List<TarefaObserver> observers;

    public TarefaService(TarefaRepository tarefaRepository, List<TarefaObserver> observers) {
        this.tarefaRepository = tarefaRepository;
        this.observers = observers;
    }

    public List<TarefaResponseDTO> listar() {
        return tarefaRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public TarefaResponseDTO buscarPorId(Long id) {
        Tarefa tarefa = obterOuFalhar(id);
        return toResponseDTO(tarefa);
    }

    public TarefaResponseDTO criar(TarefaRequestDTO requestDTO) {
        Tarefa tarefa = Tarefa.builder()
                .titulo(requestDTO.titulo())
                .descricao(requestDTO.descricao())
                .prioridade(requestDTO.prioridade())
                .status(StatusTarefa.TODO)
                .dataCriacao(LocalDateTime.now())
                .build();

        Tarefa salva = tarefaRepository.save(tarefa);

        observers.forEach(observer -> observer.onTarefaCriada(salva));

        return toResponseDTO(salva);
    }

    public void excluir(Long id) {
        Tarefa tarefa = obterOuFalhar(id);

        tarefaRepository.deleteById(id);

        observers.forEach(observer -> observer.onTarefaExcluida(tarefa));
    }

    private Tarefa obterOuFalhar(Long id) {
        return tarefaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tarefa nao encontrada com id: " + id));
    }

    private TarefaResponseDTO toResponseDTO(Tarefa tarefa) {
        return TarefaResponseDTO.builder()
                .id(tarefa.getId())
                .titulo(tarefa.getTitulo())
                .descricao(tarefa.getDescricao())
                .status(tarefa.getStatus())
                .prioridade(tarefa.getPrioridade())
                .dataCriacao(tarefa.getDataCriacao())
                .build();
    }
}
