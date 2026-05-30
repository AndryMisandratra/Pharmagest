package com.Web.Pharmagest.repository;

import com.Web.Pharmagest.entity.Ordonnance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdonnanceRepository
        extends JpaRepository<Ordonnance, Long> {

    // Anti-doublon : vérifier si un numéro existe déjà
    Optional<Ordonnance> findByNumero(String numero);
    boolean existsByNumero(String numero);

    // Ordonnances d'un patient
    List<Ordonnance> findByPatientIdOrderBySaisieleDesc(Long patientId);

    // Ordonnances par statut
    List<Ordonnance> findByStatut(Ordonnance.StatutOrdonnance statut);

    // Ordonnances d'un patient par statut
    List<Ordonnance> findByPatientIdAndStatut(
            Long patientId,
            Ordonnance.StatutOrdonnance statut);
}