package com.Web.Pharmagest.controller;

import com.Web.Pharmagest.dto.request.VenteRequest;
import com.Web.Pharmagest.dto.response.VenteResponse;
import com.Web.Pharmagest.service.VenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ventes")
@RequiredArgsConstructor
public class VenteController {

    private final VenteService venteService;

    // ================================================
    // POST /api/ventes
    // CAISSIER, PREPARATEUR ou PHARMACIEN
    // ================================================
    @PostMapping
    @PreAuthorize("hasAnyAuthority(" +
            "'ROLE_CAISSIER'," +
            "'ROLE_PREPARATEUR'," +
            "'ROLE_PHARMACIEN')")
    public ResponseEntity<VenteResponse> creerVente(
            @Valid @RequestBody VenteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(venteService.creerVente(
                        request,
                        userDetails.getUsername()
                ));
    }

    // ================================================
    // GET /api/ventes/{id}
    // ================================================
    @GetMapping("/{id}")
    public ResponseEntity<VenteResponse> getVente(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                venteService.getVenteById(id));
    }

    // ================================================
    // PATCH /api/ventes/{id}/annuler
    // PHARMACIEN seulement
    // ================================================
    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasAuthority('ROLE_PHARMACIEN')")
    public ResponseEntity<VenteResponse> annulerVente(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                venteService.annulerVente(
                        id,
                        userDetails.getUsername()
                ));
    }
}