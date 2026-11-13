package nl.rotterdam.verbonden.core.features.dossier_mail.repository;

import nl.rotterdam.verbonden.core.features.dossier_mail.domain.DossierMailStatus;
import nl.rotterdam.verbonden.core.persistence.DossierMailEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DossierMailRepository extends JpaRepository<DossierMailEntity, Long> {

    /** Haalt de pogingen in dezelfde query op, voor het aantal en het tijdstip van de laatste poging. */
    @EntityGraph(attributePaths = "pogingen")
    List<DossierMailEntity> findByDossier_UuidOrderByAangemaaktOpDescIdDesc(UUID dossierUuid);

    Optional<DossierMailEntity> findByIdAndDossier_Uuid(long id, UUID dossierUuid);

    @Query("select m.id from DossierMailEntity m where m.status = :status and m.volgendePogingOp <= :tijdstip "
            + "order by m.volgendePogingOp, m.id")
    List<Long> findIdsKlaarVoorVerzending(DossierMailStatus status, LocalDateTime tijdstip);
}
