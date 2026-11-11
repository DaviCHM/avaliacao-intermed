package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
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
    void deveListarTodasAsAvaliacoes() {
        Avaliacao avaliacao = avaliacaoExistente();
        when(avaliacaoRepository.findAll(any(Sort.class))).thenReturn(List.of(avaliacao));

        List<AvaliacaoResponseDTO> resultado = avaliacaoService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(1L);
        assertThat(resultado.get(0).getNota()).isEqualTo(8);
    }

    @Test
    void deveCriarAvaliacaoComEmailDoAvaliadorEData() {
        AvaliacaoRequestDTO requestDTO = new AvaliacaoRequestDTO("fulano@empresa.com", "Bom", 9);

        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(invocation -> {
            Avaliacao avaliacao = invocation.getArgument(0);
            avaliacao.setId(1L);
            return avaliacao;
        });

        AvaliacaoResponseDTO resultado = avaliacaoService.criar(requestDTO, "admin@teste.com");

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getEmailAvaliador()).isEqualTo("admin@teste.com");
        assertThat(resultado.getEmailAvaliado()).isEqualTo("fulano@empresa.com");
        assertThat(resultado.getNota()).isEqualTo(9);
        assertThat(resultado.getDataAvaliacao()).isNotNull();

        verify(avaliacaoRepository, times(1)).save(any(Avaliacao.class));
    }

    private Avaliacao avaliacaoExistente() {
        return Avaliacao.builder()
                .id(1L)
                .emailAvaliador("admin@teste.com")
                .emailAvaliado("fulano@empresa.com")
                .comentarios("Comentario")
                .nota(8)
                .dataAvaliacao(LocalDateTime.now())
                .build();
    }
}
