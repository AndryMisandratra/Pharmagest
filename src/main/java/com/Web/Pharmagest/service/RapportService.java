package com.Web.Pharmagest.service;

import com.Web.Pharmagest.entity.Vente;
import com.Web.Pharmagest.repository.*;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
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
                        .setFontSize(20)
                        .setBold()
                        .setFontColor(ColorConstants.DARK_GRAY)
                        .setTextAlignment(TextAlignment.CENTER)
        );
        document.add(
                new Paragraph(titre)
                        .setFontSize(16)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
        );
        document.add(
                new Paragraph(
                        "--------------------------------------------")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(ColorConstants.GRAY)
        );
    }

    private void ajouterEnTeteTableau(Table table,
                                      String[] colonnes) {
        for (String col : colonnes) {
            table.addHeaderCell(
                    new Cell()
                            .add(new Paragraph(col)
                                    .setBold()
                                    .setFontColor(ColorConstants.WHITE))
                            .setBackgroundColor(ColorConstants.DARK_GRAY)
                            .setPadding(5)
            );
        }
    }

    private Cell creerCellule(String contenu) {
        return new Cell()
                .add(new Paragraph(
                        contenu != null ? contenu : "-"))
                .setPadding(4)
                .setFontSize(9);
    }

    // ================================================
    // RAPPORT INVENTAIRE
    // ================================================
    public byte[] genererRapportInventaire() {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

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
    // RAPPORT VENTES
    // ================================================
    public byte[] genererRapportVentes(
            LocalDate debut, LocalDate fin) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            ajouterEnTete(document, "RAPPORT DES VENTES");

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

            // Utilise la méthode @Query pour éviter
            // les problèmes de nommage Spring Data
            var ventes = venteRepository
                    .findVentesParPeriode(debutDt, finDt);

            Table table = new Table(
                    UnitValue.createPercentArray(
                            new float[]{15, 18, 15, 15, 12, 15, 10}))
                    .setWidth(UnitValue.createPercentValue(100));

            ajouterEnTeteTableau(table, new String[]{
                    "Référence", "Date", "Vendeur",
                    "Patient", "Paiement", "Total TTC", "Statut"
            });

            BigDecimal totalGeneral = BigDecimal.ZERO;

            for (var v : ventes) {
                table.addCell(creerCellule(v.getReference()));
                table.addCell(creerCellule(
                        v.getEffectueele().format(DATETIME_FORMAT)));
                table.addCell(creerCellule(
                        v.getVendeur().getNom()
                                + " " + v.getVendeur().getPrenom()));
                table.addCell(creerCellule(
                        v.getPatient() != null
                                ? v.getPatient().getNom()
                                + " " + v.getPatient().getPrenom()
                                : "Anonyme"));
                table.addCell(creerCellule(
                        v.getModePaiement().name()));
                table.addCell(creerCellule(
                        v.getMontantTotalTtc().toString()
                                + " Ar"));
                table.addCell(creerCellule(
                        v.getStatut().name()));

                if (v.getStatut() == Vente.StatutVente.VALIDEE) {
                    totalGeneral = totalGeneral
                            .add(v.getMontantTotalTtc());
                }
            }

            document.add(table);
            document.add(new Paragraph("\n"));
            document.add(
                    new Paragraph("Nombre de ventes : "
                            + ventes.size()).setBold()
            );
            document.add(
                    new Paragraph("Chiffre d'affaires : "
                            + totalGeneral + " Ar")
                            .setBold()
                            .setFontSize(14)
                            .setFontColor(ColorConstants.DARK_GRAY)
            );

            document.close();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erreur génération PDF ventes : "
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
            Document document = new Document(pdf);

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