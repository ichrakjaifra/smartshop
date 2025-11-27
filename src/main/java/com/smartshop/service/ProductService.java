package com.smartshop.service;

import com.smartshop.dto.ProductDTO;
import com.smartshop.entity.Product;
import com.smartshop.mapper.ProductMapper;
import com.smartshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final AuthService authService;

    public ProductDTO creerProduit(ProductDTO productDTO, HttpSession session) {
        if (!authService.isAdmin(session)) {
            throw new RuntimeException("Accès refusé: Admin requis");
        }

        Product product = productMapper.toEntity(productDTO);
        product.setDeleted(false);
        product = productRepository.save(product);
        return productMapper.toDTO(product);
    }

    public List<ProductDTO> trouverTousProduits(HttpSession session) {
        // Accessible par ADMIN et CLIENT
        return productRepository.findByDeletedFalse().stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO trouverProduitParId(Long id, HttpSession session) {
        Product product = productRepository.findById(id)
                .filter(p -> !p.getDeleted())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
        return productMapper.toDTO(product);
    }

    public ProductDTO mettreAJourProduit(Long id, ProductDTO productDTO, HttpSession session) {
        if (!authService.isAdmin(session)) {
            throw new RuntimeException("Accès refusé: Admin requis");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        product.setNom(productDTO.getNom());
        product.setDescription(productDTO.getDescription());
        product.setPrixUnitaire(productDTO.getPrixUnitaire());
        product.setStockDisponible(productDTO.getStockDisponible());

        product = productRepository.save(product);
        return productMapper.toDTO(product);
    }

    public void supprimerProduit(Long id, HttpSession session) {
        if (!authService.isAdmin(session)) {
            throw new RuntimeException("Accès refusé: Admin requis");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        // Soft delete
        product.setDeleted(true);
        productRepository.save(product);
    }
}