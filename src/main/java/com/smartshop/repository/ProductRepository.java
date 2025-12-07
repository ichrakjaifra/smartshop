package com.smartshop.repository;

import com.smartshop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findByDeletedFalse();

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.stockDisponible > 0")
    List<Product> findAvailableProducts();

    List<Product> findByNomContainingIgnoreCaseAndDeletedFalse(String nom);
}