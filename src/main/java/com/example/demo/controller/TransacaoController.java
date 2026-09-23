package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.TransacaoRequestDTO;
import com.example.demo.dto.TransacaoResponseDTO;
import com.example.demo.service.TransacaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/transacao")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @GetMapping
    public ResponseEntity<List<TransacaoResponseDTO>> listar(
            @RequestParam(required = false) Long idCliente) {
        return ResponseEntity.ok(transacaoService.listar(idCliente));
    }

    @PostMapping
    public ResponseEntity<TransacaoResponseDTO> criar(@Valid @RequestBody TransacaoRequestDTO requestDTO) {
        TransacaoResponseDTO criada = transacaoService.criar(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        transacaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
