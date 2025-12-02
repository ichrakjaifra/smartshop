package com.smartshop.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaiementResponseDTO {
    private Long id;
    private Integer numeroPaiement;
    private BigDecimal montant;
    private String typePaiement;
    private String statut;
    private String reference;
}
