package com.Web.Pharmagest.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lignes_commande")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ligne_commande_id")
    private Long id;

    // La commande fournisseur à laquelle appartient cette ligne
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", nullable = false)
    private CommandeFournisseur commande;

    // Le médicament commandé
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicament_id", nullable = false)
    private Medicament medicament;

    @Column(name = "ligne_commande_quantite_commandee", nullable = false)
    private Integer quantiteCommandee;

    // Mis à jour lors de la réception physique de la livraison
    @Column(name = "ligne_commande_quantite_recue", nullable = false)
    @Builder.Default
    private Integer quantiteRecue = 0;

    // Prix négocié pour cette commande spécifique
    @Column(name = "ligne_commande_prix_achat_ht",
            precision = 10, scale = 2)
    private BigDecimal prixAchatHt;

    // Saisi lors de la réception physique
    @Column(name = "ligne_commande_lot_numero_recu", length = 50)
    private String lotNumeroRecu;

    // DATE de péremption du lot reçu (saisie à la réception)
    @Column(name = "ligne_commande_date_peremption_recue")
    private LocalDate datePeremptionRecue;
}
