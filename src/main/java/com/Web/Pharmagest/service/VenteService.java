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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        // 3. Charger l'ordonnance (optionnel)
        Ordonnance ordonnance = null;
        if (request.getOrdonnanceId() != null) {
            ordonnance = ordonnanceRepository
                    .findById(request.getOrdonnanceId())
                    .orElseThrow(() -> new RuntimeException(
                            "Ordonnance non trouvée"
                    ));

            // Vérifier que l'ordonnance n'est pas déjà servie
            if (ordonnance.getStatut() ==
                    Ordonnance.StatutOrdonnance.SERVIE) {
                throw new RuntimeException(
                        "Cette ordonnance a déjà été honorée !"
                );
            }
        }

        // 4. Générer la référence unique
        // Format : V-20250315-0042
        String reference = genererReference();

        // 5. Initialiser les totaux
        BigDecimal totalHt = BigDecimal.ZERO;
        BigDecimal totalTva = BigDecimal.ZERO;
        BigDecimal totalTtc = BigDecimal.ZERO;
        BigDecimal totalRembourseSs = BigDecimal.ZERO;

        // 6. Créer la vente (sans lignes pour l'instant)
        var vente = Vente.builder()
                .patient(patient)
                .ordonnance(ordonnance)
                .vendeur(vendeur)
                .reference(reference)
                .montantTotalHt(BigDecimal.ZERO)
                .montantTva(BigDecimal.ZERO)
                .montantTotalTtc(BigDecimal.ZERO)
                .montantRembourseSs(BigDecimal.ZERO)
                .montantRembourseMutuelle(BigDecimal.ZERO)
                .montantPayeClient(BigDecimal.ZERO)
                .modePaiement(Vente.ModePaiement
                        .valueOf(request.getModePaiement()))
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
                    Medicament.StatutLegal.ORDONNANCE
                    || medicament.getStatutLegal() ==
                    Medicament.StatutLegal.ORDONNANCE_SECURISEE) {
                if (ordonnance == null) {
                    throw new RuntimeException(
                            "Ordonnance requise pour : "
                                    + medicament.getDenomination()
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
            // Récupère les lots triés par date de péremption ASC
            var lotsFEFO = lotStockRepository
                    .findLotsDisponiblesFEFO(medicament.getId());

            if (lotsFEFO.isEmpty()) {
                throw new RuntimeException(
                        "Aucun lot disponible pour : "
                                + medicament.getDenomination()
                );
            }

            int quantiteRestante = ligneReq.getQuantite();
            LotStock lotUtilise = null;
            int quantitePrelevee = 0;

            // Prélever dans les lots en commençant par le plus proche
            // de la péremption
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
                lotUtilise = lot;
                quantitePrelevee = aPrelevert;

                // Enregistrer mouvement SORTIE_VENTE
                var mouvement = MouvementStock.builder()
                        .medicament(medicament)
                        .lot(lot)
                        .vente(savedVente)
                        .type(MouvementStock.TypeMouvement.SORTIE_VENTE)
                        .quantite(aPrelevert)
                        .motif("Vente " + reference)
                        .effectuePar(vendeur)
                        .build();
                mouvementStockRepository.save(mouvement);
            }

            // Calculer les montants de la ligne
            BigDecimal prixUnitaireTtc =
                    medicament.getPrixVenteTtc();
            BigDecimal prixUnitaireHt =
                    medicament.getPrixAchatHt();
            BigDecimal tauxTva = medicament.getTauxTva();
            BigDecimal sousTotalTtc = prixUnitaireTtc
                    .multiply(BigDecimal.valueOf(
                            ligneReq.getQuantite()
                    ));

            // Taux de remboursement SS
            BigDecimal tauxRembSs = medicament
                    .getEstRemboursableSs()
                    ? (medicament.getTauxRemboursementSs() != null
                    ? medicament.getTauxRemboursementSs()
                    : BigDecimal.ZERO)
                    : BigDecimal.ZERO;

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

            // Accumuler les totaux
            totalTtc = totalTtc.add(sousTotalTtc);
            BigDecimal tvaLigne = sousTotalTtc
                    .multiply(tauxTva)
                    .divide(BigDecimal.valueOf(100 + tauxTva
                            .doubleValue()));
            totalTva = totalTva.add(tvaLigne);

            // Calculer remboursement SS
            BigDecimal rembourseLigne = sousTotalTtc
                    .multiply(tauxRembSs)
                    .divide(BigDecimal.valueOf(100));
            totalRembourseSs = totalRembourseSs.add(rembourseLigne);

            // Mettre à jour stock total du médicament
            medicament.setStockQuantiteTotale(
                    medicament.getStockQuantiteTotale()
                            - ligneReq.getQuantite()
            );
            medicamentRepository.save(medicament);
        }

        // 8. Calculer le total HT
        totalHt = totalTtc.subtract(totalTva);
        BigDecimal montantPayeClient =
                totalTtc.subtract(totalRembourseSs);

        // 9. Mettre à jour la vente avec les totaux
        savedVente.setMontantTotalHt(totalHt);
        savedVente.setMontantTva(totalTva);
        savedVente.setMontantTotalTtc(totalTtc);
        savedVente.setMontantRembourseSs(totalRembourseSs);
        savedVente.setMontantPayeClient(montantPayeClient);
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
    // ANNULER une vente — restock automatique
    // PHARMACIEN seulement
    // ================================================
    @Transactional
    public VenteResponse annulerVente(Long id,
                                      String emailPharmacien) {
        var vente = venteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Vente non trouvée : " + id
                ));

        if (vente.getStatut() != Vente.StatutVente.VALIDEE) {
            throw new RuntimeException(
                    "Impossible d'annuler une vente "
                            + vente.getStatut()
            );
        }

        var pharmacien = utilisateurRepository
                .findByEmail(emailPharmacien)
                .orElseThrow();

        // Récupérer les lignes de la vente
        var lignes = ligneVenteRepository.findByVenteId(id);

        // Restock automatique pour chaque ligne
        for (var ligne : lignes) {
            var lot = ligne.getLot();
            var medicament = ligne.getMedicament();

            // Remettre en stock
            lot.setQuantiteRestante(
                    lot.getQuantiteRestante() + ligne.getQuantite()
            );
            lot.setEstActif(true);
            lotStockRepository.save(lot);

            // Mettre à jour stock médicament
            medicament.setStockQuantiteTotale(
                    medicament.getStockQuantiteTotale()
                            + ligne.getQuantite()
            );
            medicamentRepository.save(medicament);

            // Enregistrer mouvement ENTREE_RETOUR
            var mouvement = MouvementStock.builder()
                    .medicament(medicament)
                    .lot(lot)
                    .vente(vente)
                    .type(MouvementStock.TypeMouvement.ENTREE_RETOUR)
                    .quantite(ligne.getQuantite())
                    .motif("Annulation vente " + vente.getReference())
                    .effectuePar(pharmacien)
                    .build();
            mouvementStockRepository.save(mouvement);
        }

        // Marquer la vente comme annulée
        vente.setStatut(Vente.StatutVente.ANNULEE);
        vente.setAnnuleeLe(LocalDateTime.now());
        vente.setAnnuleePar(pharmacien);
        venteRepository.save(vente);

        // Remettre l'ordonnance en EN_ATTENTE si applicable
        if (vente.getOrdonnance() != null) {
            vente.getOrdonnance().setStatut(
                    Ordonnance.StatutOrdonnance.EN_ATTENTE
            );
            ordonnanceRepository.save(vente.getOrdonnance());
        }

        return toResponse(vente, lignes);
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
    // GÉNÉRER une référence unique
    // Format : V-20250315-0042
    // ================================================
    private String genererReference() {
        String date = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = venteRepository.count() + 1;
        return String.format("V-%s-%04d", date, count);
    }

    // ================================================
    // CONVERSION Entité → DTO
    // ================================================
    private VenteResponse toResponse(Vente v,
                                     List<LigneVente> lignes) {
        return VenteResponse.builder()
                .id(v.getId())
                .reference(v.getReference())
                .patientId(v.getPatient() != null
                        ? v.getPatient().getId() : null)
                .patientNom(v.getPatient() != null
                        ? v.getPatient().getNom() : null)
                .patientPrenom(v.getPatient() != null
                        ? v.getPatient().getPrenom() : null)
                .ordonnanceId(v.getOrdonnance() != null
                        ? v.getOrdonnance().getId() : null)
                .ordonnanceNumero(v.getOrdonnance() != null
                        ? v.getOrdonnance().getNumero() : null)
                .vendeurNom(v.getVendeur().getNom())
                .vendeurPrenom(v.getVendeur().getPrenom())
                .montantTotalHt(v.getMontantTotalHt())
                .montantTva(v.getMontantTva())
                .montantTotalTtc(v.getMontantTotalTtc())
                .montantRembourseSs(v.getMontantRembourseSs())
                .montantRembourseMutuelle(
                        v.getMontantRembourseMutuelle())
                .montantPayeClient(v.getMontantPayeClient())
                .modePaiement(v.getModePaiement().name())
                .statut(v.getStatut().name())
                .effectueLe(v.getEffectueele())
                .lignes(lignes.stream()
                        .map(this::toLigneResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private LigneVenteResponse toLigneResponse(LigneVente l) {
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
}