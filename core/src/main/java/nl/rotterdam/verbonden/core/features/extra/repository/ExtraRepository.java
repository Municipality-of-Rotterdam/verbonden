package nl.rotterdam.verbonden.core.features.extra.repository;

import nl.rotterdam.verbonden.core.features.extra.domain.ExtraType;
import nl.rotterdam.verbonden.core.persistence.ExtraEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExtraRepository extends JpaRepository<ExtraEntity, Long> {

    long countByType(ExtraType type);

    @Query("""
            SELECT e FROM ExtraEntity e
            WHERE e.type = :type
              AND e.active = true
              AND (e.startdatum IS NULL OR e.startdatum <= :vandaag)
              AND (e.einddatum IS NULL OR e.einddatum > :vandaag)
            ORDER BY e.naam
            """)
    List<ExtraEntity> findActiefByType(@Param("type") ExtraType type, @Param("vandaag") LocalDate vandaag);
}
