package com.Web.Pharmagest.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fournisseurs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fournisseur_id")
    private Long id;

    @Column(name = "fournisseur_raison_sociale", nullable = false, length = 200)
    private String raisonSociale;

    @Column(name = "fournisseur_email", length = 150)
    private String email;

    @Column(name = "fournisseur_telephone", length = 25)
    private String telephone;

    @Column(name = "fournisseur_adresse", columnDefinition = "TEXT")
    private String adresse;

    @Column(name = "fournisseur_delai_livraison_jours")
    @Builder.Default
    private Integer delaiLivraisonJours = 3;

    @Column(name = "fournisseur_est_actif", nullable = false)
    @Builder.Default
    private Boolean estActif = true;

    @Column(name = "fournisseur_cree_le", nullable = false, updatable = false)
    private LocalDateTime creeLe;

    @PrePersist
    protected void onCreate() {
        creeLe = LocalDateTime.now();
    }
}