package nl.rotterdam.huwelijk.features.location_administration.repository;

import nl.rotterdam.huwelijk.features.location_administration.domain.HuwelijksType;
import nl.rotterdam.huwelijk.persistence.HuwelijkstypeLocatieEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HuwelijkstypeLocatieRepository extends JpaRepository<HuwelijkstypeLocatieEntity, HuwelijksType> {

    Optional<HuwelijkstypeLocatieEntity> findById(HuwelijksType huwelijkstype);
}
