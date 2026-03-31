package com.Web.Pharmagest.controller;

import com.Web.Pharmagest.entity.CategorieMedicament;
import com.Web.Pharmagest.repository.CategorieMedicamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategorieMedicamentController {

    private final CategorieMedicamentRepository
            categorieRepository;

    // GET /api/categories — tous les rôles
    @GetMapping
    public ResponseEntity<List<CategorieMedicament>> getAll() {
        return ResponseEntity.ok(
                categorieRepository.findAll());
    }

    // POST /api/categories — PHARMACIEN seulement
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_PHARMACIEN')")
    public ResponseEntity<CategorieMedicament> create(
            @RequestBody CategorieMedicament categorie) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categorieRepository.save(categorie));
    }
}