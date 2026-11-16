package nl.rotterdam.verbonden.core.online_betalen;

/**
 * Waarvoor een betaling is.
 */
public enum BetaalSoort {

    /** De eerste betaling, waarmee het paar de aanvraag indient. */
    REGISTRATIE,

    /**
     * Een betaling voor een betaalmutatie: een wijziging na het indienen, bijvoorbeeld een duurder trouwboekje of
     * alsnog een internationale akte. De burger start die op de statuspagina, bij "Te betalen".
     */
    MUTATIE
}
