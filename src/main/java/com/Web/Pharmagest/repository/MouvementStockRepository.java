package com.Web.Pharmagest.repository;

import com.Web.Pharmagest.entity.MouvementStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MouvementStockRepository
        extends JpaRepository<MouvementStock, Long> {

    // Historique des mouvements d'un médicament
    List<MouvementStock> findByMedicamentIdOrderByEffectueLeDesc(
            Long medicamentId);

    // Mouvements par type sur une période
    List<MouvementStock> findByTypeAndEffectueLeBetween(
            MouvementStock.TypeMouvement type,
            LocalDateTime debut,
            LocalDateTime fin);

    // Mouvements d'un médicament sur une période
    List<MouvementStock> findByMedicamentIdAndEffectueLeBetween(
            Long medicamentId,
            LocalDateTime debut,
            LocalDateTime fin);
}