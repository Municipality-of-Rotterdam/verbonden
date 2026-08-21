package nl.rotterdam.verbonden.features.marriage_intake.repository;

import nl.rotterdam.verbonden.persistence.HuwelijksDossierEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DossierRepository extends JpaRepository<HuwelijksDossierEntity, Long> {

    Optional<HuwelijksDossierEntity> findByUuid(UUID uuid);

    Optional<HuwelijksDossierEntity> findByPartners_Bsn(String bsn);
}
