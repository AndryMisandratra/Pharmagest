package com.Web.Pharmagest.repository;

import com.Web.Pharmagest.entity.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FournisseurRepository
        extends JpaRepository<Fournisseur, Long> {

    // Liste des fournisseurs actifs uniquement
    List<Fournisseur> findByEstActifTrue();

    // Recherche par raison sociale (insensible à la casse)
    List<Fournisseur> findByRaisonSocialeContainingIgnoreCase(
            String raisonSociale);
}