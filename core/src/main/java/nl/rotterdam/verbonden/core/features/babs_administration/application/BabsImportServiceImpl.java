package nl.rotterdam.verbonden.core.features.babs_administration.application;

import nl.rotterdam.verbonden.core.features.babs_administration.domain.BabsImportResult;
import nl.rotterdam.verbonden.core.features.babs_administration.domain.PersonFullName;
import nl.rotterdam.verbonden.core.features.babs_administration.repository.BabsRepository;
import nl.rotterdam.verbonden.core.persistence.BabsEntity;
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
class BabsImportServiceImpl implements BabsImportService {

    private static final Logger log = LoggerFactory.getLogger(BabsImportServiceImpl.class);
    private static final String ROTTERDAM_TROUWAMBTENAAR_URL = "https://www.rotterdam.nl/trouwambtenaar";

    private final BabsRepository babsRepository;

    BabsImportServiceImpl(BabsRepository babsRepository) {
        this.babsRepository = babsRepository;
    }

    @Override
    @Transactional
    public BabsImportResult importeerVanRotterdam() {
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
            return new BabsImportResult(0, 1,
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
            return new BabsImportResult(0, 0, messages);
        }

        for (String url : ambtenaarUrls) {
            try {
                BabsEntity babs = parseerBabsVanPagina(url);
                if (babs != null) {
                    babsRepository.save(babs);
                    imported++;
                    messages.add("Geïmporteerd: " + babs.getNaam());
                }
            } catch (IOException e) {
                errors++;
                messages.add("Fout bij importeren van " + url + ": " + e.getMessage());
                log.warn("Kon BABS niet importeren van {}: {}", url, e.getMessage());
            }
        }

        return new BabsImportResult(imported, errors, messages);
    }

    BabsEntity parseerBabsVanPagina(String url) throws IOException {
        System.out.println("Importeren van: " + url);
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (compatible; HuwelijkPOC)")
                .timeout(15_000)
                .get();

        Element heading = doc.selectFirst("h1");
        if (heading == null || heading.text().isBlank()) {
            return null;
        }

        BabsEntity babs = new BabsEntity();
        babs.setNaam(new PersonFullName(heading.text().trim()));

        // Foto
        Element img = doc.selectFirst("img[class^=styles_profilePicture]");
        if (img != null) {
            String src = img.absUrl("src");

            if (!src.isEmpty()) {
                babs.setFotoUrl(src);
            }
        }

        babs.setDetailUrl(url);
        babs.setActief(true);
        return babs;
    }
}
