package nl.rotterdam.verbonden.core.features.marriage_intake.application;

import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetaaloverzichtDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetalingStartResultaat;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetalingStandDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.TeBetalenDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Online betalen van een huwelijksdossier. Het dossier wordt ingediend zodra de betaalprovider meldt dat de
 * betaling gelukt is (zie {@link nl.rotterdam.verbonden.core.online_betalen.BetaalUitslagVerwerker}), niet
 * wanneer de burger terugkomt van de betaalpagina.
 */
public interface OnlineBetalenService {

    /**
     * Start de betaling van de totaalprijs van het dossier. Kost het dossier niets, dan wordt het meteen ingediend.
     *
     * @param betaler   de ingelogde partner die betaalt
     * @param retourUrl absolute URL waar de burger na het betalen terugkomt
     * @throws nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietWijzigbaarException
     *         wanneer het dossier al is ingediend
     * @throws nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietCompleetException
     *         wanneer nog niet alle keuzes en gegevens zijn ingevuld
     * @throws nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetalingLooptAlException
     *         wanneer er al een betaling loopt waarvan de uitslag nog niet binnen is
     * @throws nl.rotterdam.verbonden.core.online_betalen.OnlineBetalenNietBeschikbaarException
     *         wanneer de betaalprovider de betaling nu niet kan aanmaken
     */
    BetalingStartResultaat startBetaling(UUID dossierId, BurgerServiceNummer betaler, String retourUrl);

    /**
     * Start de betaling van een betaalmutatie van een ingediend dossier.
     *
     * @param betaler   de ingelogde partner die betaalt
     * @param retourUrl absolute URL waar de burger na het betalen terugkomt
     * @return de URL van de betaalprovider
     * @throws nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetaalmutatieNietTeBetalenException
     *         wanneer de mutatie al betaald of verwijderd is, of het dossier geen betaalmutaties (meer) kent
     * @throws nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetalingLooptAlException
     *         wanneer er voor de mutatie al een betaling loopt
     * @throws nl.rotterdam.verbonden.core.online_betalen.OnlineBetalenNietBeschikbaarException
     *         wanneer de betaalprovider de betaling nu niet kan aanmaken
     */
    String startMutatieBetaling(UUID dossierId, long betaalmutatieId, BurgerServiceNummer betaler, String retourUrl);

    /**
     * De betaalmutaties die nog niet betaald zijn. Leeg zolang het dossier geen betaalmutaties kent
     * (zie {@link nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus#BETAALMUTATIES_MOGELIJK}).
     */
    List<TeBetalenDto> findTeBetalen(UUID dossierId);

    /**
     * Wat het paar heeft betaald en nog moet betalen.
     */
    BetaaloverzichtDto findBetaaloverzicht(UUID dossierId);

    /**
     * Soort en stand van de laatst gestarte betaalpoging, of {@link Optional#empty()} als er nog niet betaald is.
     */
    Optional<BetalingStandDto> findLaatsteBetaling(UUID dossierId);
}
