package com.Web.Pharmagest.controller;

import com.Web.Pharmagest.dto.response.DashboardResponse;
import com.Web.Pharmagest.service.AlerteService;
import com.Web.Pharmagest.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final AlerteService alerteService;

    // ================================================
    // GET /api/dashboard/resume
    // KPIs du jour + alertes + top médicaments
    // Tous les rôles peuvent accéder
    // ================================================
    @GetMapping("/resume")
    public ResponseEntity<DashboardResponse> getResume() {
        return ResponseEntity.ok(
                dashboardService.getResume()
        );
    }

    // ================================================
    // POST /api/dashboard/alertes/tester
    // Déclenche manuellement l'envoi de l'email d'alertes
    // Utile pour tester sans attendre 8h
    // PHARMACIEN seulement
    // ================================================
    @PostMapping("/alertes/tester")
    public ResponseEntity<String> testerAlertes() {
        alerteService.verifierEtEnvoyerAlertes();
        return ResponseEntity.ok(
                "Email d'alertes envoyé à aandrymisa@gmail.com"
        );
    }
}
