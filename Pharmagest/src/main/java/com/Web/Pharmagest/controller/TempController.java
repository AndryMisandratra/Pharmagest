package com.Web.Pharmagest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/temp")
@RequiredArgsConstructor
public class TempController {

    private final PasswordEncoder passwordEncoder;

    // Endpoint temporaire pour générer un hash BCrypt
    // À SUPPRIMER après utilisation !
    @GetMapping("/hash")
    public String generateHash(@RequestParam String password) {
        return passwordEncoder.encode(password);
    }
}