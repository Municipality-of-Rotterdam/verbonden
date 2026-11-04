package nl.rotterdam.verbonden.core.features.trouwlijst_administration.application;

import nl.rotterdam.verbonden.core.features.trouwlijst_administration.domain.ListTrouwlijstDossierDto;
import nl.rotterdam.verbonden.core.features.trouwlijst_administration.domain.TrouwlijstPartnerDto;
import nl.rotterdam.verbonden.core.features.trouwlijst_administration.domain.TrouwlijstWeekDto;
import nl.rotterdam.verbonden.core.features.trouwlijst_administration.repository.TrouwlijstRepository;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.PartnerGegevensDto;
import nl.rotterdam.verbonden.core.persistence.AfspraakEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.IntStream;

@Service
class TrouwlijstAdministrationServiceImpl implements TrouwlijstAdministrationService {

    private static final int AANTAL_WEKEN_VOORUIT = 52;

    private final TrouwlijstRepository trouwlijstRepository;
    private final MarriageIntakeService marriageIntakeService;
    private final TrouwlijstExcelExporter excelExporter = new TrouwlijstExcelExporter();
    private final Clock clock;

    TrouwlijstAdministrationServiceImpl(TrouwlijstRepository trouwlijstRepository,
                                        MarriageIntakeService marriageIntakeService,
                                        Clock clock) {
        this.trouwlijstRepository = trouwlijstRepository;
        this.marriageIntakeService = marriageIntakeService;
        this.clock = clock;
    }

    @Override
    public List<TrouwlijstWeekDto> findWeken() {
        LocalDate vorigeWeek = LocalDate.now(clock).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1);
        return IntStream.rangeClosed(0, AANTAL_WEKEN_VOORUIT)
                .mapToObj(i -> vorigeWeek.plusWeeks(i))
                .map(maandag -> new TrouwlijstWeekDto(maandag, maandag.plusDays(6)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListTrouwlijstDossierDto> findBevestigdeDossiers(LocalDate van, LocalDate tot) {
        return trouwlijstRepository.findByDatumBetweenAndStatus(van, tot, DossierStatus.GEACCEPTEERD).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exporteer(LocalDate van, LocalDate tot) {
        return excelExporter.exporteer(findBevestigdeDossiers(van, tot));
    }

    private ListTrouwlijstDossierDto toDto(AfspraakEntity afspraak) {
        var dossier = afspraak.getDossier();
        List<TrouwlijstPartnerDto> partners = marriageIntakeService.findPartnerGegevens(dossier.getUuid()).stream()
                .map(TrouwlijstAdministrationServiceImpl::toPartnerDto)
                .toList();
        return new ListTrouwlijstDossierDto(
                dossier.getUuid(),
                afspraak.getDatum(),
                afspraak.getStartTijd(),
                afspraak.getLocatie().getNaam(),
                null,
                dossier.getRegistratieType(),
                dossier.getCeremonieSoort(),
                partners,
                null);
    }

    private static TrouwlijstPartnerDto toPartnerDto(PartnerGegevensDto partner) {
        String achternaam = partner.gekozenAchternaam() != null && !partner.gekozenAchternaam().isBlank()
                ? partner.gekozenAchternaam() : partner.achternaam();
        String naam = ((partner.voornamen() != null ? partner.voornamen() : "") + " "
                + (achternaam != null ? achternaam : "")).trim();
        return new TrouwlijstPartnerDto(naam,
                partner.emailadres() != null ? partner.emailadres().getValue() : null,
                partner.telefoonnummer() != null ? partner.telefoonnummer().getValue() : null);
    }
}
