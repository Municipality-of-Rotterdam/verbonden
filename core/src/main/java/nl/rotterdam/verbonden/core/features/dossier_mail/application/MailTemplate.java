package nl.rotterdam.verbonden.core.features.dossier_mail.application;

import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Een e-mailtemplate uit {@code resources/mail-templates}, met een kleine, mustache-achtige syntaxis:
 * <ul>
 *   <li>{@code {{naam}}} — wordt vervangen door de waarde van {@code naam}, veilig voor in HTML gemaakt.</li>
 *   <li>{@code {{#naam}}...{{/naam}}} — herhaalt de tekst voor elk item als {@code naam} een lijst van
 *       waarden-per-naam is, en toont de tekst één keer als {@code naam} een niet-lege tekst of {@code true} is.</li>
 *   <li>{@code {{^naam}}...{{/naam}}} — toont de tekst alleen als {@code naam} leeg, {@code false} of een lege
 *       lijst is.</li>
 * </ul>
 * Een naam die niet in de waarden voorkomt, is een fout in de template of de code en geeft een exception.
 */
final class MailTemplate {

    private static final Pattern DEEL =
            Pattern.compile("\\{\\{([#^])(\\w+)}}(.*?)\\{\\{/\\2}}|\\{\\{(\\w+)}}", Pattern.DOTALL);

    private final String inhoud;

    private MailTemplate(String inhoud) {
        this.inhoud = inhoud;
    }

    static MailTemplate laad(String naam) {
        try (InputStream stream = MailTemplate.class.getResourceAsStream("/mail-templates/" + naam)) {
            if (stream == null) {
                throw new IllegalArgumentException("E-mailtemplate niet gevonden: " + naam);
            }
            return new MailTemplate(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("E-mailtemplate niet te lezen: " + naam, e);
        }
    }

    String render(Map<String, Object> waarden) {
        return render(inhoud, waarden, Map.of());
    }

    private static String render(String tekst, Map<String, Object> waarden, Map<String, Object> buitenWaarden) {
        Map<String, Object> beschikbaar = new HashMap<>(buitenWaarden);
        beschikbaar.putAll(waarden);

        // Eén keer van links naar rechts, zodat ingevulde waarden niet opnieuw als template worden gelezen.
        Matcher deel = DEEL.matcher(tekst);
        StringBuilder resultaat = new StringBuilder();
        while (deel.find()) {
            String vervanging;
            if (deel.group(4) != null) {
                Object waarde = zoek(beschikbaar, deel.group(4));
                vervanging = waarde == null ? "" : HtmlUtils.htmlEscape(waarde.toString());
            } else {
                Object waarde = zoek(beschikbaar, deel.group(2));
                String binnenste = deel.group(3);
                vervanging = deel.group(1).equals("^")
                        ? (isLeeg(waarde) ? render(binnenste, Map.of(), beschikbaar) : "")
                        : sectie(binnenste, waarde, beschikbaar);
            }
            deel.appendReplacement(resultaat, Matcher.quoteReplacement(vervanging));
        }
        deel.appendTail(resultaat);
        return resultaat.toString();
    }

    @SuppressWarnings("unchecked")
    private static String sectie(String binnenste, Object waarde, Map<String, Object> beschikbaar) {
        if (isLeeg(waarde)) {
            return "";
        }
        if (waarde instanceof Collection<?> items) {
            StringBuilder resultaat = new StringBuilder();
            for (Object item : items) {
                resultaat.append(render(binnenste, (Map<String, Object>) item, beschikbaar));
            }
            return resultaat.toString();
        }
        return render(binnenste, Map.of(), beschikbaar);
    }

    private static Object zoek(Map<String, Object> waarden, String naam) {
        if (!waarden.containsKey(naam)) {
            throw new IllegalArgumentException("Onbekende naam in e-mailtemplate: " + naam);
        }
        return waarden.get(naam);
    }

    private static boolean isLeeg(Object waarde) {
        return waarde == null
                || Boolean.FALSE.equals(waarde)
                || waarde instanceof Collection<?> lijst && lijst.isEmpty()
                || waarde instanceof CharSequence tekst && tekst.toString().isBlank();
    }
}
