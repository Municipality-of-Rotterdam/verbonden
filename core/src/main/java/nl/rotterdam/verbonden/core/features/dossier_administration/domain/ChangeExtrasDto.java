package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

import java.io.Serializable;

public record ChangeExtrasDto(
        boolean ringenUitwisselen,
        boolean muziek,
        Long trouwboekjeId,
        boolean internationaleAkte
) implements Serializable {
}
