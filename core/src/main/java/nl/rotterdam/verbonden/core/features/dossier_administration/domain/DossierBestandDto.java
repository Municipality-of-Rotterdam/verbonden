package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

/**
 * Een bestand met inhoud, om te downloaden.
 */
public record DossierBestandDto(
        String bestandsnaam,
        DossierBestandType bestandType,
        byte[] inhoud
) {
}
