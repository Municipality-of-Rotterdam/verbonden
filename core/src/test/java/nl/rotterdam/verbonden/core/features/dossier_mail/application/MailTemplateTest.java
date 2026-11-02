package nl.rotterdam.verbonden.core.features.dossier_mail.application;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MailTemplateTest {

    @Test
    void render_vultWaardenIn_enMaaktZeVeiligVoorHtml() {
        MailTemplate template = MailTemplate.laad("bevestiging-indiening.html");

        String html = template.render(waarden("Jan <b>&</b> Piet"));

        assertThat(html).contains("Beste Jan &lt;b&gt;&amp;&lt;/b&gt; Piet,");
        assertThat(html).doesNotContain("{{");
    }

    @Test
    void render_herhaaltLijstenEnToontSectiesAlleenAlsErEenWaardeIs() {
        MailTemplate template = MailTemplate.laad("bevestiging-indiening.html");
        Map<String, Object> waarden = waarden("Jan en Piet");
        waarden.put("ceremoniePrijs", null);
        waarden.put("totaalPrijs", "");
        waarden.put("extras", List.of(Map.of("naam", "Ringen", "prijs", "")));

        String html = template.render(waarden);

        assertThat(html).contains("<li>Kwik</li>").contains("<li>Kwek</li>");
        assertThat(html).contains("<li>Ringen</li>").doesNotContain("U heeft geen extra's gekozen.");
        assertThat(html).doesNotContain("Kosten ceremonie").doesNotContain("Totaal:");
    }

    @Test
    void render_zonderExtras_toontDatErGeenExtrasZijn() {
        String html = MailTemplate.laad("bevestiging-indiening.html").render(waarden("Jan en Piet"));

        assertThat(html).contains("U heeft geen extra's gekozen.");
    }

    @Test
    void render_waardeMetTemplateTekens_wordtNietOpnieuwAlsTemplateGelezen() {
        String html = MailTemplate.laad("bevestiging-indiening.html").render(waarden("{{onbekend}} $1"));

        assertThat(html).contains("Beste {{onbekend}} $1,");
    }

    @Test
    void render_onbekendeNaam_geeftEenFout() {
        Map<String, Object> waarden = waarden("Jan en Piet");
        waarden.remove("locatie");

        assertThatThrownBy(() -> MailTemplate.laad("bevestiging-indiening.html").render(waarden))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("locatie");
    }

    @Test
    void laad_onbekendeTemplate_geeftEenFout() {
        assertThatThrownBy(() -> MailTemplate.laad("bestaat-niet.html"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Map<String, Object> waarden(String aanhef) {
        Map<String, Object> waarden = new HashMap<>();
        waarden.put("aanhef", aanhef);
        waarden.put("registratieType", "Huwelijk");
        waarden.put("ceremonieSoort", "Klein");
        waarden.put("ceremoniePrijs", "€ 100,00");
        waarden.put("datum", "1 januari 2027");
        waarden.put("tijd", "09:00");
        waarden.put("locatie", "Stadhuis");
        waarden.put("partners", List.of(Map.of("naam", "Jan"), Map.of("naam", "Piet")));
        waarden.put("getuigen", List.of(Map.of("naam", "Kwik"), Map.of("naam", "Kwek")));
        waarden.put("extras", List.of());
        waarden.put("totaalPrijs", "€ 100,00");
        return waarden;
    }
}
