package com.Web.Pharmagest.service;

import com.Web.Pharmagest.dto.request.OrdonnanceRequest;
import com.Web.Pharmagest.dto.response.OrdonnanceResponse;
import com.Web.Pharmagest.entity.Ordonnance;
import com.Web.Pharmagest.entity.Patient;
import com.Web.Pharmagest.repository.OrdonnanceRepository;
import com.Web.Pharmagest.repository.PatientRepository;
import com.Web.Pharmagest.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrdonnanceService {

    private final OrdonnanceRepository ordonnanceRepository;
    private final PatientRepository patientRepository;
    private final UtilisateurRepository utilisateurRepository;

    // ================================================
    // CRÉER une ordonnance
    // Contrôle anti-doublon sur le numéro
    // ================================================
    @Transactional
    public OrdonnanceResponse creerOrdonnance(
            OrdonnanceRequest request,
            String emailUtilisateur) {

        // ⚠️ ANTI-DOUBLON : vérifier que le numéro
        // n'existe pas déjà en BDD
        if (ordonnanceRepository.existsByNumero(request.getNumero())) {
            throw new RuntimeException(
                    "Ordonnance déjà enregistrée avec le numéro : "
                            + request.getNumero()
                            + " — Cette ordonnance a déjà été honorée !"
            );
        }

        // Charger le patient (optionnel - peut être null)
        Patient patient = null;
        if (request.getPatientId() != null) {
            patient = patientRepository
                    .findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException(
                            "Patient non trouvé : " + request.getPatientId()
                    ));
        }

        // Charger l'utilisateur qui saisit
        var utilisateur = utilisateurRepository
                .findByEmail(emailUtilisateur)
                .orElseThrow(() -> new RuntimeException(
                        "Utilisateur non trouvé"
                ));

        var ordonnance = Ordonnance.builder()
                .patient(patient)
                .numero(request.getNumero())
                .medecinNom(request.getMedecinNom())
                .medecinRpps(request.getMedecinRpps())
                .datePrescription(request.getDatePrescription())
                .dateValidite(request.getDateValidite())
                .statut(Ordonnance.StatutOrdonnance.EN_ATTENTE)
                .fichierScanUrl(request.getFichierScanUrl())
                .aTiersPayant(request.getATiersPayant() != null
                        ? request.getATiersPayant() : false)
                .saisiePar(utilisateur)
                .build();

        return toResponse(ordonnanceRepository.save(ordonnance));
    }

    // ================================================
    // CHANGER le statut d'une ordonnance
    // (EN_ATTENTE → SERVIE, PARTIELLEMENT_SERVIE, EXPIREE)
    // ================================================
    @Transactional
    public OrdonnanceResponse changerStatut(Long id, String statut) {
        var ordonnance = ordonnanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Ordonnance non trouvée : " + id
                ));

        ordonnance.setStatut(
                Ordonnance.StatutOrdonnance.valueOf(statut)
        );

        return toResponse(ordonnanceRepository.save(ordonnance));
    }

    // ================================================
    // OBTENIR une ordonnance par ID
    // ================================================
    public OrdonnanceResponse getOrdonnanceById(Long id) {
        return toResponse(
                ordonnanceRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException(
                                "Ordonnance non trouvée : " + id
                        ))
        );
    }

    // ================================================
    // VÉRIFIER anti-doublon par numéro
    // ================================================
    public OrdonnanceResponse getOrdonnanceByNumero(String numero) {
        return toResponse(
                ordonnanceRepository.findByNumero(numero)
                        .orElseThrow(() -> new RuntimeException(
                                "Ordonnance non trouvée : " + numero
                        ))
        );
    }

    // ================================================
    // LISTER les ordonnances d'un patient
    // ================================================
    public List<OrdonnanceResponse> getOrdonnancesParPatient(
            Long patientId) {
        return ordonnanceRepository
                .findByPatientIdOrderBySaisieleDesc(patientId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ================================================
    // LISTER par statut
    // ================================================
    public List<OrdonnanceResponse> getOrdonnancesParStatut(
            String statut) {
        return ordonnanceRepository
                .findByStatut(
                        Ordonnance.StatutOrdonnance.valueOf(statut)
                )
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ================================================
    // CONVERSION Entité → DTO
    // ================================================
    private OrdonnanceResponse toResponse(Ordonnance o) {
        String saisieParNom = o.getSaisiePar() != null ? o.getSaisiePar().getNom() : null;
        String saisieParPrenom = o.getSaisiePar() != null ? o.getSaisiePar().getPrenom() : null;
        
        return OrdonnanceResponse.builder()
                .id(o.getId())
                .patientId(o.getPatient() != null ? o.getPatient().getId() : null)
                .patientNom(o.getPatient() != null ? o.getPatient().getNom() : null)
                .patientPrenom(o.getPatient() != null ? o.getPatient().getPrenom() : null)
                .numero(o.getNumero())
                .medecinNom(o.getMedecinNom())
                .medecinRpps(o.getMedecinRpps())
                .datePrescription(o.getDatePrescription())
                .dateValidite(o.getDateValidite())
                .statut(o.getStatut().name())
                .fichierScanUrl(o.getFichierScanUrl())
                .aTiersPayant(o.getATiersPayant())
                .saisieLe(o.getSaisiele())
                .saisieParNom(saisieParNom)
                .saisieParPrenom(saisieParPrenom)
                .build();
    }
}