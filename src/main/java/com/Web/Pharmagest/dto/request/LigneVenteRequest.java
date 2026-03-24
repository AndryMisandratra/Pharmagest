package com.Web.Pharmagest.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LigneVenteRequest {

    @NotNull(message = "Le médicament est obligatoire")
    private Long medicamentId;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être supérieure à 0")
    private Integer quantite;
}