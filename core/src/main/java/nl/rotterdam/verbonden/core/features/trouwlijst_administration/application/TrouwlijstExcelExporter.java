package nl.rotterdam.verbonden.core.features.trouwlijst_administration.application;

import nl.rotterdam.verbonden.core.features.trouwlijst_administration.domain.ListTrouwlijstDossierDto;
import nl.rotterdam.verbonden.core.features.trouwlijst_administration.domain.TrouwlijstPartnerDto;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Maakt het Excel-bestand van de trouwlijst: per datum en per locatie een blok met de dossiers.
 */
class TrouwlijstExcelExporter {

    private static final Locale NEDERLANDS = Locale.forLanguageTag("nl-NL");
    private static final DateTimeFormatter TIJD_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final List<String> KOLOMKOPPEN = List.of(
            "ABS", "Soort", "Ceremonie", "Tijd", "Partner 1", "Partner 2", "Tel. privé 1", "Tel. privé 2",
            "E-mail 1", "E-mail 2", "Taal");

    byte[] exporteer(List<ListTrouwlijstDossierDto> dossiers) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Trouwlijst");
            CellStyle datumStijl = stijl(workbook, 14, IndexedColors.LIGHT_YELLOW, false);
            CellStyle locatieStijl = stijl(workbook, 12, null, false);
            CellStyle kopStijl = stijl(workbook, 10, IndexedColors.PALE_BLUE, true);

            Map<LocalDate, Map<String, List<ListTrouwlijstDossierDto>>> perDatum = new TreeMap<>();
            for (ListTrouwlijstDossierDto dossier : dossiers) {
                perDatum.computeIfAbsent(dossier.datum(), d -> new LinkedHashMap<>())
                        .computeIfAbsent(dossier.locatie(), l -> new ArrayList<>())
                        .add(dossier);
            }

            int rijIndex = 0;
            for (var datumEntry : perDatum.entrySet()) {
                LocalDate datum = datumEntry.getKey();
                String datumTekst = "Datum: " + datum.getDayOfWeek().getDisplayName(TextStyle.FULL, NEDERLANDS) + " "
                        + datum.getDayOfMonth() + " " + datum.getMonth().getDisplayName(TextStyle.FULL, NEDERLANDS)
                        + " " + datum.getYear();
                schrijfTitel(sheet.createRow(rijIndex++), datumTekst, datumStijl);
                for (var locatieEntry : datumEntry.getValue().entrySet()) {
                    schrijfTitel(sheet.createRow(rijIndex++), "Trouwlocatie: " + locatieEntry.getKey(), locatieStijl);
                    Row kop = sheet.createRow(rijIndex++);
                    for (int i = 0; i < KOLOMKOPPEN.size(); i++) {
                        kop.createCell(i).setCellValue(KOLOMKOPPEN.get(i));
                        kop.getCell(i).setCellStyle(kopStijl);
                    }
                    for (ListTrouwlijstDossierDto dossier : locatieEntry.getValue()) {
                        schrijfDossier(sheet.createRow(rijIndex++), dossier);
                    }
                    rijIndex++;
                }
            }
            for (int i = 0; i < KOLOMKOPPEN.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void schrijfTitel(Row rij, String tekst, CellStyle stijl) {
        rij.createCell(0).setCellValue(tekst);
        rij.getCell(0).setCellStyle(stijl);
    }

    private static void schrijfDossier(Row rij, ListTrouwlijstDossierDto dossier) {
        TrouwlijstPartnerDto partner1 = partner(dossier, 0);
        TrouwlijstPartnerDto partner2 = partner(dossier, 1);
        List<String> waarden = List.of(
                tekst(dossier.babs()),
                dossier.registratieType().getLabel(),
                dossier.ceremonieSoort().getLabel(),
                dossier.startTijd().format(TIJD_FORMATTER),
                tekst(partner1 != null ? partner1.naam() : null),
                tekst(partner2 != null ? partner2.naam() : null),
                tekst(partner1 != null ? partner1.telefoonnummer() : null),
                tekst(partner2 != null ? partner2.telefoonnummer() : null),
                tekst(partner1 != null ? partner1.emailadres() : null),
                tekst(partner2 != null ? partner2.emailadres() : null),
                tekst(dossier.taal()));
        for (int i = 0; i < waarden.size(); i++) {
            rij.createCell(i).setCellValue(waarden.get(i));
        }
    }

    private static TrouwlijstPartnerDto partner(ListTrouwlijstDossierDto dossier, int index) {
        return dossier.partners().size() > index ? dossier.partners().get(index) : null;
    }

    private static String tekst(String waarde) {
        return waarde != null ? waarde : "";
    }

    private static CellStyle stijl(XSSFWorkbook workbook, int fontGrootte, IndexedColors achtergrond, boolean rand) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) fontGrootte);
        CellStyle stijl = workbook.createCellStyle();
        stijl.setFont(font);
        if (achtergrond != null) {
            stijl.setFillForegroundColor(achtergrond.getIndex());
            stijl.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        if (rand) {
            stijl.setBorderBottom(BorderStyle.THIN);
            stijl.setBorderTop(BorderStyle.THIN);
        }
        return stijl;
    }
}
