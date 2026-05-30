package com.Web.Pharmagest.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordonnances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ordonnance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ordonnance_id")
    private Long id;

    // Plusieurs ordonnances → un patient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    // Clé anti-doublon : une ordonnance ne peut être honorée qu'une fois
    @Column(name = "ordonnance_numero", nullable = false,
            unique = true, length = 50)
    private String numero;

    @Column(name = "ordonnance_medecin_nom", nullable = false, length = 150)
    private String medecinNom;

    // RPPS = identifiant officiel du médecin en France
    @Column(name = "ordonnance_medecin_rpps", length = 20)
    private String medecinRpps;

    // DATE (sans heure) → LocalDate
    @Column(name = "ordonnance_date_prescription", nullable = false)
    private LocalDate datePrescription;

    @Column(name = "ordonnance_date_validite")
    private LocalDate dateValidite;

    @Enumerated(EnumType.STRING)
    @Column(name = "ordonnance_statut", nullable = false, length = 30)
    private StatutOrdonnance statut;

    // Chemin vers le scan PDF/image archivé sur le serveur
    @Column(name = "ordonnance_fichier_scan_url", length = 500)
    private String fichierScanUrl;

    @Column(name = "ordonnance_a_tiers_payant", nullable = false)
    @Builder.Default
    private Boolean aTiersPayant = false;

    @Column(name = "ordonnance_saisie_le", nullable = false, updatable = false)
    private LocalDateTime saisiele;

    // Qui a saisi l'ordonnance (préparateur ou pharmacien)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordonnance_saisie_par_id", nullable = false)
    private Utilisateur saisiePar;

    @PrePersist
    protected void onCreate() {
        saisiele = LocalDateTime.now();
        // Statut par défaut à la création
        if (statut == null) statut = StatutOrdonnance.EN_ATTENTE;
    }

    public enum StatutOrdonnance {
        EN_ATTENTE,
        SERVIE,
        PARTIELLEMENT_SERVIE,
        EXPIREE
    }
}