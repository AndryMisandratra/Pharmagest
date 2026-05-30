package com.Web.Pharmagest.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MouvementStockResponse {

    private Long id;
    private Long medicamentId;
    private String medicamentDenomination;
    private Long lotId;
    private String lotNumero;
    private Long venteId;
    private Long commandeId;
    private String type;
    private Integer quantite;
    private String motif;
    private LocalDateTime effectueLe;
    private String effectueParNom;
    private String effectueParPrenom;
}