package nl.rotterdam.verbonden.core.identity;

/**
 * Rolconstanten die {@link AuthenticatedUser#getRoles()} kan teruggeven.
 */
public enum Roles {

    /**
    vbr_beheer (FB) - Functioneel beheer
        Producten toevoegen/wijzigen (trouwboekjes, teksten, prijzen); Locaties toevoegen/wijzigen (teksten, prijzen e.d.); documenten verwijderen, afgesloten dossier heropenen, status aanpassen, rapportages maken
    vbr_superuser (Sylvia, Joan) - Functioneel beheer
        Producten toevoegen/wijzigen (trouwboekjes, teksten, prijzen); Locaties toevoegen/wijzigen (teksten, prijzen e.d.); documenten verwijderen, afgesloten dossier heropenen, status aanpassen, rapportages maken
    vbr_keyuser (consulenten)	- Uitgebreide rechten
        Geavanceerde dossiermutaties;  Agenda blokken/openen
    vbr_werkverdeler (senioren: Sandra, Maria, Silvia)
        dossiers op naam medewerker BS zetten, betalingen raadplegen/crediteren (digitale kassa Worldline)
    vbr_planner (Artie, Silvia)
        Trouwlijst maken
    vbr_medewerker - Standaard medewerker Burgelijke Stand
        Huwelijken bekijken en aanmaken; Documenten toevoegen; Afhandelen huwelijksdossiers; Babs toewijzen aan huwelijk.
    vbr_babs - Trouwambtenaar
        (BABS)	Zaken op naam zoeken; Zaken inzien; Zaken overdragen aan andere BABS; Overzicht van eigen zaken maken
    **/

    BEHEER,
    SUPERUSER,
    KEYUSER,
    WERKVERDELER,
    PLANNER,
    MEDEWERKER,
    BABS,
}
