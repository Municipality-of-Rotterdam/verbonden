package nl.rotterdam.verbonden.features.marriage_type_administration.repository;

import nl.rotterdam.verbonden.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.persistence.MarriageTypeLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MarriageTypeLocationRepository extends JpaRepository<MarriageTypeLocationEntity, Long> {

    Optional<MarriageTypeLocationEntity> findByMarriageType_Soort(CeremonieSoort soort);
}
