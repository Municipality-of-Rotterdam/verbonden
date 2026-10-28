package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import nl.rotterdam.verbonden.core.domain.Bedrag;

import java.io.Serializable;

public record SidebarExtraItemDto(String naam, Bedrag prijs) implements Serializable {
}
