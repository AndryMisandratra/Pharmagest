package com.Web.Pharmagest.service;

import com.Web.Pharmagest.dto.response.DashboardResponse;
import com.Web.Pharmagest.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final MedicamentRepository medicamentRepository;
    private final LotStockRepository lotStockRepository;
    private final VenteRepository venteRepository;
    private final LigneVenteRepository ligneVenteRepository;

    // ================================================
    // RÉSUMÉ COMPLET DU DASHBOARD
    // ================================================
    public DashboardResponse getResume() {

        // Période du jour : 00:00:00 → 23:59:59
        LocalDateTime debutJour = LocalDateTime.of(
                LocalDate.now(), LocalTime.MIDNIGHT
        );
        LocalDateTime finJour = LocalDateTime.of(
                LocalDate.now(), LocalTime.MAX
        );

        // ===== KPIs DU JOUR =====
        BigDecimal caJour = venteRepository
                .calculerChiffreAffaires(debutJour, finJour);

        Long nbVentesJour = venteRepository
                .countVentesParPeriode(debutJour, finJour);

        Long nbMedicaments = medicamentRepository.count();

        // ===== ALERTES STOCK =====
        var stockBas = medicamentRepository
                .findMedicamentsStockBas();
        var stockFaible = medicamentRepository
                .findMedicamentsStockFaible();

        // ===== ALERTES PÉREMPTION =====
        var lotsRouge = lotStockRepository
                .findLotsExpirantAvant(
                        LocalDate.now().plusDays(30)
                );
        var lotsOrange = lotStockRepository
                .findLotsExpirantAvant(
                        LocalDate.now().plusDays(90)
                ).stream()
                .filter(lot -> lot.getDatePeremption()
                        .isAfter(LocalDate.now().plusDays(30)))
                .toList();

        // ===== TOP 5 MÉDICAMENTS =====
        // Période : 30 derniers jours
        LocalDateTime debut30j = LocalDateTime.now().minusDays(30);
        var topMedicaments = ligneVenteRepository
                .findTopMedicamentsVendus(debut30j, finJour)
                .stream()
                .limit(5)
                .map(row -> DashboardResponse.TopMedicamentDTO.builder()
                        .denomination((String) row[1])
                        .quantiteVendue(((Number) row[2]).longValue())
                        .build()
                )
                .collect(Collectors.toList());

        // ===== NOMS DES MÉDICAMENTS EN ALERTE =====
        List<String> nomsStockBas = stockBas.stream()
                .map(m -> m.getDenomination()
                        + " (stock: " + m.getStockQuantiteTotale() + ")")
                .collect(Collectors.toList());

        List<String> nomsStockFaible = stockFaible.stream()
                .map(m -> m.getDenomination()
                        + " (stock: " + m.getStockQuantiteTotale() + ")")
                .collect(Collectors.toList());

        // ===== CONSTRUCTION DE LA RÉPONSE =====
        return DashboardResponse.builder()
                .chiffreAffairesJour(
                        caJour != null ? caJour : BigDecimal.ZERO)
                .nombreVentesJour(
                        nbVentesJour != null ? nbVentesJour : 0L)
                .nombreMedicamentsActifs(
                        (int)(long) nbMedicaments)
                .nombreAlerteRougeStock(stockBas.size())
                .nombreAlerteOrangeStock(stockFaible.size())
                .medicamentsStockBas(nomsStockBas)
                .medicamentsStockFaible(nomsStockFaible)
                .nombreAlerteRougePeremption(lotsRouge.size())
                .nombreAlerteOrangePeremption(lotsOrange.size())
                .topMedicaments(topMedicaments)
                .build();
    }
}