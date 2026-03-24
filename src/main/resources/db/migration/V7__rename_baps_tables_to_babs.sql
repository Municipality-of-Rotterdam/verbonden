ALTER TABLE baps RENAME TO babs;
ALTER TABLE baps_beschikbare_dagen RENAME TO babs_beschikbare_dagen;
ALTER TABLE babs_beschikbare_dagen RENAME COLUMN baps_id TO babs_id;
