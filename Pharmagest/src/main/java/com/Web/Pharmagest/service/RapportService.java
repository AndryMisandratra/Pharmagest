package com.Web.Pharmagest.service;

import com.Web.Pharmagest.entity.Vente;
import com.Web.Pharmagest.repository.*;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RapportService {

    private final MedicamentRepository medicamentRepository;
    private final LotStockRepository lotStockRepository;
    private final VenteRepository venteRepository;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ================================================
    // MÉTHODES PRIVÉES UTILITAIRES
    // Définies EN PREMIER pour éviter les erreurs
    // ================================================

    private void ajouterEnTete(Document document, String titre) {
        document.add(
                new Paragraph("PHARMAGEST")
                        .setFontSize(22)
                        .setBold()
                        .setFontColor(ColorConstants.DARK_GRAY)
                        .setTextAlignment(TextAlignment.CENTER)
        );
        document.add(
                new Paragraph(titre)
                        .setFontSize(16)
                        .setBold()
                        .setFontColor(ColorConstants.BLACK)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(4)
        );
        document.add(
                new Paragraph("Généré le : "
                        + LocalDateTime.now().format(DATETIME_FORMAT))
                        .setFontSize(9)
                        .setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setMarginTop(2)
        );
        document.add(new Paragraph("\n"));
    }

    private void ajouterEnTeteTableau(Table table,
                                      String[] colonnes) {
        for (String col : colonnes) {
            table.addHeaderCell(creerHeaderCell(col));
        }
    }

    private Cell creerHeaderCell(String contenu) {
        return new Cell()
                .add(new Paragraph(contenu)
                        .setBold()
                        .setFontSize(10)
                        .setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(6)
                .setBorder(new SolidBorder(ColorConstants.DARK_GRAY, 0.8f));
    }

    private Cell creerCellule(String contenu) {
        return new Cell()
                .add(new Paragraph(contenu != null ? contenu : "-"))
                .setPadding(6)
                .setFontSize(9)
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
                .setBackgroundColor(ColorConstants.WHITE);
    }

    private Cell creerEnteteResume(String contenu) {
        return new Cell()
                .add(new Paragraph(contenu)
                        .setBold()
                        .setFontSize(10)
                        .setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(6)
                .setBorder(new SolidBorder(ColorConstants.DARK_GRAY, 0.8f));
    }

    private String safe(String valeur) {
        return valeur != null ? valeur : "-";
    }

    private String safe(BigDecimal valeur) {
        return valeur != null ? valeur.toString() : "0";
    }

    private String safe(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMAT) : "-";
    }

    private String safe(Vente.ModePaiement modePaiement) {
        return modePaiement != null ? modePaiement.name() : "-";
    }

    private String safe(Vente.StatutVente statut) {
        return statut != null ? statut.name() : "-";
    }

    private String safeStatut(Vente vente) {
        return vente != null && vente.getStatut() != null
                ? vente.getStatut().name() : "-";
    }

    // ================================================
    // RAPPORT INVENTAIRE
    // ================================================
    public byte[] genererRapportInventaire() {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(36, 36, 36, 36);

            ajouterEnTete(document, "RAPPORT D'INVENTAIRE");

            document.add(
                    new Paragraph("Généré le : "
                            + LocalDateTime.now().format(DATETIME_FORMAT))
                            .setFontSize(10)
                            .setFontColor(ColorConstants.GRAY)
            );
            document.add(new Paragraph("\n"));

            Table table = new Table(
                    UnitValue.createPercentArray(
                            new float[]{15, 25, 20, 10, 10, 10, 10}))
                    .setWidth(UnitValue.createPercentValue(100));

            ajouterEnTeteTableau(table, new String[]{
                    "Code CIP", "Dénomination", "DCI",
                    "Forme", "Stock", "Seuil", "Alerte"
            });

            var medicaments =
                    medicamentRepository.findByEstDisponibleTrue();

            for (var m : medicaments) {

                String alerte = "NORMAL";
                if (m.getStockQuantiteTotale()
                        <= m.getStockSeuilAlerte()) {
                    alerte = "ROUGE";
                } else if (m.getStockQuantiteTotale()
                        <= m.getStockSeuilAlerte() * 2) {
                    alerte = "ORANGE";
                }

                table.addCell(creerCellule(m.getCodeCip()));
                table.addCell(creerCellule(m.getDenomination()));
                table.addCell(creerCellule(m.getDci()));
                table.addCell(creerCellule(m.getFormeGalenique()));
                table.addCell(creerCellule(
                        String.valueOf(m.getStockQuantiteTotale())));
                table.addCell(creerCellule(
                        String.valueOf(m.getStockSeuilAlerte())));

                Cell celluleAlerte = creerCellule(alerte);
                if (alerte.equals("ROUGE")) {
                    celluleAlerte
                            .setBackgroundColor(ColorConstants.RED)
                            .setFontColor(ColorConstants.WHITE);
                } else if (alerte.equals("ORANGE")) {
                    celluleAlerte
                            .setBackgroundColor(ColorConstants.ORANGE);
                }
                table.addCell(celluleAlerte);
            }

            document.add(table);
            document.add(new Paragraph("\n"));
            document.add(
                    new Paragraph("Total médicaments : "
                            + medicaments.size()).setBold()
            );

            long enAlerte = medicaments.stream()
                    .filter(m -> m.getStockQuantiteTotale()
                            <= m.getStockSeuilAlerte())
                    .count();

            document.add(
                    new Paragraph(
                            "Médicaments en alerte rouge : " + enAlerte)
                            .setFontColor(ColorConstants.RED)
                            .setBold()
            );

            document.close();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erreur génération PDF inventaire : "
                            + e.getMessage()
            );
        }

        return baos.toByteArray();
    }

    // ================================================
    // RAPPORT FINANCIER
    // ================================================
    public byte[] genererRapportFinancier(
            LocalDate debut, LocalDate fin) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(36, 36, 36, 36);

            ajouterEnTete(document, "RAPPORT FINANCIER");

            document.add(
                    new Paragraph("Période : "
                            + debut.format(DATE_FORMAT)
                            + " au " + fin.format(DATE_FORMAT))
                            .setFontSize(11)
            );
            document.add(new Paragraph("\n"));

            LocalDateTime debutDt =
                    LocalDateTime.of(debut, LocalTime.MIDNIGHT);
            LocalDateTime finDt =
                    LocalDateTime.of(fin, LocalTime.MAX);

            var ventes = venteRepository
                    .findVentesParPeriode(debutDt, finDt);

            var ventesValidees = ventes.stream()
                    .filter(v -> v.getStatut()
                            == Vente.StatutVente.VALIDEE)
                    .toList();

            var totalHt = ventesValidees.stream()
                    .map(Vente::getMontantTotalHt)
                    .reduce(BigDecimal.ZERO,
                            BigDecimal::add);
            var totalTva = ventesValidees.stream()
                    .map(Vente::getMontantTva)
                    .reduce(BigDecimal.ZERO,
                            BigDecimal::add);
            var totalTtc = ventesValidees.stream()
                    .map(Vente::getMontantTotalTtc)
                    .reduce(BigDecimal.ZERO,
                            BigDecimal::add);
            var totalRembourseSs = ventesValidees.stream()
                    .map(Vente::getMontantRembourseSs)
                    .reduce(BigDecimal.ZERO,
                            BigDecimal::add);

            Table resume = new Table(
                    UnitValue.createPercentArray(new float[]{40f, 60f}))
                    .setWidth(UnitValue.createPercentValue(100));
            resume.addCell(creerCellule("KPI"));
            resume.addCell(creerCellule("Valeur"));
            resume.addCell(creerCellule("Ventes validées"));
            resume.addCell(creerCellule(String.valueOf(
                    ventesValidees.size())));
            resume.addCell(creerCellule("Chiffre d'affaires HT"));
            resume.addCell(creerCellule(safe(totalHt) + " Ar"));
            resume.addCell(creerCellule("TVA collectée"));
            resume.addCell(creerCellule(safe(totalTva) + " Ar"));
            resume.addCell(creerCellule("Chiffre d'affaires TTC"));
            resume.addCell(creerCellule(safe(totalTtc) + " Ar"));
            resume.addCell(creerCellule("Total remboursé SS"));
            resume.addCell(creerCellule(safe(totalRembourseSs) + " Ar"));

            document.add(
                    new Paragraph("Résumé financier")
                            .setBold()
                            .setFontSize(12)
                            .setMarginBottom(6)
            );
            document.add(resume);
            document.add(new Paragraph("\n"));

            Table table = new Table(
                    UnitValue.createPercentArray(
                            new float[]{18, 18, 14, 14, 18, 18}))
                    .setWidth(UnitValue.createPercentValue(100));

            ajouterEnTeteTableau(table, new String[]{
                    "Référence", "Date", "Total HT",
                    "TVA", "Total TTC", "Statut"
            });

            if (ventes.isEmpty()) {
                table.addCell(new Cell(1, 6)
                        .add(new Paragraph("Aucune vente"))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setItalic()
                        .setFontSize(10)
                        .setPadding(8));
            } else {
                for (var v : ventes) {
                    table.addCell(creerCellule(safe(v.getReference())));
                    table.addCell(creerCellule(safe(v.getEffectueele())));
                    table.addCell(creerCellule(safe(v.getMontantTotalHt()) + " Ar"));
                    table.addCell(creerCellule(safe(v.getMontantTva()) + " Ar"));
                    table.addCell(creerCellule(safe(v.getMontantTotalTtc()) + " Ar"));
                    table.addCell(creerCellule(safe(v.getStatut())));
                }
            }

            document.add(
                    new Paragraph("Détail des ventes")
                            .setBold()
                            .setFontSize(12)
                            .setMarginBottom(6)
            );
            document.add(table);
            document.close();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erreur génération PDF financier : "
                            + e.getMessage()
            );
        }

        return baos.toByteArray();
    }

    // ================================================
    // RAPPORT ALERTES
    // ================================================
    public byte[] genererRapportAlertes() {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(36, 36, 36, 36);

            ajouterEnTete(document, "RAPPORT DES ALERTES");

            document.add(
                    new Paragraph("Généré le : "
                            + LocalDateTime.now().format(DATETIME_FORMAT))
                            .setFontSize(10)
            );
            document.add(new Paragraph("\n"));

            // ===== STOCK BAS =====
            document.add(
                    new Paragraph("RUPTURES DE STOCK")
                            .setBold()
                            .setFontSize(13)
                            .setFontColor(ColorConstants.RED)
            );

            var stockBas =
                    medicamentRepository.findMedicamentsStockBas();

            if (stockBas.isEmpty()) {
                document.add(
                        new Paragraph(
                                "Aucune rupture de stock détectée")
                                .setFontColor(ColorConstants.GREEN)
                );
            } else {
                Table tableStock = new Table(
                        UnitValue.createPercentArray(
                                new float[]{30, 25, 15, 15, 15}))
                        .setWidth(UnitValue.createPercentValue(100));

                ajouterEnTeteTableau(tableStock, new String[]{
                        "Médicament", "DCI",
                        "Stock actuel", "Seuil", "Statut"
                });

                for (var m : stockBas) {
                    tableStock.addCell(
                            creerCellule(m.getDenomination()));
                    tableStock.addCell(creerCellule(m.getDci()));
                    tableStock.addCell(creerCellule(
                            String.valueOf(
                                    m.getStockQuantiteTotale())));
                    tableStock.addCell(creerCellule(
                            String.valueOf(m.getStockSeuilAlerte())));
                    tableStock.addCell(
                            creerCellule("RUPTURE")
                                    .setBackgroundColor(ColorConstants.RED)
                                    .setFontColor(ColorConstants.WHITE)
                    );
                }
                document.add(tableStock);
            }

            document.add(new Paragraph("\n"));

            // ===== PÉREMPTIONS =====
            document.add(
                    new Paragraph(
                            "LOTS PÉRIMANT DANS MOINS DE 90 JOURS")
                            .setBold()
                            .setFontSize(13)
                            .setFontColor(ColorConstants.ORANGE)
            );

            var lotsExpiration = lotStockRepository
                    .findLotsExpirantAvant(
                            LocalDate.now().plusDays(90));

            if (lotsExpiration.isEmpty()) {
                document.add(
                        new Paragraph(
                                "Aucun lot en péremption prochaine")
                                .setFontColor(ColorConstants.GREEN)
                );
            } else {
                Table tableLots = new Table(
                        UnitValue.createPercentArray(
                                new float[]{25, 15, 15, 15, 15, 15}))
                        .setWidth(UnitValue.createPercentValue(100));

                ajouterEnTeteTableau(tableLots, new String[]{
                        "Médicament", "N° Lot", "Qté",
                        "Date péremption", "Jours restants", "Alerte"
                });

                for (var lot : lotsExpiration) {
                    long jours = ChronoUnit.DAYS.between(
                            LocalDate.now(),
                            lot.getDatePeremption()
                    );
                    String alerteNiveau =
                            jours <= 30 ? "ROUGE" : "ORANGE";

                    tableLots.addCell(creerCellule(
                            lot.getMedicament().getDenomination()));
                    tableLots.addCell(
                            creerCellule(lot.getNumero()));
                    tableLots.addCell(creerCellule(
                            String.valueOf(
                                    lot.getQuantiteRestante())));
                    tableLots.addCell(creerCellule(
                            lot.getDatePeremption()
                                    .format(DATE_FORMAT)));
                    tableLots.addCell(creerCellule(
                            String.valueOf(jours)));

                    Cell cellAlerte = creerCellule(alerteNiveau);
                    if (alerteNiveau.equals("ROUGE")) {
                        cellAlerte
                                .setBackgroundColor(ColorConstants.RED)
                                .setFontColor(ColorConstants.WHITE);
                    } else {
                        cellAlerte.setBackgroundColor(
                                ColorConstants.ORANGE);
                    }
                    tableLots.addCell(cellAlerte);
                }
                document.add(tableLots);
            }

            document.close();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erreur génération PDF alertes : "
                            + e.getMessage()
            );
        }

        return baos.toByteArray();
    }
}