package com.smartshop.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Long id;
    private String nom;
    private String description;
    private BigDecimal prixUnitaire;
    private Integer stockDisponible;
}