-- De burger geeft de taal van de ceremonie en het aantal bezoekers op. Beide zijn verplicht voor het indienen,
-- maar bestaande dossiers en concepten hebben ze nog niet.
ALTER TABLE huwelijksdossiers
    ADD COLUMN taal VARCHAR(20),
    ADD COLUMN aantal_bezoekers INTEGER;
