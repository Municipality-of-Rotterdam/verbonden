package nl.rotterdam.huwelijk.features.location_administration.domain;

import java.util.List;

public record NietBeschikbareDagImportResult(
        int geimporteerd,
        int overgeslagen,
        int fouten,
        List<String> meldingen
) {
}
