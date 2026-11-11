package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AvaliacaoRequestDTO;
import com.example.demo.dto.AvaliacaoResponseDTO;
import com.example.demo.service.AvaliacaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/avaliacoes")
public class AvaliacaoController {

    // Claim colocada no token pela Action do Auth0
    private static final String EMAIL_CLAIM = "https://avaliacoes-api/email";

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    // ADMIN e USER (regra no SecurityConfig)
    @GetMapping
    public ResponseEntity<List<AvaliacaoResponseDTO>> listar() {
        return ResponseEntity.ok(avaliacaoService.listar());
    }

    // Somente ADMIN (regra no SecurityConfig)
    @PostMapping
    public ResponseEntity<AvaliacaoResponseDTO> criar(@Valid @RequestBody AvaliacaoRequestDTO requestDTO,
                                                      @AuthenticationPrincipal Jwt jwt) {
        String emailAvaliador = jwt.getClaimAsString(EMAIL_CLAIM);
        AvaliacaoResponseDTO criada = avaliacaoService.criar(requestDTO, emailAvaliador);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }
}
