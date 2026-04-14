-- Tabelnamen in het Nederlands en meervoud
ALTER TABLE babs RENAME TO babsen;
ALTER TABLE babs_beschikbare_dagen RENAME TO babsen_beschikbare_dagen;
ALTER TABLE trouwlocatie RENAME TO trouwlocaties;
ALTER TABLE locatie_beschikbaarheid RENAME TO trouwlocaties_beschikbaarheden;
ALTER TABLE locatie_niet_beschikbare_dag RENAME TO trouwlocaties_niet_beschikbare_dagen;
ALTER TABLE huwelijks_dossier RENAME TO huwelijksdossiers;
ALTER TABLE marriage_type RENAME TO huwelijkstypen;
ALTER TABLE marriage_type_location RENAME TO huwelijkstypen_locaties;
