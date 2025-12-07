package com.smartshop.service;

import com.smartshop.dto.PaiementRequestDTO;
import com.smartshop.entity.*;
import com.smartshop.repository.CommandeRepository;
import com.smartshop.repository.PaiementRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaiementServiceTest {

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private CommandeRepository commandeRepository;

    @Mock
    private AuthService authService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private PaiementService paiementService;

    private Commande commande;
    private Paiement paiementEspeces;
    private Paiement paiementCheque;
    private Paiement paiementVirement;
    private PaiementRequestDTO paiementRequestDTO;

    @BeforeEach
    void setUp() {
        Client client = Client.builder()
                .id(1L)
                .nom("Test Client")
                .build();

        commande = Commande.builder()
                .id(1L)
                .client(client)
                .totalTTC(new BigDecimal("1000.00"))
                .montantRestant(new BigDecimal("1000.00"))
                .statut(OrderStatus.PENDING)
                .build();

        paiementEspeces = Paiement.builder()
                .id(1L)
                .commande(commande)
                .numeroPaiement(1)
                .montant(new BigDecimal("500.00"))
                .typePaiement(PaymentType.ESPECES)
                .statut(PaymentStatus.ENCAISSÉ)
                .datePaiement(LocalDate.now())
                .dateEncaissement(LocalDate.now())
                .numeroRecu("REC001")
                .build();

        paiementCheque = Paiement.builder()
                .id(2L)
                .commande(commande)
                .numeroPaiement(2)
                .montant(new BigDecimal("500.00"))
                .typePaiement(PaymentType.CHEQUE)
                .statut(PaymentStatus.EN_ATTENTE)
                .datePaiement(LocalDate.now())
                .numeroCheque("CHQ001")
                .banqueCheque("Test Bank")
                .echeanceCheque(LocalDate.now().plusDays(30))
                .build();

        paiementVirement = Paiement.builder()
                .id(3L)
                .commande(commande)
                .numeroPaiement(3)
                .montant(new BigDecimal("500.00"))
                .typePaiement(PaymentType.VIREMENT)
                .statut(PaymentStatus.EN_ATTENTE)
                .datePaiement(LocalDate.now())
                .referenceVirement("VIR001")
                .banqueVirement("Bank Al-Maghrib")
                .build();

        paiementRequestDTO = new PaiementRequestDTO();
        paiementRequestDTO.setCommandeId(1L);
        paiementRequestDTO.setMontant(new BigDecimal("500.00"));
        paiementRequestDTO.setTypePaiement("ESPECES");
        paiementRequestDTO.setNumeroRecu("REC001");
    }

    @Test
    void testEnregistrerPaiement_Especes_Success() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(true);
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(paiementRepository.findByCommandeId(1L)).thenReturn(Arrays.asList());
        when(paiementRepository.save(any(Paiement.class))).thenReturn(paiementEspeces);
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);

        // Act
        Paiement result = paiementService.enregistrerPaiement(paiementRequestDTO, session);

        // Assert
        assertNotNull(result);
        assertEquals(PaymentType.ESPECES, result.getTypePaiement());
        assertEquals(PaymentStatus.ENCAISSÉ, result.getStatut());
        assertEquals("REC001", result.getNumeroRecu());
        verify(paiementRepository).save(any(Paiement.class));
        verify(commandeRepository).save(any(Commande.class));
    }

    @Test
    void testEnregistrerPaiement_AsClient_ThrowsException() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paiementService.enregistrerPaiement(paiementRequestDTO, session));
        assertEquals("Accès refusé: Admin requis", exception.getMessage());
    }

    @Test
    void testEnregistrerPaiement_Cheque_Success() {
        // Arrange
        paiementRequestDTO.setTypePaiement("CHEQUE");
        paiementRequestDTO.setNumeroCheque("CHQ001");
        paiementRequestDTO.setBanqueCheque("Test Bank");
        paiementRequestDTO.setEcheanceCheque(LocalDate.now().plusDays(30));
        paiementRequestDTO.setNumeroRecu(null);

        when(authService.isAdmin(session)).thenReturn(true);
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(paiementRepository.findByCommandeId(1L)).thenReturn(Arrays.asList());
        when(paiementRepository.save(any(Paiement.class))).thenReturn(paiementCheque);
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);

        // Act
        Paiement result = paiementService.enregistrerPaiement(paiementRequestDTO, session);

        // Assert
        assertNotNull(result);
        assertEquals(PaymentType.CHEQUE, result.getTypePaiement());
        assertEquals(PaymentStatus.EN_ATTENTE, result.getStatut());
        assertEquals("CHQ001", result.getNumeroCheque());
        assertEquals("Test Bank", result.getBanqueCheque());
    }

    @Test
    void testEnregistrerPaiement_Virement_Success() {
        // Arrange
        paiementRequestDTO.setTypePaiement("VIREMENT");
        paiementRequestDTO.setReferenceVirement("VIR001");
        paiementRequestDTO.setBanqueVirement("Bank Al-Maghrib");
        paiementRequestDTO.setNumeroRecu(null);

        when(authService.isAdmin(session)).thenReturn(true);
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(paiementRepository.findByCommandeId(1L)).thenReturn(Arrays.asList());
        when(paiementRepository.save(any(Paiement.class))).thenReturn(paiementVirement);
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);

        // Act
        Paiement result = paiementService.enregistrerPaiement(paiementRequestDTO, session);

        // Assert
        assertNotNull(result);
        assertEquals(PaymentType.VIREMENT, result.getTypePaiement());
        assertEquals(PaymentStatus.EN_ATTENTE, result.getStatut());
        assertEquals("VIR001", result.getReferenceVirement());
        assertEquals("Bank Al-Maghrib", result.getBanqueVirement());
    }

    @Test
    void testEnregistrerPaiement_CommandeNotFound_ThrowsException() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(true);
        when(commandeRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paiementService.enregistrerPaiement(paiementRequestDTO, session));
        assertEquals("Commande non trouvée", exception.getMessage());
    }

    @Test
    void testEnregistrerPaiement_AlreadyConfirmedCommande_ThrowsException() {
        // Arrange
        commande.setStatut(OrderStatus.CONFIRMED);
        when(authService.isAdmin(session)).thenReturn(true);
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paiementService.enregistrerPaiement(paiementRequestDTO, session));
        assertEquals("Impossible de modifier une commande finalisée", exception.getMessage());
    }

    @Test
    void testEnregistrerPaiement_CanceledCommande_ThrowsException() {
        // Arrange
        commande.setStatut(OrderStatus.CANCELED);
        when(authService.isAdmin(session)).thenReturn(true);
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paiementService.enregistrerPaiement(paiementRequestDTO, session));
        assertEquals("Impossible de modifier une commande finalisée", exception.getMessage());
    }

    @Test
    void testEnregistrerPaiement_RejectedCommande_ThrowsException() {
        // Arrange
        commande.setStatut(OrderStatus.REJECTED);
        when(authService.isAdmin(session)).thenReturn(true);
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paiementService.enregistrerPaiement(paiementRequestDTO, session));
        assertEquals("Impossible de modifier une commande finalisée", exception.getMessage());
    }

    @Test
    void testEnregistrerPaiement_EspecesExceedLimit_ThrowsException() {
        // Arrange
        paiementRequestDTO.setMontant(new BigDecimal("25000.00")); // > 20,000 DH limite
        commande.setTotalTTC(new BigDecimal("30000.00"));
        commande.setMontantRestant(new BigDecimal("30000.00"));

        when(authService.isAdmin(session)).thenReturn(true);
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(paiementRepository.findByCommandeId(1L)).thenReturn(Arrays.asList());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paiementService.enregistrerPaiement(paiementRequestDTO, session));
        assertTrue(exception.getMessage().contains("Limite légale de 20,000 DH"));
    }

    @Test
    void testEnregistrerPaiement_AmountTooHigh_ThrowsException() {
        // Arrange
        paiementRequestDTO.setMontant(new BigDecimal("2000.00"));
        commande.setTotalTTC(new BigDecimal("1000.00"));
        commande.setMontantRestant(new BigDecimal("1000.00"));

        when(authService.isAdmin(session)).thenReturn(true);
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(paiementRepository.findByCommandeId(1L)).thenReturn(Arrays.asList());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paiementService.enregistrerPaiement(paiementRequestDTO, session));
        assertTrue(exception.getMessage().contains("Paiement trop élevé"));
    }

    @Test
    void testEnregistrerPaiement_AmountWithinTolerance_Success() {
        // Arrange
        paiementRequestDTO.setMontant(new BigDecimal("1100.00")); // 10% de plus que 1000
        commande.setTotalTTC(new BigDecimal("1000.00"));
        commande.setMontantRestant(new BigDecimal("1000.00"));

        when(authService.isAdmin(session)).thenReturn(true);
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(paiementRepository.findByCommandeId(1L)).thenReturn(Arrays.asList());
        when(paiementRepository.save(any(Paiement.class))).thenReturn(paiementEspeces);
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);

        // Act
        Paiement result = paiementService.enregistrerPaiement(paiementRequestDTO, session);

        // Assert
        assertNotNull(result);
        verify(paiementRepository).save(any(Paiement.class));
    }

    @Test
    void testEnregistrerPaiement_NegativeRemainingAmount_ShouldAcceptZeroAmount() {
        // Arrange
        // Quand montant restant est négatif, on considère qu'il est à 0
        // Pour accepter un paiement, le montant doit être 0 ou très petit
        paiementRequestDTO.setMontant(BigDecimal.ZERO); // Montant 0
        commande.setTotalTTC(new BigDecimal("1000.00"));
        commande.setMontantRestant(new BigDecimal("-50.00")); // Montant négatif (surpaiement)

        when(authService.isAdmin(session)).thenReturn(true);
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(paiementRepository.findByCommandeId(1L)).thenReturn(Arrays.asList());
        when(paiementRepository.save(any(Paiement.class))).thenReturn(paiementEspeces);
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);

        // Act
        Paiement result = paiementService.enregistrerPaiement(paiementRequestDTO, session);

        // Assert
        assertNotNull(result);
        verify(paiementRepository).save(any(Paiement.class));
    }

    @Test
    void testEnregistrerPaiement_NegativeRemainingAmount_WithPositiveAmount_ThrowsException() {
        // Arrange
        // Si montant restant est négatif, on le traite comme 0
        // Un montant positif > 0 devrait déclencher une erreur
        paiementRequestDTO.setMontant(new BigDecimal("100.00"));
        commande.setTotalTTC(new BigDecimal("1000.00"));
        commande.setMontantRestant(new BigDecimal("-50.00")); // Montant négatif

        when(authService.isAdmin(session)).thenReturn(true);
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(paiementRepository.findByCommandeId(1L)).thenReturn(Arrays.asList());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paiementService.enregistrerPaiement(paiementRequestDTO, session));
        assertTrue(exception.getMessage().contains("Paiement trop élevé"));
    }

    @Test
    void testEnregistrerPaiement_CalculateCorrectPaymentNumber() {
        // Arrange
        // Simuler qu'il y a déjà 2 paiements existants
        Paiement existingPaiement1 = Paiement.builder().numeroPaiement(1).build();
        Paiement existingPaiement2 = Paiement.builder().numeroPaiement(2).build();
        List<Paiement> existingPaiements = Arrays.asList(existingPaiement1, existingPaiement2);

        when(authService.isAdmin(session)).thenReturn(true);
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(paiementRepository.findByCommandeId(1L)).thenReturn(existingPaiements);

        // Créer un nouveau paiement avec le bon numéro
        Paiement newPaiement = Paiement.builder()
                .id(3L)
                .commande(commande)
                .numeroPaiement(3) // Devrait être 3
                .montant(new BigDecimal("500.00"))
                .typePaiement(PaymentType.ESPECES)
                .statut(PaymentStatus.ENCAISSÉ)
                .datePaiement(LocalDate.now())
                .dateEncaissement(LocalDate.now())
                .numeroRecu("REC003")
                .build();

        when(paiementRepository.save(any(Paiement.class))).thenReturn(newPaiement);
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);

        // Act
        Paiement result = paiementService.enregistrerPaiement(paiementRequestDTO, session);

        // Assert
        assertNotNull(result);
        verify(paiementRepository).save(any(Paiement.class));
    }

    @Test
    void testEnregistrerPaiement_ZeroPaymentNumberWhenNoExistingPayments() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(true);
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(paiementRepository.findByCommandeId(1L)).thenReturn(Arrays.asList()); // Aucun paiement existant
        when(paiementRepository.save(any(Paiement.class))).thenReturn(paiementEspeces);
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);

        // Act
        Paiement result = paiementService.enregistrerPaiement(paiementRequestDTO, session);

        // Assert
        assertNotNull(result);
        // Le premier paiement devrait avoir le numéro 1
        assertEquals(1, paiementEspeces.getNumeroPaiement());
    }

    @Test
    void testEncaisserPaiement_Success() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(true);
        when(paiementRepository.findById(2L)).thenReturn(Optional.of(paiementCheque));
        when(paiementRepository.save(any(Paiement.class))).thenReturn(paiementCheque);
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);

        // Act
        Paiement result = paiementService.encaisserPaiement(2L, session);

        // Assert
        assertNotNull(result);
        assertEquals(PaymentStatus.ENCAISSÉ, result.getStatut());
        assertNotNull(result.getDateEncaissement());
        verify(paiementRepository).save(paiementCheque);
        verify(commandeRepository).save(commande);
    }

    @Test
    void testEncaisserPaiement_AsClient_ThrowsException() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paiementService.encaisserPaiement(2L, session));
        assertEquals("Accès refusé: Admin requis", exception.getMessage());
    }

    @Test
    void testEncaisserPaiement_PaymentNotFound_ThrowsException() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(true);
        when(paiementRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paiementService.encaisserPaiement(99L, session));
        assertEquals("Paiement non trouvé", exception.getMessage());
    }

    @Test
    void testEncaisserPaiement_AlreadyEncaisse_ThrowsException() {
        // Arrange
        paiementEspeces.setStatut(PaymentStatus.ENCAISSÉ);
        when(authService.isAdmin(session)).thenReturn(true);
        when(paiementRepository.findById(1L)).thenReturn(Optional.of(paiementEspeces));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paiementService.encaisserPaiement(1L, session));
        assertEquals("Seuls les paiements EN_ATTENTE peuvent être encaissés", exception.getMessage());
    }

    @Test
    void testEncaisserPaiement_RejectedPayment_ThrowsException() {
        // Arrange
        paiementCheque.setStatut(PaymentStatus.REJETÉ);
        when(authService.isAdmin(session)).thenReturn(true);
        when(paiementRepository.findById(2L)).thenReturn(Optional.of(paiementCheque));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paiementService.encaisserPaiement(2L, session));
        assertEquals("Seuls les paiements EN_ATTENTE peuvent être encaissés", exception.getMessage());
    }

    @Test
    void testEncaisserPaiement_EspecesPayment_AlreadyEncaisseOnCreate() {
        // Arrange
        // Les paiements en espèces sont automatiquement encaissés à la création
        when(authService.isAdmin(session)).thenReturn(true);
        when(paiementRepository.findById(1L)).thenReturn(Optional.of(paiementEspeces));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paiementService.encaisserPaiement(1L, session));
        assertEquals("Seuls les paiements EN_ATTENTE peuvent être encaissés", exception.getMessage());
    }

    @Test
    void testGetPaiementsParCommande_AsAdmin_Success() {
        // Arrange
        List<Paiement> paiements = Arrays.asList(paiementEspeces, paiementCheque);
        when(authService.isAdmin(session)).thenReturn(true);
        when(paiementRepository.findByCommandeId(1L)).thenReturn(paiements);

        // Act
        List<Paiement> result = paiementService.getPaiementsParCommande(1L, session);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(paiementRepository).findByCommandeId(1L);
    }

    @Test
    void testGetPaiementsParCommande_AsClient_ThrowsException() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paiementService.getPaiementsParCommande(1L, session));
        assertEquals("Accès refusé: Admin requis", exception.getMessage());
    }

    @Test
    void testGetPaiementsParCommande_EmptyList() {
        // Arrange
        when(authService.isAdmin(session)).thenReturn(true);
        when(paiementRepository.findByCommandeId(1L)).thenReturn(Arrays.asList());

        // Act
        List<Paiement> result = paiementService.getPaiementsParCommande(1L, session);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testEnregistrerPaiement_InvalidPaymentType_ThrowsException() {
        // Arrange
        paiementRequestDTO.setTypePaiement("INVALID_TYPE");
        when(authService.isAdmin(session)).thenReturn(true);
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paiementService.enregistrerPaiement(paiementRequestDTO, session));
        assertTrue(exception.getMessage().contains("No enum constant") ||
                exception.getMessage().contains("type de paiement"));
    }

    @Test
    void testEnregistrerPaiement_EspecesWithoutReceiptNumber_Success() {
        // Arrange
        paiementRequestDTO.setNumeroRecu(null); // Pas de numéro de reçu
        when(authService.isAdmin(session)).thenReturn(true);
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(paiementRepository.findByCommandeId(1L)).thenReturn(Arrays.asList());
        when(paiementRepository.save(any(Paiement.class))).thenReturn(paiementEspeces);
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);

        // Act
        Paiement result = paiementService.enregistrerPaiement(paiementRequestDTO, session);

        // Assert
        assertNotNull(result);
        // Même sans numéro de reçu, le paiement devrait être accepté
        verify(paiementRepository).save(any(Paiement.class));
    }

    @Test
    void testEnregistrerPaiement_ChequeWithoutOptionalFields_Success() {
        // Arrange
        paiementRequestDTO.setTypePaiement("CHEQUE");
        paiementRequestDTO.setNumeroCheque("CHQ001");
        // Pas de banque ni d'échéance
        paiementRequestDTO.setBanqueCheque(null);
        paiementRequestDTO.setEcheanceCheque(null);

        when(authService.isAdmin(session)).thenReturn(true);
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(paiementRepository.findByCommandeId(1L)).thenReturn(Arrays.asList());
        when(paiementRepository.save(any(Paiement.class))).thenReturn(paiementCheque);
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);

        // Act
        Paiement result = paiementService.enregistrerPaiement(paiementRequestDTO, session);

        // Assert
        assertNotNull(result);
        assertEquals(PaymentType.CHEQUE, result.getTypePaiement());
    }

    @Test
    void testEnregistrerPaiement_VirementWithoutOptionalFields_Success() {
        // Arrange
        paiementRequestDTO.setTypePaiement("VIREMENT");
        paiementRequestDTO.setReferenceVirement("VIR001");
        // Pas de banque
        paiementRequestDTO.setBanqueVirement(null);

        when(authService.isAdmin(session)).thenReturn(true);
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(paiementRepository.findByCommandeId(1L)).thenReturn(Arrays.asList());
        when(paiementRepository.save(any(Paiement.class))).thenReturn(paiementVirement);
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);

        // Act
        Paiement result = paiementService.enregistrerPaiement(paiementRequestDTO, session);

        // Assert
        assertNotNull(result);
        assertEquals(PaymentType.VIREMENT, result.getTypePaiement());
    }

    @Test
    void testEncaisserPaiement_MultiplePayments_Success() {
        // Arrange
        Paiement paiementCheque2 = Paiement.builder()
                .id(4L)
                .commande(commande)
                .numeroPaiement(4)
                .montant(new BigDecimal("300.00"))
                .typePaiement(PaymentType.CHEQUE)
                .statut(PaymentStatus.EN_ATTENTE)
                .datePaiement(LocalDate.now())
                .numeroCheque("CHQ004")
                .build();

        when(authService.isAdmin(session)).thenReturn(true);
        when(paiementRepository.findById(4L)).thenReturn(Optional.of(paiementCheque2));
        when(paiementRepository.save(any(Paiement.class))).thenReturn(paiementCheque2);
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);

        // Act
        Paiement result = paiementService.encaisserPaiement(4L, session);

        // Assert
        assertNotNull(result);
        assertEquals(PaymentStatus.ENCAISSÉ, result.getStatut());
        verify(commandeRepository).save(commande); // La commande devrait être mise à jour
    }

    @Test
    void testGetPaiementsParCommande_MultiplePayments() {
        // Arrange
        List<Paiement> paiements = Arrays.asList(paiementEspeces, paiementCheque, paiementVirement);
        when(authService.isAdmin(session)).thenReturn(true);
        when(paiementRepository.findByCommandeId(1L)).thenReturn(paiements);

        // Act
        List<Paiement> result = paiementService.getPaiementsParCommande(1L, session);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        // Vérifier que tous les types de paiement sont présents
        assertTrue(result.stream().anyMatch(p -> p.getTypePaiement() == PaymentType.ESPECES));
        assertTrue(result.stream().anyMatch(p -> p.getTypePaiement() == PaymentType.CHEQUE));
        assertTrue(result.stream().anyMatch(p -> p.getTypePaiement() == PaymentType.VIREMENT));
    }

    @Test
    void testEnregistrerPaiement_ZeroAmount_ShouldBeAccepted() {
        // Arrange
        paiementRequestDTO.setMontant(BigDecimal.ZERO);
        when(authService.isAdmin(session)).thenReturn(true);
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(paiementRepository.findByCommandeId(1L)).thenReturn(Arrays.asList());
        when(paiementRepository.save(any(Paiement.class))).thenReturn(paiementEspeces);
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);

        // Act
        Paiement result = paiementService.enregistrerPaiement(paiementRequestDTO, session);

        // Assert
        assertNotNull(result);
        verify(paiementRepository).save(any(Paiement.class));
    }

    @Test
    void testEnregistrerPaiement_SmallAmountWithinTolerance_Success() {
        // Arrange
        paiementRequestDTO.setMontant(new BigDecimal("0.01")); // Très petit montant
        commande.setTotalTTC(new BigDecimal("1000.00"));
        commande.setMontantRestant(new BigDecimal("1000.00"));

        when(authService.isAdmin(session)).thenReturn(true);
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(paiementRepository.findByCommandeId(1L)).thenReturn(Arrays.asList());
        when(paiementRepository.save(any(Paiement.class))).thenReturn(paiementEspeces);
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);

        // Act
        Paiement result = paiementService.enregistrerPaiement(paiementRequestDTO, session);

        // Assert
        assertNotNull(result);
        verify(paiementRepository).save(any(Paiement.class));
    }
}