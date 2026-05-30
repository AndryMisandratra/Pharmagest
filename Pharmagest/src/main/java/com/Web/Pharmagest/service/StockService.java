package com.Web.Pharmagest.service;

import com.Web.Pharmagest.dto.request.LotDateUpdateRequest;
import com.Web.Pharmagest.dto.request.LotStockRequest;
import com.Web.Pharmagest.dto.request.MouvementStockRequest;
import com.Web.Pharmagest.dto.response.LotStockResponse;
import com.Web.Pharmagest.dto.response.MouvementStockResponse;
import com.Web.Pharmagest.entity.*;
import com.Web.Pharmagest.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

    private final LotStockRepository lotStockRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final MedicamentRepository medicamentRepository;
    private final CommandeFournisseurRepository commandeRepository;
    private final UtilisateurRepository utilisateurRepository;

    // ================================================
    // CRÉER un lot manuellement
    // ================================================
    @Transactional
    public LotStockResponse creerLot(LotStockRequest request,
                                     String emailUtilisateur) {

        // Charger le médicament
        var medicament = medicamentRepository
                .findById(request.getMedicamentId())
                .orElseThrow(() -> new RuntimeException(
                        "Médicament non trouvé"
                ));

        // Charger la commande (optionnel)
        var commande = request.getCommandeId() != null
                ? commandeRepository
                .findById(request.getCommandeId())
                .orElse(null)
                : null;

        // Charger l'utilisateur qui crée le lot
        var utilisateur = utilisateurRepository
                .findByEmail(emailUtilisateur)
                .orElseThrow(() -> new RuntimeException(
                        "Utilisateur non trouvé"
                ));

        // Créer le lot
        var lot = LotStock.builder()
                .medicament(medicament)
                .commande(commande)
                .numero(request.getNumero())
                .quantiteRecue(request.getQuantiteRecue())
                .quantiteRestante(request.getQuantiteRecue())
                .dateFabrication(request.getDateFabrication())
                .datePeremption(request.getDatePeremption())
                .prixAchatHt(request.getPrixAchatHt())
                .creePar(utilisateur)
                .build();

        var savedLot = lotStockRepository.save(lot);

        // Mettre à jour le stock total du médicament
        medicament.setStockQuantiteTotale(
                medicament.getStockQuantiteTotale()
                        + request.getQuantiteRecue()
        );
        medicamentRepository.save(medicament);

        // Enregistrer le mouvement ENTREE_RECEPTION
        enregistrerMouvement(
                medicament, savedLot, null, commande,
                MouvementStock.TypeMouvement.ENTREE_RECEPTION,
                request.getQuantiteRecue(),
                "Réception lot " + request.getNumero(),
                utilisateur
        );

        return toLotResponse(savedLot);
    }

    // ================================================
    // RETIRER un lot (périmé, perdu, cassé)
    // ================================================
    @Transactional
    public LotStockResponse retirerLot(Long lotId,
                                       String motif,
                                       String emailUtilisateur) {
        var lot = lotStockRepository.findById(lotId)
                .orElseThrow(() -> new RuntimeException(
                        "Lot non trouvé"
                ));

        var utilisateur = utilisateurRepository
                .findByEmail(emailUtilisateur)
                .orElseThrow();

        // Déterminer le type de mouvement selon le motif
        var typeMouvement = motif != null
                && motif.toLowerCase().contains("périmé")
                ? MouvementStock.TypeMouvement.SORTIE_PERIME
                : MouvementStock.TypeMouvement.SORTIE_PERTE;

        // Mettre à jour le stock du médicament
        var medicament = lot.getMedicament();
        medicament.setStockQuantiteTotale(
                medicament.getStockQuantiteTotale()
                        - lot.getQuantiteRestante()
        );
        medicamentRepository.save(medicament);

        // Enregistrer le mouvement
        enregistrerMouvement(
                medicament, lot, null, null,
                typeMouvement,
                lot.getQuantiteRestante(),
                motif,
                utilisateur
        );

        // Désactiver le lot
        lot.setEstActif(false);
        lot.setQuantiteRestante(0);
        return toLotResponse(lotStockRepository.save(lot));
    }

    // ================================================
    // LISTER les lots d'un médicament (triés FEFO)
    // ================================================
    public List<LotStockResponse> getLotsParMedicament(
            Long medicamentId) {
        return lotStockRepository
                .findLotsDisponiblesFEFO(medicamentId)
                .stream()
                .map(this::toLotResponse)
                .collect(Collectors.toList());
    }

    // ================================================
    // LOTS qui expirent dans N jours
    // ================================================
    public List<LotStockResponse> getLotsExpirantDans(int jours) {
        LocalDate dateLimite = LocalDate.now().plusDays(jours);
        return lotStockRepository
                .findLotsExpirantAvant(dateLimite)
                .stream()
                .map(this::toLotResponse)
                .collect(Collectors.toList());
    }

    // ================================================
    // ENREGISTRER un mouvement de stock
    // ================================================
    @Transactional
    public MouvementStockResponse enregistrerMouvementManuel(
            MouvementStockRequest request,
            String emailUtilisateur) {

        var medicament = medicamentRepository
                .findById(request.getMedicamentId())
                .orElseThrow(() -> new RuntimeException(
                        "Médicament non trouvé"
                ));

        var utilisateur = utilisateurRepository
                .findByEmail(emailUtilisateur)
                .orElseThrow();

        var lot = request.getLotId() != null
                ? lotStockRepository
                .findById(request.getLotId())
                .orElse(null)
                : null;

        var typeMouvement = MouvementStock.TypeMouvement
                .valueOf(request.getType());

        // Mettre à jour le stock selon le type
        int delta = 0;
        switch (typeMouvement) {
            case ENTREE_RECEPTION, ENTREE_RETOUR -> delta =
                    +request.getQuantite();
            case SORTIE_VENTE, SORTIE_PERIME,
                 SORTIE_PERTE -> delta = -request.getQuantite();
            case INVENTAIRE -> {
                // Inventaire = correction absolue
                int diff = request.getQuantite()
                        - medicament.getStockQuantiteTotale();
                delta = diff;
            }
        }

        medicament.setStockQuantiteTotale(
                medicament.getStockQuantiteTotale() + delta
        );
        medicamentRepository.save(medicament);

        var mouvement = enregistrerMouvement(
                medicament, lot, null, null,
                typeMouvement,
                request.getQuantite(),
                request.getMotif(),
                utilisateur
        );

        return toMouvementResponse(mouvement);
    }

    // ================================================
    // HISTORIQUE des mouvements d'un médicament
    // ================================================
    public List<MouvementStockResponse> getHistoriqueMouvements(
            Long medicamentId) {
        return mouvementStockRepository
                .findByMedicamentIdOrderByEffectueLeDesc(medicamentId)
                .stream()
                .map(this::toMouvementResponse)
                .collect(Collectors.toList());
    }

    // ================================================
    // METTRE À JOUR les dates d'un lot
    // datePeremption et/ou dateFabrication
    // ================================================
    @Transactional
    public LotStockResponse mettreAJourDatesLot(
            Long lotId,
            LotDateUpdateRequest request) {

        var lot = lotStockRepository.findById(lotId)
                .orElseThrow(() -> new RuntimeException(
                        "Lot non trouvé : " + lotId
                ));

        if (request.getDatePeremption() != null) {
            lot.setDatePeremption(request.getDatePeremption());
        }
        if (request.getDateFabrication() != null) {
            lot.setDateFabrication(request.getDateFabrication());
        }

        return toLotResponse(lotStockRepository.save(lot));
    }

    // ================================================
    // OBTENIR un lot par son ID
    // ================================================
    public LotStockResponse getLotById(Long lotId) {
        var lot = lotStockRepository.findById(lotId)
                .orElseThrow(() -> new RuntimeException(
                        "Lot non trouvé : " + lotId
                ));
        return toLotResponse(lot);
    }

    // ================================================
    // MÉTHODE PRIVÉE : enregistrer un mouvement
    // Utilisée en interne par plusieurs méthodes
    // ================================================
    private MouvementStock enregistrerMouvement(
            Medicament medicament,
            LotStock lot,
            Vente vente,
            CommandeFournisseur commande,
            MouvementStock.TypeMouvement type,
            Integer quantite,
            String motif,
            Utilisateur utilisateur) {

        var mouvement = MouvementStock.builder()
                .medicament(medicament)
                .lot(lot)
                .vente(vente)
                .commande(commande)
                .type(type)
                .quantite(quantite)
                .motif(motif)
                .effectuePar(utilisateur)
                .build();

        return mouvementStockRepository.save(mouvement);
    }

    // ================================================
    // CONVERSIONS Entité → DTO
    // ================================================
    private LotStockResponse toLotResponse(LotStock lot) {

        Long joursAvantPeremption = null;
        String alertePeremption = "INCONNU";

        if (lot.getDatePeremption() != null) {
            joursAvantPeremption = ChronoUnit.DAYS.between(
                    LocalDate.now(), lot.getDatePeremption()
            );

            if (joursAvantPeremption < 0) {
                alertePeremption = "PERIME";
            } else if (joursAvantPeremption <= 30) {
                alertePeremption = "ROUGE";
            } else if (joursAvantPeremption <= 90) {
                alertePeremption = "ORANGE";
            } else {
                alertePeremption = "NORMAL";
            }
        }

        return LotStockResponse.builder()
                .id(lot.getId())
                .medicamentId(lot.getMedicament().getId())
                .medicamentDenomination(
                        lot.getMedicament().getDenomination())
                .commandeId(lot.getCommande() != null
                        ? lot.getCommande().getId() : null)
                .numero(lot.getNumero())
                .quantiteRecue(lot.getQuantiteRecue())
                .quantiteRestante(lot.getQuantiteRestante())
                .dateFabrication(lot.getDateFabrication())
                .datePeremption(lot.getDatePeremption())
                .prixAchatHt(lot.getPrixAchatHt())
                .estActif(lot.getEstActif())
                .creeLe(lot.getCreeLe())
                .joursAvantPeremption(joursAvantPeremption)
                .alertePeremption(alertePeremption)
                .build();
    }

    private MouvementStockResponse toMouvementResponse(
            MouvementStock m) {
        return MouvementStockResponse.builder()
                .id(m.getId())
                .medicamentId(m.getMedicament().getId())
                .medicamentDenomination(
                        m.getMedicament().getDenomination())
                .lotId(m.getLot() != null
                        ? m.getLot().getId() : null)
                .lotNumero(m.getLot() != null
                        ? m.getLot().getNumero() : null)
                .venteId(m.getVente() != null
                        ? m.getVente().getId() : null)
                .commandeId(m.getCommande() != null
                        ? m.getCommande().getId() : null)
                .type(m.getType().name())
                .quantite(m.getQuantite())
                .motif(m.getMotif())
                .effectueLe(m.getEffectueLe())
                .effectueParNom(m.getEffectuePar().getNom())
                .effectueParPrenom(m.getEffectuePar().getPrenom())
                .build();
    }
}