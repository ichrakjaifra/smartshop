package com.smartshop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "paiements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paiement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @Column(nullable = false)
    private Integer numeroPaiement;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType typePaiement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus statut;

    @Builder.Default
    private LocalDate datePaiement = LocalDate.now();

    private LocalDate dateEncaissement;

    // Champs spécifiques selon le type de paiement
    private String numeroCheque;
    private String banqueCheque;
    private LocalDate echeanceCheque;

    private String referenceVirement;
    private String banqueVirement;

    private String numeroRecu;

    public void validerPaiementEspeces() {
        if (typePaiement == PaymentType.ESPECES && montant.compareTo(new BigDecimal("20000")) > 0) {
            throw new RuntimeException("Limite légale de 20,000 DH dépassée pour les espèces");
        }
    }
}


