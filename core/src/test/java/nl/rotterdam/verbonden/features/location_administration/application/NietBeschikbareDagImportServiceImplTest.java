package nl.rotterdam.verbonden.features.location_administration.application;

import nl.rotterdam.verbonden.features.location_administration.domain.NietBeschikbareDagImportResult;
import nl.rotterdam.verbonden.features.location_administration.repository.LocatieRepository;
import nl.rotterdam.verbonden.features.location_administration.repository.NietBeschikbareDagRepository;
import nl.rotterdam.verbonden.identity.AuthenticatedUser;
import nl.rotterdam.verbonden.identity.CurrentUserProvider;
import nl.rotterdam.verbonden.persistence.LocatieNietBeschikbareDagEntity;
import nl.rotterdam.verbonden.persistence.TrouwlocatieEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NietBeschikbareDagImportServiceImplTest {

    @Mock
    private LocatieRepository locatieRepository;

    @Mock
    private NietBeschikbareDagRepository repository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private AuthenticatedUser authenticatedUser;

    private NietBeschikbareDagImportServiceImpl service;

    private TrouwlocatieEntity locatie;

    @BeforeEach
    void setUp() {
        service = new NietBeschikbareDagImportServiceImpl(locatieRepository, repository, currentUserProvider);
        locatie = new TrouwlocatieEntity();
        locatie.setId(1L);
        locatie.setNaam("Stadhuis Rotterdam");

        lenient().when(currentUserProvider.getCurrentUser()).thenReturn(authenticatedUser);
        lenient().when(authenticatedUser.getUserId()).thenReturn("testgebruiker");
    }

    @Test
    void importeerVanXlsx_metGeldigeSampleFile_importeertAlleRijen() {
        when(locatieRepository.findById(1L)).thenReturn(Optional.of(locatie));
        when(repository.findDatumsByLocatieId(1L)).thenReturn(new HashSet<>());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InputStream is = getClass().getResourceAsStream("/xlsx/niet_beschikbare_dagen_sample.xlsx");
        assertThat(is).isNotNull();

        NietBeschikbareDagImportResult result = service.importeerVanXlsx(1L, is);

        assertThat(result.geimporteerd()).isEqualTo(5);
        assertThat(result.overgeslagen()).isEqualTo(0);
        assertThat(result.fouten()).isEqualTo(0);
        verify(repository, times(5)).save(any(LocatieNietBeschikbareDagEntity.class));
    }

    @Test
    void importeerVanXlsx_metTekstDatums_importeertAlleRijen() {
        when(locatieRepository.findById(1L)).thenReturn(Optional.of(locatie));
        when(repository.findDatumsByLocatieId(1L)).thenReturn(new HashSet<>());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InputStream is = getClass().getResourceAsStream("/xlsx/niet_beschikbare_dagen_tekst_datums.xlsx");
        assertThat(is).isNotNull();

        NietBeschikbareDagImportResult result = service.importeerVanXlsx(1L, is);

        assertThat(result.geimporteerd()).isEqualTo(3);
        assertThat(result.overgeslagen()).isEqualTo(0);
        assertThat(result.fouten()).isEqualTo(0);
    }

    @Test
    void importeerVanXlsx_metBestaandeDatum_wordtOvergeslagen() {
        when(locatieRepository.findById(1L)).thenReturn(Optional.of(locatie));
        Set<LocalDate> bestaand = new HashSet<>();
        bestaand.add(LocalDate.of(2026, 1, 1));
        bestaand.add(LocalDate.of(2026, 4, 5));
        when(repository.findDatumsByLocatieId(1L)).thenReturn(bestaand);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InputStream is = getClass().getResourceAsStream("/xlsx/niet_beschikbare_dagen_sample.xlsx");
        assertThat(is).isNotNull();

        NietBeschikbareDagImportResult result = service.importeerVanXlsx(1L, is);

        assertThat(result.geimporteerd()).isEqualTo(3);
        assertThat(result.overgeslagen()).isEqualTo(2);
        assertThat(result.fouten()).isEqualTo(0);
        verify(repository, times(3)).save(any(LocatieNietBeschikbareDagEntity.class));
    }

    @Test
    void importeerVanXlsx_metFoutigeRijen_registreertFouten() {
        when(locatieRepository.findById(1L)).thenReturn(Optional.of(locatie));
        when(repository.findDatumsByLocatieId(1L)).thenReturn(new HashSet<>());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InputStream is = getClass().getResourceAsStream("/xlsx/niet_beschikbare_dagen_met_fouten.xlsx");
        assertThat(is).isNotNull();

        NietBeschikbareDagImportResult result = service.importeerVanXlsx(1L, is);

        assertThat(result.geimporteerd()).isEqualTo(1);
        assertThat(result.fouten()).isEqualTo(2);
        verify(repository, times(1)).save(any(LocatieNietBeschikbareDagEntity.class));
    }

    @Test
    void importeerVanXlsx_locatieNietGevonden_geeftFout() {
        when(locatieRepository.findById(anyLong())).thenReturn(Optional.empty());

        InputStream is = getClass().getResourceAsStream("/xlsx/niet_beschikbare_dagen_sample.xlsx");
        NietBeschikbareDagImportResult result = service.importeerVanXlsx(99L, is);

        assertThat(result.fouten()).isEqualTo(1);
        assertThat(result.geimporteerd()).isEqualTo(0);
        assertThat(result.meldingen()).hasSize(1);
        assertThat(result.meldingen().get(0)).contains("99");
        verify(repository, never()).save(any());
    }

    @Test
    void importeerVanXlsx_slaatJuisteDatumEnRedenOp() {
        when(locatieRepository.findById(1L)).thenReturn(Optional.of(locatie));
        when(repository.findDatumsByLocatieId(1L)).thenReturn(new HashSet<>());

        ArgumentCaptor<LocatieNietBeschikbareDagEntity> captor =
                ArgumentCaptor.forClass(LocatieNietBeschikbareDagEntity.class);
        when(repository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        InputStream is = getClass().getResourceAsStream("/xlsx/niet_beschikbare_dagen_tekst_datums.xlsx");
        service.importeerVanXlsx(1L, is);

        List<LocatieNietBeschikbareDagEntity> saved = captor.getAllValues();
        assertThat(saved).hasSize(3);
        assertThat(saved.get(0).getDatum()).isEqualTo(LocalDate.of(2026, 5, 5));
        assertThat(saved.get(0).getReden()).isEqualTo("Bevrijdingsdag");
        assertThat(saved.get(0).getUserid()).isEqualTo("testgebruiker");
        assertThat(saved.get(0).getLaatsteWijzigDatum()).isNotNull();
    }

    @Test
    void importeerVanXlsx_duplicaatInZelfdeBestand_wordtSlechtsEenmaalGeimporteerd() {
        when(locatieRepository.findById(1L)).thenReturn(Optional.of(locatie));
        when(repository.findDatumsByLocatieId(1L)).thenReturn(new HashSet<>());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InputStream is = getClass().getResourceAsStream("/xlsx/niet_beschikbare_dagen_met_duplicaten.xlsx");
        assertThat(is).isNotNull();

        NietBeschikbareDagImportResult result = service.importeerVanXlsx(1L, is);

        // 3 rows: 2 unique dates imported, 1 duplicate overgeslagen
        assertThat(result.geimporteerd()).isEqualTo(2);
        assertThat(result.overgeslagen()).isEqualTo(1);
        assertThat(result.fouten()).isEqualTo(0);
        verify(repository, times(2)).save(any(LocatieNietBeschikbareDagEntity.class));
    }
}
