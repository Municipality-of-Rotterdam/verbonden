package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

/**
 * Wat een medewerker in een dossier heeft ingezien; vastgelegd in het inzagelog (AVG).
 */
public enum InzageActie {

    /** De gegevens van het dossier, inclusief persoonsgegevens van de partners, bekeken. */
    DOSSIER_BEKEKEN,

    /** Een aan het dossier toegevoegd bestand gedownload. */
    BESTAND_GEDOWNLOAD,

    /** De inhoud van een aan de partners verstuurde e-mail bekeken. */
    EMAIL_BEKEKEN
}
