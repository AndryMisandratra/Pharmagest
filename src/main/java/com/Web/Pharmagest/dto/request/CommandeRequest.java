package com.Web.Pharmagest.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class CommandeRequest {

    @NotNull(message = "Le fournisseur est obligatoire")
    private Long fournisseurId;

    private String notes;

    @NotEmpty(message = "La commande doit contenir au moins un médicament")
    private List<LigneCommandeRequest> lignes;
}