package nl.rotterdam.verbonden.core.features.trouwlijst_administration.application;

import nl.rotterdam.verbonden.core.features.trouwlijst_administration.domain.ListTrouwlijstDossierDto;
import nl.rotterdam.verbonden.core.features.trouwlijst_administration.domain.TrouwlijstAfspraakDto;
import nl.rotterdam.verbonden.core.features.trouwlijst_administration.domain.TrouwlijstPartnerDto;
import nl.rotterdam.verbonden.core.features.trouwlijst_administration.domain.TrouwlijstWeekDto;
import nl.rotterdam.verbonden.core.features.trouwlijst_administration.repository.TrouwlijstRepository;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.PartnerGegevensDto;
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

    private ListTrouwlijstDossierDto toDto(TrouwlijstAfspraakDto afspraak) {
        List<TrouwlijstPartnerDto> partners = marriageIntakeService.findPartnerGegevens(afspraak.dossierId()).stream()
                .map(TrouwlijstAdministrationServiceImpl::toPartnerDto)
                .toList();
        return new ListTrouwlijstDossierDto(
                afspraak.dossierId(),
                afspraak.datum(),
                afspraak.startTijd(),
                afspraak.locatie(),
                null,
                afspraak.registratieType(),
                afspraak.ceremonieSoort(),
                partners,
                afspraak.taal() != null ? afspraak.taal().getLabel() : null,
                afspraak.aantalBezoekers());
    }

    private static TrouwlijstPartnerDto toPartnerDto(PartnerGegevensDto partner) {
        String naam = ((partner.voornamen() != null ? partner.voornamen() : "") + " "
                + (partner.achternaam() != null ? partner.achternaam() : "")).trim();
        return new TrouwlijstPartnerDto(naam,
                partner.emailadres() != null ? partner.emailadres().getValue() : null,
                partner.telefoonnummer() != null ? partner.telefoonnummer().getValue() : null);
    }
}
