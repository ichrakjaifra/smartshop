package com.smartshop.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaiementResponseDTO {
    private Long id;
    private Integer numeroPaiement;
    private BigDecimal montant;
    private String typePaiement;
    private String statut;
    private LocalDate datePaiement;
    private LocalDate dateEncaissement;
    private String reference;

    // Références spécifiques
    private String numeroRecu;
    private String numeroCheque;
    private String banqueCheque;
    private LocalDate echeanceCheque;
    private String referenceVirement;
    private String banqueVirement;

    // Seulement l'ID de la commande, pas l'objet complet
    private Long commandeId;
}
