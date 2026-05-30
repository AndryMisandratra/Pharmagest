package com.Web.Pharmagest.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PatientRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    private LocalDate dateNaissance;

    private String telephone;

    // 15 chiffres — format français
    @Size(min = 15, max = 15,
            message = "Le numéro de sécurité sociale doit faire 15 chiffres")
    private String numSecuSociale;

    private String mutuelle;

    @DecimalMin(value = "0.0", message = "Le taux doit être positif")
    @DecimalMax(value = "100.0", message = "Le taux ne peut dépasser 100%")
    private BigDecimal tauxMutuelle;

    private String allergiesConnues;
}