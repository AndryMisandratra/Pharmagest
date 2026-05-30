-- V3: Modifier patient_id pour permettre null dans ordonnances
ALTER TABLE ordonnances ALTER COLUMN patient_id DROP NOT NULL;