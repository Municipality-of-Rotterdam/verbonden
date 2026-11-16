package nl.rotterdam.verbonden.core.features.betaling_administration.application;

import nl.rotterdam.verbonden.core.features.betaling_administration.domain.ListBetalingDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Online betalingen inzien. Crediteren valt buiten de applicatie.
 */
public interface BetalingAdministrationService {

    /**
     * Zoekt op (een deel van) het betalingskenmerk of het dossier-ID, of op het kenmerk bij de betaalprovider (bij
     * OBM het basket-id). Zo is een betaling die je bij OBM of de betaalprovider ziet, met het dossier erbij terug
     * te vinden. Een lege zoekterm (ook {@code null}) geeft alles. Nieuwste eerst.
     */
    Page<ListBetalingDto> search(String zoekterm, Pageable pageable);

    long count(String zoekterm);

    /** Alle betaalpogingen van een dossier, oudste eerst. */
    List<ListBetalingDto> findByDossierId(UUID dossierId);
}
