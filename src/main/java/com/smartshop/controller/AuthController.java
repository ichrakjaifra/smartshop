package com.smartshop.controller;

import com.smartshop.dto.LoginRequest;
import com.smartshop.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpSession session) {
        boolean success = authService.login(loginRequest.getUsername(), loginRequest.getPassword(), session);

        if (success) {
            return ResponseEntity.ok().body("Login successful");
        } else {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok().body("Logout successful");
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkAuth(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return ResponseEntity.ok().body("Authenticated as: " + session.getAttribute("role"));
        } else {
            return ResponseEntity.status(401).body("Not authenticated");
        }
    }
}
