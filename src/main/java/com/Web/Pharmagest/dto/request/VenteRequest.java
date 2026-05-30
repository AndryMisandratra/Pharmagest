package com.Web.Pharmagest.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class VenteRequest {

    // Nullable : client peut être anonyme
    private Long patientId;

    // Nullable : vente libre sans ordonnance
    private Long ordonnanceId;

    @NotNull(message = "Le mode de paiement est obligatoire")
    private String modePaiement;

    // Liste des médicaments vendus
    @NotEmpty(message = "La vente doit contenir au moins un médicament")
    private List<LigneVenteRequest> lignes;
}