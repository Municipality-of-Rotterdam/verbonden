ALTER TABLE huwelijks_dossier
    ADD COLUMN locatie_id BIGINT REFERENCES trouwlocatie (id);
