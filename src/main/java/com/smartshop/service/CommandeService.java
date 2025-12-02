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

    @Transactional
    public CommandeResponseDTO creerCommande(CommandeRequestDTO requestDTO, HttpSession session) {
        if (!authService.isAdmin(session)) {
            throw new RuntimeException("Accès refusé: Admin requis");
        }

        // Vérifier le client
        Client client = clientRepository.findById(requestDTO.getClientId())
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        // Créer la commande
        Commande commande = Commande.builder()
                .client(client)
                .codePromo(requestDTO.getCodePromo())
                .statut(OrderStatus.PENDING)
                .build();

        // Ajouter les items
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemDTO itemDTO : requestDTO.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Produit non trouvé: " + itemDTO.getProductId()));

            // Vérifier le stock
            if (product.getStockDisponible() < itemDTO.getQuantite()) {
                throw new RuntimeException("Stock insuffisant pour: " + product.getNom());
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
        for (OrderItem item : commande.getItems()) {
            if (item.getProduit().getStockDisponible() < item.getQuantite()) {
                commande.setStatut(OrderStatus.REJECTED);
                break;
            }
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