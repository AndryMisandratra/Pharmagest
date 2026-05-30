package com.Web.Pharmagest.controller;

import com.Web.Pharmagest.dto.request.VenteRequest;
import com.Web.Pharmagest.dto.response.VenteResponse;
import com.Web.Pharmagest.service.VenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.time.LocalDateTime;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/ventes")
@RequiredArgsConstructor
public class VenteController {

    private final VenteService venteService;

    // ================================================
    // GET /api/ventes?debut=&fin=
    // Liste des ventes sur une période
    // PHARMACIEN seulement
    // ================================================
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_PHARMACIEN')")
    public ResponseEntity<List<VenteResponse>> getVentes(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate debut,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fin) {

        // Si pas de dates → ventes du jour par défaut
        LocalDate dateDebut = debut != null
                ? debut : LocalDate.now();
        LocalDate dateFin   = fin != null
                ? fin : LocalDate.now();

        LocalDateTime debutDt = dateDebut.atStartOfDay();
        LocalDateTime finDt   = dateFin.atTime(LocalTime.MAX);

        return ResponseEntity.ok(
                venteService.getVentesParPeriode(
                        debutDt, finDt
                )
        );
    }

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

    // GET /api/ventes/{id}/ticket-pdf
    // Accessible à tous les rôles authentifiés
        @GetMapping("/{id}/ticket-pdf")
    // ← pas de @PreAuthorize ici
        public ResponseEntity<byte[]> getTicketPdf(
                @PathVariable Long id) {

            byte[] pdf = venteService.genererTicketPdf(id);

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=ticket_"
                                    + id + ".pdf"
                    )
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        }
}