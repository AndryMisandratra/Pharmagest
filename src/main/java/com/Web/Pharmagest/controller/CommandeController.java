package com.Web.Pharmagest.controller;

import com.Web.Pharmagest.dto.request.CommandeRequest;
import com.Web.Pharmagest.dto.request.ReceptionCommandeRequest;
import com.Web.Pharmagest.dto.response.CommandeResponse;
import com.Web.Pharmagest.service.CommandeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/commandes")
@RequiredArgsConstructor
public class CommandeController {

    private final CommandeService commandeService;

    // GET /api/commandes?statut=&fournisseurId=
    @GetMapping
    public ResponseEntity<List<CommandeResponse>> lister(
            @RequestParam(required = false) Long fournisseurId,
            @RequestParam(required = false) String statut) {
        return ResponseEntity.ok(
                commandeService.listerCommandes(
                        fournisseurId, statut));
    }

    // POST /api/commandes — PREPARATEUR ou PHARMACIEN
    @PostMapping
    @PreAuthorize("hasAnyAuthority(" +
            "'ROLE_PREPARATEUR','ROLE_PHARMACIEN')")
    public ResponseEntity<CommandeResponse> creer(
            @Valid @RequestBody CommandeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(commandeService.creerCommande(
                        request,
                        userDetails.getUsername()
                ));
    }

    // PATCH /api/commandes/{id}/envoyer — PHARMACIEN
    @PatchMapping("/{id}/envoyer")
    @PreAuthorize("hasAuthority('ROLE_PHARMACIEN')")
    public ResponseEntity<CommandeResponse> envoyer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                commandeService.envoyerCommande(
                        id,
                        userDetails.getUsername()
                ));
    }

    // POST /api/commandes/{id}/reception — PREPARATEUR
    @PostMapping("/{id}/reception")
    @PreAuthorize("hasAnyAuthority(" +
            "'ROLE_PREPARATEUR','ROLE_PHARMACIEN')")
    public ResponseEntity<CommandeResponse> receptionner(
            @PathVariable Long id,
            @Valid @RequestBody ReceptionCommandeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                commandeService.receptionnerCommande(
                        id,
                        request,
                        userDetails.getUsername()
                ));
    }

    // GET /api/commandes/suggestions
    @GetMapping("/suggestions")
    @PreAuthorize("hasAnyAuthority('ROLE_PREPARATEUR','ROLE_PHARMACIEN')")
    public ResponseEntity<List<CommandeResponse.LigneCommandeResponse>> suggestions() {
        return ResponseEntity.ok(
                commandeService.getSuggestions());
    }
}