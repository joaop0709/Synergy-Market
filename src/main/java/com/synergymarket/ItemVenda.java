package com.synergymarket.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemVendaDTO {
    private Long id;

    @NotNull(message = "Produto é obrigatório")
    private Long produtoId;
    private String produtoNome;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade mínima é 1")
    private Integer quantidade;

    private BigDecimal precoUnitario;
    private BigDecimal subtotal;
}
