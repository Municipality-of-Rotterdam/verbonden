-- De internationale huwelijksakte is geen keuze uit een beheerde lijst meer, maar een vaste optie
-- bij een huwelijk. De tabel extras bevat daarna alleen nog trouwboekjes.

ALTER TABLE huwelijksdossiers
    ADD COLUMN internationale_akte BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE huwelijksdossiers
SET internationale_akte = TRUE
WHERE internationale_akte_id IS NOT NULL;

ALTER TABLE huwelijksdossiers
    DROP COLUMN internationale_akte_id;

DELETE FROM extras
WHERE type <> 'trouwboekje';

ALTER TABLE extras
    DROP CONSTRAINT extras_type_check;

ALTER TABLE extras
    DROP COLUMN type;

ALTER TABLE extras RENAME TO trouwboekjes;
ALTER SEQUENCE extras_id_seq RENAME TO trouwboekjes_id_seq;
ALTER INDEX extras_pkey RENAME TO trouwboekjes_pkey;
