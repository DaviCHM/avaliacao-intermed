package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
public class TransacaoResponseDTO {

    private Long id;
    private Long idCliente;
    private String emailCliente;
    private String codigoAcao;
    private Integer quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal valorTotal;
    private LocalDateTime dataTransacao;
}
