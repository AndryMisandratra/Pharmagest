package com.Web.Pharmagest.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotStockResponse {

    private Long id;
    private Long medicamentId;
    private String medicamentDenomination;
    private Long commandeId;
    private String numero;
    private Integer quantiteRecue;
    private Integer quantiteRestante;
    private LocalDate dateFabrication;
    private LocalDate datePeremption;
    private BigDecimal prixAchatHt;
    private Boolean estActif;
    private LocalDateTime creeLe;

    // Nombre de jours avant péremption (calculé)
    private Long joursAvantPeremption;

    // Niveau d'alerte péremption calculé
    // "NORMAL", "ORANGE" (< 90j), "ROUGE" (< 30j), "PERIME"
    private String alertePeremption;
}