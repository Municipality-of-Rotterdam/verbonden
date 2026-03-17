package nl.rotterdam.huwelijk.features.location_administration.domain;

public record CreateLocatieDto(
        String naam,
        String fotoUrl,
        String omschrijving,
        String detailUrl
) {
}
