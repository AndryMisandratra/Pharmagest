package com.Web.Pharmagest.controller;

import com.Web.Pharmagest.service.CsvService;
import com.Web.Pharmagest.service.RapportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/rapports")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_PHARMACIEN')")
public class RapportController {

    private final RapportService rapportService;

    // ================================================
    // GET /api/rapports/inventaire
    // Inventaire complet en PDF
    // ================================================
    @GetMapping("/inventaire")
    public ResponseEntity<byte[]> inventaire() {

        byte[] pdf = rapportService.genererRapportInventaire();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=inventaire_"
                                + LocalDate.now() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ================================================
    // GET /api/rapports/ventes?debut=&fin=
    // Rapport des ventes sur une période
    // ================================================
    @GetMapping("/ventes")
    public ResponseEntity<byte[]> ventes(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate debut,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fin) {

        byte[] pdf = rapportService
                .genererRapportVentes(debut, fin);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=ventes_"
                                + debut + "_" + fin + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ================================================
    // GET /api/rapports/alertes
    // État des alertes stock + péremption
    // ================================================
    @GetMapping("/alertes")
    public ResponseEntity<byte[]> alertes() {

        byte[] pdf = rapportService.genererRapportAlertes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=alertes_"
                                + LocalDate.now() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ===== Ajouter dans RapportController =====

    private final CsvService csvService;

    // GET /api/rapports/inventaire/csv
    @GetMapping("/inventaire/csv")
    public ResponseEntity<byte[]> inventaireCsv() {
        byte[] csv = csvService.exporterInventaireCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=inventaire_"
                                + LocalDate.now() + ".csv")
                .contentType(MediaType.parseMediaType(
                        "text/csv; charset=UTF-8"))
                .body(csv);
    }

    // GET /api/rapports/ventes/csv?debut=&fin=
    @GetMapping("/ventes/csv")
    public ResponseEntity<byte[]> ventesCsv(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate debut,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fin) {
        byte[] csv = csvService.exporterVentesCsv(debut, fin);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=ventes_"
                                + debut + "_" + fin + ".csv")
                .contentType(MediaType.parseMediaType(
                        "text/csv; charset=UTF-8"))
                .body(csv);
    }

    // GET /api/rapports/financier/csv?debut=&fin=
    @GetMapping("/financier/csv")
    public ResponseEntity<byte[]> financierCsv(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate debut,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fin) {
        byte[] csv = csvService
                .exporterRapportFinancierCsv(debut, fin);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=financier_"
                                + debut + "_" + fin + ".csv")
                .contentType(MediaType.parseMediaType(
                        "text/csv; charset=UTF-8"))
                .body(csv);
    }

    // GET /api/rapports/alertes/csv
    @GetMapping("/alertes/csv")
    public ResponseEntity<byte[]> alertesCsv() {
        byte[] csv = csvService.exporterAlertesCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=alertes_"
                                + LocalDate.now() + ".csv")
                .contentType(MediaType.parseMediaType(
                        "text/csv; charset=UTF-8"))
                .body(csv);
    }
}