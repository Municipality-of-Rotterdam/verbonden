package nl.rotterdam.verbonden.features.marriage_intake.repository;

import nl.rotterdam.verbonden.persistence.AfspraakEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AfspraakRepository extends JpaRepository<AfspraakEntity, Long> {

    Optional<AfspraakEntity> findFirstByDossier_Id(long dossierId);

    List<AfspraakEntity> findByLocatie_IdAndDatum(long locatieId, LocalDate datum);

    void deleteByDossier_Id(long dossierId);
}
