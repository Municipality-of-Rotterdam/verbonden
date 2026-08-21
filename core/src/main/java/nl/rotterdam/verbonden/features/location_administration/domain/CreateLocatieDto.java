package nl.rotterdam.verbonden.features.location_administration.domain;

public record CreateLocatieDto(
        String naam,
        String fotoUrl,
        String omschrijving,
        String detailUrl
) {
}
