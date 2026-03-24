package com.Web.Pharmagest.repository;

import com.Web.Pharmagest.entity.Vente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VenteRepository
        extends JpaRepository<Vente, Long> {

    // Recherche par référence unique (ex: V-20250315-0042)
    Optional<Vente> findByReference(String reference);

    // Ventes sur une période (pour les rapports)
    List<Vente> findByEffectueeleBetweenOrderByEffectueeleDesc(
            LocalDateTime debut,
            LocalDateTime fin);

    // Ventes d'un vendeur sur une période
    List<Vente> findByVendeurIdAndEffectueeleBetween(
            Long vendeurId,
            LocalDateTime debut,
            LocalDateTime fin);

    // Chiffre d'affaires total sur une période
    @Query("SELECT COALESCE(SUM(v.montantTotalTtc), 0) " +
            "FROM Vente v " +
            "WHERE v.statut = 'VALIDEE' " +
            "AND v.effectueele BETWEEN :debut AND :fin")
    BigDecimal calculerChiffreAffaires(
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin);

    // Nombre de ventes du jour
    @Query("SELECT COUNT(v) FROM Vente v " +
            "WHERE v.statut = 'VALIDEE' " +
            "AND v.effectueele BETWEEN :debut AND :fin")
    Long countVentesParPeriode(
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin);

    // Ventes en attente de remboursement tiers payant
    List<Vente> findByModePaiementAndStatut(
            Vente.ModePaiement modePaiement,
            Vente.StatutVente statut);
    // Ajoute cette méthode dans VenteRepository
    @Query("SELECT v FROM Vente v " +
            "WHERE v.effectueele BETWEEN :debut AND :fin " +
            "ORDER BY v.effectueele DESC")
    List<Vente> findVentesParPeriode(
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin);
}