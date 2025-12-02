package com.smartshop.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaiementRequestDTO {
    @NotNull(message = "Commande ID est obligatoire")
    private Long commandeId;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal montant;

    @NotNull(message = "Le type de paiement est obligatoire")
    private String typePaiement;

    private String numeroCheque;
    private String banqueCheque;
    private LocalDate echeanceCheque;

    private String referenceVirement;
    private String banqueVirement;

    private String numeroRecu;
}