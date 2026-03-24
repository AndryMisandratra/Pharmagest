package com.Web.Pharmagest.controller;

import com.Web.Pharmagest.dto.request.PatientRequest;
import com.Web.Pharmagest.dto.response.PatientResponse;
import com.Web.Pharmagest.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    // ================================================
    // GET /api/patients?search=
    // PREPARATEUR ou PHARMACIEN
    // ================================================
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_PREPARATEUR','ROLE_PHARMACIEN')")
    public ResponseEntity<List<PatientResponse>> getPatients(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(
                patientService.rechercherPatients(search));
    }

    // ================================================
    // GET /api/patients/{id}
    // ================================================
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_PREPARATEUR','ROLE_PHARMACIEN')")
    public ResponseEntity<PatientResponse> getPatient(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                patientService.getPatientById(id));
    }

    // ================================================
    // POST /api/patients
    // ================================================
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_PREPARATEUR','ROLE_PHARMACIEN')")
    public ResponseEntity<PatientResponse> creerPatient(
            @Valid @RequestBody PatientRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(patientService.creerPatient(request));
    }

    // ================================================
    // PUT /api/patients/{id}
    // ================================================
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_PREPARATEUR','ROLE_PHARMACIEN')")
    public ResponseEntity<PatientResponse> modifierPatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequest request) {
        return ResponseEntity.ok(
                patientService.modifierPatient(id, request));
    }


}