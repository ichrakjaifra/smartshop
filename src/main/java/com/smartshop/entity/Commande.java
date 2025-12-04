package com.smartshop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commandes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Paiement> paiements = new ArrayList<>();

    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(precision = 12, scale = 2)
    private BigDecimal sousTotalHT;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantRemise = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal montantHTApresRemise;

    @Column(precision = 12, scale = 2)
    private BigDecimal tva;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalTTC;

    @Column(precision = 12, scale = 2)
    private BigDecimal montantRestant;

    private String codePromo;

    @Column(name = "code_promo_utilise")
    private Boolean codePromoUtilise = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus statut = OrderStatus.PENDING;

    @PrePersist
    @PreUpdate
    public void calculerTotaux() {
        // Sous-total HT
        this.sousTotalHT = items.stream()
                .map(OrderItem::getTotalLigne)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        // Application des remises
        BigDecimal remiseFidelite = calculerRemiseFidelite();
        BigDecimal remisePromo = calculerRemisePromo();
        this.montantRemise = remiseFidelite.add(remisePromo)
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        // Montant HT après remise
        this.montantHTApresRemise = sousTotalHT.subtract(montantRemise)
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        // TVA (20%)
        this.tva = montantHTApresRemise.multiply(new BigDecimal("0.20"))
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        // Total TTC
        this.totalTTC = montantHTApresRemise.add(tva)
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        // Montant restant
        BigDecimal totalPaye = paiements.stream()
                .filter(p -> p.getStatut() == PaymentStatus.ENCAISSÉ)
                .map(Paiement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.montantRestant = totalTTC.subtract(totalPaye)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal calculerRemiseFidelite() {
        if (client == null) return BigDecimal.ZERO;

        CustomerTier niveau = client.getNiveauFidelite();
        if (niveau == CustomerTier.SILVER && sousTotalHT.compareTo(new BigDecimal("500")) >= 0) {
            return sousTotalHT.multiply(new BigDecimal("0.05"));
        } else if (niveau == CustomerTier.GOLD && sousTotalHT.compareTo(new BigDecimal("800")) >= 0) {
            return sousTotalHT.multiply(new BigDecimal("0.10"));
        } else if (niveau == CustomerTier.PLATINUM && sousTotalHT.compareTo(new BigDecimal("1200")) >= 0) {
            return sousTotalHT.multiply(new BigDecimal("0.15"));
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculerRemisePromo() {
        if (codePromo != null && !codePromo.trim().isEmpty() && !codePromoUtilise) {
            return sousTotalHT.multiply(new BigDecimal("0.05"))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public boolean estEntierementPayee() {
        return montantRestant != null && montantRestant.compareTo(BigDecimal.ZERO) == 0;
    }
}


