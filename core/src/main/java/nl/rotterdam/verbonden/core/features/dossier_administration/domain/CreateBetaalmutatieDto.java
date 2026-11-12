package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

import nl.rotterdam.verbonden.core.domain.Bedrag;

/**
 * @param omschrijving wat de burger ziet bij "Te betalen", bijvoorbeeld "Trouwboekje Luxe (bijbesteld)"
 * @param bedrag       groter dan nul
 */
public record CreateBetaalmutatieDto(String omschrijving, Bedrag bedrag) {

    public static final int MAX_LENGTE_OMSCHRIJVING = 100;
}
