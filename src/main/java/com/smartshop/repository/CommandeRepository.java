package com.smartshop.repository;

import com.smartshop.entity.Commande;
import com.smartshop.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {
    List<Commande> findByClientId(Long clientId);
    List<Commande> findByStatut(OrderStatus statut);

    @Query("SELECT c FROM Commande c WHERE c.client.id = :clientId ORDER BY c.dateCreation DESC")
    List<Commande> findCommandesByClientIdOrderByDateDesc(Long clientId);

    boolean existsByCodePromoAndCodePromoUtiliseTrue(String codePromo);

    Optional<Commande> findByCodePromo(String codePromo);
}