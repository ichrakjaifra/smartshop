package com.smartshop.controller;

import com.smartshop.dto.CommandeRequestDTO;
import com.smartshop.dto.CommandeResponseDTO;
import com.smartshop.service.CommandeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/commandes")
@RequiredArgsConstructor
public class CommandeController {
    private final CommandeService commandeService;

    @PostMapping
    public ResponseEntity<CommandeResponseDTO> creerCommande(@Valid @RequestBody CommandeRequestDTO requestDTO,
                                                             HttpSession session) {
        return ResponseEntity.ok(commandeService.creerCommande(requestDTO, session));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<CommandeResponseDTO>> trouverCommandesParClient(@PathVariable Long clientId,
                                                                               HttpSession session) {
        return ResponseEntity.ok(commandeService.trouverCommandesParClient(clientId, session));
    }

    @PutMapping("/{id}/confirmer")
    public ResponseEntity<CommandeResponseDTO> confirmerCommande(@PathVariable Long id, HttpSession session) {
        return ResponseEntity.ok(commandeService.confirmerCommande(id, session));
    }

    @PutMapping("/{id}/annuler")
    public ResponseEntity<CommandeResponseDTO> annulerCommande(@PathVariable Long id, HttpSession session) {
        return ResponseEntity.ok(commandeService.annulerCommande(id, session));
    }
}