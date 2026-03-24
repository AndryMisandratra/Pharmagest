package com.Web.Pharmagest.service;

import com.Web.Pharmagest.dto.request.PatientRequest;
import com.Web.Pharmagest.dto.response.PatientResponse;
import com.Web.Pharmagest.entity.Patient;
import com.Web.Pharmagest.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientService {

    private final PatientRepository patientRepository;

    // ================================================
    // CRÉER un patient
    // ================================================
    @Transactional
    public PatientResponse creerPatient(PatientRequest request) {

        var patient = Patient.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .dateNaissance(request.getDateNaissance())
                .telephone(request.getTelephone())
                .numSecuSociale(request.getNumSecuSociale())
                .mutuelle(request.getMutuelle())
                .tauxMutuelle(request.getTauxMutuelle())
                .allergiesConnues(request.getAllergiesConnues())
                .build();

        return toResponse(patientRepository.save(patient));
    }

    // ================================================
    // MODIFIER un patient
    // ================================================
    @Transactional
    public PatientResponse modifierPatient(Long id,
                                           PatientRequest request) {
        var patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Patient non trouvé : " + id
                ));

        patient.setNom(request.getNom());
        patient.setPrenom(request.getPrenom());
        patient.setDateNaissance(request.getDateNaissance());
        patient.setTelephone(request.getTelephone());
        patient.setNumSecuSociale(request.getNumSecuSociale());
        patient.setMutuelle(request.getMutuelle());
        patient.setTauxMutuelle(request.getTauxMutuelle());
        patient.setAllergiesConnues(request.getAllergiesConnues());

        return toResponse(patientRepository.save(patient));
    }

    // ================================================
    // RECHERCHER des patients
    // ================================================
    public List<PatientResponse> rechercherPatients(String search) {
        if (search == null || search.isBlank()) {
            return patientRepository.findAll()
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }
        return patientRepository.rechercherPatients(search)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ================================================
    // OBTENIR un patient par ID
    // ================================================
    public PatientResponse getPatientById(Long id) {
        return toResponse(
                patientRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException(
                                "Patient non trouvé : " + id
                        ))
        );
    }

    // ================================================
    // CONVERSION Entité → DTO
    // ================================================
    private PatientResponse toResponse(Patient p) {
        return PatientResponse.builder()
                .id(p.getId())
                .nom(p.getNom())
                .prenom(p.getPrenom())
                .dateNaissance(p.getDateNaissance())
                .telephone(p.getTelephone())
                .numSecuSociale(p.getNumSecuSociale())
                .mutuelle(p.getMutuelle())
                .tauxMutuelle(p.getTauxMutuelle())
                .allergiesConnues(p.getAllergiesConnues())
                .creeLe(p.getCreeLe())
                .build();
    }
}