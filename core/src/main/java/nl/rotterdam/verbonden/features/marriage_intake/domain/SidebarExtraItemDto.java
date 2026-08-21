package nl.rotterdam.verbonden.features.marriage_intake.domain;

import java.io.Serializable;
import java.math.BigDecimal;

public record SidebarExtraItemDto(String naam, BigDecimal prijs) implements Serializable {
}
