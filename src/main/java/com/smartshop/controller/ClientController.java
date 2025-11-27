package com.smartshop.controller;

import com.smartshop.dto.ClientDTO;
import com.smartshop.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientDTO> creerClient(@Valid @RequestBody ClientDTO clientDTO, HttpSession session) {
        return ResponseEntity.ok(clientService.creerClient(clientDTO, session));
    }

    @GetMapping
    public ResponseEntity<List<ClientDTO>> trouverTousClients(HttpSession session) {
        return ResponseEntity.ok(clientService.trouverTousClients(session));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> trouverClientParId(@PathVariable Long id, HttpSession session) {
        return ResponseEntity.ok(clientService.trouverClientParId(id, session));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientDTO> mettreAJourClient(@PathVariable Long id,
                                                       @Valid @RequestBody ClientDTO clientDTO,
                                                       HttpSession session) {
        return ResponseEntity.ok(clientService.mettreAJourClient(id, clientDTO, session));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerClient(@PathVariable Long id, HttpSession session) {
        clientService.supprimerClient(id, session);
        return ResponseEntity.ok().build();
    }
}
