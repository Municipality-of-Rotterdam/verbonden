-- Een dossier kan door de backoffice worden geannuleerd, of vervallen doordat de burger een uitnodiging voor een
-- ander dossier accepteert. Het blijft dan bewaard (bewaartermijn), maar telt niet meer als het dossier van de burger.
ALTER TABLE huwelijksdossiers
    DROP CONSTRAINT huwelijksdossiers_status_check;

ALTER TABLE huwelijksdossiers
    ADD CONSTRAINT huwelijksdossiers_status_check
        CHECK (status IN ('CONCEPT', 'INGEDIEND', 'GEACCEPTEERD', 'AFGEWEZEN', 'GEANNULEERD'));
