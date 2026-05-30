package com.Web.Pharmagest.controller;

import com.Web.Pharmagest.dto.request.UtilisateurRequest;
import com.Web.Pharmagest.dto.response.UtilisateurResponse;
import com.Web.Pharmagest.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
// Tous les endpoints ici = PHARMACIEN seulement
@PreAuthorize("hasAuthority('ROLE_PHARMACIEN')")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    // ================================================
    // GET /api/utilisateurs
    // Liste tous les comptes du personnel
    // ================================================
    @GetMapping
    public ResponseEntity<List<UtilisateurResponse>> lister() {
        return ResponseEntity.ok(
                utilisateurService.listerUtilisateurs());
    }

    // ================================================
    // GET /api/utilisateurs/{id}
    // ================================================
    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurResponse> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                utilisateurService.getUtilisateurById(id));
    }

    // ================================================
    // POST /api/utilisateurs
    // Créer un nouveau compte personnel
    // ================================================
    @PostMapping
    public ResponseEntity<UtilisateurResponse> creer(
            @Valid @RequestBody UtilisateurRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(utilisateurService
                        .creerUtilisateur(request));
    }

    // ================================================
    // PUT /api/utilisateurs/{id}
    // Modifier un compte existant
    // ================================================
    @PutMapping("/{id}")
    public ResponseEntity<UtilisateurResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody UtilisateurRequest request) {
        return ResponseEntity.ok(
                utilisateurService
                        .modifierUtilisateur(id, request));
    }

    // ================================================
    // PATCH /api/utilisateurs/{id}/actif
    // Activer ou désactiver un compte
    // ================================================
    @PatchMapping("/{id}/actif")
    public ResponseEntity<UtilisateurResponse> changerActif(
            @PathVariable Long id,
            @RequestParam Boolean estActif) {
        return ResponseEntity.ok(
                utilisateurService
                        .changerStatutActif(id, estActif));
    }
}
