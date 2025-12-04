package com.smartshop.repository;

import com.smartshop.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    List<Paiement> findByCommandeId(Long commandeId);

    @Query("SELECT p FROM Paiement p WHERE p.commande.id = :commandeId ORDER BY p.numeroPaiement")
    List<Paiement> findPaiementsByCommandeOrderByNumero(Long commandeId);
}