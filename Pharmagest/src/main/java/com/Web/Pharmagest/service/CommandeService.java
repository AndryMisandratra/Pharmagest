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
import java.util.ArrayList;
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
    // MODIFIER une commande (BROUILLON uniquement)
    // Peut modifier : notes, fournisseur, lignes
    // ================================================
    @Transactional
    public CommandeResponse modifierCommande(
            Long id,
            CommandeRequest request,
            String emailModificateur) {

        var commande = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Commande non trouvée : " + id
                ));

        if (commande.getStatut() !=
                CommandeFournisseur.StatutCommande.BROUILLON) {
            throw new RuntimeException(
                    "Seule une commande en BROUILLON peut être modifiée"
            );
        }

        if (request.getFournisseurId() != null) {
            var fournisseur = fournisseurRepository
                    .findById(request.getFournisseurId())
                    .orElseThrow(() -> new RuntimeException(
                            "Fournisseur non trouvé"
                    ));
            commande.setFournisseur(fournisseur);
        }

        if (request.getNotes() != null) {
            commande.setNotes(request.getNotes());
        }

        commandeRepository.save(commande);

        if (request.getLignes() != null && !request.getLignes().isEmpty()) {
            ligneCommandeRepository.deleteByCommandeId(id);

            BigDecimal montantTotal = BigDecimal.ZERO;
            for (var ligneReq : request.getLignes()) {
                if (ligneReq.getMedicamentId() == null) {
                    continue;
                }
                
                var medicament = medicamentRepository
                        .findById(ligneReq.getMedicamentId())
                        .orElse(null);
                
                if (medicament == null) {
                    continue;
                }

                var ligne = LigneCommande.builder()
                        .commande(commande)
                        .medicament(medicament)
                        .quantiteCommandee(ligneReq.getQuantiteCommandee() != null 
                                ? ligneReq.getQuantiteCommandee() 
                                : 1)
                        .prixAchatHt(ligneReq.getPrixAchatHt())
                        .build();

                ligneCommandeRepository.save(ligne);

                if (ligneReq.getPrixAchatHt() != null && ligne.getQuantiteCommandee() != null) {
                    montantTotal = montantTotal.add(
                            ligneReq.getPrixAchatHt()
                                    .multiply(BigDecimal.valueOf(
                                            ligne.getQuantiteCommandee()
                                    ))
                    );
                }
            }

            if (montantTotal.compareTo(BigDecimal.ZERO) > 0) {
                commande.setMontantTotalHt(montantTotal);
            }
            commandeRepository.save(commande);
        }

        return toResponse(commandeRepository.save(commande));
    }

    // ================================================
    // OBTENIR une commande par son ID
    // ================================================
    public CommandeResponse getCommandeById(Long id) {
        var commande = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Commande non trouvée : " + id
                ));
        return toResponse(commande);
    }

    // ================================================
    // ENVOYER une commande (BROUILLON → ENVOYEE)
    // Pré-génère les numéros de lot automatiquement
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

        var lignes = ligneCommandeRepository.findByCommandeId(id);
        for (var ligne : lignes) {
            String numeroLot = genererNumeroLot(commande, ligne.getMedicament());
            ligne.setLotNumeroRecu(numeroLot);
            ligne.setQuantiteRecue(ligne.getQuantiteCommandee());
            ligneCommandeRepository.save(ligne);
        }

        return toResponse(commandeRepository.save(commande));
    }

    private String genererNumeroLot(
            CommandeFournisseur commande,
            Medicament medicament) {
        String code = medicament.getCodeCip() != null
                ? medicament.getCodeCip()
                : String.valueOf(medicament.getId());
        String date = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "LOT-" + commande.getReference()
                + "-" + code + "-" + date;
    }

    // ================================================
    // RÉCEPTIONNER une commande → créer les lots
    // Les numéros de lot sont déjà pré-générés
    // L'utilisateur saisit : quantiteRecue, datePeremption, dateFabrication
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

        if (commande.getStatut() !=
                CommandeFournisseur.StatutCommande.ENVOYEE) {
            throw new RuntimeException(
                    "Seule une commande ENVOYEE peut être réceptionnée"
            );
        }

        var receptionnaire = utilisateurRepository
                .findByEmail(emailReceptionnaire)
                .orElseThrow();

        for (var ligneRecep : request.getLignes()) {

            var ligneCommande = ligneCommandeRepository
                    .findById(ligneRecep.getLigneCommandeId())
                    .orElseThrow(() -> new RuntimeException(
                            "Ligne de commande non trouvée"
                    ));

            ligneCommande.setDatePeremptionRecue(ligneRecep.getDatePeremption());
            ligneCommande.setDateFabricationRecue(ligneRecep.getDateFabrication());
            if (ligneRecep.getPrixAchatHt() != null) {
                ligneCommande.setPrixAchatHt(ligneRecep.getPrixAchatHt());
            }
            ligneCommandeRepository.save(ligneCommande);

            if (ligneCommande.getMedicament() == null) {
                throw new RuntimeException("Médicament non trouvé pour la ligne de commande");
            }

            Integer qteRecue = ligneCommande.getQuantiteRecue();

            if (qteRecue == null || qteRecue < ligneCommande.getQuantiteCommandee()) {
                throw new RuntimeException(
                    "La quantité réceptionnée (" + qteRecue + ") doit être égale à la quantité commandée ("
                    + ligneCommande.getQuantiteCommandee() + ") pour le médicament "
                    + ligneCommande.getMedicament().getDenomination()
                );
            }

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
                    .numero(ligneCommande.getLotNumeroRecu())
                    .quantiteRecue(qteRecue)
                    .quantiteRestante(qteRecue)
                    .datePeremption(ligneRecep.getDatePeremption())
                    .dateFabrication(ligneRecep.getDateFabrication())
                    .prixAchatHt(prixAchat)
                    .creePar(receptionnaire)
                    .build();

            var savedLot = lotStockRepository.save(lot);

            medicament.setStockQuantiteTotale(
                    medicament.getStockQuantiteTotale() + qteRecue
            );
            medicamentRepository.save(medicament);

            var mouvement = MouvementStock.builder()
                    .medicament(medicament)
                    .lot(savedLot)
                    .commande(commande)
                    .type(MouvementStock.TypeMouvement.ENTREE_RECEPTION)
                    .quantite(qteRecue)
                    .motif("Réception commande " + commande.getReference())
                    .effectuePar(receptionnaire)
                    .build();
            mouvementStockRepository.save(mouvement);
        }

        commande.setStatut(CommandeFournisseur.StatutCommande.CLOTUREE);
        commande.setDateReception(LocalDateTime.now());
        commandeRepository.save(commande);

        return toResponse(commande);
    }

    // ================================================
    // LISTER les commandes (avec filtres)
    // ================================================
    public List<CommandeResponse> listerCommandes(
            Long fournisseurId, String statut) {

        System.out.println("DEBUG listerCommandes - fournisseurId: " + fournisseurId + ", statut: " + statut);
        
        List<CommandeFournisseur> commandes;
        
        try {
            boolean hasStatutFilter = statut != null && !statut.trim().isEmpty();
            System.out.println("DEBUG hasStatutFilter: " + hasStatutFilter);
            
            if (fournisseurId != null && hasStatutFilter) {
                System.out.println("DEBUG: Using findByFournisseurIdAndStatut");
                commandes = commandeRepository
                        .findByFournisseurIdAndStatut(
                                fournisseurId,
                                CommandeFournisseur.StatutCommande
                                        .valueOf(statut.trim())
                        );
            } else if (fournisseurId != null) {
                System.out.println("DEBUG: Using findByFournisseurIdOrderByDateCommandeDesc");
                commandes = commandeRepository
                        .findByFournisseurIdOrderByDateCommandeDesc(
                                fournisseurId);
            } else if (hasStatutFilter) {
                System.out.println("DEBUG: Using findByStatut");
                commandes = commandeRepository
                        .findByStatut(
                                CommandeFournisseur.StatutCommande
                                        .valueOf(statut.trim())
                        );
            } else {
                System.out.println("DEBUG: Using findAll");
                commandes = commandeRepository.findAll();
            }
            
            System.out.println("DEBUG commandes count: " + (commandes != null ? commandes.size() : "null"));
            
        } catch (IllegalArgumentException e) {
            System.err.println("DEBUG IllegalArgumentException: " + e.getMessage());
            throw new RuntimeException("Valeur de filtre invalide: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("DEBUG Exception: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur listing commandes: " + e.getMessage(), e);
        }
        
        if (commandes == null) {
            return new ArrayList<>();
        }
        
        List<CommandeResponse> result = new ArrayList<>();
        for (CommandeFournisseur c : commandes) {
            try {
                System.out.println("DEBUG: Processing commande " + c.getId());
                result.add(toResponse(c));
            } catch (Exception e) {
                System.err.println("Erreur conversion commande " + c.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        return result;
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
        System.out.println("DEBUG toResponse for commande: " + c.getId());
        List<CommandeResponse.LigneCommandeResponse> lignes = new ArrayList<>();
        
        try {
            List<LigneCommande> lignesCommande = ligneCommandeRepository
                    .findByCommandeIdWithMedicament(c.getId());
            
            System.out.println("DEBUG lignesCommande count: " + (lignesCommande != null ? lignesCommande.size() : "null"));
            
            if (lignesCommande != null) {
                for (LigneCommande l : lignesCommande) {
                    try {
                        if (l.getMedicament() != null) {
                            lignes.add(CommandeResponse.LigneCommandeResponse
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
                                    .dateFabricationRecue(
                                            l.getDateFabricationRecue())
                                    .build());
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur ligne commande " + l.getId() + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement lignes commande: " + e.getMessage());
        }

        Long fournisseurId = c.getFournisseur() != null 
                ? c.getFournisseur().getId() 
                : null;
        String fournisseurRaisonSociale = c.getFournisseur() != null 
                ? c.getFournisseur().getRaisonSociale() 
                : null;
        String createurNom = c.getCreateur() != null 
                ? c.getCreateur().getNom() 
                : null;
        String createurPrenom = c.getCreateur() != null 
                ? c.getCreateur().getPrenom() 
                : null;

        return CommandeResponse.builder()
                .id(c.getId())
                .reference(c.getReference())
                .fournisseurId(fournisseurId)
                .fournisseurRaisonSociale(fournisseurRaisonSociale)
                .createurNom(createurNom)
                .createurPrenom(createurPrenom)
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