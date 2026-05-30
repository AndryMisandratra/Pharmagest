package com.Web.Pharmagest.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {

    private Long id;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String telephone;
    private String numSecuSociale;
    private String mutuelle;
    private BigDecimal tauxMutuelle;
    private String allergiesConnues;
    private LocalDateTime creeLe;
}