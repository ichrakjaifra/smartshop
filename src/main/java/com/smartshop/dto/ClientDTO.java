package com.smartshop.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ClientDTO {
    private Long id;
    private String nom;
    private String email;
    private String telephone;
    private String adresse;
    private String niveauFidelite;
    private Integer totalCommandes;
    private BigDecimal montantCumule;
    private LocalDate datePremiereCommande;
    private LocalDate dateDerniereCommande;
}
