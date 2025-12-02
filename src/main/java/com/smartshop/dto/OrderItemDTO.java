package com.smartshop.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemDTO {
    @NotNull(message = "Product ID est obligatoire")
    private Long productId;

    @NotNull(message = "La quantité est obligatoire")
    private Integer quantite;
}
