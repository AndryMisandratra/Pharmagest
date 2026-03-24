package com.Web.Pharmagest.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mouvements_stock")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MouvementStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mouvement_id")
    private Long id;

    // Médicament concerné par le mouvement
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicament_id", nullable = false)
    private Medicament medicament;

    // Nullable selon le type de mouvement
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    private LotStock lot;

    // Nullable : rempli uniquement si mouvement lié à une vente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vente_id")
    private Vente vente;

    // Nullable : rempli uniquement si mouvement lié à une commande
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id")
    private CommandeFournisseur commande;

    @Enumerated(EnumType.STRING)
    @Column(name = "mouvement_type", nullable = false, length = 30)
    private TypeMouvement type;

    // Toujours positif — le sens est défini par le type
    // ex: SORTIE_VENTE quantite=5 → stock -5
    //     ENTREE_RECEPTION quantite=100 → stock +100
    @Column(name = "mouvement_quantite", nullable = false)
    private Integer quantite;

    @Column(name = "mouvement_motif", columnDefinition = "TEXT")
    private String motif;

    // Journal immuable : updatable=false, insertable=true
    @Column(name = "mouvement_effectue_le", nullable = false,
            updatable = false)
    private LocalDateTime effectueLe;

    // Qui a effectué le mouvement
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mouvement_effectue_par_id", nullable = false)
    private Utilisateur effectuePar;

    @PrePersist
    protected void onCreate() {
        effectueLe = LocalDateTime.now();
    }

    public enum TypeMouvement {
        ENTREE_RECEPTION,   // Réception commande fournisseur
        ENTREE_RETOUR,      // Retour client (annulation vente)
        SORTIE_VENTE,       // Vente au comptoir
        SORTIE_PERIME,      // Retrait lot périmé
        SORTIE_PERTE,       // Casse ou perte
        INVENTAIRE          // Correction lors d'un inventaire
    }
}