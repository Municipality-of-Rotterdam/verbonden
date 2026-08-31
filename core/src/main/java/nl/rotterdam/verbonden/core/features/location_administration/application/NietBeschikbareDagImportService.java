package nl.rotterdam.verbonden.core.features.location_administration.application;

import nl.rotterdam.verbonden.core.features.location_administration.domain.NietBeschikbareDagImportResult;

import java.io.InputStream;

public interface NietBeschikbareDagImportService {

    /**
     * Importeert niet beschikbare dagen vanuit een xlsx-bestand.
     *
     * <p>Verwacht formaat: rij 1 is de kopregel (wordt overgeslagen), daarna per rij:
     * <ul>
     *   <li>Kolom A: datum (als Excel-datum of tekst in ISO 8601-formaat yyyy-MM-dd)</li>
     *   <li>Kolom B: reden (tekst)</li>
     * </ul>
     *
     * <p>Reeds bestaande datums voor de locatie worden overgeslagen.
     *
     * @param locatieId het ID van de trouwlocatie
     * @param inputStream de invoerstroom van het xlsx-bestand
     * @return het importresultaat met aantallen en meldingen
     */
    NietBeschikbareDagImportResult importeerVanXlsx(long locatieId, InputStream inputStream);
}
