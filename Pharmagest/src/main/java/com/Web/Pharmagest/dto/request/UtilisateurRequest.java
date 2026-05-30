package com.Web.Pharmagest.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UtilisateurRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format email invalide")
    @Pattern(regexp = "^[^\\s@]+@(gmail\\.com|googlemail\\.com)$",
             message = "L'email doit être une adresse Gmail valide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8,
            message = "Le mot de passe doit faire au moins 8 caractères")
    private String password;

    @NotBlank(message = "Le rôle est obligatoire")
    private String role;
}