package com.Web.Pharmagest.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ventes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vente_id")
    private Long id;

    // Nullable : client peut être anonyme (vente sans ordonnance)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    // Nullable : vente libre sans ordonnance
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordonnance_id")
    private Ordonnance ordonnance;

    // Qui a encaissé (caissier ou préparateur)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendeur_id", nullable = false)
    private Utilisateur vendeur;

    // Référence unique ex: V-20250315-0042
    @Column(name = "vente_reference", nullable = false,
            unique = true, length = 30)
    private String reference;

    // ===== MONTANTS FINANCIERS =====
    // Règle : HT + TVA = TTC
    @Column(name = "vente_montant_total_ht", nullable = false,
            precision = 12, scale = 2)
    private BigDecimal montantTotalHt;

    @Column(name = "vente_montant_tva", nullable = false,
            precision = 12, scale = 2)
    private BigDecimal montantTva;

    @Column(name = "vente_montant_total_ttc", nullable = false,
            precision = 12, scale = 2)
    private BigDecimal montantTotalTtc;

    // Part remboursée par la Sécurité Sociale
    @Column(name = "vente_montant_rembourse_ss", nullable = false,
            precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal montantRembourseSs = BigDecimal.ZERO;

    // Part remboursée par la mutuelle
    @Column(name = "vente_montant_rembourse_mutuelle", nullable = false,
            precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal montantRembourseMutuelle = BigDecimal.ZERO;

    // Ce que le client paie réellement après remboursements
    @Column(name = "vente_montant_paye_client", nullable = false,
            precision = 10, scale = 2)
    private BigDecimal montantPayeClient;

    @Enumerated(EnumType.STRING)
    @Column(name = "vente_mode_paiement", nullable = false, length = 30)
    private ModePaiement modePaiement;

    @Enumerated(EnumType.STRING)
    @Column(name = "vente_statut", nullable = false, length = 20)
    private StatutVente statut;

    @Column(name = "vente_effectuee_le", nullable = false, updatable = false)
    private LocalDateTime effectueele;

    // Rempli uniquement si la vente est annulée
    @Column(name = "vente_annulee_le")
    private LocalDateTime annuleeLe;

    // Qui a annulé (pharmacien uniquement selon le CDC)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vente_annulee_par_id")
    private Utilisateur annuleePar;

    @PrePersist
    protected void onCreate() {
        effectueele = LocalDateTime.now();
        if (statut == null) statut = StatutVente.VALIDEE;
    }

    public enum ModePaiement {
        ESPECES,
        CARTE,
        CHEQUE,
        TIERS_PAYANT,
        MIXTE
    }

    public enum StatutVente {
        VALIDEE,
        ANNULEE,
        REMBOURSEE
    }
}