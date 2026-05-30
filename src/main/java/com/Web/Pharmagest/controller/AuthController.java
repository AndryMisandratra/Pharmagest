package com.Web.Pharmagest.controller;

import com.Web.Pharmagest.dto.request.LoginRequest;
import com.Web.Pharmagest.dto.response.LoginResponse;
import com.Web.Pharmagest.dto.response.UtilisateurResponse;
import com.Web.Pharmagest.repository.UtilisateurRepository;
import com.Web.Pharmagest.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UtilisateurRepository utilisateurRepository;

    // ================================================
    // POST /api/auth/login
    // PUBLIC : pas besoin de token pour accéder
    // Body : { "email": "...", "password": "..." }
    // ================================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {

        try {
            // 1. Spring Security vérifie email + mot de passe
            //    → appelle UserDetailsServiceImpl.loadUserByUsername()
            //    → compare le mot de passe avec BCrypt
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            // Email ou mot de passe incorrect
            return ResponseEntity
                    .status(401)
                    .body("Email ou mot de passe incorrect");
        }

        // 2. Authentification réussie → charge l'utilisateur depuis la BDD
        var utilisateur = utilisateurRepository
                .findByEmail(request.getEmail())
                .orElseThrow();

        // 3. Génère le token JWT
        //    On crée un UserDetails temporaire pour JwtService
        var userDetails = org.springframework.security.core.userdetails.User
                .builder()
                .username(utilisateur.getEmail())
                .password(utilisateur.getPasswordHash())
                .authorities(utilisateur.getRole().name())
                .build();

        String token = jwtService.generateToken(userDetails);

        // 4. Retourne le token + infos utilisateur
        return ResponseEntity.ok(
                LoginResponse.builder()
                        .token(token)
                        .tokenType("Bearer")
                        .expiresIn(86400000L) // 24h en ms
                        .email(utilisateur.getEmail())
                        .nom(utilisateur.getNom())
                        .prenom(utilisateur.getPrenom())
                        .role(utilisateur.getRole().name())
                        .build()
        );
    }

    // ================================================
    // GET /api/auth/me
    // PROTÉGÉ : nécessite un token JWT valide
    // Retourne les infos de l'utilisateur connecté
    // ================================================
    @GetMapping("/me")
    public ResponseEntity<UtilisateurResponse> me(
            // @AuthenticationPrincipal = l'utilisateur extrait du token JWT
            @AuthenticationPrincipal UserDetails userDetails) {

        var utilisateur = utilisateurRepository
                .findByEmail(userDetails.getUsername())
                .orElseThrow();

        return ResponseEntity.ok(
                UtilisateurResponse.builder()
                        .id(utilisateur.getId())
                        .nom(utilisateur.getNom())
                        .prenom(utilisateur.getPrenom())
                        .email(utilisateur.getEmail())
                        .role(utilisateur.getRole().name())
                        .estActif(utilisateur.getEstActif())
                        .creeLe(utilisateur.getCreeLe())
                        .build()
        );
    }
}