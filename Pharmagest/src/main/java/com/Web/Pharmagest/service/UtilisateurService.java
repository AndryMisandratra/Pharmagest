package com.Web.Pharmagest.service;

import com.Web.Pharmagest.dto.request.UtilisateurRequest;
import com.Web.Pharmagest.dto.response.UtilisateurResponse;
import com.Web.Pharmagest.entity.Utilisateur;
import com.Web.Pharmagest.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    // ================================================
    // CRÉER un utilisateur
    // PHARMACIEN seulement
    // ================================================
    @Transactional
    public UtilisateurResponse creerUtilisateur(
            UtilisateurRequest request) {

        // Vérifier que l'email n'existe pas déjà
        if (utilisateurRepository.existsByEmail(
                request.getEmail())) {
            throw new RuntimeException(
                    "Email déjà utilisé : " + request.getEmail()
            );
        }

        // Valider le rôle
        Utilisateur.Role role;
        try {
            role = Utilisateur.Role.valueOf(request.getRole());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "Rôle invalide : " + request.getRole()
                            + " — Valeurs acceptées : "
                            + "ROLE_PHARMACIEN, ROLE_PREPARATEUR, "
                            + "ROLE_CAISSIER, ROLE_ADMIN"
            );
        }

        var utilisateur = Utilisateur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                // Hashage BCrypt du mot de passe
                .passwordHash(passwordEncoder.encode(
                        request.getPassword()))
                .role(role)
                .estActif(true)
                .build();

        return toResponse(
                utilisateurRepository.save(utilisateur));
    }

    // ================================================
    // MODIFIER un utilisateur
    // ================================================
    @Transactional
    public UtilisateurResponse modifierUtilisateur(
            Long id, UtilisateurRequest request) {

        var utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Utilisateur non trouvé : " + id
                ));

        // Vérifier email si changé
        if (!utilisateur.getEmail().equals(request.getEmail())
                && utilisateurRepository.existsByEmail(
                request.getEmail())) {
            throw new RuntimeException(
                    "Email déjà utilisé : " + request.getEmail()
            );
        }

        Utilisateur.Role role;
        try {
            role = Utilisateur.Role.valueOf(request.getRole());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "Rôle invalide : " + request.getRole()
            );
        }

        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setRole(role);

        // Ne rehasher le mot de passe que s'il a changé
        if (request.getPassword() != null
                && !request.getPassword().isBlank()) {
            utilisateur.setPasswordHash(
                    passwordEncoder.encode(request.getPassword())
            );
        }

        return toResponse(
                utilisateurRepository.save(utilisateur));
    }

    // ================================================
    // ACTIVER / DÉSACTIVER un compte
    // ================================================
    @Transactional
    public UtilisateurResponse changerStatutActif(
            Long id, Boolean estActif) {

        var utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Utilisateur non trouvé : " + id
                ));

        utilisateur.setEstActif(estActif);
        return toResponse(
                utilisateurRepository.save(utilisateur));
    }

    // ================================================
    // LISTER tous les utilisateurs
    // ================================================
    public List<UtilisateurResponse> listerUtilisateurs() {
        return utilisateurRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ================================================
    // OBTENIR un utilisateur par ID
    // ================================================
    public UtilisateurResponse getUtilisateurById(Long id) {
        return toResponse(
                utilisateurRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException(
                                "Utilisateur non trouvé : " + id
                        ))
        );
    }

    // ================================================
    // CONVERSION Entité → DTO
    // ⚠️ On ne retourne JAMAIS le passwordHash !
    // ================================================
    private UtilisateurResponse toResponse(Utilisateur u) {
        return UtilisateurResponse.builder()
                .id(u.getId())
                .nom(u.getNom())
                .prenom(u.getPrenom())
                .email(u.getEmail())
                .role(u.getRole().name())
                .estActif(u.getEstActif())
                .creeLe(u.getCreeLe())
                .build();
    }
}