package nl.rotterdam.huwelijk.features.location_administration.application;

import nl.rotterdam.huwelijk.features.location_administration.domain.ChangeBeschikbaarheidDto;
import nl.rotterdam.huwelijk.features.location_administration.domain.ChangeLocatieDto;
import nl.rotterdam.huwelijk.features.location_administration.domain.ChangeNietBeschikbareDagDto;
import nl.rotterdam.huwelijk.features.location_administration.domain.CreateBeschikbaarheidDto;
import nl.rotterdam.huwelijk.features.location_administration.domain.CreateLocatieDto;
import nl.rotterdam.huwelijk.features.location_administration.domain.CreateNietBeschikbareDagDto;
import nl.rotterdam.huwelijk.features.location_administration.domain.ListBeschikbaarheidDto;
import nl.rotterdam.huwelijk.features.location_administration.domain.ListLocatieDto;
import nl.rotterdam.huwelijk.features.location_administration.domain.ListNietBeschikbareDagDto;
import nl.rotterdam.huwelijk.features.location_administration.repository.BeschikbaarheidRepository;
import nl.rotterdam.huwelijk.features.location_administration.repository.LocatieRepository;
import nl.rotterdam.huwelijk.features.location_administration.repository.NietBeschikbareDagRepository;
import nl.rotterdam.huwelijk.persistence.LocatieBeschikbaarheidEntity;
import nl.rotterdam.huwelijk.persistence.LocatieNietBeschikbareDagEntity;
import nl.rotterdam.huwelijk.persistence.TrouwlocatieEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
class LocationAdministrationServiceImpl implements LocationAdministrationService {

    private final LocatieRepository locatieRepository;
    private final BeschikbaarheidRepository beschikbaarheidRepository;
    private final NietBeschikbareDagRepository nietBeschikbareDagRepository;

    LocationAdministrationServiceImpl(LocatieRepository locatieRepository,
                                      BeschikbaarheidRepository beschikbaarheidRepository,
                                      NietBeschikbareDagRepository nietBeschikbareDagRepository) {
        this.locatieRepository = locatieRepository;
        this.beschikbaarheidRepository = beschikbaarheidRepository;
        this.nietBeschikbareDagRepository = nietBeschikbareDagRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ListLocatieDto> findAll(Pageable pageable) {
        return locatieRepository.findAllProjected(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChangeLocatieDto> findById(long id) {
        return locatieRepository.findById(id).map(this::toChangeDto);
    }

    @Override
    @Transactional
    public long create(CreateLocatieDto dto) {
        TrouwlocatieEntity entity = new TrouwlocatieEntity();
        entity.setNaam(dto.naam());
        entity.setFotoUrl(dto.fotoUrl());
        entity.setOmschrijving(dto.omschrijving());
        entity.setDetailUrl(dto.detailUrl());
        return locatieRepository.save(entity).getId();
    }

    @Override
    @Transactional
    public void update(ChangeLocatieDto dto) {
        TrouwlocatieEntity entity = locatieRepository.findById(dto.id())
                .orElseThrow(() -> new IllegalArgumentException("Trouwlocatie niet gevonden: " + dto.id()));
        entity.setNaam(dto.naam());
        entity.setFotoUrl(dto.fotoUrl());
        entity.setOmschrijving(dto.omschrijving());
        entity.setDetailUrl(dto.detailUrl());
        locatieRepository.save(entity);
    }

    @Override
    @Transactional
    public void delete(long id) {
        locatieRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return locatieRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListBeschikbaarheidDto> findBeschikbaarheden(long locatieId) {
        return beschikbaarheidRepository.findByLocatieId(locatieId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChangeBeschikbaarheidDto> findBeschikbaarheidById(long id) {
        return beschikbaarheidRepository.findById(id).map(this::toChangeBeschikbaarheidDto);
    }

    @Override
    @Transactional
    public long createBeschikbaarheid(CreateBeschikbaarheidDto dto) {
        TrouwlocatieEntity locatie = locatieRepository.findById(dto.locatieId())
                .orElseThrow(() -> new IllegalArgumentException("Trouwlocatie niet gevonden: " + dto.locatieId()));
        LocatieBeschikbaarheidEntity entity = new LocatieBeschikbaarheidEntity();
        entity.setLocatie(locatie);
        entity.setHuwelijkstype(dto.huwelijkstype());
        entity.setDagVanDeWeek(dto.dagVanDeWeek());
        entity.setStartTijd(dto.startTijd());
        entity.setEindTijd(dto.eindTijd());
        entity.setDuurInMinuten(dto.duurInMinuten());
        entity.setPrijs(dto.prijs());
        entity.setIngangsdatum(dto.ingangsdatum());
        entity.setEinddatum(dto.einddatum());
        return beschikbaarheidRepository.save(entity).getId();
    }

    @Override
    @Transactional
    public void updateBeschikbaarheid(ChangeBeschikbaarheidDto dto) {
        LocatieBeschikbaarheidEntity entity = beschikbaarheidRepository.findById(dto.id())
                .orElseThrow(() -> new IllegalArgumentException("Beschikbaarheid niet gevonden: " + dto.id()));
        entity.setHuwelijkstype(dto.huwelijkstype());
        entity.setDagVanDeWeek(dto.dagVanDeWeek());
        entity.setStartTijd(dto.startTijd());
        entity.setEindTijd(dto.eindTijd());
        entity.setDuurInMinuten(dto.duurInMinuten());
        entity.setPrijs(dto.prijs());
        entity.setIngangsdatum(dto.ingangsdatum());
        entity.setEinddatum(dto.einddatum());
        beschikbaarheidRepository.save(entity);
    }

    @Override
    @Transactional
    public void deleteBeschikbaarheid(long id) {
        beschikbaarheidRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListNietBeschikbareDagDto> findNietBeschikbareDagen(long locatieId) {
        return nietBeschikbareDagRepository.findByLocatieId(locatieId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChangeNietBeschikbareDagDto> findNietBeschikbareDagById(long id) {
        return nietBeschikbareDagRepository.findById(id).map(this::toChangeNietBeschikbareDagDto);
    }

    @Override
    @Transactional
    public long createNietBeschikbareDag(CreateNietBeschikbareDagDto dto) {
        TrouwlocatieEntity locatie = locatieRepository.findById(dto.locatieId())
                .orElseThrow(() -> new IllegalArgumentException("Trouwlocatie niet gevonden: " + dto.locatieId()));
        LocatieNietBeschikbareDagEntity entity = new LocatieNietBeschikbareDagEntity();
        entity.setLocatie(locatie);
        entity.setDatum(dto.datum());
        entity.setReden(dto.reden());
        entity.setLaatsteWijzigDatum(LocalDateTime.now());
        entity.setUserid(currentUserid());
        return nietBeschikbareDagRepository.save(entity).getId();
    }

    @Override
    @Transactional
    public void updateNietBeschikbareDag(ChangeNietBeschikbareDagDto dto) {
        LocatieNietBeschikbareDagEntity entity = nietBeschikbareDagRepository.findById(dto.id())
                .orElseThrow(() -> new IllegalArgumentException("Niet beschikbare dag niet gevonden: " + dto.id()));
        entity.setDatum(dto.datum());
        entity.setReden(dto.reden());
        entity.setLaatsteWijzigDatum(LocalDateTime.now());
        entity.setUserid(currentUserid());
        nietBeschikbareDagRepository.save(entity);
    }

    @Override
    @Transactional
    public void deleteNietBeschikbareDag(long id) {
        nietBeschikbareDagRepository.deleteById(id);
    }

    private static String currentUserid() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getName() != null ? auth.getName() : "onbekend";
    }

    private ChangeLocatieDto toChangeDto(TrouwlocatieEntity entity) {
        return new ChangeLocatieDto(entity.getId(), entity.getNaam(), entity.getFotoUrl(),
                entity.getOmschrijving(), entity.getDetailUrl());
    }

    private ChangeBeschikbaarheidDto toChangeBeschikbaarheidDto(LocatieBeschikbaarheidEntity entity) {
        return new ChangeBeschikbaarheidDto(
                entity.getId(),
                entity.getHuwelijkstype(),
                entity.getDagVanDeWeek(),
                entity.getStartTijd(),
                entity.getEindTijd(),
                entity.getDuurInMinuten(),
                entity.getPrijs(),
                entity.getIngangsdatum(),
                entity.getEinddatum()
        );
    }

    private ChangeNietBeschikbareDagDto toChangeNietBeschikbareDagDto(LocatieNietBeschikbareDagEntity entity) {
        return new ChangeNietBeschikbareDagDto(
                entity.getId(),
                entity.getDatum(),
                entity.getReden()
        );
    }
}
