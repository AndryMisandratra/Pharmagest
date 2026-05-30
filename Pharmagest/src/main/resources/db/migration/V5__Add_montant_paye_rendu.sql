-- Ajout des colonnes montantPaye et montantRendu pour le paiement en espèces
ALTER TABLE ventes ADD COLUMN IF NOT EXISTS vente_montant_paye DECIMAL(10,2);
ALTER TABLE ventes ADD COLUMN IF NOT EXISTS vente_montant_rendu DECIMAL(10,2);

-- Mettre à jour les valeurs NULL avec 0 pour les anciennes ventes
UPDATE ventes SET vente_montant_paye = 0 WHERE vente_montant_paye IS NULL;
UPDATE ventes SET vente_montant_rendu = 0 WHERE vente_montant_rendu IS NULL;

-- Ajouter la contrainte NOT NULL après
ALTER TABLE ventes ALTER COLUMN vente_montant_paye SET NOT NULL;
ALTER TABLE ventes ALTER COLUMN vente_montant_rendu SET NOT NULL;
