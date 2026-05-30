package com.Web.Pharmagest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicamentResponse {

    private Long id;
    private String codeCip;
    private String denomination;
    private String dci;
    private String formeGalenique;
    private String dosage;

    // Infos catégorie (pas juste l'id)
    private Long categorieId;
    private String categorieLibelle;

    // Infos fournisseur
    private Long fournisseurId;
    private String fournisseurRaisonSociale;

    private String statutLegal;
    private BigDecimal prixVenteTtc;
    private BigDecimal prixAchatHt;  // visible ROLE_PHARMACIEN seulement
    private BigDecimal tauxTva;

    private Integer stockQuantiteTotale;
    private Integer stockSeuilAlerte;

    // Niveau d'alerte calculé
    // "NORMAL", "ORANGE", "ROUGE"
    private String niveauAlerte;

    private String emplacement;
    private String fabricant;
    private Boolean estRemboursableSs;
    private BigDecimal tauxRemboursementSs;
    private Boolean estDisponible;
    private LocalDateTime creeLe;
}