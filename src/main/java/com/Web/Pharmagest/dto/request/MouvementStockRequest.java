package com.Web.Pharmagest.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MouvementStockRequest {

    @NotNull(message = "Le médicament est obligatoire")
    private Long medicamentId;

    // Nullable selon le type de mouvement
    private Long lotId;
    private Long venteId;
    private Long commandeId;

    @NotBlank(message = "Le type de mouvement est obligatoire")
    private String type;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être supérieure à 0")
    private Integer quantite;

    private String motif;
}