package com.Web.Pharmagest.service;

import com.Web.Pharmagest.dto.request.FournisseurRequest;
import com.Web.Pharmagest.dto.response.FournisseurResponse;
import com.Web.Pharmagest.entity.Fournisseur;
import com.Web.Pharmagest.repository.FournisseurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FournisseurService {

    private final FournisseurRepository fournisseurRepository;

    // ================================================
    // CRÉER un fournisseur
    // ================================================
    @Transactional
    public FournisseurResponse creerFournisseur(
            FournisseurRequest request) {

        var fournisseur = Fournisseur.builder()
                .raisonSociale(request.getRaisonSociale())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .adresse(request.getAdresse())
                .delaiLivraisonJours(
                        request.getDelaiLivraisonJours() != null
                                ? request.getDelaiLivraisonJours() : 3)
                .build();

        return toResponse(fournisseurRepository.save(fournisseur));
    }

    // ================================================
    // MODIFIER un fournisseur
    // ================================================
    @Transactional
    public FournisseurResponse modifierFournisseur(
            Long id, FournisseurRequest request) {

        var fournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Fournisseur non trouvé : " + id
                ));

        fournisseur.setRaisonSociale(request.getRaisonSociale());
        fournisseur.setEmail(request.getEmail());
        fournisseur.setTelephone(request.getTelephone());
        fournisseur.setAdresse(request.getAdresse());
        fournisseur.setDelaiLivraisonJours(
                request.getDelaiLivraisonJours());

        return toResponse(fournisseurRepository.save(fournisseur));
    }

    // ================================================
    // LISTER tous les fournisseurs
    // ================================================
    public List<FournisseurResponse> listerFournisseurs() {
        return fournisseurRepository.findByEstActifTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ================================================
    // OBTENIR un fournisseur par ID
    // ================================================
    public FournisseurResponse getFournisseurById(Long id) {
        return toResponse(
                fournisseurRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException(
                                "Fournisseur non trouvé : " + id
                        ))
        );
    }

    // ================================================
    // CONVERSION Entité → DTO
    // ================================================
    private FournisseurResponse toResponse(Fournisseur f) {
        return FournisseurResponse.builder()
                .id(f.getId())
                .raisonSociale(f.getRaisonSociale())
                .email(f.getEmail())
                .telephone(f.getTelephone())
                .adresse(f.getAdresse())
                .delaiLivraisonJours(f.getDelaiLivraisonJours())
                .estActif(f.getEstActif())
                .creeLe(f.getCreeLe())
                .build();
    }
}