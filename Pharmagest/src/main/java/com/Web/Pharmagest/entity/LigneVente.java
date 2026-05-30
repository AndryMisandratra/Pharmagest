package com.Web.Pharmagest.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "lignes_vente")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneVente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ligne_vente_id")
    private Long id;

    // La vente à laquelle appartient cette ligne
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vente_id", nullable = false)
    private Vente vente;

    // Le médicament vendu
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicament_id", nullable = false)
    private Medicament medicament;

    // Le lot utilisé — traçabilité FEFO lot par lot
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private LotStock lot;

    @Column(name = "ligne_vente_quantite", nullable = false)
    private Integer quantite;

    // ===== SNAPSHOTS DES PRIX =====
    // Ces prix sont copiés au moment de la vente
    // Ils ne changent PAS si le catalogue est mis à jour ensuite
    // C'est essentiel pour l'historique et la comptabilité

    @Column(name = "ligne_vente_prix_unitaire_ttc", nullable = false,
            precision = 10, scale = 2)
    private BigDecimal prixUnitaireTtc;

    @Column(name = "ligne_vente_prix_unitaire_ht", nullable = false,
            precision = 10, scale = 2)
    private BigDecimal prixUnitaireHt;

    @Column(name = "ligne_vente_taux_tva", nullable = false,
            precision = 5, scale = 2)
    private BigDecimal tauxTva;

    // sous_total = quantite × prix_unitaire_ttc
    @Column(name = "ligne_vente_sous_total_ttc", nullable = false,
            precision = 12, scale = 2)
    private BigDecimal sousTotalTtc;

    // Taux de remboursement SS appliqué sur cette ligne
    @Column(name = "ligne_vente_taux_remboursement", nullable = false,
            precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tauxRemboursement = BigDecimal.ZERO;
}