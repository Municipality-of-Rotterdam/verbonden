package nl.rotterdam.huwelijk.features.marriage_intake.repository;

import nl.rotterdam.huwelijk.persistence.HuwelijksDossierEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DossierRepository extends JpaRepository<HuwelijksDossierEntity, Long> {
}
