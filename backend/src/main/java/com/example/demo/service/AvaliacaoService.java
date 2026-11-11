package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.demo.dto.AvaliacaoRequestDTO;
import com.example.demo.dto.AvaliacaoResponseDTO;
import com.example.demo.model.Avaliacao;
import com.example.demo.repository.AvaliacaoRepository;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
    }

    // Guarda a listagem no cache (Redis em producao)
    @Cacheable("avaliacoes")
    public List<AvaliacaoResponseDTO> listar() {
        return new ArrayList<>(avaliacaoRepository.findAll(Sort.by("id")).stream()
                .map(this::toResponseDTO)
                .toList());
    }

    // Limpa o cache da listagem sempre que cadastra
    @CacheEvict(value = "avaliacoes", allEntries = true)
    public AvaliacaoResponseDTO criar(AvaliacaoRequestDTO requestDTO, String emailAvaliador) {
        Avaliacao avaliacao = Avaliacao.builder()
                .emailAvaliador(emailAvaliador)
                .emailAvaliado(requestDTO.emailAvaliado())
                .comentarios(requestDTO.comentarios())
                .nota(requestDTO.nota())
                .dataAvaliacao(LocalDateTime.now())
                .build();

        Avaliacao salva = avaliacaoRepository.save(avaliacao);

        return toResponseDTO(salva);
    }

    private AvaliacaoResponseDTO toResponseDTO(Avaliacao avaliacao) {
        return AvaliacaoResponseDTO.builder()
                .id(avaliacao.getId())
                .emailAvaliador(avaliacao.getEmailAvaliador())
                .emailAvaliado(avaliacao.getEmailAvaliado())
                .comentarios(avaliacao.getComentarios())
                .nota(avaliacao.getNota())
                .dataAvaliacao(avaliacao.getDataAvaliacao())
                .build();
    }
}
