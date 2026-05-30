-- V2: Ajouter colonne patient_nom pour les clients non enregistrés
ALTER TABLE ventes ADD COLUMN vente_patient_nom VARCHAR(100);