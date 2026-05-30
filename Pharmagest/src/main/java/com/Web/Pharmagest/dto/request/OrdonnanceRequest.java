package com.Web.Pharmagest.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class OrdonnanceRequest {

    // Nullable pour permettre création sans patient existant
    private Long patientId;

    @NotBlank(message = "Le numéro d'ordonnance est obligatoire")
    private String numero;

    @NotBlank(message = "Le nom du médecin est obligatoire")
    private String medecinNom;

    // RPPS = identifiant officiel du médecin
    private String medecinRpps;

    @NotNull(message = "La date de prescription est obligatoire")
    private LocalDate datePrescription;

    private LocalDate dateValidite;

    // Chemin du scan uploadé
    private String fichierScanUrl;

    private Boolean aTiersPayant = false;
}