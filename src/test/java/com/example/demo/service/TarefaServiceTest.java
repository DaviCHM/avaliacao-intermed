package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dto.TarefaRequestDTO;
import com.example.demo.dto.TarefaResponseDTO;
import com.example.demo.exception.RecursoNaoEncontradoException;
import com.example.demo.model.PrioridadeTarefa;
import com.example.demo.model.StatusTarefa;
import com.example.demo.model.Tarefa;
import com.example.demo.observer.TarefaObserver;
import com.example.demo.repository.TarefaRepository;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock
    private TarefaRepository tarefaRepository;

    private TarefaObserver observer;

    private TarefaService tarefaService;

    @BeforeEach
    void setUp() {
        observer = mock(TarefaObserver.class);
        tarefaService = new TarefaService(tarefaRepository, List.of(observer));
    }

    @Test
    void deveListarTodasAsTarefas() {
        Tarefa tarefa = tarefaExistente();
        when(tarefaRepository.findAll()).thenReturn(List.of(tarefa));

        List<TarefaResponseDTO> resultado = tarefaService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(tarefa.getId());
        assertThat(resultado.get(0).getTitulo()).isEqualTo(tarefa.getTitulo());
    }

    @Test
    void deveBuscarTarefaPorId() {
        Tarefa tarefa = tarefaExistente();
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));

        TarefaResponseDTO resultado = tarefaService.buscarPorId(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getStatus()).isEqualTo(StatusTarefa.TODO);
        assertThat(resultado.getPrioridade()).isEqualTo(PrioridadeTarefa.ALTA);
    }

    @Test
    void deveLancarExcecaoAoBuscarTarefaInexistente() {
        when(tarefaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tarefaService.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deveCriarTarefaENotificarObservers() {
        TarefaRequestDTO requestDTO = new TarefaRequestDTO("Titulo", "Descricao", PrioridadeTarefa.ALTA);

        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(invocation -> {
            Tarefa tarefa = invocation.getArgument(0);
            tarefa.setId(1L);
            return tarefa;
        });

        TarefaResponseDTO resultado = tarefaService.criar(requestDTO);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getTitulo()).isEqualTo("Titulo");
        assertThat(resultado.getStatus()).isEqualTo(StatusTarefa.TODO);
        assertThat(resultado.getPrioridade()).isEqualTo(PrioridadeTarefa.ALTA);
        assertThat(resultado.getDataCriacao()).isNotNull();

        verify(observer, times(1)).onTarefaCriada(any(Tarefa.class));
    }

    @Test
    void deveExcluirTarefaENotificarObservers() {
        Tarefa tarefa = tarefaExistente();
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));

        tarefaService.excluir(1L);

        verify(tarefaRepository, times(1)).deleteById(1L);
        verify(observer, times(1)).onTarefaExcluida(tarefa);
    }

    @Test
    void deveLancarExcecaoAoExcluirTarefaInexistente() {
        when(tarefaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tarefaService.excluir(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("99");

        verify(tarefaRepository, never()).deleteById(any());
        verify(observer, never()).onTarefaExcluida(any());
    }

    private Tarefa tarefaExistente() {
        return Tarefa.builder()
                .id(1L)
                .titulo("Titulo")
                .descricao("Descricao")
                .status(StatusTarefa.TODO)
                .prioridade(PrioridadeTarefa.ALTA)
                .dataCriacao(java.time.LocalDateTime.now())
                .build();
    }
}
