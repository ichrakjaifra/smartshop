package com.smartshop.service;

import com.smartshop.dto.CommandeRequestDTO;
import com.smartshop.dto.CommandeResponseDTO;
import com.smartshop.dto.OrderItemDTO;
import com.smartshop.entity.*;
import com.smartshop.mapper.CommandeMapper;
import com.smartshop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommandeService {
    private final CommandeRepository commandeRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final CommandeMapper commandeMapper;
    private final AuthService authService;

    public CommandeResponseDTO getCommandeById(Long commandeId, HttpSession session) {
        if (!authService.isAdmin(session) && !authService.isClient(session)) {
            throw new RuntimeException("Accès refusé: Authentification requise");
        }

        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        // Si c'est un client, vérifier qu'il a accès à cette commande
        if (authService.isClient(session)) {
            Long currentUserId = authService.getCurrentUserId(session);
            Client client = clientRepository.findByUserId(currentUserId)
                    .orElseThrow(() -> new RuntimeException("Client non trouvé"));

            if (!commande.getClient().getId().equals(client.getId())) {
                throw new RuntimeException("Accès refusé: Cette commande ne vous appartient pas");
            }
        }

        return commandeMapper.toDTO(commande);
    }

    @Transactional
    public CommandeResponseDTO creerCommande(CommandeRequestDTO requestDTO, HttpSession session) {
        if (!authService.isAdmin(session)) {
            throw new RuntimeException("Accès refusé: Admin requis");
        }

        // Vérifier le client
        Client client = clientRepository.findById(requestDTO.getClientId())
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        // VALIDER LE CODE PROMO SI PRÉSENT
        String codePromo = requestDTO.getCodePromo();
        if (codePromo != null && !codePromo.trim().isEmpty()) {
            // Vérifier le format
            if (!codePromo.matches("PROMO-[A-Z0-9]{4}")) {
                throw new RuntimeException("Format de code promo invalide. Format attendu: PROMO-XXXX");
            }

            // Vérifier si déjà utilisé DANS UNE COMMANDE CONFIRMÉE
            boolean codeDejaUtilise = commandeRepository.existsByCodePromoAndCodePromoUtiliseTrue(codePromo);
            if (codeDejaUtilise) {
                throw new RuntimeException("Code promo déjà utilisé");
            }
        }

        // Créer la commande
        Commande commande = Commande.builder()
                .client(client)
                .codePromo(codePromo)
                .statut(OrderStatus.PENDING)
                .codePromoUtilise(false)
                .build();

        // Ajouter les items et vérifier le stock
        List<OrderItem> items = new ArrayList<>();
        boolean stockInsuffisant = false;

        for (OrderItemDTO itemDTO : requestDTO.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Produit non trouvé: " + itemDTO.getProductId()));

            // Vérifier le stock
            if (product.getStockDisponible() < itemDTO.getQuantite()) {
                stockInsuffisant = true;
                //throw new RuntimeException("Stock insuffisant pour: " + product.getNom());
            }

            OrderItem orderItem = OrderItem.builder()
                    .commande(commande)
                    .produit(product)
                    .quantite(itemDTO.getQuantite())
                    .prixUnitaire(product.getPrixUnitaire())
                    .build();
            orderItem.calculerTotalLigne();
            items.add(orderItem);
        }

        commande.setItems(items);
        commande.calculerTotaux();

        // Vérifier si rejet automatique pour stock
        /*for (OrderItem item : commande.getItems()) {
            if (item.getProduit().getStockDisponible() < item.getQuantite()) {
                commande.setStatut(OrderStatus.REJECTED);
                break;
            }
        }*/
        if (stockInsuffisant) {
            commande.setStatut(OrderStatus.REJECTED);
        }

        // Sauvegarder
        Commande savedCommande = commandeRepository.save(commande);

        // Décrémenter le stock si commande n'est pas rejetée
        if (savedCommande.getStatut() != OrderStatus.REJECTED) {
            for (OrderItem item : savedCommande.getItems()) {
                Product product = item.getProduit();
                product.setStockDisponible(product.getStockDisponible() - item.getQuantite());
                productRepository.save(product);
            }
        }

        return commandeMapper.toDTO(savedCommande);
    }

    public List<CommandeResponseDTO> trouverCommandesParClient(Long clientId, HttpSession session) {
        if (authService.isClient(session)) {
            // Un client ne peut voir que ses propres commandes
            Long currentUserId = authService.getCurrentUserId(session);
            Client client = clientRepository.findByUserId(currentUserId)
                    .orElseThrow(() -> new RuntimeException("Client non trouvé"));

            if (!client.getId().equals(clientId)) {
                throw new RuntimeException("Accès refusé");
            }
        }

        List<Commande> commandes = commandeRepository.findByClientId(clientId);
        return commandes.stream()
                .map(commandeMapper::toDTO)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    @Transactional
    public CommandeResponseDTO confirmerCommande(Long commandeId, HttpSession session) {
        if (!authService.isAdmin(session)) {
            throw new RuntimeException("Accès refusé: Admin requis");
        }

        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        if (!commande.estEntierementPayee()) {
            throw new RuntimeException("La commande n'est pas entièrement payée");
        }

        if (commande.getStatut() != OrderStatus.PENDING) {
            throw new RuntimeException("Seules les commandes PENDING peuvent être confirmées");
        }

        // MARQUER LE CODE PROMO COMME UTILISÉ SI PRÉSENT
        if (commande.getCodePromo() != null && !commande.getCodePromo().trim().isEmpty()) {
            // Vérifier si le code n'a pas déjà été utilisé ailleurs
            boolean codeDejaUtilise = commandeRepository.existsByCodePromoAndCodePromoUtiliseTrue(
                    commande.getCodePromo()
            );

            if (codeDejaUtilise) {
                throw new RuntimeException("Code promo déjà utilisé sur une autre commande");
            }

            commande.setCodePromoUtilise(true);
        }

        commande.setStatut(OrderStatus.CONFIRMED);
        Commande savedCommande = commandeRepository.save(commande);

        // Mettre à jour les statistiques du client
        Client client = commande.getClient();
        client.incrementerCommandes(commande.getTotalTTC());
        clientRepository.save(client);

        return commandeMapper.toDTO(savedCommande);
    }

    public CommandeResponseDTO annulerCommande(Long commandeId, HttpSession session) {
        if (!authService.isAdmin(session)) {
            throw new RuntimeException("Accès refusé: Admin requis");
        }

        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        if (commande.getStatut() != OrderStatus.PENDING) {
            throw new RuntimeException("Seules les commandes PENDING peuvent être annulées");
        }

        commande.setStatut(OrderStatus.CANCELED);

        // Restaurer le stock
        for (OrderItem item : commande.getItems()) {
            Product product = item.getProduit();
            product.setStockDisponible(product.getStockDisponible() + item.getQuantite());
            productRepository.save(product);
        }

        Commande savedCommande = commandeRepository.save(commande);
        return commandeMapper.toDTO(savedCommande);
    }
}