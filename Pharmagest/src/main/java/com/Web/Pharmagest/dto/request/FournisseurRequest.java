package com.Web.Pharmagest.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class FournisseurRequest {

    @NotBlank(message = "La raison sociale est obligatoire")
    private String raisonSociale;

    @Email(message = "Format email invalide")
    private String email;

    private String telephone;
    private String adresse;

    @Min(value = 1, message = "Le délai doit être supérieur à 0")
    private Integer delaiLivraisonJours = 3;
}