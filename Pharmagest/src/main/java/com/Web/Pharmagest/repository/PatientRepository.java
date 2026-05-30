package com.Web.Pharmagest.repository;

import com.Web.Pharmagest.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PatientRepository
        extends JpaRepository<Patient, Long> {

    // Recherche par nom ou prénom
    @Query("SELECT p FROM Patient p " +
            "WHERE LOWER(p.nom) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.prenom) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Patient> rechercherPatients(@Param("search") String search);

    // Recherche par numéro de sécurité sociale
    java.util.Optional<Patient> findByNumSecuSociale(String numSecuSociale);
}