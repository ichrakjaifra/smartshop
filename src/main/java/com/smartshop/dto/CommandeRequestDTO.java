package com.smartshop.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
public class CommandeRequestDTO {
    @NotNull(message = "Client ID est obligatoire")
    private Long clientId;

    @Size(min = 1, message = "La commande doit contenir au moins un produit")
    private List<OrderItemDTO> items;

    @Pattern(regexp = "PROMO-[A-Z0-9]{4}|^$",
            message = "Format code promo invalide. Format: PROMO-XXXX (4 caractères majuscules/chiffres)")
    private String codePromo;
}

