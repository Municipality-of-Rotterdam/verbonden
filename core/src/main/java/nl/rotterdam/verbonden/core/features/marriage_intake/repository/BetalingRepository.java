package nl.rotterdam.verbonden.core.features.marriage_intake.repository;

import nl.rotterdam.verbonden.core.persistence.BetalingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BetalingRepository extends JpaRepository<BetalingEntity, Long> {

    Optional<BetalingEntity> findByBetalingskenmerk(String betalingskenmerk);

    /**
     * De laatst gestarte betaalpoging van het dossier.
     */
    Optional<BetalingEntity> findFirstByDossier_UuidOrderByIdDesc(UUID dossierUuid);
}
