-- De afspraakplanning zoekt afspraken en niet-beschikbare dagen op per locatie en datum(bereik)
CREATE INDEX idx_afspraken_locatie_datum ON afspraken (locatie_id, datum);
CREATE INDEX idx_trouwlocaties_niet_beschikbare_dagen_locatie_datum
    ON trouwlocaties_niet_beschikbare_dagen (locatie_id, datum);
