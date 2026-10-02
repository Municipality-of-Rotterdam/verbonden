package nl.rotterdam.verbonden.core.identity;

import java.time.LocalDate;

/**
 * @param officieleNaam de naam waarmee de burger wordt aangesproken, bijv. in de header:
 *                      voorletters + achternaam ("E.J. van Muiswinkel"), inclusief tussenvoegsel,
 *                      adellijke titel en naamgebruik. Wordt kant-en-klaar door de adapter aangeleverd
 *                      (in Rotterdam: {@code NatuurlijkPersoonNaam.getOfficialName()}), zodat de naam
 *                      overal hetzelfde is als in Mijn Loket.
 */
public record PersonInfo(
        String achternaam,
        String voornamen,
        String officieleNaam,
        LocalDate geboortedatum,
        String geboorteplaats,
        String nationaliteit,
        String burgerlijkeStaat
) {
}
