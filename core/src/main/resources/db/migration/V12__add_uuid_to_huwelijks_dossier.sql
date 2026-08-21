CREATE EXTENSION IF NOT EXISTS pgcrypto;
ALTER TABLE huwelijks_dossier ADD COLUMN uuid UUID NOT NULL DEFAULT gen_random_uuid();
ALTER TABLE huwelijks_dossier ADD CONSTRAINT uq_huwelijks_dossier_uuid UNIQUE (uuid);
