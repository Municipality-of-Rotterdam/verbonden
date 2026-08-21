package nl.rotterdam.huwelijk.identity;

import java.util.Optional;

/**
 * Zoekt persoonsgegevens op bij een BSN. Vervangt de inline mock-persoonsdata
 * die vroeger in {@code MarriageIntakeServiceImpl} stond; de daadwerkelijke
 * implementatie (mock, of een echte Haal Centraal-koppeling) komt uit een
 * adapter-module.
 */
public interface PersonLookupService {

    Optional<PersonInfo> findByBsn(String bsn);
}
