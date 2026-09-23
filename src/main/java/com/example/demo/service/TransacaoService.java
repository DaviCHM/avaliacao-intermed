package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.example.demo.dto.ClienteApiResponseDTO;
import com.example.demo.dto.TransacaoRequestDTO;
import com.example.demo.dto.TransacaoResponseDTO;
import com.example.demo.exception.RecursoNaoEncontradoException;
import com.example.demo.model.Transacao;
import com.example.demo.repository.TransacaoRepository;

@Service
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final RestClient restClient;

    @Value("${users.api.base-url}")
    private String usersApiBaseUrl;

    public TransacaoService(TransacaoRepository transacaoRepository, RestClient.Builder restClientBuilder) {
        this.transacaoRepository = transacaoRepository;
        this.restClient = restClientBuilder.build();
    }

    public List<TransacaoResponseDTO> listar(Long idCliente) {
        List<Transacao> transacoes = (idCliente != null)
                ? transacaoRepository.findByIdCliente(idCliente)
                : transacaoRepository.findAll();

        return transacoes.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public TransacaoResponseDTO criar(TransacaoRequestDTO requestDTO) {
        String emailCliente = buscarEmailCliente(requestDTO.idCliente());

        BigDecimal valorTotal = requestDTO.precoUnitario()
                .multiply(BigDecimal.valueOf(requestDTO.quantidade()));

        Transacao transacao = Transacao.builder()
                .idCliente(requestDTO.idCliente())
                .emailCliente(emailCliente)
                .codigoAcao(requestDTO.codigoAcao())
                .quantidade(requestDTO.quantidade())
                .precoUnitario(requestDTO.precoUnitario())
                .valorTotal(valorTotal)
                .dataTransacao(LocalDateTime.now())
                .build();

        Transacao salva = transacaoRepository.save(transacao);
        return toResponseDTO(salva);
    }

    public void deletar(Long id) {
        if (!transacaoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Transacao nao encontrada com id: " + id);
        }
        transacaoRepository.deleteById(id);
    }

    private String buscarEmailCliente(Long idCliente) {
        try {
            ClienteApiResponseDTO cliente = restClient.get()
                    .uri(usersApiBaseUrl + "/users/{idCliente}", idCliente)
                    .retrieve()
                    .body(ClienteApiResponseDTO.class);

            return cliente != null ? cliente.email() : null;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new RecursoNaoEncontradoException("Cliente nao encontrado com id: " + idCliente);
        }
    }

    private TransacaoResponseDTO toResponseDTO(Transacao transacao) {
        return TransacaoResponseDTO.builder()
                .id(transacao.getId())
                .idCliente(transacao.getIdCliente())
                .emailCliente(transacao.getEmailCliente())
                .codigoAcao(transacao.getCodigoAcao())
                .quantidade(transacao.getQuantidade())
                .precoUnitario(transacao.getPrecoUnitario())
                .valorTotal(transacao.getValorTotal())
                .dataTransacao(transacao.getDataTransacao())
                .build();
    }
}
