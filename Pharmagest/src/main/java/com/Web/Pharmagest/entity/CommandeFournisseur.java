package com.Web.Pharmagest.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "commandes_fournisseur")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandeFournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commande_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id", nullable = false)
    private Fournisseur fournisseur;

    // Qui a créé la commande
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createur_id", nullable = false)
    private Utilisateur createur;

    // Nullable : validé uniquement par le pharmacien
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valideur_id")
    private Utilisateur valideur;

    // Référence unique ex: CMD-20250315-005
    @Column(name = "commande_reference", nullable = false,
            unique = true, length = 30)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(name = "commande_statut", nullable = false, length = 30)
    private StatutCommande statut;

    @Column(name = "commande_montant_total_ht", precision = 14, scale = 2)
    private BigDecimal montantTotalHt;

    @Column(name = "commande_date_commande", nullable = false, updatable = false)
    private LocalDateTime dateCommande;

    @Column(name = "commande_date_envoi")
    private LocalDateTime dateEnvoi;

    @Column(name = "commande_date_reception")
    private LocalDateTime dateReception;

    // DATE (sans heure) → LocalDate
    @Column(name = "commande_date_livraison_estimee")
    private LocalDate dateLivraisonEstimee;

    @Column(name = "commande_notes", columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void onCreate() {
        dateCommande = LocalDateTime.now();
        if (statut == null) statut = StatutCommande.BROUILLON;
    }

    public enum StatutCommande {
        BROUILLON,
        ENVOYEE,
        EN_ATTENTE,
        CLOTUREE,
        RECUE_PARTIELLEMENT,
        ANNULEE
    }
}