package com.Web.Pharmagest.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ModePaiementConverter implements AttributeConverter<Vente.ModePaiement, String> {

    @Override
    public String convertToDatabaseColumn(Vente.ModePaiement attribute) {
        return attribute != null ? attribute.name() : Vente.ModePaiement.ESPECES.name();
    }

    @Override
    public Vente.ModePaiement convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return Vente.ModePaiement.ESPECES;
        }

        try {
            return Vente.ModePaiement.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            return Vente.ModePaiement.ESPECES;
        }
    }
}