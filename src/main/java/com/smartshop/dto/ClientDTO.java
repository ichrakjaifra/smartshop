package com.smartshop.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ClientDTO {
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @Pattern(regexp = "^\\+212[5-7][0-9]{8}$", message = "Le téléphone doit être au format marocain: +212XXXXXXXXX")
    private String telephone;

    @NotBlank(message = "L'adresse est obligatoire")
    @Size(min = 5, max = 200, message = "L'adresse doit contenir entre 5 et 200 caractères")
    private String adresse;

    private String niveauFidelite;
    private Integer totalCommandes;
    private BigDecimal montantCumule;
    private LocalDate datePremiereCommande;
    private LocalDate dateDerniereCommande;
}