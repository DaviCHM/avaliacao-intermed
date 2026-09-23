package com.example.demo.dto;

import java.time.LocalDateTime;

import com.example.demo.model.PrioridadeTarefa;
import com.example.demo.model.StatusTarefa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarefaResponseDTO {

    private Long id;
    private String titulo;
    private String descricao;
    private StatusTarefa status;
    private PrioridadeTarefa prioridade;
    private LocalDateTime dataCriacao;
}
