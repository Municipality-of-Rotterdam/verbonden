package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * De soorten bestanden die een medewerker aan een dossier mag toevoegen: gangbare documenten en afbeeldingen.
 * Het type wordt bepaald door de extensie en gecontroleerd aan de hand van de inhoud, zodat bijvoorbeeld een
 * hernoemd programma of ZIP-bestand wordt geweigerd. Bij het downloaden bepaalt het type (en niet wat de
 * browser van de uploader meestuurde) het content-type.
 */
public enum DossierBestandType {

    PDF("application/pdf", List.of("pdf"), begintMet("%PDF-")),
    JPEG("image/jpeg", List.of("jpg", "jpeg"), begintMet(0xFF, 0xD8, 0xFF)),
    PNG("image/png", List.of("png"), begintMet(0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)),
    GIF("image/gif", List.of("gif"), begintMet("GIF87a").or(begintMet("GIF89a"))),
    WEBP("image/webp", List.of("webp"), inhoud -> begintMet("RIFF").test(inhoud) && bevatOp(inhoud, 8, "WEBP")),
    TIFF("image/tiff", List.of("tif", "tiff"), begintMet('I', 'I', 42, 0).or(begintMet('M', 'M', 0, 42))),
    HEIC("image/heic", List.of("heic", "heif"), inhoud -> bevatOp(inhoud, 4, "ftyp")),
    DOC("application/msword", List.of("doc"), begintMet(0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1)),
    XLS("application/vnd.ms-excel", List.of("xls"), begintMet(0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1)),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", List.of("docx"),
            DossierBestandType::isOfficeOpenXml),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", List.of("xlsx"),
            DossierBestandType::isOfficeOpenXml),
    PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", List.of("pptx"),
            DossierBestandType::isOfficeOpenXml),
    ODT("application/vnd.oasis.opendocument.text", List.of("odt"), DossierBestandType::isOpenDocument),
    ODS("application/vnd.oasis.opendocument.spreadsheet", List.of("ods"), DossierBestandType::isOpenDocument),
    RTF("application/rtf", List.of("rtf"), begintMet("{\\rtf")),
    TXT("text/plain", List.of("txt"), DossierBestandType::isPlatteTekst);

    /** Maximale grootte van een bestand: genoeg voor een scan of foto van een document. */
    public static final long MAX_GROOTTE = 20L * 1024 * 1024;

    private final String contentType;
    private final List<String> extensies;
    private final Predicate<byte[]> inhoudControle;

    DossierBestandType(String contentType, List<String> extensies, Predicate<byte[]> inhoudControle) {
        this.contentType = contentType;
        this.extensies = extensies;
        this.inhoudControle = inhoudControle;
    }

    public String getContentType() {
        return contentType;
    }

    /**
     * Het type dat bij de extensie van de bestandsnaam hoort, of leeg wanneer die extensie niet is toegestaan.
     */
    public static Optional<DossierBestandType> vanBestandsnaam(String bestandsnaam) {
        int punt = bestandsnaam.lastIndexOf('.');
        if (punt < 0) {
            return Optional.empty();
        }
        String extensie = bestandsnaam.substring(punt + 1).toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(type -> type.extensies.contains(extensie))
                .findFirst();
    }

    /**
     * Alle toegestane extensies, voor in een melding of het {@code accept}-attribuut van een uploadveld.
     */
    public static List<String> alleExtensies() {
        return Arrays.stream(values())
                .flatMap(type -> type.extensies.stream())
                .toList();
    }

    /**
     * Of de inhoud past bij dit type. Leest alleen het begin van het bestand, of bij een ZIP-gebaseerd
     * documentformaat de namen van de onderdelen (zonder ze uit te pakken).
     */
    public boolean heeftGeldigeInhoud(byte[] inhoud) {
        return inhoudControle.test(inhoud);
    }

    private static Predicate<byte[]> begintMet(String tekst) {
        return inhoud -> bevatOp(inhoud, 0, tekst);
    }

    private static Predicate<byte[]> begintMet(int... bytes) {
        return inhoud -> {
            if (inhoud.length < bytes.length) {
                return false;
            }
            for (int i = 0; i < bytes.length; i++) {
                if ((inhoud[i] & 0xFF) != bytes[i]) {
                    return false;
                }
            }
            return true;
        };
    }

    private static boolean bevatOp(byte[] inhoud, int positie, String tekst) {
        byte[] verwacht = tekst.getBytes(StandardCharsets.US_ASCII);
        return inhoud.length >= positie + verwacht.length
                && Arrays.equals(inhoud, positie, positie + verwacht.length, verwacht, 0, verwacht.length);
    }

    /** Word-, Excel- en PowerPoint-bestanden (docx, xlsx, pptx) zijn een ZIP met een {@code [Content_Types].xml}. */
    private static boolean isOfficeOpenXml(byte[] inhoud) {
        return isZipMetOnderdeel(inhoud, "[Content_Types].xml", false);
    }

    /** OpenDocument-bestanden (odt, ods) zijn een ZIP die begint met een onderdeel {@code mimetype}. */
    private static boolean isOpenDocument(byte[] inhoud) {
        return isZipMetOnderdeel(inhoud, "mimetype", true);
    }

    private static boolean isZipMetOnderdeel(byte[] inhoud, String onderdeel, boolean alleenAlsEerste) {
        if (!begintMet('P', 'K', 3, 4).test(inhoud)) {
            return false;
        }
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(inhoud))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.getName().equals(onderdeel)) {
                    return true;
                }
                if (alleenAlsEerste) {
                    return false;
                }
            }
            return false;
        } catch (IOException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Platte tekst heeft geen herkenbaar begin; weiger in elk geval binaire inhoud, programma's en scripts.
     */
    private static boolean isPlatteTekst(byte[] inhoud) {
        for (byte b : inhoud) {
            if (b == 0) {
                return false;
            }
        }
        return !begintMet("MZ").test(inhoud)
                && !begintMet(0x7F, 'E', 'L', 'F').test(inhoud)
                && !begintMet("#!").test(inhoud)
                && !begintMet('P', 'K').test(inhoud);
    }
}
