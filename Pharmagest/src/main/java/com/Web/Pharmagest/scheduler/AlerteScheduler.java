package com.Web.Pharmagest.scheduler;

import com.Web.Pharmagest.repository.UtilisateurRepository;
import com.Web.Pharmagest.service.AlerteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlerteScheduler {

    private final AlerteService alerteService;
    private final UtilisateurRepository utilisateurRepository;

    // ================================================
    // Cette classe ne déclenche plus l'envoi programmé à 8h.
    // L'alerte est désormais envoyée directement lors de la
    // première connexion hebdomadaire d'un utilisateur.
    // ================================================
    public void envoyerAlertesQuotidiennes() {
        LocalDateTime debutJour = LocalDate.now()
                .atStartOfDay();

        boolean premiereConnexion = !utilisateurRepository
                .aAuMoinsUneConnexionDepuis(debutJour);

        log.info("⏰ Vérification scheduler alertes — {}",
                LocalDateTime.now());
        log.info("Première connexion du jour: {}",
                premiereConnexion);

        if (premiereConnexion) {
            log.info("📧 Aucune connexion n'a encore eu lieu aujourd'hui, mais l'alerte sera envoyée au premier login.");
        } else {
            log.info("⏭️ Alertes déjà envoyées aujourd'hui (connexion déjà enregistrée)");
        }
    }
}