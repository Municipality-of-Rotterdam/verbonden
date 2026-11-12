-- Een betaalmutatie die Verbonden zelf aanmaakt omdat een ingediend dossier duurder werd (prijswijziging), hoort bij
-- de totaalprijs van het dossier. Een handmatige betaalmutatie komt bovenop de totaalprijs. Het verschil telt mee bij
-- de controle of er meer betaald is dan het dossier kost.
ALTER TABLE betaalmutaties
    ADD COLUMN prijswijziging BOOLEAN NOT NULL DEFAULT FALSE;
