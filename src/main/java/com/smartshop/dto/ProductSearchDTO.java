package com.smartshop.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductSearchDTO {
    private String nom;
    private BigDecimal prixMin;
    private BigDecimal prixMax;
    private Boolean enStock;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "id";
    private String sortDirection = "ASC";
}