-- Vervang de vrije-tekst kolom beschikbare_dagen door een genormaliseerde koppeltabel.
-- De @ElementCollection op BapsEntity slaat elke dag op als een aparte rij.

ALTER TABLE baps
    DROP COLUMN IF EXISTS beschikbare_dagen;

CREATE TABLE baps_beschikbare_dagen (
    baps_id BIGINT      NOT NULL REFERENCES baps (id),
    dag     VARCHAR(50) NOT NULL
);
