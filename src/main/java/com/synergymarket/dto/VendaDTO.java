package com.synergymarket.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VendaDTO {
    private Long id;
    private LocalDateTime data;
    private BigDecimal valorTotal;

    @NotNull(message = "Cliente é obrigatório")
    private Long clienteId;
    private String clienteNome;

    @NotNull(message = "Pelo menos um item é obrigatório")
    @Size(min = 1, message = "A venda deve ter ao menos um item")
    private List<ItemVendaDTO> itens;
}
