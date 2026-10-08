package nl.rotterdam.verbonden.core.features.dossier_administration.application;

import nl.rotterdam.verbonden.core.features.dossier_administration.domain.BestandNietToegestaanException;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ChangeCeremonieDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ChangeExtrasDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ChangePartnerDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.CreateBalieDossierDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.CreateDossierBestandDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DossierBestandDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DossierDetailDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ListDossierDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.PartnerHeeftAlDossierException;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.PartnerNietInBrpException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietWijzigbaarException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SaveGetuigenDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.TrouwboekjeKeuzeDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Dossierbeheer door medewerkers. Anders dan de burger mag een medewerker een dossier in elke status wijzigen,
 * bijvoorbeeld wanneer het paar na het indienen nog iets aan het huwelijk wil veranderen.
 */
public interface DossierAdministrationService {

    Page<ListDossierDto> search(String zoekterm, Pageable pageable);

    long count(String zoekterm);

    /**
     * Alle gegevens van een dossier. Legt in het inzagelog vast dat de ingelogde medewerker het dossier heeft
     * bekeken (AVG).
     *
     * @throws IllegalArgumentException wanneer het dossier niet bestaat
     */
    DossierDetailDto findDetail(UUID dossierId);

    /**
     * Maakt een dossier aan voor twee partners die aan de balie of in een videogesprek aanwezig zijn. Beide
     * partners worden direct aan het dossier gekoppeld; de ingelogde medewerker wordt vastgelegd als degene
     * die hun identiteit heeft gecontroleerd. De partner(s) met BSN kunnen daarna zelf online verder.
     *
     * @return het id van het nieuwe dossier
     * @throws PartnerNietInBrpException      wanneer bij een BSN geen persoon in de BRP staat
     * @throws PartnerHeeftAlDossierException wanneer een partner al in een dossier staat
     */
    UUID create(CreateBalieDossierDto dto);

    void updateCeremonie(UUID dossierId, ChangeCeremonieDto dto);

    /**
     * De tijden waarop het huwelijk van dit dossier op {@code datum} nog kan plaatsvinden.
     */
    List<LocalTime> findBeschikbareTijden(UUID dossierId, LocalDate datum);

    /**
     * Verzet het huwelijk naar een vrij tijdslot.
     *
     * @throws IllegalStateException wanneer het tijdslot niet (meer) vrij is
     */
    void updateAfspraak(UUID dossierId, LocalDate datum, LocalTime startTijd);

    /**
     * Wijzigt de gekozen achternaam en contactgegevens van een partner, en voor een partner zonder BSN ook diens
     * persoonsnummer en persoonsgegevens.
     *
     * @throws org.springframework.dao.OptimisticLockingFailureException wanneer de contactgegevens intussen door
     *         iemand anders zijn gewijzigd
     */
    void updatePartner(UUID dossierId, ChangePartnerDto dto);

    /**
     * Vervangt de getuigen; getuigen zonder naam worden weggelaten.
     */
    void updateGetuigen(UUID dossierId, List<SaveGetuigenDto> getuigen);

    List<TrouwboekjeKeuzeDto> findTrouwboekjes();

    void updateExtras(UUID dossierId, ChangeExtrasDto dto);

    /**
     * Dient een (aan de balie aangemaakt) dossier in, zodat het beoordeeld kan worden.
     *
     * @throws DossierNietWijzigbaarException wanneer het dossier niet de status {@link DossierStatus#CONCEPT} heeft
     * @throws nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietCompleetException
     *         wanneer nog niet alles is ingevuld
     */
    void dienIn(UUID dossierId);

    /**
     * Accepteert een ingediend dossier.
     *
     * @throws DossierNietWijzigbaarException wanneer het dossier niet de status {@link DossierStatus#INGEDIEND} heeft
     */
    void accepteer(UUID dossierId);

    /**
     * Wijst een ingediend dossier af.
     *
     * @throws DossierNietWijzigbaarException wanneer het dossier niet de status {@link DossierStatus#INGEDIEND} heeft
     */
    void wijsAf(UUID dossierId);

    /**
     * Voegt een bestand toe aan het dossier, namens de ingelogde medewerker.
     *
     * @return het id van het bestand
     * @throws BestandNietToegestaanException wanneer het soort bestand niet is toegestaan, de inhoud niet bij de
     *                                        extensie past, of het bestand te groot is
     */
    long createBestand(UUID dossierId, CreateDossierBestandDto dto);

    /**
     * Een bestand van het dossier, met inhoud. Legt in het inzagelog vast dat de ingelogde medewerker het
     * bestand heeft gedownload (AVG).
     *
     * @throws IllegalArgumentException wanneer het bestand niet bij dit dossier hoort
     */
    DossierBestandDto findBestand(UUID dossierId, long bestandId);

    void deleteBestand(UUID dossierId, long bestandId);
}
