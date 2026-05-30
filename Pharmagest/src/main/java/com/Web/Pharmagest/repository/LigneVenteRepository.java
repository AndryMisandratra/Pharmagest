package com.Web.Pharmagest.repository;

import com.Web.Pharmagest.entity.LigneVente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LigneVenteRepository
        extends JpaRepository<LigneVente, Long> {

    // Lignes d'une vente
    List<LigneVente> findByVenteId(Long venteId);

    // Top médicaments les plus vendus sur une période
    @Query("SELECT l.medicament.id, l.medicament.denomination, " +
            "SUM(l.quantite) as totalVendu " +
            "FROM LigneVente l " +
            "WHERE l.vente.effectueele BETWEEN :debut AND :fin " +
            "AND l.vente.statut = 'VALIDEE' " +
            "GROUP BY l.medicament.id, l.medicament.denomination " +
            "ORDER BY totalVendu DESC")
    List<Object[]> findTopMedicamentsVendus(
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin);
}