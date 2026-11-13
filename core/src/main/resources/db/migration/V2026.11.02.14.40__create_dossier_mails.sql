-- E-mails die voor een dossier zijn verstuurd (of moesten worden verstuurd), met elke verzendpoging.
CREATE TABLE dossier_mails
(
    id                BIGSERIAL PRIMARY KEY,
    version           BIGINT       NOT NULL DEFAULT 0,
    dossier_id        BIGINT       NOT NULL REFERENCES huwelijksdossiers (id) ON DELETE CASCADE,
    type              VARCHAR(40)  NOT NULL,
    afzender          VARCHAR(512) NOT NULL,
    ontvangers        TEXT         NOT NULL,
    onderwerp         VARCHAR(255) NOT NULL,
    inhoud            TEXT         NOT NULL,
    status            VARCHAR(20)  NOT NULL,
    aangemaakt_op     TIMESTAMP    NOT NULL,
    volgende_poging_op TIMESTAMP,
    CONSTRAINT dossier_mails_status_check CHECK (status IN ('IN_WACHT', 'VERZONDEN', 'MISLUKT'))
);

CREATE INDEX dossier_mails_dossier_id_idx ON dossier_mails (dossier_id);
CREATE INDEX dossier_mails_openstaand_idx ON dossier_mails (volgende_poging_op) WHERE status = 'IN_WACHT';

CREATE TABLE dossier_mail_pogingen
(
    id         BIGSERIAL PRIMARY KEY,
    mail_id    BIGINT      NOT NULL REFERENCES dossier_mails (id) ON DELETE CASCADE,
    volgnummer INT         NOT NULL,
    tijdstip   TIMESTAMP   NOT NULL,
    status     VARCHAR(20) NOT NULL,
    foutmelding TEXT,
    CONSTRAINT dossier_mail_pogingen_status_check CHECK (status IN ('GESLAAGD', 'MISLUKT')),
    CONSTRAINT dossier_mail_pogingen_volgnummer_uq UNIQUE (mail_id, volgnummer)
);
