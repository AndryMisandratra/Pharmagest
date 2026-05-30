package com.Web.Pharmagest.repository;

import com.Web.Pharmagest.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UtilisateurRepository
        extends JpaRepository<Utilisateur, Long> {

    // Spring Data génère automatiquement le SQL :
    // SELECT * FROM utilisateurs WHERE utilisateur_email = ?
    Optional<Utilisateur> findByEmail(String email);

    // Vérifie si un email existe déjà (pour la création de compte)
    boolean existsByEmail(String email);

    // Vérifie si au moins un utilisateur s'est connecté depuis le début d'une période
    @Query("SELECT COUNT(u) > 0 FROM Utilisateur u " +
           "WHERE u.derniereConnexion >= :debutPeriode")
    boolean aAuMoinsUneConnexionDepuis(
            LocalDateTime debutPeriode);
}