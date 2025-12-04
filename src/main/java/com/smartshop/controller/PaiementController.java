package com.smartshop.controller;

import com.smartshop.dto.PaiementRequestDTO;
import com.smartshop.dto.PaiementResponseDTO;
import com.smartshop.entity.Paiement;
import com.smartshop.mapper.PaiementMapper;
import com.smartshop.service.PaiementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
public class PaiementController {
    private final PaiementService paiementService;
    private final PaiementMapper paiementMapper;

    @PostMapping
    public ResponseEntity<PaiementResponseDTO> enregistrerPaiement(@Valid @RequestBody PaiementRequestDTO requestDTO,
                                                                   HttpSession session) {
        Paiement paiement = paiementService.enregistrerPaiement(requestDTO, session);
        return ResponseEntity.ok(paiementMapper.toDTO(paiement));
    }

    @PutMapping("/{id}/encaisser")
    public ResponseEntity<PaiementResponseDTO> encaisserPaiement(@PathVariable Long id, HttpSession session) {
        Paiement paiement = paiementService.encaisserPaiement(id, session);
        return ResponseEntity.ok(paiementMapper.toDTO(paiement));
    }

    @GetMapping("/commande/{commandeId}")
    public ResponseEntity<List<PaiementResponseDTO>> getPaiementsParCommande(@PathVariable Long commandeId,
                                                                             HttpSession session) {
        List<Paiement> paiements = paiementService.getPaiementsParCommande(commandeId, session);
        List<PaiementResponseDTO> dtos = paiements.stream()
                .map(paiementMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}