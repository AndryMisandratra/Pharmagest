package com.Web.Pharmagest.repository;

import com.Web.Pharmagest.entity.LotStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LotStockRepository
        extends JpaRepository<LotStock, Long> {

    // FEFO : lots d'un médicament triés par date de péremption
    // → Le lot qui expire le plus tôt est utilisé en premier
    @Query("SELECT l FROM LotStock l " +
            "WHERE l.medicament.id = :medicamentId " +
            "AND l.estActif = true " +
            "AND l.quantiteRestante > 0 " +
            "ORDER BY l.datePeremption ASC")
    List<LotStock> findLotsDisponiblesFEFO(
            @Param("medicamentId") Long medicamentId);

    // Lots périmant dans moins de N jours
    // → Pour les alertes de péremption
    @Query("SELECT l FROM LotStock l " +
            "WHERE l.estActif = true " +
            "AND l.quantiteRestante > 0 " +
            "AND l.datePeremption <= :dateLimite " +
            "ORDER BY l.datePeremption ASC")
    List<LotStock> findLotsExpirantAvant(
            @Param("dateLimite") LocalDate dateLimite);

    // Tous les lots actifs d'un médicament
    List<LotStock> findByMedicamentIdAndEstActifTrue(Long medicamentId);

    // Lots déjà périmés (pour nettoyage)
    @Query("SELECT l FROM LotStock l " +
            "WHERE l.estActif = true " +
            "AND l.datePeremption < :aujourdhui")
    List<LotStock> findLotsPerimes(
            @Param("aujourdhui") LocalDate aujourdhui);
}