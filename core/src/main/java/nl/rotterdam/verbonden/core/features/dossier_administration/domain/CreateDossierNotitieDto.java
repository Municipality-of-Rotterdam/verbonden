package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

import java.io.Serializable;

public record CreateDossierNotitieDto(String tekst) implements Serializable {

    public static final int MAX_LENGTE = 1000;
}
