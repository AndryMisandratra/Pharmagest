package com.Web.Pharmagest.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class VenteRequest {

    // Nullable : client peut être anonyme
    private Long patientId;

    // Nullable : garder compatibilité ancienne version
    private Long ordonnanceId;

    // Nouveau: nom du patient (si pas de patient enregistré)
    private String patientNom;

    // Nouveau: indique si la vente est avec ordonnance
    private Boolean hasOrdonnance;

    // Nouveau: numéro d'ordonnance (si hasOrdonnance = true)
    private String numeroOrdonnance;

    // Nouveau: nom du médecin (si hasOrdonnance = true)
    private String medecinNom;

    // Nouveau: RPPS du médecin
    private String medecinRpps;

    // Nouveau: date de prescription
    private String datePrescription;

    // Nouveau: tiers payant
    private Boolean aTiersPayant;

    private BigDecimal montantPaye;

    private BigDecimal montantRendu;

    // Liste des médicaments vendus
    @NotEmpty(message = "La vente doit contenir au moins un médicament")
    private List<LigneVenteRequest> lignes;
}