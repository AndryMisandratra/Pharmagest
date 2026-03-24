package com.Web.Pharmagest.repository;

import com.Web.Pharmagest.entity.CommandeFournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommandeFournisseurRepository
        extends JpaRepository<CommandeFournisseur, Long> {

    // Recherche par référence unique
    Optional<CommandeFournisseur> findByReference(String reference);

    // Commandes par statut
    List<CommandeFournisseur> findByStatut(
            CommandeFournisseur.StatutCommande statut);

    // Commandes d'un fournisseur
    List<CommandeFournisseur> findByFournisseurIdOrderByDateCommandeDesc(
            Long fournisseurId);

    // Commandes d'un fournisseur par statut
    List<CommandeFournisseur> findByFournisseurIdAndStatut(
            Long fournisseurId,
            CommandeFournisseur.StatutCommande statut);

    // Vérifier si une référence existe
    boolean existsByReference(String reference);
}