package nl.rotterdam.verbonden.core.features.trouwlijst_administration.domain;

import java.io.Serializable;

/**
 * @param emailadres     {@code null} wanneer onbekend
 * @param telefoonnummer {@code null} wanneer onbekend
 */
public record TrouwlijstPartnerDto(String naam, String emailadres, String telefoonnummer) implements Serializable {
}
