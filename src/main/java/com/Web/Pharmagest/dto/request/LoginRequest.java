package com.Web.Pharmagest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data  // génère getters/setters
public class LoginRequest {

    // @NotBlank = ne peut pas être null, vide ou que des espaces
    // @Email = doit respecter le format email (ex: user@gmail.com)
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;
}