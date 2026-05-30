package com.Web.Pharmagest.controller;

import com.Web.Pharmagest.dto.request.FournisseurRequest;
import com.Web.Pharmagest.dto.response.FournisseurResponse;
import com.Web.Pharmagest.service.FournisseurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/fournisseurs")
@RequiredArgsConstructor
public class FournisseurController {

    private final FournisseurService fournisseurService;

    // GET /api/fournisseurs — tous les rôles
    @GetMapping
    public ResponseEntity<List<FournisseurResponse>> lister() {
        return ResponseEntity.ok(
                fournisseurService.listerFournisseurs());
    }

    // GET /api/fournisseurs/{id}
    @GetMapping("/{id}")
    public ResponseEntity<FournisseurResponse> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                fournisseurService.getFournisseurById(id));
    }

    // POST /api/fournisseurs — PHARMACIEN seulement
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_PHARMACIEN')")
    public ResponseEntity<FournisseurResponse> creer(
            @Valid @RequestBody FournisseurRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(fournisseurService.creerFournisseur(request));
    }

    // PUT /api/fournisseurs/{id} — PHARMACIEN seulement
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_PHARMACIEN')")
    public ResponseEntity<FournisseurResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody FournisseurRequest request) {
        return ResponseEntity.ok(
                fournisseurService.modifierFournisseur(id, request));
    }
}