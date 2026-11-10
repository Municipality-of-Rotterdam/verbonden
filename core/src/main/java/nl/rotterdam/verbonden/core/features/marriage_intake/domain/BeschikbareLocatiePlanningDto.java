package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import nl.rotterdam.verbonden.core.domain.Bedrag;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public record BeschikbareLocatiePlanningDto(Set<Long> locatieIds, Map<Long, Bedrag> prijzen)
        implements Serializable {

    public BeschikbareLocatiePlanningDto {
        locatieIds = Set.copyOf(locatieIds);
        prijzen = Map.copyOf(prijzen);
    }
}
