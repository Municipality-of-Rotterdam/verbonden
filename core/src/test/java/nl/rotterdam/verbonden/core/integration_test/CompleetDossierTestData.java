package nl.rotterdam.verbonden.core.integration_test;

import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.features.location_administration.application.LocationAdministrationService;
import nl.rotterdam.verbonden.core.features.location_administration.domain.CreateBeschikbaarheidDto;
import nl.rotterdam.verbonden.core.features.location_administration.domain.HuwelijksType;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.IntakeMarriageTypeDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SaveGetuigenDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Maakt via de services dossiers aan voor tests rond het indienen en beoordelen van een aanvraag.
 */
@Component
public class CompleetDossierTestData {

    public static final BurgerServiceNummer BSN_PARTNER_1 = new BurgerServiceNummer("999990020");
    public static final BurgerServiceNummer BSN_PARTNER_2 = new BurgerServiceNummer("999990032");

    private final MarriageIntakeService marriageIntakeService;
    private final LocationAdministrationService locationAdministrationService;

    CompleetDossierTestData(MarriageIntakeService marriageIntakeService,
                            LocationAdministrationService locationAdministrationService) {
        this.marriageIntakeService = marriageIntakeService;
        this.locationAdministrationService = locationAdministrationService;
    }

    /**
     * Een klein huwelijk met alleen partner 1: nog niet compleet.
     */
    public UUID maakConceptDossier() {
        long locatieId = marriageIntakeService.findAllMarriageTypes().stream()
                .filter(mt -> mt.soort() == CeremonieSoort.KLEIN)
                .map(IntakeMarriageTypeDto::locatieId)
                .findFirst()
                .orElseThrow();
        return marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.KLEIN, locatieId, BSN_PARTNER_1));
    }

    /**
     * Een klein huwelijk met datum, locatie, beide partners met gekozen achternaam en twee getuigen.
     */
    public UUID maakCompleetDossier() {
        UUID dossierId = maakConceptDossier();
        long locatieId = marriageIntakeService.findAllMarriageTypes().stream()
                .filter(mt -> mt.soort() == CeremonieSoort.KLEIN)
                .map(IntakeMarriageTypeDto::locatieId)
                .findFirst()
                .orElseThrow();

        for (DayOfWeek dag : DayOfWeek.values()) {
            locationAdministrationService.createBeschikbaarheid(new CreateBeschikbaarheidDto(
                    locatieId, HuwelijksType.GRATIS, dag, LocalTime.of(9, 0), LocalTime.of(10, 0), 10,
                    BigDecimal.ZERO, LocalDate.now().minusMonths(1), LocalDate.now().plusYears(2)));
        }
        LocalDateTime slot = marriageIntakeService.findAllBeschikbareSlots(dossierId).iterator().next();
        marriageIntakeService.slaAfspraakOp(dossierId, slot.toLocalDate(), slot.toLocalTime());

        marriageIntakeService.acceptInvitation(dossierId, BSN_PARTNER_2);
        marriageIntakeService.slaPartnerGegevensOp(dossierId, BSN_PARTNER_1, "Jansen");
        marriageIntakeService.slaPartnerGegevensOp(dossierId, BSN_PARTNER_2, "Jansen");
        marriageIntakeService.slaGetuigenOp(dossierId, List.of(
                new SaveGetuigenDto(1, "Kwik van Willegenburgh"),
                new SaveGetuigenDto(2, "Kwek van Willegenburgh")));
        return dossierId;
    }
}
