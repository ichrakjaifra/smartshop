package com.smartshop.controller;

import com.smartshop.dto.ProductDTO;
import com.smartshop.dto.ProductSearchDTO;
import com.smartshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductDTO> creerProduit(@Valid @RequestBody ProductDTO productDTO, HttpSession session) {
        return ResponseEntity.ok(productService.creerProduit(productDTO, session));
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> trouverTousProduits(HttpSession session) {
        return ResponseEntity.ok(productService.trouverTousProduits(session));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> trouverProduitParId(@PathVariable Long id, HttpSession session) {
        return ResponseEntity.ok(productService.trouverProduitParId(id, session));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> mettreAJourProduit(@PathVariable Long id,
                                                         @Valid @RequestBody ProductDTO productDTO,
                                                         HttpSession session) {
        return ResponseEntity.ok(productService.mettreAJourProduit(id, productDTO, session));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerProduit(@PathVariable Long id, HttpSession session) {
        productService.supprimerProduit(id, session);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/search")
    public ResponseEntity<Page<ProductDTO>> rechercherProduits(
            @Valid @RequestBody ProductSearchDTO searchDTO,
            HttpSession session) {
        return ResponseEntity.ok(productService.rechercherProduits(searchDTO, session));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDTO>> rechercherProduitsGet(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) BigDecimal prixMin,
            @RequestParam(required = false) BigDecimal prixMax,
            @RequestParam(required = false) Boolean enStock,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            HttpSession session) {

        ProductSearchDTO searchDTO = new ProductSearchDTO();
        searchDTO.setNom(nom);
        searchDTO.setPrixMin(prixMin);
        searchDTO.setPrixMax(prixMax);
        searchDTO.setEnStock(enStock);
        searchDTO.setPage(page);
        searchDTO.setSize(size);
        searchDTO.setSortBy(sortBy);
        searchDTO.setSortDirection(sortDirection);

        return ResponseEntity.ok(productService.rechercherProduits(searchDTO, session));
    }
}