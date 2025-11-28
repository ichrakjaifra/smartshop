package com.smartshop.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommandeResponseDTO {
    private Long id;
    private Long clientId;
    private String clientNom;
    private LocalDateTime dateCreation;
    private BigDecimal sousTotalHT;
    private BigDecimal montantRemise;
    private BigDecimal montantHTApresRemise;
    private BigDecimal tva;
    private BigDecimal totalTTC;
    private BigDecimal montantRestant;
    private String codePromo;
    private String statut;
    private List<OrderItemResponseDTO> items;
    private List<PaiementResponseDTO> paiements;
}

@Data
class OrderItemResponseDTO {
    private Long productId;
    private String productNom;
    private Integer quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal totalLigne;
}

@Data
class PaiementResponseDTO {
    private Long id;
    private Integer numeroPaiement;
    private BigDecimal montant;
    private String typePaiement;
    private String statut;
    private String reference;
}