-- V4: Fix RECUE_PARTIELLEMENT status in database
-- Update any existing RECUE_PARTIELLEMENT to CLOTUREE
UPDATE commandes_fournisseur 
SET commande_statut = 'CLOTUREE' 
WHERE commande_statut = 'RECUE_PARTIELLEMENT';
