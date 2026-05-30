package com.Web.Pharmagest.service;

import com.Web.Pharmagest.repository.*;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CsvService {

    private final MedicamentRepository medicamentRepository;
    private final VenteRepository venteRepository;
    private final LigneVenteRepository ligneVenteRepository;
    private final LotStockRepository lotStockRepository;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ================================================
    // EXPORT CSV — Inventaire des médicaments
    // ================================================
    public byte[] exporterInventaireCsv() {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(baos,
                        StandardCharsets.UTF_8))) {

            // En-tête BOM pour Excel (évite les problèmes
            // d'encodage des caractères spéciaux)
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);

            // Ligne d'en-tête
            writer.writeNext(new String[]{
                    "Code CIP",
                    "Dénomination",
                    "DCI",
                    "Forme galénique",
                    "Dosage",
                    "Catégorie",
                    "Statut légal",
                    "Prix vente TTC",
                    "Prix achat HT",
                    "Stock actuel",
                    "Seuil alerte",
                    "Niveau alerte",
                    "Emplacement",
                    "Fabricant",
                    "Remboursable SS",
                    "Taux remboursement SS",
                    "Disponible"
            });

            // Données
            var medicaments =
                    medicamentRepository.findAll();

            for (var m : medicaments) {

                // Calcul niveau alerte
                String niveauAlerte = "NORMAL";
                if (m.getStockQuantiteTotale()
                        <= m.getStockSeuilAlerte()) {
                    niveauAlerte = "ROUGE";
                } else if (m.getStockQuantiteTotale()
                        <= m.getStockSeuilAlerte() * 2) {
                    niveauAlerte = "ORANGE";
                }

                writer.writeNext(new String[]{
                        m.getCodeCip(),
                        m.getDenomination(),
                        m.getDci(),
                        m.getFormeGalenique(),
                        m.getDosage() != null ? m.getDosage() : "",
                        m.getCategorie().getLibelle(),
                        m.getStatutLegal().name(),
                        m.getPrixVenteTtc().toString(),
                        m.getPrixAchatHt().toString(),
                        String.valueOf(m.getStockQuantiteTotale()),
                        String.valueOf(m.getStockSeuilAlerte()),
                        niveauAlerte,
                        m.getEmplacement() != null
                                ? m.getEmplacement() : "",
                        m.getFabricant() != null
                                ? m.getFabricant() : "",
                        m.getEstRemboursableSs() ? "OUI" : "NON",
                        m.getTauxRemboursementSs() != null
                                ? m.getTauxRemboursementSs()
                                .toString() + "%" : "0%",
                        m.getEstDisponible() ? "OUI" : "NON"
                });
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erreur export CSV inventaire : "
                            + e.getMessage()
            );
        }

        return baos.toByteArray();
    }

    // ================================================
    // EXPORT CSV — Ventes sur une période
    // ================================================
    public byte[] exporterVentesCsv(
            LocalDate debut, LocalDate fin) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(baos,
                        StandardCharsets.UTF_8))) {

            // BOM pour Excel
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);

            // En-tête
            writer.writeNext(new String[]{
                    "Référence",
                    "Date",
                    "Vendeur",
                    "Patient",
                    "Mode paiement",
                    "Montant HT",
                    "TVA",
                    "Total TTC",
                    "Remboursé SS",
                    "Remboursé Mutuelle",
                    "Payé client",
                    "Statut"
            });

            LocalDateTime debutDt = LocalDateTime
                    .of(debut, LocalTime.MIDNIGHT);
            LocalDateTime finDt = LocalDateTime
                    .of(fin, LocalTime.MAX);

            var ventes = venteRepository
                    .findVentesParPeriode(debutDt, finDt);

            for (var v : ventes) {
                writer.writeNext(new String[]{
                        v.getReference(),
                        v.getEffectueele().format(DATETIME_FORMAT),
                        v.getVendeur().getNom()
                                + " " + v.getVendeur().getPrenom(),
                        v.getPatient() != null
                                ? v.getPatient().getNom()
                                + " " + v.getPatient().getPrenom()
                                : "Anonyme",
                        v.getModePaiement().name(),
                        v.getMontantTotalHt().toString(),
                        v.getMontantTva().toString(),
                        v.getMontantTotalTtc().toString(),
                        v.getMontantRembourseSs().toString(),
                        v.getMontantRembourseMutuelle().toString(),
                        v.getMontantPayeClient().toString(),
                        v.getStatut().name()
                });
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erreur export CSV ventes : "
                            + e.getMessage()
            );
        }

        return baos.toByteArray();
    }

    // ================================================
    // EXPORT CSV — Rapport financier (CA + TVA)
    // ================================================
    public byte[] exporterRapportFinancierCsv(
            LocalDate debut, LocalDate fin) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(baos,
                        StandardCharsets.UTF_8))) {

            // BOM pour Excel
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);

            LocalDateTime debutDt = LocalDateTime
                    .of(debut, LocalTime.MIDNIGHT);
            LocalDateTime finDt = LocalDateTime
                    .of(fin, LocalTime.MAX);

            var ventes = venteRepository
                    .findVentesParPeriode(debutDt, finDt);

            // Calculs globaux
            var totalHt = ventes.stream()
                    .filter(v -> v.getStatut()
                            == com.Web.Pharmagest.entity
                            .Vente.StatutVente.VALIDEE)
                    .map(v -> v.getMontantTotalHt())
                    .reduce(java.math.BigDecimal.ZERO,
                            java.math.BigDecimal::add);

            var totalTva = ventes.stream()
                    .filter(v -> v.getStatut()
                            == com.Web.Pharmagest.entity
                            .Vente.StatutVente.VALIDEE)
                    .map(v -> v.getMontantTva())
                    .reduce(java.math.BigDecimal.ZERO,
                            java.math.BigDecimal::add);

            var totalTtc = ventes.stream()
                    .filter(v -> v.getStatut()
                            == com.Web.Pharmagest.entity
                            .Vente.StatutVente.VALIDEE)
                    .map(v -> v.getMontantTotalTtc())
                    .reduce(java.math.BigDecimal.ZERO,
                            java.math.BigDecimal::add);

            var totalRembourseSs = ventes.stream()
                    .filter(v -> v.getStatut()
                            == com.Web.Pharmagest.entity
                            .Vente.StatutVente.VALIDEE)
                    .map(v -> v.getMontantRembourseSs())
                    .reduce(java.math.BigDecimal.ZERO,
                            java.math.BigDecimal::add);

            // ===== SECTION RÉSUMÉ =====
            writer.writeNext(new String[]{
                    "RAPPORT FINANCIER PHARMAGEST"
            });
            writer.writeNext(new String[]{
                    "Période",
                    debut.format(DATE_FORMAT)
                            + " au " + fin.format(DATE_FORMAT)
            });
            writer.writeNext(new String[]{""});

            // KPIs globaux
            writer.writeNext(new String[]{
                    "RÉSUMÉ GLOBAL", ""
            });
            writer.writeNext(new String[]{
                    "Nombre de ventes validées",
                    String.valueOf(ventes.stream()
                            .filter(v -> v.getStatut()
                                    == com.Web.Pharmagest.entity
                                    .Vente.StatutVente.VALIDEE)
                            .count())
            });
            writer.writeNext(new String[]{
                    "Chiffre d'affaires HT", totalHt.toString()
            });
            writer.writeNext(new String[]{
                    "TVA collectée", totalTva.toString()
            });
            writer.writeNext(new String[]{
                    "Chiffre d'affaires TTC", totalTtc.toString()
            });
            writer.writeNext(new String[]{
                    "Total remboursé SS", totalRembourseSs.toString()
            });
            writer.writeNext(new String[]{""});

            // ===== SECTION DÉTAIL PAR JOUR =====
            writer.writeNext(new String[]{
                    "DÉTAIL PAR VENTE", "", "", "", ""
            });
            writer.writeNext(new String[]{
                    "Référence", "Date", "Total HT",
                    "TVA", "Total TTC", "Statut"
            });

            for (var v : ventes) {
                writer.writeNext(new String[]{
                        v.getReference(),
                        v.getEffectueele().format(DATETIME_FORMAT),
                        v.getMontantTotalHt().toString(),
                        v.getMontantTva().toString(),
                        v.getMontantTotalTtc().toString(),
                        v.getStatut().name()
                });
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erreur export CSV financier : "
                            + e.getMessage()
            );
        }

        return baos.toByteArray();
    }

    // ================================================
    // EXPORT CSV — Alertes stock + péremption
    // ================================================
    public byte[] exporterAlertesCsv() {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(baos,
                        StandardCharsets.UTF_8))) {

            // BOM pour Excel
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);

            // ===== SECTION STOCK BAS =====
            writer.writeNext(new String[]{
                    "=== ALERTES STOCK BAS ==="
            });
            writer.writeNext(new String[]{
                    "Médicament", "DCI", "Stock actuel",
                    "Seuil alerte", "Niveau alerte"
            });

            var stockBas = medicamentRepository
                    .findMedicamentsStockBas();
            var stockFaible = medicamentRepository
                    .findMedicamentsStockFaible();

            for (var m : stockBas) {
                writer.writeNext(new String[]{
                        m.getDenomination(),
                        m.getDci(),
                        String.valueOf(m.getStockQuantiteTotale()),
                        String.valueOf(m.getStockSeuilAlerte()),
                        "ROUGE"
                });
            }

            for (var m : stockFaible) {
                writer.writeNext(new String[]{
                        m.getDenomination(),
                        m.getDci(),
                        String.valueOf(m.getStockQuantiteTotale()),
                        String.valueOf(m.getStockSeuilAlerte()),
                        "ORANGE"
                });
            }

            writer.writeNext(new String[]{""});

            // ===== SECTION PÉREMPTIONS =====
            writer.writeNext(new String[]{
                    "=== ALERTES PÉREMPTION (90 jours) ==="
            });
            writer.writeNext(new String[]{
                    "Médicament", "N° Lot", "Qté restante",
                    "Date péremption", "Jours restants", "Alerte"
            });

            var lotsExpiration = lotStockRepository
                    .findLotsExpirantAvant(
                            LocalDate.now().plusDays(90));

            for (var lot : lotsExpiration) {
                long jours = java.time.temporal.ChronoUnit.DAYS
                        .between(LocalDate.now(),
                                lot.getDatePeremption());

                writer.writeNext(new String[]{
                        lot.getMedicament().getDenomination(),
                        lot.getNumero(),
                        String.valueOf(lot.getQuantiteRestante()),
                        lot.getDatePeremption().format(DATE_FORMAT),
                        String.valueOf(jours),
                        jours <= 30 ? "ROUGE" : "ORANGE"
                });
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erreur export CSV alertes : "
                            + e.getMessage()
            );
        }

        return baos.toByteArray();
    }
}