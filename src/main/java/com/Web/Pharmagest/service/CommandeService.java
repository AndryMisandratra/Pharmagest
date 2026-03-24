package com.Web.Pharmagest.service;

import com.Web.Pharmagest.dto.request.CommandeRequest;
import com.Web.Pharmagest.dto.request.ReceptionCommandeRequest;
import com.Web.Pharmagest.dto.response.CommandeResponse;
import com.Web.Pharmagest.entity.*;
import com.Web.Pharmagest.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommandeService {

    private final CommandeFournisseurRepository commandeRepository;
    private final LigneCommandeRepository ligneCommandeRepository;
    private final FournisseurRepository fournisseurRepository;
    private final MedicamentRepository medicamentRepository;
    private final LotStockRepository lotStockRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final UtilisateurRepository utilisateurRepository;

    // ================================================
    // CRÉER un bon de commande
    // ================================================
    @Transactional
    public CommandeResponse creerCommande(
            CommandeRequest request,
            String emailCreateur) {

        var fournisseur = fournisseurRepository
                .findById(request.getFournisseurId())
                .orElseThrow(() -> new RuntimeException(
                        "Fournisseur non trouvé"
                ));

        var createur = utilisateurRepository
                .findByEmail(emailCreateur)
                .orElseThrow();

        // Générer référence unique
        // Format : CMD-20250315-005
        String reference = genererReference();

        var commande = CommandeFournisseur.builder()
                .fournisseur(fournisseur)
                .createur(createur)
                .reference(reference)
                .statut(CommandeFournisseur.StatutCommande.BROUILLON)
                .notes(request.getNotes())
                .build();

        var savedCommande = commandeRepository.save(commande);

        // Créer les lignes de commande
        BigDecimal montantTotal = BigDecimal.ZERO;

        for (var ligneReq : request.getLignes()) {
            var medicament = medicamentRepository
                    .findById(ligneReq.getMedicamentId())
                    .orElseThrow(() -> new RuntimeException(
                            "Médicament non trouvé : "
                                    + ligneReq.getMedicamentId()
                    ));

            var ligne = LigneCommande.builder()
                    .commande(savedCommande)
                    .medicament(medicament)
                    .quantiteCommandee(ligneReq.getQuantiteCommandee())
                    .prixAchatHt(ligneReq.getPrixAchatHt())
                    .build();

            ligneCommandeRepository.save(ligne);

            // Calculer montant total
            if (ligneReq.getPrixAchatHt() != null) {
                montantTotal = montantTotal.add(
                        ligneReq.getPrixAchatHt()
                                .multiply(BigDecimal.valueOf(
                                        ligneReq.getQuantiteCommandee()
                                ))
                );
            }
        }

        // Mettre à jour le montant total
        savedCommande.setMontantTotalHt(montantTotal);
        commandeRepository.save(savedCommande);

        return toResponse(savedCommande);
    }

    // ================================================
    // ENVOYER une commande (BROUILLON → ENVOYEE)
    // PHARMACIEN seulement
    // ================================================
    @Transactional
    public CommandeResponse envoyerCommande(Long id,
                                            String emailPharmacien) {
        var commande = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Commande non trouvée : " + id
                ));

        if (commande.getStatut() !=
                CommandeFournisseur.StatutCommande.BROUILLON) {
            throw new RuntimeException(
                    "Seule une commande en BROUILLON peut être envoyée"
            );
        }

        var pharmacien = utilisateurRepository
                .findByEmail(emailPharmacien)
                .orElseThrow();

        commande.setStatut(
                CommandeFournisseur.StatutCommande.ENVOYEE);
        commande.setValideur(pharmacien);
        commande.setDateEnvoi(LocalDateTime.now());

        return toResponse(commandeRepository.save(commande));
    }

    // ================================================
    // RÉCEPTIONNER une commande → créer les lots auto
    // ================================================
    @Transactional
    public CommandeResponse receptionnerCommande(
            Long commandeId,
            ReceptionCommandeRequest request,
            String emailReceptionnaire) {

        var commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException(
                        "Commande non trouvée : " + commandeId
                ));

        var receptionnaire = utilisateurRepository
                .findByEmail(emailReceptionnaire)
                .orElseThrow();

        boolean receptionComplete = true;

        for (var ligneRecep : request.getLignes()) {

            // Charger la ligne de commande
            var ligneCommande = ligneCommandeRepository
                    .findById(ligneRecep.getLigneCommandeId())
                    .orElseThrow(() -> new RuntimeException(
                            "Ligne de commande non trouvée"
                    ));

            // Mettre à jour la ligne avec les infos reçues
            ligneCommande.setQuantiteRecue(
                    ligneRecep.getQuantiteRecue());
            ligneCommande.setLotNumeroRecu(
                    ligneRecep.getLotNumero());
            ligneCommande.setDatePeremptionRecue(
                    ligneRecep.getDatePeremption());
            if (ligneRecep.getPrixAchatHt() != null) {
                ligneCommande.setPrixAchatHt(
                        ligneRecep.getPrixAchatHt());
            }
            ligneCommandeRepository.save(ligneCommande);

            // Vérifier si réception complète
            if (!ligneRecep.getQuantiteRecue()
                    .equals(ligneCommande.getQuantiteCommandee())) {
                receptionComplete = false;
            }

            // Créer le lot automatiquement
            var medicament = ligneCommande.getMedicament();
            BigDecimal prixAchat =
                    ligneRecep.getPrixAchatHt() != null
                            ? ligneRecep.getPrixAchatHt()
                            : (ligneCommande.getPrixAchatHt() != null
                            ? ligneCommande.getPrixAchatHt()
                            : medicament.getPrixAchatHt());

            var lot = LotStock.builder()
                    .medicament(medicament)
                    .commande(commande)
                    .numero(ligneRecep.getLotNumero())
                    .quantiteRecue(ligneRecep.getQuantiteRecue())
                    .quantiteRestante(ligneRecep.getQuantiteRecue())
                    .datePeremption(ligneRecep.getDatePeremption())
                    .prixAchatHt(prixAchat)
                    .creePar(receptionnaire)
                    .build();

            var savedLot = lotStockRepository.save(lot);

            // Mettre à jour le stock du médicament
            medicament.setStockQuantiteTotale(
                    medicament.getStockQuantiteTotale()
                            + ligneRecep.getQuantiteRecue()
            );
            medicamentRepository.save(medicament);

            // Enregistrer mouvement ENTREE_RECEPTION
            var mouvement = MouvementStock.builder()
                    .medicament(medicament)
                    .lot(savedLot)
                    .commande(commande)
                    .type(MouvementStock.TypeMouvement
                            .ENTREE_RECEPTION)
                    .quantite(ligneRecep.getQuantiteRecue())
                    .motif("Réception commande "
                            + commande.getReference())
                    .effectuePar(receptionnaire)
                    .build();
            mouvementStockRepository.save(mouvement);
        }

        // Mettre à jour le statut de la commande
        commande.setStatut(receptionComplete
                ? CommandeFournisseur.StatutCommande.CLOTUREE
                : CommandeFournisseur.StatutCommande
                .RECUE_PARTIELLEMENT
        );
        commande.setDateReception(LocalDateTime.now());
        commandeRepository.save(commande);

        return toResponse(commande);
    }

    // ================================================
    // LISTER les commandes (avec filtres)
    // ================================================
    public List<CommandeResponse> listerCommandes(
            Long fournisseurId, String statut) {

        if (fournisseurId != null && statut != null) {
            return commandeRepository
                    .findByFournisseurIdAndStatut(
                            fournisseurId,
                            CommandeFournisseur.StatutCommande
                                    .valueOf(statut)
                    )
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }
        if (fournisseurId != null) {
            return commandeRepository
                    .findByFournisseurIdOrderByDateCommandeDesc(
                            fournisseurId)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }
        if (statut != null) {
            return commandeRepository
                    .findByStatut(
                            CommandeFournisseur.StatutCommande
                                    .valueOf(statut)
                    )
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }
        return commandeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ================================================
    // SUGGESTIONS automatiques basées sur alertes stock
    // ================================================
    public List<CommandeResponse.LigneCommandeResponse>
    getSuggestions() {
        return medicamentRepository.findMedicamentsStockBas()
                .stream()
                .map(m -> CommandeResponse.LigneCommandeResponse
                        .builder()
                        .medicamentId(m.getId())
                        .medicamentDenomination(m.getDenomination())
                        .quantiteCommandee(
                                m.getStockSeuilAlerte() * 3)
                        .prixAchatHt(m.getPrixAchatHt())
                        .build()
                )
                .collect(Collectors.toList());
    }

    // ================================================
    // GÉNÉRER une référence unique
    // ================================================
    private String genererReference() {
        String date = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = commandeRepository.count() + 1;
        return String.format("CMD-%s-%03d", date, count);
    }

    // ================================================
    // CONVERSION Entité → DTO
    // ================================================
    private CommandeResponse toResponse(CommandeFournisseur c) {
        var lignes = ligneCommandeRepository
                .findByCommandeId(c.getId())
                .stream()
                .map(l -> CommandeResponse.LigneCommandeResponse
                        .builder()
                        .id(l.getId())
                        .medicamentId(l.getMedicament().getId())
                        .medicamentDenomination(
                                l.getMedicament().getDenomination())
                        .quantiteCommandee(l.getQuantiteCommandee())
                        .quantiteRecue(l.getQuantiteRecue())
                        .prixAchatHt(l.getPrixAchatHt())
                        .lotNumeroRecu(l.getLotNumeroRecu())
                        .datePeremptionRecue(
                                l.getDatePeremptionRecue())
                        .build()
                )
                .collect(Collectors.toList());

        return CommandeResponse.builder()
                .id(c.getId())
                .reference(c.getReference())
                .fournisseurId(c.getFournisseur().getId())
                .fournisseurRaisonSociale(
                        c.getFournisseur().getRaisonSociale())
                .createurNom(c.getCreateur().getNom())
                .createurPrenom(c.getCreateur().getPrenom())
                .valideurNom(c.getValideur() != null
                        ? c.getValideur().getNom() : null)
                .valideurPrenom(c.getValideur() != null
                        ? c.getValideur().getPrenom() : null)
                .statut(c.getStatut().name())
                .montantTotalHt(c.getMontantTotalHt())
                .dateCommande(c.getDateCommande())
                .dateEnvoi(c.getDateEnvoi())
                .dateReception(c.getDateReception())
                .dateLivraisonEstimee(c.getDateLivraisonEstimee())
                .notes(c.getNotes())
                .lignes(lignes)
                .build();
    }
}