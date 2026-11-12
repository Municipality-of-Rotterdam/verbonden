package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

import nl.rotterdam.verbonden.core.domain.BuitenlandsPersoonsnummer;
import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.domain.Telefoonnummer;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.AanmaakKanaal;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;

/**
 * Een dossier dat een medewerker aanmaakt terwijl beide partners aan de balie of in een videogesprek aanwezig
 * zijn; de medewerker heeft de identiteit van beide partners gecontroleerd.
 *
 * <p>Partner 1 heeft altijd een BSN. Partner 2 heeft óf een BSN ({@code bsnPartner2}), óf een persoonsnummer uit
 * het buitenlandse paspoort met de persoonsgegevens daarvan ({@code persoonsnummerPartner2} en
 * {@code persoonsgegevensPartner2}).
 */
public record CreateBalieDossierDto(
        AanmaakKanaal kanaal,
        RegistratieType registratieType,
        CeremonieSoort ceremonieSoort,
        BurgerServiceNummer bsnPartner1,
        BurgerServiceNummer bsnPartner2,
        BuitenlandsPersoonsnummer persoonsnummerPartner2,
        PersoonsgegevensDto persoonsgegevensPartner2,
        Long locatieId,
        String vrijeLocatieNaam,
        String vrijeLocatieAdres,
        Telefoonnummer vrijeLocatieTelefoonnummer,
        String vrijeLocatieContactpersoon
) {

    public CreateBalieDossierDto(AanmaakKanaal kanaal, RegistratieType registratieType, CeremonieSoort ceremonieSoort,
                                 BurgerServiceNummer bsnPartner1, BurgerServiceNummer bsnPartner2,
                                 BuitenlandsPersoonsnummer persoonsnummerPartner2,
                                 PersoonsgegevensDto persoonsgegevensPartner2) {
        this(kanaal, registratieType, ceremonieSoort, bsnPartner1, bsnPartner2, persoonsnummerPartner2,
                persoonsgegevensPartner2, null, null, null, null, null);
    }
}
