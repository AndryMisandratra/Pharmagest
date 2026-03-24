package com.Web.Pharmagest.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class LigneCommandeRequest {

    @NotNull(message = "Le médicament est obligatoire")
    private Long medicamentId;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être supérieure à 0")
    private Integer quantiteCommandee;

    @DecimalMin(value = "0.0", message = "Le prix doit être positif")
    private BigDecimal prixAchatHt;
}