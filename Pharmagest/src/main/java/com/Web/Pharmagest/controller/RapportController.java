package com.Web.Pharmagest.controller;

import com.Web.Pharmagest.service.CsvService;
import com.Web.Pharmagest.service.RapportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/rapports")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
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
    // GET /api/rapports/financier
    // Rapport financier en PDF
    // ================================================
    @GetMapping("/financier")
    public ResponseEntity<byte[]> financier(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate debut,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fin) {
        try {
            byte[] pdf = rapportService
                    .genererRapportFinancier(debut, fin);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=financier_"
                                    + debut + "_" + fin + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception ex) {
            log.error("Erreur export PDF financier pour {} - {}", debut, fin, ex);
            throw ex;
        }
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

    // GET /api/rapports/financier/csv?debut=&fin=
    @GetMapping("/financier/csv")
    public ResponseEntity<byte[]> financierCsv(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate debut,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fin) {
        try {
            byte[] csv = csvService
                    .exporterRapportFinancierCsv(debut, fin);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=financier_"
                                    + debut + "_" + fin + ".csv")
                    .contentType(MediaType.parseMediaType(
                            "text/csv; charset=UTF-8"))
                    .body(csv);
        } catch (Exception ex) {
            log.error("Erreur export CSV financier pour {} - {}", debut, fin, ex);
            throw ex;
        }
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