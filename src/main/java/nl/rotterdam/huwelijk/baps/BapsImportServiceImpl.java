package nl.rotterdam.huwelijk.baps;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class BapsImportServiceImpl implements BapsImportService {

    private static final Logger log = LoggerFactory.getLogger(BapsImportServiceImpl.class);
    private static final String ROTTERDAM_TROUWAMBTENAAR_URL = "https://www.rotterdam.nl/trouwambtenaar";

    private final BapsRepository bapsRepository;

    public BapsImportServiceImpl(BapsRepository bapsRepository) {
        this.bapsRepository = bapsRepository;
    }

    @Override
    @Transactional
    public BapsImportResult importeerVanRotterdam() {
        int imported = 0;
        int errors = 0;
        List<String> messages = new ArrayList<>();

        Document overzichtPage;
        try {
            overzichtPage = Jsoup.connect(ROTTERDAM_TROUWAMBTENAAR_URL)
                    .userAgent("Mozilla/5.0 (compatible; HuwelijkPOC)")
                    .timeout(15_000)
                    .get();
        } catch (IOException e) {
            log.error("Kon Rotterdam trouwambtenaar pagina niet ophalen: {}", e.getMessage());
            return new BapsImportResult(0, 1,
                    List.of("Kon pagina niet ophalen: " + e.getMessage()));
        }

        // Find the h2 with text "Trouwambtenaren" and collect links from the ul that follows it
        List<String> ambtenaarUrls = new ArrayList<>();
        Element trouwambtenarHeading = null;
        for (Element h2 : overzichtPage.select("h2")) {
            if (h2.text().contains("Trouwambtenaren")) {
                trouwambtenarHeading = h2;
                break;
            }
        }
        if (trouwambtenarHeading != null) {
            Element ul = trouwambtenarHeading.nextElementSibling();
            while (ul != null && !ul.tagName().equals("ul")) {
                ul = ul.nextElementSibling();
            }
            if (ul != null) {
                for (Element li : ul.select("li")) {
                    Element a = li.selectFirst("a[href]");
                    if (a != null) {
                        String href = a.absUrl("href");
                        if (!href.isEmpty() && !ambtenaarUrls.contains(href)) {
                            ambtenaarUrls.add(href);
                        }
                    }
                }
            }
        }

        if (ambtenaarUrls.isEmpty()) {
            messages.add("Geen trouwambtenaar-links gevonden op de overzichtspagina.");
            return new BapsImportResult(0, 0, messages);
        }

        for (String url : ambtenaarUrls) {
            try {
                Baps baps = parseerBapsVanPagina(url);
                if (baps != null) {
                    bapsRepository.save(baps);
                    imported++;
                    messages.add("Geïmporteerd: " + baps.getNaam());
                }
            } catch (IOException e) {
                errors++;
                messages.add("Fout bij importeren van " + url + ": " + e.getMessage());
                log.warn("Kon BAPS niet importeren van {}: {}", url, e.getMessage());
            }
        }

        return new BapsImportResult(imported, errors, messages);
    }

    private Baps parseerBapsVanPagina(String url) throws IOException {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (compatible; HuwelijkPOC)")
                .timeout(15_000)
                .get();

        Element heading = doc.selectFirst("h1");
        if (heading == null || heading.text().isBlank()) {
            return null;
        }

        Baps baps = new Baps();
        baps.setNaam(heading.text().trim());

        // Foto
        Element img = doc.selectFirst(".person-photo img, .profile-image img, "
                + ".artikel__afbeelding img, article img");
        if (img != null) {
            String src = img.absUrl("src");
            if (!src.isEmpty()) {
                baps.setFotoUrl(src);
            }
        }

        // Beschrijving / hobbies – neem de eerste tekstparagraaf
        Element content = doc.selectFirst(
                ".rich-text p, .content-body p, .artikel__tekst p, article p");
        if (content != null && !content.text().isBlank()) {
            baps.setBeschrijving(content.text().trim());
        }

        baps.setActief(true);
        return baps;
    }
}
