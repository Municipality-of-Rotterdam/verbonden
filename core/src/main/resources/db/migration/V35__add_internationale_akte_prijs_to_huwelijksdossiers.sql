-- De prijs van de internationale huwelijksakte wordt bij het indienen vastgelegd, omdat het tarief afhangt
-- van de datum van indienen.
ALTER TABLE huwelijksdossiers
    ADD COLUMN internationale_akte_prijs NUMERIC(10, 2);
