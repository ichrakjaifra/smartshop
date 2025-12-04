package com.smartshop.service;

import com.smartshop.dto.PaiementRequestDTO;
import com.smartshop.entity.*;
import com.smartshop.repository.CommandeRepository;
import com.smartshop.repository.PaiementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaiementService {
    private final PaiementRepository paiementRepository;
    private final CommandeRepository commandeRepository;
    private final AuthService authService;

    @Transactional
    public Paiement enregistrerPaiement(PaiementRequestDTO requestDTO, HttpSession session) {
        if (!authService.isAdmin(session)) {
            throw new RuntimeException("Accès refusé: Admin requis");
        }

        Commande commande = commandeRepository.findById(requestDTO.getCommandeId())
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        // Vérifier que la commande n'est pas déjà finalisée
        if (commande.getStatut() == OrderStatus.CONFIRMED ||
                commande.getStatut() == OrderStatus.CANCELED ||
                commande.getStatut() == OrderStatus.REJECTED) {
            throw new RuntimeException("Impossible de modifier une commande finalisée");
        }

        // Déterminer le numéro de paiement
        List<Paiement> paiementsExistants = paiementRepository.findByCommandeId(commande.getId());
        int numeroPaiement = paiementsExistants.size() + 1;

        // Créer le paiement
        Paiement paiement = Paiement.builder()
                .commande(commande)
                .numeroPaiement(numeroPaiement)
                .montant(requestDTO.getMontant())
                .typePaiement(PaymentType.valueOf(requestDTO.getTypePaiement()))
                .statut(PaymentStatus.EN_ATTENTE)
                .datePaiement(LocalDate.now())
                .build();

        // Remplir les champs spécifiques
        switch (paiement.getTypePaiement()) {
            case ESPECES:
                paiement.setNumeroRecu(requestDTO.getNumeroRecu());
                paiement.validerPaiementEspeces();
                paiement.setStatut(PaymentStatus.ENCAISSÉ);
                paiement.setDateEncaissement(LocalDate.now());
                break;

            case CHEQUE:
                paiement.setNumeroCheque(requestDTO.getNumeroCheque());
                paiement.setBanqueCheque(requestDTO.getBanqueCheque());
                paiement.setEcheanceCheque(requestDTO.getEcheanceCheque());
                break;

            case VIREMENT:
                paiement.setReferenceVirement(requestDTO.getReferenceVirement());
                paiement.setBanqueVirement(requestDTO.getBanqueVirement());
                break;
        }

        paiement = paiementRepository.save(paiement);

        // Recalculer les totaux de la commande
        commande.calculerTotaux();
        commandeRepository.save(commande);

        return paiement;
    }

    @Transactional
    public Paiement encaisserPaiement(Long paiementId, HttpSession session) {
        if (!authService.isAdmin(session)) {
            throw new RuntimeException("Accès refusé: Admin requis");
        }

        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé"));

        if (paiement.getStatut() != PaymentStatus.EN_ATTENTE) {
            throw new RuntimeException("Seuls les paiements EN_ATTENTE peuvent être encaissés");
        }

        paiement.setStatut(PaymentStatus.ENCAISSÉ);
        paiement.setDateEncaissement(LocalDate.now());

        paiement = paiementRepository.save(paiement);

        // Recalculer les totaux de la commande
        Commande commande = paiement.getCommande();
        commande.calculerTotaux();
        commandeRepository.save(commande);

        return paiement;
    }

    public List<Paiement> getPaiementsParCommande(Long commandeId, HttpSession session) {
        if (!authService.isAdmin(session)) {
            throw new RuntimeException("Accès refusé: Admin requis");
        }

        return paiementRepository.findByCommandeId(commandeId);
    }
}