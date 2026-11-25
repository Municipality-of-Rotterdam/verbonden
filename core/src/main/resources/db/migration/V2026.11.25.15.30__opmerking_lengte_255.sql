UPDATE dossier_bestanden SET opmerking = LEFT(opmerking, 255) WHERE LENGTH(opmerking) > 255;
ALTER TABLE dossier_bestanden ALTER COLUMN opmerking TYPE VARCHAR(255);
