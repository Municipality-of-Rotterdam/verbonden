-- Een dossier kan ook door een medewerker worden aangemaakt, aan de balie of in een videogesprek.
ALTER TABLE huwelijksdossiers
    ADD COLUMN kanaal          VARCHAR(20) NOT NULL DEFAULT 'ONLINE',
    ADD COLUMN aangemaakt_door VARCHAR(255);

ALTER TABLE huwelijksdossiers
    ADD CONSTRAINT huwelijksdossiers_kanaal_check
        CHECK (kanaal IN ('ONLINE', 'BALIE', 'VIDEO'));

-- Partner 2 heeft niet altijd een BSN, maar wel een persoonsnummer uit het buitenlandse paspoort. Diens
-- persoonsgegevens komen dan niet uit de BRP, maar worden door de medewerker ingevoerd.
ALTER TABLE huwelijksdossiers_partners
    ALTER COLUMN bsn DROP NOT NULL,
    ADD COLUMN buitenlands_persoonsnummer    VARCHAR(50),
    ADD COLUMN achternaam                    VARCHAR(255),
    ADD COLUMN voornamen                     VARCHAR(255),
    ADD COLUMN geboortedatum                 DATE,
    ADD COLUMN geboorteplaats                VARCHAR(255),
    ADD COLUMN nationaliteit                 VARCHAR(255),
    ADD COLUMN burgerlijke_staat             VARCHAR(255),
    ADD COLUMN identiteit_gecontroleerd_door VARCHAR(255),
    ADD COLUMN identiteit_gecontroleerd_op   TIMESTAMP;

ALTER TABLE huwelijksdossiers_partners
    ADD CONSTRAINT huwelijksdossiers_partners_identificatie_check
        CHECK (bsn IS NOT NULL OR buitenlands_persoonsnummer IS NOT NULL),
    ADD CONSTRAINT huwelijksdossiers_partners_partner1_bsn_check
        CHECK (volgorde <> 1 OR bsn IS NOT NULL);

-- Bestanden die een medewerker aan een dossier toevoegt
CREATE TABLE dossier_bestanden
(
    id             BIGSERIAL PRIMARY KEY,
    dossier_id     BIGINT       NOT NULL REFERENCES huwelijksdossiers (id) ON DELETE CASCADE,
    bestandsnaam   VARCHAR(255) NOT NULL,
    bestand_type   VARCHAR(20)  NOT NULL,
    grootte        BIGINT       NOT NULL,
    inhoud         BYTEA        NOT NULL,
    toegevoegd_door VARCHAR(255) NOT NULL,
    toegevoegd_op  TIMESTAMP    NOT NULL
);

CREATE INDEX dossier_bestanden_dossier_id_idx ON dossier_bestanden (dossier_id);

-- AVG: welke medewerker heeft wanneer persoonsgegevens in een dossier ingezien. Bewust zonder foreign key,
-- zodat de inzage ook na het verwijderen van een dossier te herleiden blijft.
CREATE TABLE dossier_inzage_log
(
    id           BIGSERIAL PRIMARY KEY,
    dossier_uuid UUID         NOT NULL,
    medewerker   VARCHAR(255) NOT NULL,
    actie        VARCHAR(30)  NOT NULL,
    tijdstip     TIMESTAMP    NOT NULL
);

CREATE INDEX dossier_inzage_log_dossier_uuid_idx ON dossier_inzage_log (dossier_uuid);
CREATE INDEX dossier_inzage_log_medewerker_idx ON dossier_inzage_log (medewerker);
