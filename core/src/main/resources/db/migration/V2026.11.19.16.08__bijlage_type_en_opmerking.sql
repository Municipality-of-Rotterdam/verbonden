ALTER TABLE dossier_bestanden
    ADD COLUMN bijlage_type VARCHAR(50) NOT NULL DEFAULT 'OVERIGE',
    ADD COLUMN opmerking VARCHAR(1000);
