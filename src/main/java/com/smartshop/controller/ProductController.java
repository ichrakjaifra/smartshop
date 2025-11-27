package com.smartshop.controller;

import com.smartshop.dto.ProductDTO;
import com.smartshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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
}