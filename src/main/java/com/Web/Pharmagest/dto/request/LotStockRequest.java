package com.Web.Pharmagest.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LotStockRequest {

    @NotNull(message = "Le médicament est obligatoire")
    private Long medicamentId;

    // Nullable : lot peut être créé sans commande fournisseur
    private Long commandeId;

    @NotBlank(message = "Le numéro de lot est obligatoire")
    private String numero;

    @NotNull(message = "La quantité reçue est obligatoire")
    @Min(value = 1, message = "La quantité doit être supérieure à 0")
    private Integer quantiteRecue;

    private LocalDate dateFabrication;

    @NotNull(message = "La date de péremption est obligatoire")
    @Future(message = "La date de péremption doit être dans le futur")
    private LocalDate datePeremption;

    @NotNull(message = "Le prix d'achat HT est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false,
            message = "Le prix doit être positif")
    private BigDecimal prixAchatHt;
}