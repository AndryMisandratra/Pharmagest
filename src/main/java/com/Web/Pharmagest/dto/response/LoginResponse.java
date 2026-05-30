package com.Web.Pharmagest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    // Le token JWT que React stockera (localStorage ou cookie)
    private String token;

    // Type du token (toujours "Bearer" pour JWT)
    private String tokenType;

    // Durée de validité en millisecondes (24h = 86400000)
    private Long expiresIn;

    // Infos de l'utilisateur connecté (pour afficher dans l'interface)
    private String email;
    private String nom;
    private String prenom;
    private String role;
}