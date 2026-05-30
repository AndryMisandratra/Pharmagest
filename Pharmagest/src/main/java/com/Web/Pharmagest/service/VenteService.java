package com.Web.Pharmagest.service;

import com.Web.Pharmagest.dto.request.VenteRequest;
import com.Web.Pharmagest.dto.response.LigneVenteResponse;
import com.Web.Pharmagest.dto.response.VenteResponse;
import com.Web.Pharmagest.entity.*;
import com.Web.Pharmagest.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VenteService {

    private final VenteRepository venteRepository;
    private final LigneVenteRepository ligneVenteRepository;
    private final MedicamentRepository medicamentRepository;
    private final LotStockRepository lotStockRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final OrdonnanceRepository ordonnanceRepository;
    private final PatientRepository patientRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final OrdonnanceService ordonnanceService;

    // ================================================
    // CRÉER une vente — processus FEFO complet
    // @Transactional : tout réussit ou tout annule
    // ================================================
    @Transactional
    public VenteResponse creerVente(VenteRequest request,
                                    String emailVendeur) {

        // 1. Charger le vendeur
        var vendeur = utilisateurRepository
                .findByEmail(emailVendeur)
                .orElseThrow(() -> new RuntimeException(
                        "Vendeur non trouvé"
                ));

        // 2. Charger le patient (optionnel)
        Patient patient = null;
        if (request.getPatientId() != null) {
            patient = patientRepository
                    .findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException(
                            "Patient non trouvé"
                    ));
        }

        // 3. Charger l'ordonnance (optionnel) - garder compatibilité
        Ordonnance ordonnance = null;
        if (request.getOrdonnanceId() != null) {
            ordonnance = ordonnanceRepository
                    .findById(request.getOrdonnanceId())
                    .orElseThrow(() -> new RuntimeException(
                            "Ordonnance non trouvée"
                    ));

            if (ordonnance.getStatut() ==
                    Ordonnance.StatutOrdonnance.SERVIE) {
                throw new RuntimeException(
                        "Cette ordonnance a déjà été honorée !"
                );
            }
        }

// 3b. Vérifier si vente avec ordonnance (nouveau champ)
        Boolean hasOrdonnance = request.getHasOrdonnance();
        if (hasOrdonnance == null) {
            hasOrdonnance = false;
        }

// 3c. Créer l'ordonnance si hasOrdonnance = true via OrdonnanceService
        Ordonnance nouvelleOrdonnance = null;
        if (hasOrdonnance) {
            var ordonnanceRequest = new com.Web.Pharmagest.dto.request.OrdonnanceRequest();
            
            // Si patient existant sélectionné
            if (patient != null) {
                ordonnanceRequest.setPatientId(patient.getId());
            }
            // Sinon patient_id reste null (pour patient non enregistré)
            
            ordonnanceRequest.setNumero(request.getNumeroOrdonnance());
            ordonnanceRequest.setMedecinNom(request.getMedecinNom());
            ordonnanceRequest.setMedecinRpps(request.getMedecinRpps());
            if (request.getDatePrescription() != null) {
                ordonnanceRequest.setDatePrescription(
                    LocalDate.parse(request.getDatePrescription())
                );
            } else {
                ordonnanceRequest.setDatePrescription(LocalDate.now());
            }
            ordonnanceRequest.setATiersPayant(
                request.getATiersPayant() != null 
                    ? request.getATiersPayant() 
                    : false
            );

            var ordonnanceResponse = ordonnanceService.creerOrdonnance(
                ordonnanceRequest,
                emailVendeur
            );

            nouvelleOrdonnance = ordonnanceRepository
                .findById(ordonnanceResponse.getId())
                .orElse(null);
        }

        // 4. Générer la référence unique
        String reference = genererReference();

        // 5. Initialiser les totaux
        BigDecimal totalHt              = BigDecimal.ZERO;
        BigDecimal totalTva             = BigDecimal.ZERO;
        BigDecimal totalTtc             = BigDecimal.ZERO;
        BigDecimal totalRembourseSs     = BigDecimal.ZERO;
        BigDecimal totalRembourseMutuelle = BigDecimal.ZERO;

        // 6. Créer la vente (sans lignes pour l'instant)
        var vente = Vente.builder()
                .patient(patient)
                .patientNom(request.getPatientNom())
                .ordonnance(nouvelleOrdonnance != null 
                    ? nouvelleOrdonnance 
                    : ordonnance)
                .vendeur(vendeur)
                .reference(reference)
                .montantTotalHt(BigDecimal.ZERO)
                .montantTva(BigDecimal.ZERO)
                .montantTotalTtc(BigDecimal.ZERO)
                .montantRembourseSs(BigDecimal.ZERO)
                .montantRembourseMutuelle(BigDecimal.ZERO)
                .montantPayeClient(BigDecimal.ZERO)
                .montantPaye(request.getMontantPaye() != null 
                    ? request.getMontantPaye() 
                    : BigDecimal.ZERO)
                .montantRendu(request.getMontantRendu() != null 
                    ? request.getMontantRendu() 
                    : BigDecimal.ZERO)
                .modePaiement(Vente.ModePaiement.ESPECES)
                .statut(Vente.StatutVente.VALIDEE)
                .build();

        var savedVente = venteRepository.save(vente);

        // 7. Traiter chaque ligne avec FEFO
        List<LigneVente> lignes = new ArrayList<>();

        for (var ligneReq : request.getLignes()) {

            // Charger le médicament
            var medicament = medicamentRepository
                    .findById(ligneReq.getMedicamentId())
                    .orElseThrow(() -> new RuntimeException(
                            "Médicament non trouvé : "
                                    + ligneReq.getMedicamentId()
                    ));

            // Vérifier si ordonnance requise
            if (medicament.getStatutLegal() ==
                    Medicament.StatutLegal.ORDONNANCE ||
                    medicament.getStatutLegal() ==
                            Medicament.StatutLegal.ORDONNANCE_SECURISEE) {
                if (!hasOrdonnance) {
                    throw new RuntimeException(
                            "Ordonnance requise pour : "
                                    + medicament.getDenomination()
                                    + " (Statut: "
                                    + medicament.getStatutLegal() + ")"
                    );
                }
            }

            // Vérifier stock suffisant
            if (medicament.getStockQuantiteTotale()
                    < ligneReq.getQuantite()) {
                throw new RuntimeException(
                        "Stock insuffisant pour : "
                                + medicament.getDenomination()
                                + " — Stock disponible : "
                                + medicament.getStockQuantiteTotale()
                );
            }

            // ===== ALGORITHME FEFO =====
            var lotsFEFO = lotStockRepository
                    .findLotsDisponiblesFEFO(medicament.getId());

            if (lotsFEFO.isEmpty()) {
                throw new RuntimeException(
                        "Aucun lot disponible pour : "
                                + medicament.getDenomination()
                );
            }

            int quantiteRestante = ligneReq.getQuantite();
            LotStock lotUtilise  = null;

            for (var lot : lotsFEFO) {
                if (quantiteRestante <= 0) break;

                int aPrelevert = Math.min(
                        quantiteRestante,
                        lot.getQuantiteRestante()
                );

                // Mettre à jour le lot
                lot.setQuantiteRestante(
                        lot.getQuantiteRestante() - aPrelevert
                );
                if (lot.getQuantiteRestante() == 0) {
                    lot.setEstActif(false);
                }
                lotStockRepository.save(lot);

                quantiteRestante -= aPrelevert;
                lotUtilise        = lot;

                // Enregistrer mouvement SORTIE_VENTE
                var mouvement = MouvementStock.builder()
                        .medicament(medicament)
                        .lot(lot)
                        .vente(savedVente)
                        .type(MouvementStock.TypeMouvement
                                .SORTIE_VENTE)
                        .quantite(aPrelevert)
                        .motif("Vente " + reference)
                        .effectuePar(vendeur)
                        .build();
                mouvementStockRepository.save(mouvement);
            }

            // ===== CALCULS FINANCIERS =====

            BigDecimal prixUnitaireTtc =
                    medicament.getPrixVenteTtc();
            BigDecimal prixUnitaireHt =
                    medicament.getPrixAchatHt();
            BigDecimal tauxTva = medicament.getTauxTva();

            BigDecimal sousTotalTtc = prixUnitaireTtc
                    .multiply(BigDecimal.valueOf(
                            ligneReq.getQuantite()))
                    .setScale(2, RoundingMode.HALF_UP);

            // ✅ Calcul TVA avec RoundingMode
            // Formule : TVA = TTC - (TTC / (1 + taux/100))
            BigDecimal diviseurTva = BigDecimal.ONE
                    .add(tauxTva.divide(
                            BigDecimal.valueOf(100),
                            10,
                            RoundingMode.HALF_UP
                    ));

            BigDecimal montantHtLigne = sousTotalTtc
                    .divide(diviseurTva, 2,
                            RoundingMode.HALF_UP);

            BigDecimal tvaLigne = sousTotalTtc
                    .subtract(montantHtLigne)
                    .setScale(2, RoundingMode.HALF_UP);

            totalTva = totalTva.add(tvaLigne);

            // ✅ Calcul remboursement SS avec RoundingMode
            BigDecimal tauxRembSs = BigDecimal.ZERO;
            if (medicament.getEstRemboursableSs() &&
                    medicament.getTauxRemboursementSs() != null) {
                tauxRembSs = medicament.getTauxRemboursementSs();
            }

            BigDecimal rembourseLigne = BigDecimal.ZERO;
            if (tauxRembSs.compareTo(BigDecimal.ZERO) > 0) {
                rembourseLigne = sousTotalTtc
                        .multiply(tauxRembSs)
                        .divide(
                                BigDecimal.valueOf(100),
                                2,
                                RoundingMode.HALF_UP
                        );
            }
            totalRembourseSs = totalRembourseSs
                    .add(rembourseLigne);
BigDecimal tauxMutuelle = BigDecimal.ZERO;
            if (patient != null && patient.getTauxMutuelle() != null
                    && patient.getTauxMutuelle().compareTo(
                            BigDecimal.ZERO) > 0) {
                tauxMutuelle = patient.getTauxMutuelle();
            }

            BigDecimal rembourseMutuelleLigne = BigDecimal.ZERO;
            if (tauxMutuelle.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal baseMutuelle = sousTotalTtc
                        .subtract(rembourseLigne)
                        .max(BigDecimal.ZERO);
                rembourseMutuelleLigne = baseMutuelle
                        .multiply(tauxMutuelle)
                        .divide(
                                BigDecimal.valueOf(100),
                                2,
                                RoundingMode.HALF_UP
                        );
            }
            totalRembourseMutuelle = totalRembourseMutuelle
                    .add(rembourseMutuelleLigne);

            // Accumuler total TTC
            totalTtc = totalTtc.add(sousTotalTtc);

            // Créer la ligne de vente
            var ligne = LigneVente.builder()
                    .vente(savedVente)
                    .medicament(medicament)
                    .lot(lotUtilise)
                    .quantite(ligneReq.getQuantite())
                    .prixUnitaireTtc(prixUnitaireTtc)
                    .prixUnitaireHt(prixUnitaireHt)
                    .tauxTva(tauxTva)
                    .sousTotalTtc(sousTotalTtc)
                    .tauxRemboursement(tauxRembSs)
                    .build();

            lignes.add(ligneVenteRepository.save(ligne));

            // Mettre à jour stock total du médicament
            medicament.setStockQuantiteTotale(
                    medicament.getStockQuantiteTotale()
                            - ligneReq.getQuantite()
            );
            medicamentRepository.save(medicament);
        }

        // 8. Calculer le total HT et montant à payer
        totalHt = totalTtc
                .subtract(totalTva)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal montantPayeClient = totalTtc
                .subtract(totalRembourseSs)
                .subtract(totalRembourseMutuelle)
                .setScale(2, RoundingMode.HALF_UP);

        // 9. Mettre à jour la vente avec les totaux
        savedVente.setMontantTotalHt(totalHt);
        savedVente.setMontantTva(totalTva);
        savedVente.setMontantTotalTtc(totalTtc);
        savedVente.setMontantRembourseSs(totalRembourseSs);
        savedVente.setMontantRembourseMutuelle(totalRembourseMutuelle);
        savedVente.setMontantPayeClient(montantPayeClient);
        
        BigDecimal montantPaye = request.getMontantPaye() != null 
            ? request.getMontantPaye() 
            : montantPayeClient;
        BigDecimal montantRendu = request.getMontantRendu() != null 
            ? request.getMontantRendu() 
            : BigDecimal.ZERO;
        
        savedVente.setMontantPaye(montantPaye);
        savedVente.setMontantRendu(montantRendu);
        savedVente.setModePaiement(Vente.ModePaiement.ESPECES);
        venteRepository.save(savedVente);

        // 10. Marquer l'ordonnance comme SERVIE
        if (ordonnance != null) {
            ordonnance.setStatut(
                    Ordonnance.StatutOrdonnance.SERVIE
            );
            ordonnanceRepository.save(ordonnance);
        }

        return toResponse(savedVente, lignes);
    }

    // ================================================
    // OBTENIR une vente par ID
    // ================================================
    public VenteResponse getVenteById(Long id) {
        var vente = venteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Vente non trouvée : " + id
                ));
        var lignes = ligneVenteRepository.findByVenteId(id);
        return toResponse(vente, lignes);
    }

    // ================================================
    // LISTER ventes sur une période
    // ================================================
    public List<VenteResponse> getVentesParPeriode(
            LocalDateTime debut,
            LocalDateTime fin) {
        return venteRepository
                .findVentesParPeriode(debut, fin)
                .stream()
                .map(v -> toResponse(v,
                        ligneVenteRepository
                                .findByVenteId(v.getId())))
                .collect(Collectors.toList());
    }

    // ================================================
    // GÉNÉRER une référence unique
    // Format : V-20250315-0042
    // ================================================
    private String genererReference() {
        String date = LocalDateTime.now()
                .format(DateTimeFormatter
                        .ofPattern("yyyyMMdd"));
        long count = venteRepository.count() + 1;
        return String.format("V-%s-%04d", date, count);
    }

    // ================================================
    // CONVERSION Entité → DTO
    // ================================================
    private VenteResponse toResponse(Vente v,
                                     List<LigneVente> lignes) {
        String vendeurNom = v.getVendeur() != null ? v.getVendeur().getNom() : null;
        String vendeurPrenom = v.getVendeur() != null ? v.getVendeur().getPrenom() : null;
        
        return VenteResponse.builder()
                .id(v.getId())
                .reference(v.getReference())
                .patientId(v.getPatient() != null
                        ? v.getPatient().getId() : null)
                .patientNom(v.getPatient() != null
                        ? v.getPatient().getNom()
                        : v.getPatientNom())
                .patientPrenom(v.getPatient() != null
                        ? v.getPatient().getPrenom() : null)
                .ordonnanceId(v.getOrdonnance() != null
                        ? v.getOrdonnance().getId() : null)
                .ordonnanceNumero(v.getOrdonnance() != null
                        ? v.getOrdonnance().getNumero() : null)
                .vendeurNom(vendeurNom)
                .vendeurPrenom(vendeurPrenom)
                .montantTotalHt(v.getMontantTotalHt())
                .montantTva(v.getMontantTva())
                .montantTotalTtc(v.getMontantTotalTtc())
                .montantRembourseSs(v.getMontantRembourseSs())
                .montantRembourseMutuelle(
                        v.getMontantRembourseMutuelle())
                .montantPayeClient(v.getMontantPayeClient())
                .montantPaye(v.getMontantPaye() != null 
                    ? v.getMontantPaye() : BigDecimal.ZERO)
                .montantRendu(v.getMontantRendu() != null 
                    ? v.getMontantRendu() : BigDecimal.ZERO)
                .modePaiement(v.getModePaiement() != null 
                    ? v.getModePaiement().name() : "ESPECES")
                .statut(v.getStatut().name())
                .effectueLe(v.getEffectueele())
                .lignes(lignes.stream()
                        .map(this::toLigneResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private LigneVenteResponse toLigneResponse(
            LigneVente l) {
        return LigneVenteResponse.builder()
                .id(l.getId())
                .medicamentId(l.getMedicament().getId())
                .medicamentDenomination(
                        l.getMedicament().getDenomination())
                .lotNumero(l.getLot().getNumero())
                .quantite(l.getQuantite())
                .prixUnitaireTtc(l.getPrixUnitaireTtc())
                .prixUnitaireHt(l.getPrixUnitaireHt())
                .tauxTva(l.getTauxTva())
                .sousTotalTtc(l.getSousTotalTtc())
                .tauxRemboursement(l.getTauxRemboursement())
                .build();
    }

    // ================================================
// GÉNÉRER le ticket de caisse PDF
// ================================================
    public byte[] genererTicketPdf(Long venteId) {

        var vente = venteRepository.findById(venteId)
                .orElseThrow(() -> new RuntimeException(
                        "Vente non trouvée : " + venteId
                ));

        var lignes = ligneVenteRepository
                .findByVenteId(venteId);

        ByteArrayOutputStream baos =
                new ByteArrayOutputStream();

        try {
            PdfWriter writer   = new PdfWriter(baos);
            PdfDocument pdf    = new PdfDocument(writer);
            Document document  = new Document(pdf);

            DateTimeFormatter fmt =
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            // ===== EN-TÊTE =====
            document.add(
                    new Paragraph("💊 PHARMAGEST")
                            .setFontSize(18)
                            .setBold()
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontColor(ColorConstants.DARK_GRAY)
            );
            document.add(
                    new Paragraph("Pharmacie Gestion")
                            .setFontSize(10)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontColor(ColorConstants.GRAY)
            );
            document.add(
                    new Paragraph(
                            "─────────────────────────────────")
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontColor(ColorConstants.GRAY)
            );

            // ===== INFOS VENTE =====
            document.add(
                    new Paragraph(
                            "Ticket N° : " + vente.getReference())
                            .setFontSize(10)
                            .setBold()
            );
            document.add(
                    new Paragraph(
                            "Date : " + vente.getEffectueele()
                                    .format(fmt))
                            .setFontSize(9)
                            .setFontColor(ColorConstants.GRAY)
            );
            document.add(
                    new Paragraph(
                            "Vendeur : " + vente.getVendeur()
                                    .getPrenom()
                                    + " " + vente.getVendeur().getNom())
                            .setFontSize(9)
                            .setFontColor(ColorConstants.GRAY)
            );

            // Patient si présent
            if (vente.getPatient() != null) {
                document.add(
                        new Paragraph(
                                "Patient : "
                                        + vente.getPatient().getPrenom()
                                        + " "
                                        + vente.getPatient().getNom())
                                .setFontSize(9)
                                .setFontColor(ColorConstants.GRAY)
                );
            } else if (vente.getPatientNom() != null && !vente.getPatientNom().isEmpty()) {
                document.add(
                        new Paragraph(
                                "Patient : " + vente.getPatientNom())
                                .setFontSize(9)
                                .setFontColor(ColorConstants.GRAY)
                );
            }

            document.add(
                    new Paragraph(
                            "─────────────────────────────────")
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontColor(ColorConstants.GRAY)
            );

            // ===== TABLEAU DES MÉDICAMENTS =====
            Table table = new Table(
                    UnitValue.createPercentArray(
                            new float[]{45, 15, 20, 20}))
                    .setWidth(UnitValue.createPercentValue(100));

            // En-têtes
            for (String h : new String[]{
                    "Médicament", "Qté",
                    "Prix unit.", "Sous-total"
            }) {
                table.addHeaderCell(
                        new Cell()
                                .add(new Paragraph(h)
                                        .setFontSize(8)
                                        .setBold()
                                        .setFontColor(
                                                ColorConstants.WHITE))
                                .setBackgroundColor(
                                        ColorConstants.DARK_GRAY)
                                .setPadding(4)
                );
            }

            // Lignes médicaments
            for (var ligne : lignes) {
                table.addCell(
                        new Cell()
                                .add(new Paragraph(
                                        ligne.getMedicament()
                                                .getDenomination())
                                        .setFontSize(8))
                                .setPadding(3)
                );
                table.addCell(
                        new Cell()
                                .add(new Paragraph(
                                        String.valueOf(
                                                ligne.getQuantite()))
                                        .setFontSize(8)
                                        .setTextAlignment(
                                                TextAlignment.CENTER))
                                .setPadding(3)
                );
                table.addCell(
                        new Cell()
                                .add(new Paragraph(
                                        ligne.getPrixUnitaireTtc().toString()
                                                + " Ar")
                                        .setFontSize(8)
                                        .setTextAlignment(
                                                TextAlignment.RIGHT))
                                .setPadding(3)
                );
                table.addCell(
                        new Cell()
                                .add(new Paragraph(
                                        ligne.getSousTotalTtc()
                                                .toPlainString()
                                                + " Ar")
                                        .setFontSize(8)
                                        .setTextAlignment(
                                                TextAlignment.RIGHT))
                                .setPadding(3)
                );
            }

            document.add(table);
            document.add(
                    new Paragraph(
                            "─────────────────────────────────")
                            .setFontColor(ColorConstants.GRAY)
            );

            // ===== TOTAUX =====
            document.add(
                    new Paragraph(
                            "Total HT : "
                                    + vente.getMontantTotalHt()
                                    .toPlainString()
                                    + " Ar")
                            .setFontSize(9)
                            .setTextAlignment(TextAlignment.RIGHT)
            );
            document.add(
                    new Paragraph(
                            "TVA : "
                                    + vente.getMontantTva()
                                    .toPlainString()
                                    + " Ar")
                            .setFontSize(9)
                            .setTextAlignment(TextAlignment.RIGHT)
                            .setFontColor(ColorConstants.GRAY)
            );

            if (vente.getMontantRembourseSs()
                    .compareTo(BigDecimal.ZERO) > 0) {
                document.add(
                        new Paragraph(
                                "Remboursement SS : -"
                                        + vente.getMontantRembourseSs()
                                        .toPlainString()
                                        + " Ar")
                                .setFontSize(9)
                                .setTextAlignment(
                                        TextAlignment.RIGHT)
                                .setFontColor(ColorConstants.GREEN)
                );
            }

            document.add(
                    new Paragraph(
                            "TOTAL TTC : "
                                    + vente.getMontantTotalTtc()
                                    .toPlainString()
                                    + " Ar")
                            .setFontSize(12)
                            .setBold()
                            .setTextAlignment(TextAlignment.RIGHT)
            );
            document.add(
                    new Paragraph(
                            "À PAYER : "
                                    + vente.getMontantPayeClient()
                                    .toPlainString()
                                    + " Ar")
                            .setFontSize(14)
                            .setBold()
                            .setTextAlignment(TextAlignment.RIGHT)
                            .setFontColor(ColorConstants.DARK_GRAY)
            );

            // ===== PAIEMENT ESPÈCES =====
            document.add(
                    new Paragraph(
                            "─────────────────────────────────")
                            .setFontColor(ColorConstants.GRAY)
            );
            
            BigDecimal montantPaye = vente.getMontantPaye() != null 
                ? vente.getMontantPaye() 
                : vente.getMontantPayeClient();
            BigDecimal montantRendu = vente.getMontantRendu() != null 
                ? vente.getMontantRendu() 
                : BigDecimal.ZERO;
            
            document.add(
                    new Paragraph(
                            "Montant donné : "
                                    + montantPaye.toPlainString()
                                    + " Ar")
                            .setFontSize(9)
                            .setTextAlignment(TextAlignment.RIGHT)
            );
            document.add(
                    new Paragraph(
                            "Montant rendu : "
                                    + montantRendu.toPlainString()
                                    + " Ar")
                            .setFontSize(9)
                            .setTextAlignment(TextAlignment.RIGHT)
                            .setFontColor(ColorConstants.GREEN)
            );

            // ===== PIED DE PAGE =====
            document.add(
                    new Paragraph(
                            "─────────────────────────────────")
                            .setFontColor(ColorConstants.GRAY)
            );
            document.add(
                    new Paragraph(
                            "Merci de votre confiance !")
                            .setFontSize(9)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontColor(ColorConstants.GRAY)
            );
            document.add(
                    new Paragraph(
                            "PharmaGest — Votre santé, notre priorité")
                            .setFontSize(8)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontColor(ColorConstants.LIGHT_GRAY)
            );

            document.close();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erreur génération ticket PDF : "
                            + e.getMessage()
            );
        }

        return baos.toByteArray();
    }
}
