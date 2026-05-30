package com.Web.Pharmagest.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    // ===== KPIs DU JOUR =====
    private BigDecimal chiffreAffairesJour;
    private Long nombreVentesJour;
    private Integer nombreMedicamentsActifs;

    // ===== ALERTES STOCK =====
    private Integer nombreAlerteRougeStock;
    private Integer nombreAlerteOrangeStock;
    private List<String> medicamentsStockBas;
    private List<String> medicamentsStockFaible;

    // ===== ALERTES PÉREMPTION =====
    private Integer nombreAlerteRougePeremption;
    private Integer nombreAlerteOrangePeremption;

    // ===== TOP 5 MÉDICAMENTS VENDUS =====
    private List<TopMedicamentDTO> topMedicaments;

    // Classe imbriquée pour le top médicaments
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopMedicamentDTO {
        private String denomination;
        private Long quantiteVendue;
    }
}