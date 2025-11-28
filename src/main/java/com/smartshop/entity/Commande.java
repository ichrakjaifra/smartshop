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

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus statut = OrderStatus.PENDING;



    public boolean estEntierementPayee() {
        return montantRestant != null && montantRestant.compareTo(BigDecimal.ZERO) == 0;
    }
}


