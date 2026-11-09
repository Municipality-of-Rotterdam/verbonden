package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.domain.Telefoonnummer;
import nl.rotterdam.verbonden.core.features.location_administration.domain.ListLocatieDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.AanmaakKanaal;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;

import java.io.Serializable;

public class BalieDossierFormDto implements Serializable {

    private AanmaakKanaal kanaal = AanmaakKanaal.BALIE;
    private RegistratieType registratieType = RegistratieType.HUWELIJK;
    private CeremonieSoort ceremonieSoort;
    private ListLocatieDto locatie;
    private boolean vrijeLocatie;
    private String vrijeLocatieNaam;
    private String vrijeLocatieAdres;
    private Telefoonnummer vrijeLocatieTelefoonnummer;
    private String vrijeLocatieContactpersoon;
    private BurgerServiceNummer bsnPartner1;
    private boolean identiteitPartner1Gecontroleerd;
    private boolean partner2ZonderBsn;
    private BurgerServiceNummer bsnPartner2;
    private final PersoonsgegevensFormDto partner2 = new PersoonsgegevensFormDto();
    private boolean identiteitPartner2Gecontroleerd;

    public AanmaakKanaal getKanaal() {
        return kanaal;
    }

    public void setKanaal(AanmaakKanaal kanaal) {
        this.kanaal = kanaal;
    }

    public RegistratieType getRegistratieType() {
        return registratieType;
    }

    public void setRegistratieType(RegistratieType registratieType) {
        this.registratieType = registratieType;
    }

    public CeremonieSoort getCeremonieSoort() {
        return ceremonieSoort;
    }

    public void setCeremonieSoort(CeremonieSoort ceremonieSoort) {
        this.ceremonieSoort = ceremonieSoort;
    }

    public ListLocatieDto getLocatie() {
        return locatie;
    }

    public void setLocatie(ListLocatieDto locatie) {
        this.locatie = locatie;
    }

    public boolean isVrijeLocatie() {
        return vrijeLocatie;
    }

    public void setVrijeLocatie(boolean vrijeLocatie) {
        this.vrijeLocatie = vrijeLocatie;
    }

    public String getVrijeLocatieNaam() {
        return vrijeLocatieNaam;
    }

    public void setVrijeLocatieNaam(String vrijeLocatieNaam) {
        this.vrijeLocatieNaam = vrijeLocatieNaam;
    }

    public String getVrijeLocatieAdres() {
        return vrijeLocatieAdres;
    }

    public void setVrijeLocatieAdres(String vrijeLocatieAdres) {
        this.vrijeLocatieAdres = vrijeLocatieAdres;
    }

    public Telefoonnummer getVrijeLocatieTelefoonnummer() {
        return vrijeLocatieTelefoonnummer;
    }

    public void setVrijeLocatieTelefoonnummer(Telefoonnummer vrijeLocatieTelefoonnummer) {
        this.vrijeLocatieTelefoonnummer = vrijeLocatieTelefoonnummer;
    }

    public String getVrijeLocatieContactpersoon() {
        return vrijeLocatieContactpersoon;
    }

    public void setVrijeLocatieContactpersoon(String vrijeLocatieContactpersoon) {
        this.vrijeLocatieContactpersoon = vrijeLocatieContactpersoon;
    }

    public BurgerServiceNummer getBsnPartner1() {
        return bsnPartner1;
    }

    public void setBsnPartner1(BurgerServiceNummer bsnPartner1) {
        this.bsnPartner1 = bsnPartner1;
    }

    public boolean isIdentiteitPartner1Gecontroleerd() {
        return identiteitPartner1Gecontroleerd;
    }

    public void setIdentiteitPartner1Gecontroleerd(boolean identiteitPartner1Gecontroleerd) {
        this.identiteitPartner1Gecontroleerd = identiteitPartner1Gecontroleerd;
    }

    public boolean isPartner2ZonderBsn() {
        return partner2ZonderBsn;
    }

    public void setPartner2ZonderBsn(boolean partner2ZonderBsn) {
        this.partner2ZonderBsn = partner2ZonderBsn;
    }

    public BurgerServiceNummer getBsnPartner2() {
        return bsnPartner2;
    }

    public void setBsnPartner2(BurgerServiceNummer bsnPartner2) {
        this.bsnPartner2 = bsnPartner2;
    }

    /** Persoonsnummer en persoonsgegevens van partner 2, wanneer die geen BSN heeft. */
    public PersoonsgegevensFormDto getPartner2() {
        return partner2;
    }

    public boolean isIdentiteitPartner2Gecontroleerd() {
        return identiteitPartner2Gecontroleerd;
    }

    public void setIdentiteitPartner2Gecontroleerd(boolean identiteitPartner2Gecontroleerd) {
        this.identiteitPartner2Gecontroleerd = identiteitPartner2Gecontroleerd;
    }
}
