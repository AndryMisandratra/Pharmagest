package com.Web.Pharmagest.controller;

import com.Web.Pharmagest.dto.request.OrdonnanceRequest;
import com.Web.Pharmagest.dto.response.OrdonnanceResponse;
import com.Web.Pharmagest.service.OrdonnanceService;
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
@RequestMapping("/api/ordonnances")
@RequiredArgsConstructor
public class OrdonnanceController {

    private final OrdonnanceService ordonnanceService;

    // ================================================
    // POST /api/ordonnances
    // PREPARATEUR ou PHARMACIEN
    // ================================================
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_PREPARATEUR','ROLE_PHARMACIEN')")
    public ResponseEntity<OrdonnanceResponse> creerOrdonnance(
            @Valid @RequestBody OrdonnanceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ordonnanceService.creerOrdonnance(
                        request,
                        userDetails.getUsername()
                ));
    }

    // ================================================
    // GET /api/ordonnances/{id}
    // ================================================
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_PREPARATEUR','ROLE_PHARMACIEN')")
    public ResponseEntity<OrdonnanceResponse> getOrdonnance(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ordonnanceService.getOrdonnanceById(id));
    }

    // ================================================
    // GET /api/ordonnances/numero/{numero}
    // Vérification anti-doublon
    // ================================================
    @GetMapping("/numero/{numero}")
    @PreAuthorize("hasAnyAuthority('ROLE_PREPARATEUR','ROLE_PHARMACIEN')")
    public ResponseEntity<OrdonnanceResponse> getOrdonnanceByNumero(
            @PathVariable String numero) {
        return ResponseEntity.ok(
                ordonnanceService.getOrdonnanceByNumero(numero));
    }

    // ================================================
    // GET /api/ordonnances?patientId=&statut=
    // ================================================
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_PREPARATEUR','ROLE_PHARMACIEN')")
    public ResponseEntity<List<OrdonnanceResponse>> getOrdonnances(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String statut) {

        if (patientId != null) {
            return ResponseEntity.ok(
                    ordonnanceService
                            .getOrdonnancesParPatient(patientId));
        }
        if (statut != null) {
            return ResponseEntity.ok(
                    ordonnanceService
                            .getOrdonnancesParStatut(statut));
        }
        return ResponseEntity.badRequest().build();
    }

    // ================================================
    // PATCH /api/ordonnances/{id}/statut
    // Changer le statut (SERVIE, PARTIELLEMENT_SERVIE...)
    // ================================================
    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyAuthority('ROLE_PREPARATEUR','ROLE_PHARMACIEN')")
    public ResponseEntity<OrdonnanceResponse> changerStatut(
            @PathVariable Long id,
            @RequestParam String statut) {
        return ResponseEntity.ok(
                ordonnanceService.changerStatut(id, statut));
    }
}