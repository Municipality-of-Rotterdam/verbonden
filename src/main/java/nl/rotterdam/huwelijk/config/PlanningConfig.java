package nl.rotterdam.huwelijk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuratie voor de planningsperiode van huwelijken.
 * <ul>
 *   <li>{@code huwelijk.planning.vanaf-dagen} — minimaal aantal dagen vooruit dat een huwelijk gepland kan worden (standaard 28).</li>
 *   <li>{@code huwelijk.planning.tot-dagen} — maximaal aantal dagen vooruit dat een huwelijk gepland kan worden (standaard 365).</li>
 * </ul>
 */
@Component
@ConfigurationProperties(prefix = "huwelijk.planning")
public class PlanningConfig {

    private int vanafDagen = 28;
    private int totDagen = 365;

    public int getVanafDagen() {
        return vanafDagen;
    }

    public void setVanafDagen(int vanafDagen) {
        this.vanafDagen = vanafDagen;
    }

    public int getTotDagen() {
        return totDagen;
    }

    public void setTotDagen(int totDagen) {
        this.totDagen = totDagen;
    }
}
