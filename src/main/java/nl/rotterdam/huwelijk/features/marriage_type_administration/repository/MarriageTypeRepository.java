package nl.rotterdam.huwelijk.features.marriage_type_administration.repository;

import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_type_administration.domain.ListMarriageTypeDto;
import nl.rotterdam.huwelijk.persistence.MarriageTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarriageTypeRepository extends JpaRepository<MarriageTypeEntity, Long> {

    Optional<MarriageTypeEntity> findBySoort(CeremonieSoort soort);

    @Query("""
            SELECT new nl.rotterdam.huwelijk.features.marriage_type_administration.domain.ListMarriageTypeDto(
                m.id, m.soort, m.titel, m.prijs, l.naam, m.active)
            FROM MarriageTypeEntity m
            LEFT JOIN m.location mtl
            LEFT JOIN mtl.locatie l
            ORDER BY m.titel ASC
            """)
    List<ListMarriageTypeDto> findAllProjected();
}
