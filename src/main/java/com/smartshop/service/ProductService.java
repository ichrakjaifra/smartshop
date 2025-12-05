package com.smartshop.service;

import com.smartshop.dto.ProductDTO;
import com.smartshop.dto.ProductSearchDTO;
import com.smartshop.entity.Product;
import com.smartshop.mapper.ProductMapper;
import com.smartshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
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

    public Page<ProductDTO> rechercherProduits(ProductSearchDTO searchDTO, HttpSession session) {
        // Accessible par ADMIN et CLIENT
        Specification<Product> spec = buildSpecification(searchDTO);

        // Création du Pageable avec tri
        Sort sort = Sort.by(
                searchDTO.getSortDirection().equalsIgnoreCase("DESC")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                searchDTO.getSortBy()
        );

        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);

        // Exécution de la requête paginée
        Page<Product> productsPage = productRepository.findAll(spec, pageable);

        // Conversion en Page de DTO
        return productsPage.map(productMapper::toDTO);
    }

    private Specification<Product> buildSpecification(ProductSearchDTO searchDTO) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtre par deleted = false (soft delete)
            predicates.add(criteriaBuilder.equal(root.get("deleted"), false));

            // Filtre par nom (recherche partielle insensible à la casse)
            if (searchDTO.getNom() != null && !searchDTO.getNom().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nom")),
                        "%" + searchDTO.getNom().toLowerCase() + "%"
                ));
            }

            // Filtre par prix minimum
            if (searchDTO.getPrixMin() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("prixUnitaire"),
                        searchDTO.getPrixMin()
                ));
            }

            // Filtre par prix maximum
            if (searchDTO.getPrixMax() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("prixUnitaire"),
                        searchDTO.getPrixMax()
                ));
            }

            // Filtre par disponibilité en stock
            if (searchDTO.getEnStock() != null && searchDTO.getEnStock()) {
                predicates.add(criteriaBuilder.greaterThan(
                        root.get("stockDisponible"),
                        0
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}