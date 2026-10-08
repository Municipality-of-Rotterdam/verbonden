package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import java.util.List;

/**
 * De achternamen waaruit een partner na het huwelijk kan kiezen: die van de ander, beide in een van de twee
 * volgordes, of de eigen achternaam.
 */
public final class NaamgebruikOpties {

    private NaamgebruikOpties() {
    }

    public static List<String> voor(String eigenAchternaam, String andereAchternaam) {
        return List.of(
                andereAchternaam,
                andereAchternaam + " - " + eigenAchternaam,
                eigenAchternaam + " - " + andereAchternaam,
                eigenAchternaam);
    }
}
