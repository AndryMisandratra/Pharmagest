package com.Web.Pharmagest.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ReceptionCommandeRequest {

    @NotEmpty(message = "Les lignes de réception sont obligatoires")
    private List<LigneReceptionRequest> lignes;

    // Détail d'une ligne reçue
    @Data
    public static class LigneReceptionRequest {

        @NotNull(message = "La ligne de commande est obligatoire")
        private Long ligneCommandeId;

        @NotNull(message = "La quantité reçue est obligatoire")
        @Min(value = 0, message = "La quantité ne peut pas être négative")
        private Integer quantiteRecue;

        // Saisi à la réception physique
        @NotBlank(message = "Le numéro de lot est obligatoire")
        private String lotNumero;

        @NotNull(message = "La date de péremption est obligatoire")
        @Future(message = "La date de péremption doit être dans le futur")
        private LocalDate datePeremption;

        private BigDecimal prixAchatHt;
    }
}