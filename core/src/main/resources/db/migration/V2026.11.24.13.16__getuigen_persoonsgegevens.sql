ALTER TABLE huwelijksdossiers_getuigen
    ADD COLUMN voornamen     VARCHAR(255),
    ADD COLUMN achternaam    VARCHAR(255),
    ADD COLUMN geboortedatum DATE,
    ADD COLUMN postcode      VARCHAR(10),
    ADD COLUMN woonplaats    VARCHAR(255),
    ADD COLUMN land          VARCHAR(255);
