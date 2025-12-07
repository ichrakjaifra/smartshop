package com.smartshop.service;

import com.smartshop.dto.ClientDTO;
import com.smartshop.entity.Client;
import com.smartshop.entity.User;
import com.smartshop.entity.UserRole;
import com.smartshop.mapper.ClientMapper;
import com.smartshop.repository.ClientRepository;
import com.smartshop.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClientMapper clientMapper;

    @Mock
    private AuthService authService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private ClientService clientService;

    private Client client;
    private ClientDTO clientDTO;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("test@email.com")
                .password("default123")
                .role(UserRole.CLIENT)
                .build();

        client = Client.builder()
                .id(1L)
                .nom("Test Client")
                .email("test@email.com")
                .telephone("+212600000000")
                .adresse("Test Address")
                .user(user)
                .build();

        clientDTO = new ClientDTO();
        clientDTO.setId(1L);
        clientDTO.setNom("Test Client");
        clientDTO.setEmail("test@email.com");
        clientDTO.setTelephone("+212600000000");
        clientDTO.setAdresse("Test Address");
    }

    @Test
    void testCreerClient_AsAdmin_Success() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(true);
        when(session.getAttribute("user")).thenReturn(new User());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(clientMapper.toEntity(any(ClientDTO.class))).thenReturn(client);
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toDTO(any(Client.class))).thenReturn(clientDTO);

        // Act
        ClientDTO result = clientService.creerClient(clientDTO, session);

        // Assert
        assertNotNull(result);
        assertEquals("Test Client", result.getNom());
        assertEquals("test@email.com", result.getEmail());
        verify(userRepository).save(any(User.class));
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void testCreerClient_AsClient_ThrowsException() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(false);
        when(session.getAttribute("user")).thenReturn(new User());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> clientService.creerClient(clientDTO, session));
        assertEquals("Accès refusé: Admin requis", exception.getMessage());
    }

    @Test
    void testTrouverTousClients_AsAdmin_Success() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(true);
        when(session.getAttribute("user")).thenReturn(new User());
        List<Client> clients = Arrays.asList(client);
        when(clientRepository.findAll()).thenReturn(clients);
        when(clientMapper.toDTO(any(Client.class))).thenReturn(clientDTO);

        // Act
        List<ClientDTO> result = clientService.trouverTousClients(session);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(clientRepository).findAll();
    }

    @Test
    void testTrouverClientParId_AsAdmin_Success() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(true);
        when(session.getAttribute("user")).thenReturn(new User());
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientMapper.toDTO(any(Client.class))).thenReturn(clientDTO);

        // Act
        ClientDTO result = clientService.trouverClientParId(1L, session);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(clientRepository).findById(1L);
    }

    @Test
    void testTrouverClientParId_AsClient_AccessOwnProfile() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(false);
        when(authService.isClient(session)).thenReturn(true);
        when(authService.getCurrentUserId(session)).thenReturn(1L);
        when(session.getAttribute("user")).thenReturn(new User());
        when(clientRepository.findByUserId(1L)).thenReturn(Optional.of(client));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientMapper.toDTO(any(Client.class))).thenReturn(clientDTO);

        // Act
        ClientDTO result = clientService.trouverClientParId(1L, session);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testTrouverClientParId_AsClient_AccessOtherProfile_ThrowsException() {
        // Arrange
        Client otherClient = Client.builder().id(2L).build();

        when(authService.isAdmin(session)).thenReturn(false);
        when(authService.isClient(session)).thenReturn(true);
        when(authService.getCurrentUserId(session)).thenReturn(1L);
        when(session.getAttribute("user")).thenReturn(new User());
        when(clientRepository.findByUserId(1L)).thenReturn(Optional.of(client));
        when(clientRepository.findById(2L)).thenReturn(Optional.of(otherClient));
        when(clientMapper.toDTO(any(Client.class))).thenReturn(clientDTO);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> clientService.trouverClientParId(2L, session));
        assertEquals("Accès refusé", exception.getMessage());
    }

    @Test
    void testMettreAJourClient_Success() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(true);
        when(session.getAttribute("user")).thenReturn(new User());
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toDTO(any(Client.class))).thenReturn(clientDTO);

        // Act
        ClientDTO result = clientService.mettreAJourClient(1L, clientDTO, session);

        // Assert
        assertNotNull(result);
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void testSupprimerClient_Success() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(true);
        when(session.getAttribute("user")).thenReturn(new User());
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        doNothing().when(clientRepository).delete(any(Client.class));

        // Act
        clientService.supprimerClient(1L, session);

        // Assert
        verify(clientRepository).delete(any(Client.class));
    }

    @Test
    void testNotAuthenticated_ThrowsException() {
        // Arrange
        when(session.getAttribute("user")).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> clientService.trouverTousClients(session));
        assertEquals("Accès refusé: Authentification requise", exception.getMessage());
    }

    @Test
    void testClientNotFound_ThrowsException() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(true);
        when(session.getAttribute("user")).thenReturn(new User());
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> clientService.trouverClientParId(99L, session));
        assertEquals("Client non trouvé", exception.getMessage());
    }

    @Test
    void testMettreAJourClient_ClientNotFound_ThrowsException() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(true);
        when(session.getAttribute("user")).thenReturn(new User());
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> clientService.mettreAJourClient(99L, clientDTO, session));
        assertEquals("Client non trouvé", exception.getMessage());
    }

    @Test
    void testSupprimerClient_ClientNotFound_ThrowsException() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(true);
        when(session.getAttribute("user")).thenReturn(new User());
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> clientService.supprimerClient(99L, session));
        assertEquals("Client non trouvé", exception.getMessage());
    }
}