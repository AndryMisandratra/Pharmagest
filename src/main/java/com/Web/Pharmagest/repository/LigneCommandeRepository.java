package com.Web.Pharmagest.repository;

import com.Web.Pharmagest.entity.LigneCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LigneCommandeRepository
        extends JpaRepository<LigneCommande, Long> {

    // Lignes d'une commande
    List<LigneCommande> findByCommandeId(Long commandeId);

    // Lignes d'une commande pour un médicament précis
    List<LigneCommande> findByCommandeIdAndMedicamentId(
            Long commandeId,
            Long medicamentId);
}
