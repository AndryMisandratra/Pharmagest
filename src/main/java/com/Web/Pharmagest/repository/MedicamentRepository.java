package com.Web.Pharmagest.repository;

import com.Web.Pharmagest.entity.Medicament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicamentRepository
        extends JpaRepository<Medicament, Long> {

    // Recherche par code CIP-13
    Optional<Medicament> findByCodeCip(String codeCip);

    // Recherche par nom commercial (insensible à la casse)
    List<Medicament> findByDenominationContainingIgnoreCase(
            String denomination);

    // Recherche par DCI (principe actif)
    List<Medicament> findByDciContainingIgnoreCase(String dci);

    // Médicaments disponibles uniquement
    List<Medicament> findByEstDisponibleTrue();

    // ===== ALERTES STOCK =====
    // Médicaments dont le stock est inférieur ou égal au seuil
    // → Alerte ROUGE (rupture imminente)
    @Query("SELECT m FROM Medicament m " +
            "WHERE m.estDisponible = true " +
            "AND m.stockQuantiteTotale <= m.stockSeuilAlerte")
    List<Medicament> findMedicamentsStockBas();

    // Médicaments dont le stock est inférieur à 2x le seuil
    // → Alerte ORANGE (stock faible)
    @Query("SELECT m FROM Medicament m " +
            "WHERE m.estDisponible = true " +
            "AND m.stockQuantiteTotale <= (m.stockSeuilAlerte * 2) " +
            "AND m.stockQuantiteTotale > m.stockSeuilAlerte")
    List<Medicament> findMedicamentsStockFaible();

    // Recherche globale : nom OU dci OU code CIP
    @Query("SELECT m FROM Medicament m " +
            "WHERE m.estDisponible = true " +
            "AND (" +
            "  LOWER(m.denomination) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "  OR LOWER(m.dci) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "  OR m.codeCip LIKE CONCAT('%', :search, '%')" +
            ")")
    List<Medicament> rechercherMedicaments(@Param("search") String search);

    // Médicaments par catégorie
    List<Medicament> findByCategorieIdAndEstDisponibleTrue(Long categorieId);

    // Vérifier si un code CIP existe déjà
    boolean existsByCodeCip(String codeCip);
}