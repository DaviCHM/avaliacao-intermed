package com.example.demo.dto;

import com.example.demo.model.PrioridadeTarefa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TarefaRequestDTO(

        @NotBlank
        String titulo,

        @NotBlank
        String descricao,

        @NotNull
        PrioridadeTarefa prioridade
) {
}
