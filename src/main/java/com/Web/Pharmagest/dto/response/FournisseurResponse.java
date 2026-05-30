package com.Web.Pharmagest.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FournisseurResponse {

    private Long id;
    private String raisonSociale;
    private String email;
    private String telephone;
    private String adresse;
    private Integer delaiLivraisonJours;
    private Boolean estActif;
    private LocalDateTime creeLe;
}