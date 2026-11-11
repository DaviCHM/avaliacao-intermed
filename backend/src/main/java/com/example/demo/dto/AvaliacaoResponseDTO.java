package com.example.demo.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Serializable: necessario para guardar no cache do Redis
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvaliacaoResponseDTO implements Serializable {

    private Long id;
    private String projeto;
    private String titulo;
    private String descricao;
    private Integer prioridade;
    private String emailCriador;
    private LocalDateTime dataCriacao;
}
