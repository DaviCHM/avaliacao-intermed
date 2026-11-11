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
import com.example.demo.exception.RecursoNaoEncontradoException;
import com.example.demo.model.Avaliacao;
import com.example.demo.repository.AvaliacaoRepository;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
    }

    // Guarda a listagem no cache (Redis em producao)
    @Cacheable("tarefas")
    public List<AvaliacaoResponseDTO> listar() {
        return new ArrayList<>(avaliacaoRepository.findAll(Sort.by("id")).stream()
                .map(this::toResponseDTO)
                .toList());
    }

    // Limpa o cache da listagem sempre que cadastra
    @CacheEvict(value = "tarefas", allEntries = true)
    public AvaliacaoResponseDTO criar(AvaliacaoRequestDTO requestDTO, String emailCriador) {
        Avaliacao avaliacao = Avaliacao.builder()
                .projeto(requestDTO.projeto())
                .titulo(requestDTO.titulo())
                .descricao(requestDTO.descricao())
                .prioridade(requestDTO.prioridade())
                .emailCriador(emailCriador)
                .dataCriacao(LocalDateTime.now())
                .build();

        return toResponseDTO(avaliacaoRepository.save(avaliacao));
    }

    // Limpa o cache da listagem sempre que deleta
    @CacheEvict(value = "tarefas", allEntries = true)
    public void deletar(Long id) {
        if (!avaliacaoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Tarefa nao encontrada com id: " + id);
        }
        avaliacaoRepository.deleteById(id);
    }

    private AvaliacaoResponseDTO toResponseDTO(Avaliacao avaliacao) {
        return AvaliacaoResponseDTO.builder()
                .id(avaliacao.getId())
                .projeto(avaliacao.getProjeto())
                .titulo(avaliacao.getTitulo())
                .descricao(avaliacao.getDescricao())
                .prioridade(avaliacao.getPrioridade())
                .emailCriador(avaliacao.getEmailCriador())
                .dataCriacao(avaliacao.getDataCriacao())
                .build();
    }
}
