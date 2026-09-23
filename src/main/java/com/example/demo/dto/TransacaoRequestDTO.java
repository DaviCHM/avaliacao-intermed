package com.example.demo.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransacaoRequestDTO(

        @NotNull
        Long idCliente,

        @NotBlank
        String codigoAcao,

        @NotNull
        @Positive
        Integer quantidade,

        @NotNull
        @Positive
        BigDecimal precoUnitario
) {
}
