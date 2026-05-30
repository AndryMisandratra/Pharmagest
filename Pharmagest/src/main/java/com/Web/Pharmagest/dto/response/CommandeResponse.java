package com.Web.Pharmagest.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandeResponse {

    private Long id;
    private String reference;

    // Infos fournisseur
    private Long fournisseurId;
    private String fournisseurRaisonSociale;

    // Qui a créé + qui a validé
    private String createurNom;
    private String createurPrenom;
    private String valideurNom;
    private String valideurPrenom;

    private String statut;
    private BigDecimal montantTotalHt;
    private LocalDateTime dateCommande;
    private LocalDateTime dateEnvoi;
    private LocalDateTime dateReception;
    private LocalDate dateLivraisonEstimee;
    private String notes;

    // Lignes de la commande
    private List<LigneCommandeResponse> lignes;

    // Classe imbriquée pour les lignes
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LigneCommandeResponse {
        private Long id;
        private Long medicamentId;
        private String medicamentDenomination;
        private Integer quantiteCommandee;
        private Integer quantiteRecue;
        private BigDecimal prixAchatHt;
        private String lotNumeroRecu;
        private LocalDate datePeremptionRecue;
        private LocalDate dateFabricationRecue;
    }
}