package com.Web.Pharmagest.controller;

import com.Web.Pharmagest.dto.request.MedicamentRequest;
import com.Web.Pharmagest.dto.response.MedicamentResponse;
import com.Web.Pharmagest.service.MedicamentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/medicaments")
@RequiredArgsConstructor
public class MedicamentController {

    private final MedicamentService medicamentService;

    // ================================================
    // GET /api/medicaments
    // Tous les rôles peuvent consulter le catalogue
    // ================================================
    @GetMapping
    public ResponseEntity<List<MedicamentResponse>> listerMedicaments(
            @RequestParam(required = false) String search) {

        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(
                    medicamentService.rechercher(search));
        }
        return ResponseEntity.ok(
                medicamentService.listerMedicaments());
    }

    // ================================================
    // GET /api/medicaments/{id}
    // ================================================
    @GetMapping("/{id}")
    public ResponseEntity<MedicamentResponse> getMedicament(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                medicamentService.getMedicamentById(id));
    }

    // ================================================
    // GET /api/medicaments/code-cip/{cip}
    // ================================================
    @GetMapping("/code-cip/{cip}")
    public ResponseEntity<MedicamentResponse> getMedicamentByCip(
            @PathVariable String cip) {
        return ResponseEntity.ok(
                medicamentService.getMedicamentByCip(cip));
    }

    // ================================================
    // POST /api/medicaments
    // PHARMACIEN seulement
    // ================================================
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_PHARMACIEN')")
    public ResponseEntity<MedicamentResponse> creerMedicament(
            @Valid @RequestBody MedicamentRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(medicamentService.creerMedicament(request));
    }

    // ================================================
    // PUT /api/medicaments/{id}
    // PHARMACIEN seulement
    // ================================================
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_PHARMACIEN')")
    public ResponseEntity<MedicamentResponse> modifierMedicament(
            @PathVariable Long id,
            @Valid @RequestBody MedicamentRequest request) {
        return ResponseEntity.ok(
                medicamentService.modifierMedicament(id, request));
    }

    // ================================================
    // PATCH /api/medicaments/{id}/disponibilite
    // Archiver ou réactiver un médicament
    // PHARMACIEN seulement
    // ================================================
    @PatchMapping("/{id}/disponibilite")
    @PreAuthorize("hasAuthority('ROLE_PHARMACIEN')")
    public ResponseEntity<MedicamentResponse> changerDisponibilite(
            @PathVariable Long id,
            @RequestParam Boolean disponible) {
        return ResponseEntity.ok(
                medicamentService.changerDisponibilite(id, disponible));
    }

    // ================================================
    // GET /api/medicaments/alertes/stock-bas
    // Médicaments en alerte ROUGE
    // ================================================
    @GetMapping("/alertes/stock-bas")
    public ResponseEntity<List<MedicamentResponse>> getStockBas() {
        return ResponseEntity.ok(
                medicamentService.getMedicamentsStockBas());
    }

    // ================================================
    // GET /api/medicaments/alertes/stock-faible
    // Médicaments en alerte ORANGE
    // ================================================
    @GetMapping("/alertes/stock-faible")
    public ResponseEntity<List<MedicamentResponse>> getStockFaible() {
        return ResponseEntity.ok(
                medicamentService.getMedicamentsStockFaible());
    }
}