package com.Web.Pharmagest.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneVenteResponse {

    private Long id;
    private Long medicamentId;
    private String medicamentDenomination;
    private String lotNumero;
    private Integer quantite;
    private BigDecimal prixUnitaireTtc;
    private BigDecimal prixUnitaireHt;
    private BigDecimal tauxTva;
    private BigDecimal sousTotalTtc;
    private BigDecimal tauxRemboursement;
}