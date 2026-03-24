package com.Web.Pharmagest.repository;

import com.Web.Pharmagest.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UtilisateurRepository
        extends JpaRepository<Utilisateur, Long> {

    // Spring Data génère automatiquement le SQL :
    // SELECT * FROM utilisateurs WHERE utilisateur_email = ?
    Optional<Utilisateur> findByEmail(String email);

    // Vérifie si un email existe déjà (pour la création de compte)
    boolean existsByEmail(String email);
}