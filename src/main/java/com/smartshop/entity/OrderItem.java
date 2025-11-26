package com.smartshop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product produit;

    @Column(nullable = false)
    private Integer quantite;

    @Column(precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalLigne;

    @PrePersist
    @PreUpdate
    public void calculerTotalLigne() {
        if (prixUnitaire != null && quantite != null) {
            this.totalLigne = prixUnitaire.multiply(BigDecimal.valueOf(quantite))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
        }
    }
}
