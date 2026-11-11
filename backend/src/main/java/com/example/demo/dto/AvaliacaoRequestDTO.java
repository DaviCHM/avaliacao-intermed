package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// O email de quem criou NAO vem no body: e lido do token de quem esta logado.
public record AvaliacaoRequestDTO(

        @NotBlank
        String projeto,

        @NotBlank
        String titulo,

        String descricao,

        @NotNull
        @Min(0)
        @Max(10)
        Integer prioridade
) {
}
