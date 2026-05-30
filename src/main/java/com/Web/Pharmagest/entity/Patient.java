package com.Web.Pharmagest.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_id")
    private Long id;

    @Column(name = "patient_nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "patient_prenom", nullable = false, length = 100)
    private String prenom;

    // LocalDate (pas LocalDateTime) car SQL = DATE (sans heure)
    @Column(name = "patient_date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "patient_telephone", length = 25)
    private String telephone;

    // 15 chiffres format français
    @Column(name = "patient_num_secu_sociale", length = 15)
    private String numSecuSociale;

    @Column(name = "patient_mutuelle", length = 100)
    private String mutuelle;

    @Column(name = "patient_taux_mutuelle", precision = 5, scale = 2)
    private BigDecimal tauxMutuelle;

    @Column(name = "patient_allergies_connues", columnDefinition = "TEXT")
    private String allergiesConnues;

    @Column(name = "patient_cree_le", nullable = false, updatable = false)
    private LocalDateTime creeLe;

    @PrePersist
    protected void onCreate() {
        creeLe = LocalDateTime.now();
    }
}