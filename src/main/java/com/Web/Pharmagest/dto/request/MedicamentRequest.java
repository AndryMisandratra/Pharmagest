package com.Web.Pharmagest.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MedicamentRequest {

    @NotBlank(message = "Le code CIP est obligatoire")
    @Size(min = 13, max = 13, message = "Le code CIP doit faire exactement 13 caractères")
    private String codeCip;

    @NotBlank(message = "La dénomination est obligatoire")
    private String denomination;

    @NotBlank(message = "La DCI est obligatoire")
    private String dci;

    @NotBlank(message = "La forme galénique est obligatoire")
    private String formeGalenique;

    private String dosage;

    @NotNull(message = "La catégorie est obligatoire")
    private Long categorieId;

    private Long fournisseurId;

    @NotBlank(message = "Le statut légal est obligatoire")
    private String statutLegal;

    @NotNull(message = "Le prix de vente TTC est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false,
            message = "Le prix doit être positif")
    private BigDecimal prixVenteTtc;

    @NotNull(message = "Le prix d'achat HT est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false,
            message = "Le prix doit être positif")
    private BigDecimal prixAchatHt;

    @NotNull(message = "Le taux TVA est obligatoire")
    private BigDecimal tauxTva;

    @NotNull(message = "Le seuil d'alerte est obligatoire")
    @Min(value = 0, message = "Le seuil doit être positif")
    private Integer stockSeuilAlerte;

    private String emplacement;
    private String fabricant;

    private Boolean estRemboursableSs = false;
    private BigDecimal tauxRemboursementSs;
}