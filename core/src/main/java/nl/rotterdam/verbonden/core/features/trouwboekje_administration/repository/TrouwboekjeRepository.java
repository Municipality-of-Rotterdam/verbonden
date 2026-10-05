package nl.rotterdam.verbonden.core.features.trouwboekje_administration.repository;

import nl.rotterdam.verbonden.core.persistence.TrouwboekjeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrouwboekjeRepository extends JpaRepository<TrouwboekjeEntity, Long> {

    @Query("""
            SELECT t FROM TrouwboekjeEntity t
            WHERE t.active = true
              AND (t.startdatum IS NULL OR t.startdatum <= :vandaag)
              AND (t.einddatum IS NULL OR t.einddatum > :vandaag)
            ORDER BY t.naam
            """)
    List<TrouwboekjeEntity> findActief(@Param("vandaag") LocalDate vandaag);
}
