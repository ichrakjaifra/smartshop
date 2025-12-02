package com.smartshop.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
public class CommandeRequestDTO {
    @NotNull(message = "Client ID est obligatoire")
    private Long clientId;

    @Size(min = 1, message = "La commande doit contenir au moins un produit")
    private List<OrderItemDTO> items;

    private String codePromo;
}

