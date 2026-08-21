CREATE TABLE huwelijksdossiers_partners
(
    id                BIGSERIAL PRIMARY KEY,
    dossier_id        BIGINT       NOT NULL REFERENCES huwelijksdossiers (id),
    volgorde          INTEGER      NOT NULL,
    bsn               VARCHAR(10)  NOT NULL,
    gekozen_achternaam VARCHAR(255),
    telefoonnummer    VARCHAR(50),
    emailadres        VARCHAR(255)
);

INSERT INTO huwelijksdossiers_partners (dossier_id, volgorde, bsn, gekozen_achternaam, telefoonnummer, emailadres)
SELECT id, 1, bsn1, gekozen_achternaam_bsn1, telefoonnummer_bsn1, emailadres_bsn1
FROM huwelijksdossiers
WHERE bsn1 IS NOT NULL;

INSERT INTO huwelijksdossiers_partners (dossier_id, volgorde, bsn, gekozen_achternaam, telefoonnummer, emailadres)
SELECT id, 2, bsn2, gekozen_achternaam_bsn2, telefoonnummer_bsn2, emailadres_bsn2
FROM huwelijksdossiers
WHERE bsn2 IS NOT NULL;

ALTER TABLE huwelijksdossiers
    DROP COLUMN bsn1,
    DROP COLUMN bsn2,
    DROP COLUMN gekozen_achternaam_bsn1,
    DROP COLUMN gekozen_achternaam_bsn2,
    DROP COLUMN telefoonnummer_bsn1,
    DROP COLUMN emailadres_bsn1,
    DROP COLUMN telefoonnummer_bsn2,
    DROP COLUMN emailadres_bsn2;
