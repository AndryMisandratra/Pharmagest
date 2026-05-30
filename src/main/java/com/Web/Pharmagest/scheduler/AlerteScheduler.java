package com.Web.Pharmagest.scheduler;

import com.Web.Pharmagest.service.AlerteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlerteScheduler {

    private final AlerteService alerteService;

    // ================================================
    // Exécuté automatiquement chaque matin à 8h00
    // cron = "secondes minutes heures jour mois jourSemaine"
    // "0 0 8 * * *" = à 8h00:00 tous les jours
    // ================================================
    @Scheduled(cron = "0 0 8 * * *")
    public void envoyerAlertesQuotidiennes() {
        log.info("⏰ Scheduler alertes démarré — {}",
                java.time.LocalDateTime.now());
        alerteService.verifierEtEnvoyerAlertes();
    }

    // ================================================
    // Test : exécuté toutes les 60 secondes
    // À COMMENTER en production !
    // Utile pour tester sans attendre 8h
    // ================================================
    // @Scheduled(fixedRate = 60000)
    // public void testAlertes() {
    //     log.info("Test alertes...");
    //     alerteService.verifierEtEnvoyerAlertes();
    // }
}