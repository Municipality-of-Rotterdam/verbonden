package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

public record CreateDossierBestandDto(
        String bestandsnaam,
        DossierBijlageType bijlageType,
        String opmerking,
        byte[] inhoud
) {
}
