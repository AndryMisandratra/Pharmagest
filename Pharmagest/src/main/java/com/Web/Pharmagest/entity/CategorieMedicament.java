package com.Web.Pharmagest.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories_medicament")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorieMedicament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "categorie_id")
    private Long id;

    // Code court ex: ANALG, ANTIB
    @Column(name = "categorie_code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "categorie_libelle", nullable = false, length = 100)
    private String libelle;

    // TEXT en SQL = pas de limite de taille
    @Column(name = "categorie_description", columnDefinition = "TEXT")
    private String description;
}