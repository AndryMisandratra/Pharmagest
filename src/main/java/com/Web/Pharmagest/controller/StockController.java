package com.Web.Pharmagest.controller;

import com.Web.Pharmagest.dto.request.LotStockRequest;
import com.Web.Pharmagest.dto.request.MouvementStockRequest;
import com.Web.Pharmagest.dto.response.LotStockResponse;
import com.Web.Pharmagest.dto.response.MouvementStockResponse;
import com.Web.Pharmagest.service.StockService;
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
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    // ================================================
    // GET /api/lots?medicamentId=
    // Lots d'un médicament triés FEFO
    // ================================================
    @GetMapping("/api/lots")
    public ResponseEntity<List<LotStockResponse>> getLots(
            @RequestParam Long medicamentId) {
        return ResponseEntity.ok(
                stockService.getLotsParMedicament(medicamentId));
    }

    // ================================================
    // POST /api/lots
    // Ajouter un lot manuellement
    // PREPARATEUR ou PHARMACIEN
    // ================================================
    @PostMapping("/api/lots")
    @PreAuthorize("hasAnyAuthority('ROLE_PREPARATEUR','ROLE_PHARMACIEN')")
    public ResponseEntity<LotStockResponse> creerLot(
            @Valid @RequestBody LotStockRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(stockService.creerLot(
                        request,
                        userDetails.getUsername()
                ));
    }

    // ================================================
    // PATCH /api/lots/{id}/retrait
    // Retirer un lot (périmé, perte, casse)
    // PHARMACIEN seulement
    // ================================================
    @PatchMapping("/api/lots/{id}/retrait")
    @PreAuthorize("hasAuthority('ROLE_PHARMACIEN')")
    public ResponseEntity<LotStockResponse> retirerLot(
            @PathVariable Long id,
            @RequestParam String motif,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                stockService.retirerLot(
                        id,
                        motif,
                        userDetails.getUsername()
                ));
    }

    // ================================================
    // GET /api/medicaments/alertes/peremption?jours=90
    // Lots qui expirent dans N jours
    // ================================================
    @GetMapping("/api/medicaments/alertes/peremption")
    public ResponseEntity<List<LotStockResponse>> getLotsExpirant(
            @RequestParam(defaultValue = "90") int jours) {
        return ResponseEntity.ok(
                stockService.getLotsExpirantDans(jours));
    }

    // ================================================
    // POST /api/mouvements
    // Enregistrer un mouvement de stock manuellement
    // PREPARATEUR ou PHARMACIEN
    // ================================================
    @PostMapping("/api/mouvements")
    @PreAuthorize("hasAnyAuthority('ROLE_PREPARATEUR','ROLE_PHARMACIEN')")
    public ResponseEntity<MouvementStockResponse> enregistrerMouvement(
            @Valid @RequestBody MouvementStockRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(stockService.enregistrerMouvementManuel(
                        request,
                        userDetails.getUsername()
                ));
    }

    // ================================================
    // GET /api/mouvements?medicamentId=
    // Historique des mouvements d'un médicament
    // ================================================
    @GetMapping("/api/mouvements")
    public ResponseEntity<List<MouvementStockResponse>> getHistorique(
            @RequestParam Long medicamentId) {
        return ResponseEntity.ok(
                stockService.getHistoriqueMouvements(medicamentId));
    }
}