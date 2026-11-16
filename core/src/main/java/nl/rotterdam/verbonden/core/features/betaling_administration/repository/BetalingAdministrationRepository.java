package nl.rotterdam.verbonden.core.features.betaling_administration.repository;

import nl.rotterdam.verbonden.core.features.betaling_administration.domain.ListBetalingDto;
import nl.rotterdam.verbonden.core.persistence.BetalingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BetalingAdministrationRepository extends JpaRepository<BetalingEntity, Long> {

    String SELECT_LIST_DTO = """
            select new nl.rotterdam.verbonden.core.features.betaling_administration.domain.ListBetalingDto(
                d.uuid, b.betalingskenmerk, b.soort, m.omschrijving, b.bedrag, b.status, b.betalerBsn,
                b.gestartOp, b.afgerondOp, b.externeReferentie)
            from BetalingEntity b join b.dossier d left join b.betaalmutatie m
            """;

    String ZOEKTERM = """
            where :zoekterm = ''
               or b.betalingskenmerk like concat('%', :zoekterm, '%')
               or b.externeReferentie = :zoekterm
               or lower(cast(d.uuid as string)) like lower(concat('%', :zoekterm, '%'))
            """;

    /**
     * Zoekt op (een deel van) het betalingskenmerk of het dossier-ID, of op het kenmerk bij de betaalprovider (bij
     * OBM het basket-id); een lege zoekterm geeft alles. Nieuwste eerst.
     */
    @Query(value = SELECT_LIST_DTO + ZOEKTERM + " order by b.id desc",
            countQuery = "select count(b) from BetalingEntity b join b.dossier d " + ZOEKTERM)
    Page<ListBetalingDto> search(@Param("zoekterm") String zoekterm, Pageable pageable);

    @Query("select count(b) from BetalingEntity b join b.dossier d " + ZOEKTERM)
    long count(@Param("zoekterm") String zoekterm);

    @Query(SELECT_LIST_DTO + " where d.uuid = :dossierId order by b.id")
    List<ListBetalingDto> findByDossierId(@Param("dossierId") UUID dossierId);
}
