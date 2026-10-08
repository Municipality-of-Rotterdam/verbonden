package nl.rotterdam.verbonden.core.features.dossier_administration.repository;

import nl.rotterdam.verbonden.core.features.dossier_administration.domain.InzageActie;
import nl.rotterdam.verbonden.core.persistence.DossierInzageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DossierInzageRepository extends JpaRepository<DossierInzageEntity, Long> {

    List<DossierInzageEntity> findByDossierUuidAndActie(UUID dossierUuid, InzageActie actie);
}
