package com.smartshop.service;

import com.smartshop.entity.User;
import com.smartshop.entity.UserRole;
import com.smartshop.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AuthService authService;

    private User adminUser;
    private User clientUser;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L)
                .username("admin")
                .password("admin123")
                .role(UserRole.ADMIN)
                .build();

        clientUser = User.builder()
                .id(2L)
                .username("client1")
                .password("client123")
                .role(UserRole.CLIENT)
                .build();
    }

    @Test
    void testLogin_Success() {
        // Arrange
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        // Act
        boolean result = authService.login("admin", "admin123", session);

        // Assert
        assertTrue(result);
        verify(session).setAttribute("user", adminUser);
        verify(session).setAttribute("role", UserRole.ADMIN);
        verify(session).setAttribute("userId", 1L);
    }

    @Test
    void testLogin_WrongPassword() {
        // Arrange
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        // Act
        boolean result = authService.login("admin", "wrongpassword", session);

        // Assert
        assertFalse(result);
        verify(session, never()).setAttribute(anyString(), any());
    }

    @Test
    void testLogin_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // Act
        boolean result = authService.login("unknown", "password", session);

        // Assert
        assertFalse(result);
        verify(session, never()).setAttribute(anyString(), any());
    }

    @Test
    void testLogout() {
        // Act
        authService.logout(session);

        // Assert
        verify(session).invalidate();
    }

    @Test
    void testIsAdmin_WhenAdmin() {
        // Arrange
        when(session.getAttribute("role")).thenReturn(UserRole.ADMIN);

        // Act
        boolean result = authService.isAdmin(session);

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsAdmin_WhenNotAdmin() {
        // Arrange
        when(session.getAttribute("role")).thenReturn(UserRole.CLIENT);

        // Act
        boolean result = authService.isAdmin(session);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsClient_WhenClient() {
        // Arrange
        when(session.getAttribute("role")).thenReturn(UserRole.CLIENT);

        // Act
        boolean result = authService.isClient(session);

        // Assert
        assertTrue(result);
    }

    @Test
    void testGetCurrentUserId() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(1L);

        // Act
        Long userId = authService.getCurrentUserId(session);

        // Assert
        assertEquals(1L, userId);
    }

    @Test
    void testGetCurrentUserId_WhenNull() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(null);

        // Act
        Long userId = authService.getCurrentUserId(session);

        // Assert
        assertNull(userId);
    }
}