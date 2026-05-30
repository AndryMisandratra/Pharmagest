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

    @Data
    public static class LigneReceptionRequest {

        @NotNull(message = "La ligne de commande est obligatoire")
        private Long ligneCommandeId;

        @NotNull(message = "La date de péremption est obligatoire")
        @Future(message = "La date de péremption doit être dans le futur")
        private LocalDate datePeremption;

        private LocalDate dateFabrication;

        private BigDecimal prixAchatHt;
    }
}