package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import nl.rotterdam.verbonden.core.domain.Bedrag;

import java.io.Serializable;

/**
 * Wat het paar heeft betaald en nog moet betalen, voor de statuspagina.
 *
 * @param betaald       som van alle gelukte betalingen: de aanvraag en betaalde betaalmutaties
 * @param open          som van de betaalmutaties die nog niet betaald zijn
 * @param inBehandeling het deel van {@code open} waarvan de bank de uitslag nog moet melden
 * @param nietGelukt    het deel van {@code open} waarvan de laatste betaalpoging niet is gelukt
 */
public record BetaaloverzichtDto(Bedrag betaald, Bedrag open, Bedrag inBehandeling, Bedrag nietGelukt)
        implements Serializable {

    public enum Stand {
        /** Nog nooit iets betaald en niets te betalen, bijvoorbeeld een gratis dossier. */
        GEEN,
        BETAALD,
        OPEN,
        NIET_GELUKT,
        IN_BEHANDELING
    }

    /** Wat er voor de burger het belangrijkst is om te weten. */
    public Stand stand() {
        if (inBehandeling.isPositive()) {
            return Stand.IN_BEHANDELING;
        }
        if (nietGelukt.isPositive()) {
            return Stand.NIET_GELUKT;
        }
        if (open.isPositive()) {
            return Stand.OPEN;
        }
        return betaald.isPositive() ? Stand.BETAALD : Stand.GEEN;
    }
}
