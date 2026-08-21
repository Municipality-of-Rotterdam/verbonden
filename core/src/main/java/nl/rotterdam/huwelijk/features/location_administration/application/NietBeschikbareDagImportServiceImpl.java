package nl.rotterdam.huwelijk.features.location_administration.application;

import nl.rotterdam.huwelijk.features.location_administration.domain.NietBeschikbareDagImportResult;
import nl.rotterdam.huwelijk.features.location_administration.repository.NietBeschikbareDagRepository;
import nl.rotterdam.huwelijk.features.location_administration.repository.LocatieRepository;
import nl.rotterdam.huwelijk.identity.CurrentUserProvider;
import nl.rotterdam.huwelijk.persistence.LocatieNietBeschikbareDagEntity;
import nl.rotterdam.huwelijk.persistence.TrouwlocatieEntity;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
class NietBeschikbareDagImportServiceImpl implements NietBeschikbareDagImportService {

    private static final Logger log = LoggerFactory.getLogger(NietBeschikbareDagImportServiceImpl.class);

    private final LocatieRepository locatieRepository;
    private final NietBeschikbareDagRepository repository;
    private final CurrentUserProvider currentUserProvider;

    NietBeschikbareDagImportServiceImpl(LocatieRepository locatieRepository,
                                        NietBeschikbareDagRepository repository,
                                        CurrentUserProvider currentUserProvider) {
        this.locatieRepository = locatieRepository;
        this.repository = repository;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional
    public NietBeschikbareDagImportResult importeerVanXlsx(long locatieId, InputStream inputStream) {
        TrouwlocatieEntity locatie = locatieRepository.findById(locatieId).orElse(null);
        if (locatie == null) {
            return new NietBeschikbareDagImportResult(0, 0, 1,
                    List.of("Trouwlocatie niet gevonden: " + locatieId));
        }

        Set<LocalDate> bestaandeDatums = repository.findDatumsByLocatieId(locatieId);

        int geimporteerd = 0;
        int overgeslagen = 0;
        int fouten = 0;
        List<String> meldingen = new ArrayList<>();
        String userid = currentUserProvider.getCurrentUser().getUserId();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                LocalDate datum = parseDatum(row.getCell(0));
                if (datum == null) {
                    fouten++;
                    meldingen.add("Rij " + (rowIndex + 1) + ": ongeldige of ontbrekende datum.");
                    continue;
                }

                String reden = parseReden(row.getCell(1));
                if (reden == null || reden.isBlank()) {
                    fouten++;
                    meldingen.add("Rij " + (rowIndex + 1) + " (" + datum + "): ontbrekende reden.");
                    continue;
                }

                if (bestaandeDatums.contains(datum)) {
                    overgeslagen++;
                    meldingen.add("Rij " + (rowIndex + 1) + " (" + datum + "): datum bestaat al, overgeslagen.");
                    continue;
                }

                LocatieNietBeschikbareDagEntity entity = new LocatieNietBeschikbareDagEntity();
                entity.setLocatie(locatie);
                entity.setDatum(datum);
                entity.setReden(reden);
                entity.setLaatsteWijzigDatum(LocalDateTime.now());
                entity.setUserid(userid);
                repository.save(entity);
                bestaandeDatums.add(datum);
                geimporteerd++;
                meldingen.add("Rij " + (rowIndex + 1) + " (" + datum + "): geïmporteerd.");
            }
        } catch (IOException e) {
            log.error("Fout bij lezen van xlsx-bestand: {}", e.getMessage());
            return new NietBeschikbareDagImportResult(geimporteerd, overgeslagen, fouten + 1,
                    List.of("Kon het bestand niet lezen: " + e.getMessage()));
        }

        return new NietBeschikbareDagImportResult(geimporteerd, overgeslagen, fouten, meldingen);
    }

    private static LocalDate parseDatum(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        if (cell.getCellType() == CellType.STRING) {
            String text = cell.getStringCellValue().trim();
            try {
                return LocalDate.parse(text);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private static String parseReden(Cell cell) {
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double value = cell.getNumericCellValue();
                yield value == Math.floor(value)
                        ? String.valueOf((long) value)
                        : String.valueOf(value);
            }
            default -> null;
        };
    }

}
