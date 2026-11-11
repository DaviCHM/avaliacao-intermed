package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AvaliacaoRequestDTO;
import com.example.demo.dto.AvaliacaoResponseDTO;
import com.example.demo.service.AvaliacaoService;

import jakarta.validation.Valid;

// Todas as rotas exigem token do Auth0 (regra no SecurityConfig)
@RestController
@RequestMapping("/tarefa")
public class AvaliacaoController {

    // Claim colocada no token pela Action do Auth0
    private static final String EMAIL_CLAIM = "https://avaliacoes-api/email";

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @GetMapping
    public ResponseEntity<List<AvaliacaoResponseDTO>> listar() {
        return ResponseEntity.ok(avaliacaoService.listar());
    }

    @PostMapping
    public ResponseEntity<AvaliacaoResponseDTO> criar(@Valid @RequestBody AvaliacaoRequestDTO requestDTO,
                                                   @AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString(EMAIL_CLAIM);
        if (email == null) {
            email = jwt.getSubject();
        }
        AvaliacaoResponseDTO criada = avaliacaoService.criar(requestDTO, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        avaliacaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
