package nl.rotterdam.verbonden.core.identity;

import java.util.List;

/**
 * Rolconstanten die {@link AuthenticatedUser#getRoles()} kan teruggeven.
 *
 * <p>De rollen komen overeen met de groepen {@code vbr_<rol>} van de gemeente. Welke pagina's een rol mag
 * openen staat in de {@code @AuthorizeInstantiation}-annotaties van de beheerpagina's. Het zijn {@code String}-
 * constanten (geen enum), zodat ze in annotaties gebruikt kunnen worden.
 */
public final class Roles {

    /**
     * Functioneel beheer: producten (trouwboekjes, teksten, prijzen) en locaties toevoegen en wijzigen, documenten
     * verwijderen, afgesloten dossiers heropenen, status aanpassen en rapportages maken.
     */
    public static final String BEHEER = "BEHEER";

    /** Functioneel beheer, met dezelfde rechten als {@link #BEHEER}. */
    public static final String SUPERUSER = "SUPERUSER";

    /** Consulent met uitgebreide rechten: geavanceerde dossiermutaties en agenda blokkeren/openen. */
    public static final String KEYUSER = "KEYUSER";

    /** Senior medewerker: dossiers op naam van een medewerker zetten en betalingen raadplegen/crediteren. */
    public static final String WERKVERDELER = "WERKVERDELER";

    /** Maakt de trouwlijst. */
    public static final String PLANNER = "PLANNER";

    /**
     * Standaard medewerker Burgerlijke Stand: huwelijken bekijken en aanmaken, documenten toevoegen,
     * huwelijksdossiers afhandelen en een BABS aan een huwelijk toewijzen.
     */
    public static final String MEDEWERKER = "MEDEWERKER";

    /**
     * Buitengewoon ambtenaar van de burgerlijke stand (trouwambtenaar): zaken op naam zoeken en inzien, zaken
     * overdragen aan een andere BABS en een overzicht van eigen zaken maken.
     */
    public static final String BABS = "BABS";

    /** Alle rollen hierboven. Voeg een nieuwe rol ook hier toe, zodat de autorisatietests hem meenemen. */
    public static final List<String> ALLE =
            List.of(BEHEER, SUPERUSER, KEYUSER, WERKVERDELER, PLANNER, MEDEWERKER, BABS);

    private Roles() {
    }
}
