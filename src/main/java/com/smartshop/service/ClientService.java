package com.smartshop.service;

import com.smartshop.dto.ClientDTO;
import com.smartshop.entity.Client;
import com.smartshop.entity.User;
import com.smartshop.entity.UserRole;
import com.smartshop.mapper.ClientMapper;
import com.smartshop.repository.ClientRepository;
import com.smartshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ClientMapper clientMapper;
    private final AuthService authService;

    private void verifierAuthentification(HttpSession session) {
        if (session.getAttribute("user") == null) {
            throw new RuntimeException("Accès refusé: Authentification requise");
        }
    }

    public ClientDTO creerClient(ClientDTO clientDTO, HttpSession session) {
        verifierAuthentification(session);

        if (!authService.isAdmin(session)) {
            throw new RuntimeException("Accès refusé: Admin requis");
        }

        // Créer l'utilisateur associé
        User user = User.builder()
                .username(clientDTO.getEmail())
                .password("default123")
                .role(UserRole.CLIENT)
                .build();
        user = userRepository.save(user);

        // Créer le client
        Client client = clientMapper.toEntity(clientDTO);
        client.setUser(user);
        client = clientRepository.save(client);

        return clientMapper.toDTO(client);
    }

    public List<ClientDTO> trouverTousClients(HttpSession session) {
        verifierAuthentification(session);

        if (!authService.isAdmin(session)) {
            throw new RuntimeException("Accès refusé: Admin requis");
        }

        return clientRepository.findAll().stream()
                .map(clientMapper::toDTO)
                .collect(Collectors.toList());
    }

    public ClientDTO trouverClientParId(Long id, HttpSession session) {
        verifierAuthentification(session);

        if (authService.isClient(session)) {
            Long currentUserId = authService.getCurrentUserId(session);
            Client client = clientRepository.findByUserId(currentUserId)
                    .orElseThrow(() -> new RuntimeException("Client non trouvé"));

            if (!client.getId().equals(id)) {
                throw new RuntimeException("Accès refusé");
            }
        }

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        return clientMapper.toDTO(client);
    }

    public ClientDTO mettreAJourClient(Long id, ClientDTO clientDTO, HttpSession session) {
        verifierAuthentification(session);

        if (!authService.isAdmin(session)) {
            throw new RuntimeException("Accès refusé: Admin requis");
        }

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        client.setNom(clientDTO.getNom());
        client.setEmail(clientDTO.getEmail());
        client.setTelephone(clientDTO.getTelephone());
        client.setAdresse(clientDTO.getAdresse());

        client = clientRepository.save(client);
        return clientMapper.toDTO(client);
    }

    public void supprimerClient(Long id, HttpSession session) {
        verifierAuthentification(session);

        if (!authService.isAdmin(session)) {
            throw new RuntimeException("Accès refusé: Admin requis");
        }

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        clientRepository.delete(client);
    }
}