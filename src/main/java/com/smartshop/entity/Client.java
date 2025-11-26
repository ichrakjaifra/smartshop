package com.smartshop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(unique = true, nullable = false)
    private String email;

    private String telephone;
    private String adresse;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CustomerTier niveauFidelite = CustomerTier.BASIC;

    @Builder.Default
    private Integer totalCommandes = 0;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantCumule = BigDecimal.ZERO;

    private LocalDate datePremiereCommande;
    private LocalDate dateDerniereCommande;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Commande> commandes = new ArrayList<>();

    public void incrementerCommandes(BigDecimal montant) {
        this.totalCommandes++;
        this.montantCumule = this.montantCumule.add(montant);
        this.dateDerniereCommande = LocalDate.now();

        if (this.datePremiereCommande == null) {
            this.datePremiereCommande = LocalDate.now();
        }

        mettreAJourNiveauFidelite();
    }

    private void mettreAJourNiveauFidelite() {
        if (totalCommandes >= 20 || montantCumule.compareTo(new BigDecimal("15000")) >= 0) {
            this.niveauFidelite = CustomerTier.PLATINUM;
        } else if (totalCommandes >= 10 || montantCumule.compareTo(new BigDecimal("5000")) >= 0) {
            this.niveauFidelite = CustomerTier.GOLD;
        } else if (totalCommandes >= 3 || montantCumule.compareTo(new BigDecimal("1000")) >= 0) {
            this.niveauFidelite = CustomerTier.SILVER;
        }
        // BASIC reste BASIC
    }
}

enum CustomerTier {
    BASIC, SILVER, GOLD, PLATINUM
}
