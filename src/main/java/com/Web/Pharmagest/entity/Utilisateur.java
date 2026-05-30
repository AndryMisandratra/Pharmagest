package com.Web.Pharmagest.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateurs")
@Data                    // génère getters, setters, toString, equals, hashCode
@Builder                 // permet : Utilisateur.builder().nom("X").build()
@NoArgsConstructor       // constructeur vide (requis par JPA)
@AllArgsConstructor      // constructeur avec tous les champs
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // correspond à BIGSERIAL
    @Column(name = "utilisateur_id")
    private Long id;

    @Column(name = "utilisateur_nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "utilisateur_prenom", nullable = false, length = 100)
    private String prenom;

    // unique = true → correspond à UNIQUE dans le SQL
    @Column(name = "utilisateur_email", nullable = false, unique = true, length = 150)
    private String email;

    // Le mot de passe ne sera JAMAIS stocké en clair
    // BCrypt le hashera avant insertion
    @Column(name = "utilisateur_password_hash", nullable = false)
    private String passwordHash;

    // On stocke le rôle comme String en BDD
    // EnumType.STRING = stocke "ROLE_PHARMACIEN" (pas un chiffre)
    @Enumerated(EnumType.STRING)
    @Column(name = "utilisateur_role", nullable = false, length = 30)
    private Role role;

    @Column(name = "utilisateur_est_actif", nullable = false)
    @Builder.Default  // valeur par défaut quand on utilise le Builder
    private Boolean estActif = true;

    @Column(name = "utilisateur_cree_le", nullable = false, updatable = false)
    private LocalDateTime creeLe;

    @Column(name = "utilisateur_modifie_le")
    private LocalDateTime modifieLe;

    // Rempli automatiquement avant chaque INSERT
    @PrePersist
    protected void onCreate() {
        creeLe = LocalDateTime.now();
    }

    // Rempli automatiquement avant chaque UPDATE
    @PreUpdate
    protected void onUpdate() {
        modifieLe = LocalDateTime.now();
    }

    // Enum des 4 rôles définis dans le CDC
    public enum Role {
        ROLE_PHARMACIEN,
        ROLE_PREPARATEUR,
        ROLE_CAISSIER,
        ROLE_ADMIN
    }
}