package com.Web.Pharmagest.service;

import com.Web.Pharmagest.repository.LotStockRepository;
import com.Web.Pharmagest.repository.MedicamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j  // génère un logger : log.info(), log.error()...
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlerteService {

    private final MedicamentRepository medicamentRepository;
    private final LotStockRepository lotStockRepository;
    private final JavaMailSender mailSender;

    // Récupère l'email destinataire depuis application.properties
    @Value("${app.alert.email}")
    private String alertEmail;

    // ================================================
    // VÉRIFIER et ENVOYER les alertes
    // Appelée par le scheduler chaque matin à 8h
    // ================================================
    public void verifierEtEnvoyerAlertes() {

        log.info("Début vérification des alertes...");

        // 1. Médicaments en rupture (stock <= seuil)
        var stockBas = medicamentRepository
                .findMedicamentsStockBas();

        // 2. Médicaments stock faible (stock <= 2x seuil)
        var stockFaible = medicamentRepository
                .findMedicamentsStockFaible();

        // 3. Lots périmant dans moins de 30 jours (ROUGE)
        var lotsPeremptionRouge = lotStockRepository
                .findLotsExpirantAvant(
                        LocalDate.now().plusDays(30)
                );

        // 4. Lots périmant dans moins de 90 jours (ORANGE)
        var lotsPeremptionOrange = lotStockRepository
                .findLotsExpirantAvant(
                        LocalDate.now().plusDays(90)
                );

        // Si aucune alerte → pas d'email
        if (stockBas.isEmpty()
                && stockFaible.isEmpty()
                && lotsPeremptionRouge.isEmpty()
                && lotsPeremptionOrange.isEmpty()) {
            log.info("Aucune alerte détectée.");
            return;
        }

        // Construire le corps de l'email
        StringBuilder corps = new StringBuilder();
        corps.append("=== RAPPORT D'ALERTES PHARMAGEST ===\n");
        corps.append("Date : ")
                .append(LocalDate.now().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                ))
                .append("\n\n");

        // Section stock bas (ROUGE)
        if (!stockBas.isEmpty()) {
            corps.append("🔴 RUPTURE DE STOCK IMMINENTE (")
                    .append(stockBas.size())
                    .append(" médicaments)\n");
            corps.append("─────────────────────────────\n");
            stockBas.forEach(m ->
                    corps.append("• ")
                            .append(m.getDenomination())
                            .append(" — Stock actuel : ")
                            .append(m.getStockQuantiteTotale())
                            .append(" / Seuil : ")
                            .append(m.getStockSeuilAlerte())
                            .append("\n")
            );
            corps.append("\n");
        }

        // Section stock faible (ORANGE)
        if (!stockFaible.isEmpty()) {
            corps.append("🟠 STOCK FAIBLE (")
                    .append(stockFaible.size())
                    .append(" médicaments)\n");
            corps.append("─────────────────────────────\n");
            stockFaible.forEach(m ->
                    corps.append("• ")
                            .append(m.getDenomination())
                            .append(" — Stock : ")
                            .append(m.getStockQuantiteTotale())
                            .append("\n")
            );
            corps.append("\n");
        }

        // Section péremption rouge (< 30 jours)
        if (!lotsPeremptionRouge.isEmpty()) {
            corps.append("🔴 PÉREMPTION DANS MOINS DE 30 JOURS (")
                    .append(lotsPeremptionRouge.size())
                    .append(" lots)\n");
            corps.append("─────────────────────────────\n");
            lotsPeremptionRouge.forEach(lot ->
                    corps.append("• Lot ")
                            .append(lot.getNumero())
                            .append(" — ")
                            .append(lot.getMedicament().getDenomination())
                            .append(" — Expire le : ")
                            .append(lot.getDatePeremption().format(
                                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
                            ))
                            .append(" — Qté : ")
                            .append(lot.getQuantiteRestante())
                            .append("\n")
            );
            corps.append("\n");
        }

        // Section péremption orange (< 90 jours)
        // On exclut ceux déjà dans rouge (< 30j)
        var lotsOrangeUniquement = lotsPeremptionOrange.stream()
                .filter(lot -> lot.getDatePeremption()
                        .isAfter(LocalDate.now().plusDays(30)))
                .toList();

        if (!lotsOrangeUniquement.isEmpty()) {
            corps.append("🟠 PÉREMPTION DANS MOINS DE 90 JOURS (")
                    .append(lotsOrangeUniquement.size())
                    .append(" lots)\n");
            corps.append("─────────────────────────────\n");
            lotsOrangeUniquement.forEach(lot ->
                    corps.append("• Lot ")
                            .append(lot.getNumero())
                            .append(" — ")
                            .append(lot.getMedicament().getDenomination())
                            .append(" — Expire le : ")
                            .append(lot.getDatePeremption().format(
                                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
                            ))
                            .append("\n")
            );
        }

        corps.append("\n=== Fin du rapport ===\n");
        corps.append("PharmaGest — Système automatique");

        // Envoyer l'email
        envoyerEmail(
                "🚨 Alertes PharmaGest du "
                        + LocalDate.now().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                ),
                corps.toString()
        );

        log.info("Email d'alertes envoyé à {}", alertEmail);
    }

    // ================================================
    // MÉTHODE PRIVÉE : envoyer un email simple
    // ================================================
    private void envoyerEmail(String sujet, String corps) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(alertEmail);
            message.setSubject(sujet);
            message.setText(corps);
            mailSender.send(message);
        } catch (Exception e) {
            // On log l'erreur mais on ne bloque pas l'application
            log.error("Erreur envoi email : {}", e.getMessage());
        }
    }
}