package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.example.demo.dto.AvaliacaoRequestDTO;
import com.example.demo.dto.AvaliacaoResponseDTO;
import com.example.demo.exception.RecursoNaoEncontradoException;
import com.example.demo.model.Avaliacao;
import com.example.demo.repository.AvaliacaoRepository;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    private AvaliacaoService avaliacaoService;

    @BeforeEach
    void setUp() {
        avaliacaoService = new AvaliacaoService(avaliacaoRepository);
    }

    @Test
    void deveListarTodasAsTarefas() {
        Avaliacao tarefa = Avaliacao.builder()
                .id(1L).projeto("P").titulo("T").prioridade(5)
                .emailCriador("a@a.com").dataCriacao(LocalDateTime.now()).build();
        when(avaliacaoRepository.findAll(any(Sort.class))).thenReturn(List.of(tarefa));

        List<AvaliacaoResponseDTO> resultado = avaliacaoService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTitulo()).isEqualTo("T");
    }

    @Test
    void deveCriarTarefaComEmailDoCriadorEData() {
        AvaliacaoRequestDTO requestDTO = new AvaliacaoRequestDTO("Projeto X", "Titulo", "Desc", 9);
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(invocation -> {
            Avaliacao a = invocation.getArgument(0);
            a.setId(1L);
            return a;
        });

        AvaliacaoResponseDTO resultado = avaliacaoService.criar(requestDTO, "user@teste.com");

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getEmailCriador()).isEqualTo("user@teste.com");
        assertThat(resultado.getPrioridade()).isEqualTo(9);
        assertThat(resultado.getDataCriacao()).isNotNull();
        verify(avaliacaoRepository, times(1)).save(any(Avaliacao.class));
    }

    @Test
    void deveLancar404AoDeletarInexistente() {
        when(avaliacaoRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> avaliacaoService.deletar(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
