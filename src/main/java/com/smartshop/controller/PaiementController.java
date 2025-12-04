package com.smartshop.controller;

import com.smartshop.dto.PaiementRequestDTO;
import com.smartshop.entity.Paiement;
import com.smartshop.service.PaiementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
public class PaiementController {
    private final PaiementService paiementService;

    @PostMapping
    public ResponseEntity<Paiement> enregistrerPaiement(@Valid @RequestBody PaiementRequestDTO requestDTO,
                                                        HttpSession session) {
        return ResponseEntity.ok(paiementService.enregistrerPaiement(requestDTO, session));
    }

    @PutMapping("/{id}/encaisser")
    public ResponseEntity<Paiement> encaisserPaiement(@PathVariable Long id, HttpSession session) {
        return ResponseEntity.ok(paiementService.encaisserPaiement(id, session));
    }

    @GetMapping("/commande/{commandeId}")
    public ResponseEntity<List<Paiement>> getPaiementsParCommande(@PathVariable Long commandeId,
                                                                  HttpSession session) {
        return ResponseEntity.ok(paiementService.getPaiementsParCommande(commandeId, session));
    }
}