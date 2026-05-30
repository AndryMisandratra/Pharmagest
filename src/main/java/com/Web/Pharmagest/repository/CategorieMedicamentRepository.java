package com.Web.Pharmagest.repository;

import com.Web.Pharmagest.entity.CategorieMedicament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CategorieMedicamentRepository
        extends JpaRepository<CategorieMedicament, Long> {

    // Recherche par code court (ex: ANALG, ANTIB)
    Optional<CategorieMedicament> findByCode(String code);

    // Vérifie si un code existe déjà
    boolean existsByCode(String code);
}