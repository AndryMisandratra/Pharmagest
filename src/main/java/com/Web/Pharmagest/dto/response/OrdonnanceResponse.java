package com.Web.Pharmagest.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdonnanceResponse {

    private Long id;

    // Infos patient
    private Long patientId;
    private String patientNom;
    private String patientPrenom;

    private String numero;
    private String medecinNom;
    private String medecinRpps;
    private LocalDate datePrescription;
    private LocalDate dateValidite;
    private String statut;
    private String fichierScanUrl;
    private Boolean aTiersPayant;
    private LocalDateTime saisieLe;

    // Qui a saisi l'ordonnance
    private String saisieParNom;
    private String saisieParPrenom;
}