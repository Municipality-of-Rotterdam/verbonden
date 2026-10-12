package nl.rotterdam.verbonden.core.features.marriage_intake.repository;

import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.persistence.HuwelijksDossierEntity;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DossierRepository extends JpaRepository<HuwelijksDossierEntity, Long> {

    Optional<HuwelijksDossierEntity> findByUuid(UUID uuid);

    /**
     * Een BSN heeft hoogstens één dossier met een status uit {@link DossierStatus#ACTIEF}; afgesloten dossiers
     * blijven bewaard en komen dus vaker voor.
     */
    Optional<HuwelijksDossierEntity> findByPartners_BsnAndStatusIn(BurgerServiceNummer bsn,
                                                                    Collection<DossierStatus> statussen);
}
