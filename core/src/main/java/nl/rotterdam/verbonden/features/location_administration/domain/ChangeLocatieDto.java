package nl.rotterdam.verbonden.features.location_administration.domain;

public record ChangeLocatieDto(
        long id,
        String naam,
        String fotoUrl,
        String omschrijving,
        String detailUrl
) {
}
