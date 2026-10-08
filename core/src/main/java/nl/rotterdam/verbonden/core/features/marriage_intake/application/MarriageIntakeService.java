package nl.rotterdam.verbonden.core.features.marriage_intake.application;

import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.ChangeIntakeDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierAccessOutcome;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.domain.Emailadres;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.TrouwboekjeKeuzeDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SaveExtrasDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.GetuigeDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.IntakeMarriageTypeDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.PartnerGegevensDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SaveGetuigenDto;
import nl.rotterdam.verbonden.core.domain.Telefoonnummer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface MarriageIntakeService {

    List<IntakeMarriageTypeDto> findAllMarriageTypes();

    List<PartnerGegevensDto> findPartnerGegevens(UUID dossierId);

    UUID create(CreateDossierDto dto);

    void updateIntake(UUID dossierId, ChangeIntakeDto dto);

    /**
     * Returns the UUID of the dossier in which the given BSN appears as bsn1 or bsn2,
     * or {@link Optional#empty()} when no such dossier exists.
     */
    Optional<UUID> findDossierIdByBsn(BurgerServiceNummer bsn);

    /**
     * Determines access to a requested dossier for the given BSN. Never changes a dossier: joining
     * as second partner requires an explicit {@link #acceptInvitation}, so that merely opening a
     * dossier link (a GET) does not link anyone to a dossier.
     * <ul>
     *   <li>If bsn matches bsn1 or bsn2 of the requested dossier:
     *       returns {@link DossierAccessOutcome.Scenario#GRANTED} with the requested dossier ID.</li>
     *   <li>If bsn belongs to a different existing dossier:
     *       returns {@link DossierAccessOutcome.Scenario#SWITCHED_DOSSIER} with that dossier's ID.</li>
     *   <li>If bsn is not in any dossier and the requested dossier has no bsn2 yet:
     *       returns {@link DossierAccessOutcome.Scenario#INVITED} with the requested dossier ID.</li>
     *   <li>If the requested dossier does not exist, or already has two BSNs:
     *       returns {@link DossierAccessOutcome.Scenario#NOT_AUTHORIZED} with a {@code null} dossier ID.</li>
     * </ul>
     */
    DossierAccessOutcome resolveAccess(UUID requestedDossierId, BurgerServiceNummer bsn);

    /**
     * Registers the BSN as second partner of the dossier, after the citizen explicitly accepted the
     * invitation. Re-checks the {@link DossierAccessOutcome.Scenario#INVITED} conditions, since the
     * dossier may have changed since the invitation was shown.
     *
     * @throws IllegalStateException when the BSN is not (or no longer) invited to this dossier
     */
    void acceptInvitation(UUID dossierId, BurgerServiceNummer bsn);

    DossierSamenvattingDto findByDossierId(UUID id);

    DossierStatus findStatus(UUID dossierId);

    /**
     * Legt de keuzes van het dossier definitief vast: het dossier krijgt de status
     * {@link DossierStatus#INGEDIEND} en kan daarna niet meer door de burger worden gewijzigd.
     *
     * @throws nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietWijzigbaarException
     *         wanneer het dossier geen {@link DossierStatus#CONCEPT} meer is
     * @throws nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietCompleetException
     *         wanneer nog niet alle keuzes en gegevens zijn ingevuld
     */
    void dienIn(UUID dossierId);

    /**
     * Geeft de prijs van de internationale huwelijksakte: de bij het indienen vastgelegde prijs, of — zolang
     * het dossier nog niet is ingediend — het tarief van vandaag.
     */
    BigDecimal findInternationaleAktePrijs(UUID dossierId);

    Set<LocalDate> findBeschikbareDatums(UUID dossierId, YearMonth maand);

    List<LocalDateTime> findBeschikbareSlots(UUID dossierId, YearMonth maand);

    Collection<LocalDateTime> findAllBeschikbareSlots(UUID dossierId);

    List<LocalTime> findBeschikbareTijden(UUID dossierId, LocalDate datum);

    void slaAfspraakOp(UUID dossierId, LocalDate datum, LocalTime startTijd);

    List<GetuigeDto> findGetuigen(UUID dossierId);

    void slaGetuigenOp(UUID dossierId, List<SaveGetuigenDto> getuigen);

    void slaGetuigeOp(UUID dossierId, SaveGetuigenDto getuige);

    /**
     * Legt de gekozen achternaam vast van de partner met {@code partnerBsn}. Zodra beide partners
     * gekoppeld zijn, mag elke partner dit ook voor de ander doen.
     */
    void slaPartnerGegevensOp(UUID dossierId, BurgerServiceNummer partnerBsn, String gekozenAchternaam);

    /**
     * Legt de contactgegevens vast van de partner met {@code partnerBsn}. Zodra beide partners gekoppeld
     * zijn, mag elke partner dit ook voor de ander doen; {@code versie} voorkomt dat ze daarbij elkaars
     * wijzigingen ongemerkt overschrijven.
     *
     * @param versie de {@link PartnerGegevensDto#versie()} waarop de wijziging is gebaseerd
     * @return de nieuwe versie, als basis voor een volgende wijziging
     * @throws org.springframework.dao.OptimisticLockingFailureException wanneer de contactgegevens
     *         intussen door iemand anders zijn gewijzigd
     */
    long slaContactGegevensOp(UUID dossierId, BurgerServiceNummer partnerBsn, long versie,
                              Telefoonnummer telefoonnummer, Emailadres emailadres);

    void delete(UUID dossierId);

    List<TrouwboekjeKeuzeDto> findActieveTrouwboekjes();

    SaveExtrasDto findExtrasSelecties(UUID dossierId);

    void slaExtrasOp(UUID dossierId, SaveExtrasDto dto);
}
