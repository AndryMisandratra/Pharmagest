package com.Web.Pharmagest.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicaments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Medicament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicament_id")
    private Long id;

    // Relation ManyToOne : plusieurs médicaments → une catégorie
    // @JoinColumn = nom de la colonne FK dans la table medicaments
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id", nullable = false)
    private CategorieMedicament categorie;

    // Nullable : un médicament peut ne pas avoir de fournisseur habituel
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id")
    private Fournisseur fournisseur;

    @Column(name = "medicament_code_cip", nullable = false, unique = true, length = 13)
    private String codeCip;

    @Column(name = "medicament_denomination", nullable = false, length = 200)
    private String denomination;

    @Column(name = "medicament_dci", nullable = false, length = 200)
    private String dci;

    @Column(name = "medicament_forme_galenique", nullable = false, length = 50)
    private String formeGalenique;

    @Column(name = "medicament_dosage", length = 50)
    private String dosage;

    @Enumerated(EnumType.STRING)
    @Column(name = "medicament_statut_legal", nullable = false, length = 30)
    private StatutLegal statutLegal;

    // BigDecimal pour les montants financiers (précision exacte)
    // Ne jamais utiliser double/float pour l'argent !
    @Column(name = "medicament_prix_vente_ttc", nullable = false,
            precision = 10, scale = 2)
    private BigDecimal prixVenteTtc;

    @Column(name = "medicament_prix_achat_ht", nullable = false,
            precision = 10, scale = 2)
    private BigDecimal prixAchatHt;

    @Column(name = "medicament_taux_tva", nullable = false,
            precision = 5, scale = 2)
    private BigDecimal tauxTva;

    @Column(name = "medicament_stock_quantite_totale", nullable = false)
    @Builder.Default
    private Integer stockQuantiteTotale = 0;

    @Column(name = "medicament_stock_seuil_alerte", nullable = false)
    private Integer stockSeuilAlerte;

    @Column(name = "medicament_emplacement", length = 50)
    private String emplacement;

    @Column(name = "medicament_fabricant", length = 150)
    private String fabricant;

    @Column(name = "medicament_est_remboursable_ss", nullable = false)
    @Builder.Default
    private Boolean estRemboursableSs = false;

    @Column(name = "medicament_taux_remboursement_ss",
            precision = 5, scale = 2)
    private BigDecimal tauxRemboursementSs;

    @Column(name = "medicament_est_disponible", nullable = false)
    @Builder.Default
    private Boolean estDisponible = true;

    @Column(name = "medicament_cree_le", nullable = false, updatable = false)
    private LocalDateTime creeLe;

    @Column(name = "medicament_modifie_le")
    private LocalDateTime modifieLe;

    @PrePersist
    protected void onCreate() {
        creeLe = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modifieLe = LocalDateTime.now();
    }

    public enum StatutLegal {
        LIBRE_ACCES,
        CONSEIL,
        ORDONNANCE,
        ORDONNANCE_SECURISEE
    }
}