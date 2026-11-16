package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

/**
 * Wat er moet gebeuren nadat de burger op "Betalen" heeft geklikt.
 */
public sealed interface BetalingStartResultaat {

    /** Stuur de browser naar de betaalprovider. */
    record NaarBetaalprovider(String redirectUrl) implements BetalingStartResultaat {
    }

    /** Er hoeft niets betaald te worden; het dossier is meteen ingediend. */
    record IngediendZonderBetaling() implements BetalingStartResultaat {
    }
}
