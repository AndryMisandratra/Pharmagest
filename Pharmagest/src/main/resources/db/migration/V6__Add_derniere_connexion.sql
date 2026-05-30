-- Ajout de la colonne derniere_connexion pour le suivi de la première connexion journalière
ALTER TABLE utilisateurs ADD COLUMN IF NOT EXISTS utilisateur_derniere_connexion TIMESTAMP;
