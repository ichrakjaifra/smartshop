package com.smartshop.service;

import com.smartshop.dto.ProductDTO;
import com.smartshop.dto.ProductSearchDTO;
import com.smartshop.entity.Product;
import com.smartshop.mapper.ProductMapper;
import com.smartshop.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private AuthService authService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .nom("Test Product")
                .description("Test Description")
                .prixUnitaire(new BigDecimal("100.00"))
                .stockDisponible(10)
                .deleted(false)
                .build();

        productDTO = new ProductDTO();
        productDTO.setId(1L);
        productDTO.setNom("Test Product");
        productDTO.setDescription("Test Description");
        productDTO.setPrixUnitaire(new BigDecimal("100.00"));
        productDTO.setStockDisponible(10);
    }

    @Test
    void testCreerProduit_AsAdmin_Success() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(true);
        when(productMapper.toEntity(any(ProductDTO.class))).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);

        // Act
        ProductDTO result = productService.creerProduit(productDTO, session);

        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getNom());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testCreerProduit_AsClient_ThrowsException() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productService.creerProduit(productDTO, session));
        assertEquals("Accès refusé: Admin requis", exception.getMessage());
    }

    @Test
    void testTrouverTousProduits_Success() {
        // Arrange
        List<Product> products = Arrays.asList(product);
        when(productRepository.findByDeletedFalse()).thenReturn(products);
        when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);

        // Act
        List<ProductDTO> result = productService.trouverTousProduits(session);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository).findByDeletedFalse();
    }

    @Test
    void testTrouverProduitParId_Success() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);

        // Act
        ProductDTO result = productService.trouverProduitParId(1L, session);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testTrouverProduitParId_DeletedProduct_ThrowsException() {
        // Arrange
        product.setDeleted(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productService.trouverProduitParId(1L, session));
        assertEquals("Produit non trouvé", exception.getMessage());
    }

    @Test
    void testTrouverProduitParId_NotFound_ThrowsException() {
        // Arrange
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productService.trouverProduitParId(99L, session));
        assertEquals("Produit non trouvé", exception.getMessage());
    }

    @Test
    void testMettreAJourProduit_AsAdmin_Success() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);

        // Act
        ProductDTO result = productService.mettreAJourProduit(1L, productDTO, session);

        // Assert
        assertNotNull(result);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testMettreAJourProduit_AsClient_ThrowsException() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productService.mettreAJourProduit(1L, productDTO, session));
        assertEquals("Accès refusé: Admin requis", exception.getMessage());
    }

    @Test
    void testSupprimerProduit_SoftDelete_Success() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act
        productService.supprimerProduit(1L, session);

        // Assert
        assertTrue(product.getDeleted());
        verify(productRepository).save(product);
    }

    @Test
    void testSupprimerProduit_AsClient_ThrowsException() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productService.supprimerProduit(1L, session));
        assertEquals("Accès refusé: Admin requis", exception.getMessage());
    }

    @Test
    void testRechercherProduits_Success() {
        // Arrange
        ProductSearchDTO searchDTO = new ProductSearchDTO();
        searchDTO.setNom("Test");
        searchDTO.setPage(0);
        searchDTO.setSize(10);
        searchDTO.setSortBy("nom");
        searchDTO.setSortDirection("ASC");

        Page<Product> productPage = new PageImpl<>(Arrays.asList(product));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(productPage);
        when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);

        // Act
        Page<ProductDTO> result = productService.rechercherProduits(searchDTO, session);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void testRechercherProduits_WithPriceRange() {
        // Arrange
        ProductSearchDTO searchDTO = new ProductSearchDTO();
        searchDTO.setPrixMin(new BigDecimal("50.00"));
        searchDTO.setPrixMax(new BigDecimal("150.00"));

        Page<Product> productPage = new PageImpl<>(Arrays.asList(product));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(productPage);
        when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);

        // Act
        Page<ProductDTO> result = productService.rechercherProduits(searchDTO, session);

        // Assert
        assertNotNull(result);
        verify(productRepository).findAll(any(Specification.class), any(Pageable.class));
    }
}