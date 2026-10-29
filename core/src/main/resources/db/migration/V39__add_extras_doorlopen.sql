-- Extra's zijn optioneel. Of de burger de stap Extra's heeft doorlopen, wordt per dossier bewaard, zodat de voortgang
-- in de sidebar ook voor de partner en na opnieuw inloggen klopt.
ALTER TABLE huwelijksdossiers
    ADD COLUMN extras_doorlopen BOOLEAN NOT NULL DEFAULT FALSE;
