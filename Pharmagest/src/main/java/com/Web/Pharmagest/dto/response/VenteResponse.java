package com.Web.Pharmagest.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenteResponse {

    private Long id;
    private String reference;

    // Infos patient (nullable)
    private Long patientId;
    private String patientNom;
    private String patientPrenom;

    // Infos ordonnance (depuis la table ordonnances)
    private Long ordonnanceId;
    private String ordonnanceNumero;

    // Qui a vendu
    private String vendeurNom;
    private String vendeurPrenom;

    // Montants
    private BigDecimal montantTotalHt;
    private BigDecimal montantTva;
    private BigDecimal montantTotalTtc;
    private BigDecimal montantRembourseSs;
    private BigDecimal montantRembourseMutuelle;
    private BigDecimal montantPayeClient;

    private BigDecimal montantPaye;

    private BigDecimal montantRendu;

    private String modePaiement;
    private String statut;
    private LocalDateTime effectueLe;

    // Détail des lignes
    private List<LigneVenteResponse> lignes;
}