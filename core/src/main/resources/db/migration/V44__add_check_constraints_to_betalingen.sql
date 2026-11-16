-- Alleen bekende waarden van de Java-enums BetaalSoort en BetalingStatus; net als bij huwelijksdossiers.status (V37)
-- een CHECK-constraint in plaats van een Postgres-ENUM, zodat een waarde later toevoegen alleen de constraint
-- vervangen is.
ALTER TABLE betalingen
    ADD CONSTRAINT betalingen_soort_check
        CHECK (soort IN ('REGISTRATIE', 'MUTATIE'));

ALTER TABLE betalingen
    ADD CONSTRAINT betalingen_status_check
        CHECK (status IN ('GESTART', 'GELUKT', 'MISLUKT'));
