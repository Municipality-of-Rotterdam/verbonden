package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

/**
 * Stand van één betaalpoging.
 */
public enum BetalingStatus {

    /** De burger is naar de betaalprovider gestuurd; de uitslag is nog niet binnen. */
    GESTART,

    GELUKT,

    /** Mislukt, afgebroken door de burger, of nooit afgerond. */
    MISLUKT
}
