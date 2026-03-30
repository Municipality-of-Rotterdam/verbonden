package nl.rotterdam.huwelijk.features.marriage_type_administration.repository;

import nl.rotterdam.huwelijk.features.marriage_type_administration.domain.ListMarriageTypeDto;
import nl.rotterdam.huwelijk.persistence.MarriageTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarriageTypeRepository extends JpaRepository<MarriageTypeEntity, Long> {

    @Query("""
            SELECT new nl.rotterdam.huwelijk.features.marriage_type_administration.domain.ListMarriageTypeDto(
                m.id, m.soort, m.titel, m.prijs)
            FROM MarriageTypeEntity m
            ORDER BY m.titel ASC
            """)
    List<ListMarriageTypeDto> findAllProjected();
}
