ALTER TABLE extras
    ADD CONSTRAINT extras_type_check
        CHECK (type IN ('trouwboekje', 'internationaleAkte'));
