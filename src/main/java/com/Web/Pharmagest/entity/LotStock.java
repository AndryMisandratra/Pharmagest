package com.Web.Pharmagest.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lots_stock")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lot_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicament_id", nullable = false)
    private Medicament medicament;

    // Nullable : lot peut être créé manuellement sans commande
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id")
    private CommandeFournisseur commande;

    @Column(name = "lot_numero", nullable = false, length = 50)
    private String numero;

    @Column(name = "lot_quantite_recue", nullable = false)
    private Integer quantiteRecue;

    // Décrémenté à chaque vente FEFO
    @Column(name = "lot_quantite_restante", nullable = false)
    private Integer quantiteRestante;

    @Column(name = "lot_date_fabrication")
    private LocalDate dateFabrication;

    // Critère FEFO : ORDER BY lot_date_peremption ASC
    @Column(name = "lot_date_peremption", nullable = false)
    private LocalDate datePeremption;

    // Prix d'achat de CE lot (peut différer du catalogue)
    @Column(name = "lot_prix_achat_ht", nullable = false,
            precision = 10, scale = 2)
    private BigDecimal prixAchatHt;

    // False si retiré (périmé, cassé, perdu)
    @Column(name = "lot_est_actif", nullable = false)
    @Builder.Default
    private Boolean estActif = true;

    @Column(name = "lot_cree_le", nullable = false, updatable = false)
    private LocalDateTime creeLe;

    // Qui a créé/réceptionné ce lot
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_cree_par_id")
    private Utilisateur creePar;

    @PrePersist
    protected void onCreate() {
        creeLe = LocalDateTime.now();
    }
}