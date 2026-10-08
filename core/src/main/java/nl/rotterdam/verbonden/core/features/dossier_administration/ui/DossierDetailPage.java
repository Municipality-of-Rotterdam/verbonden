package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButtonAppearance;
import nl.rotterdam.verbonden.core.administration_common.RdFormFieldFileUpload;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.BestandNietToegestaanException;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.CreateDossierBestandDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DetailPartnerDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DossierBestandDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DossierBestandType;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ListDossierBestandDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietCompleetException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.GetuigeDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.PartnerGegevensDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SidebarExtraItemDto;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.lang.Bytes;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Alle gegevens van één dossier, met links naar de pagina's om onderdelen te wijzigen, de acties die bij de
 * status horen, en de bestanden die medewerkers aan het dossier hebben toegevoegd.
 */
public class DossierDetailPage extends DossierBeheerBasePage {

    private static final DateTimeFormatter DATUM = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATUM_TIJD = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final Locale NEDERLAND = Locale.of("nl", "NL");

    public DossierDetailPage(PageParameters params) {
        super(params);

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        PageParameters dossierParams = parametersVoor(dossierId);

        pageBody.add(
                new Label("titel", "Dossier " + dossierId),
                new BookmarkablePageLink<>("terugLink", DossierAdministrationPage.class),
                feedback,

                new Label("status", dossier.status().getLabel()),
                new Label("kanaal", dossier.kanaal().getLabel()),
                new Label("aangemaaktOp", datumTijd(dossier.aangemaaktOp())),
                new Label("aangemaaktDoor", "door " + dossier.aangemaaktDoor()).setVisible(dossier.aangemaaktDoor() != null),
                new Label("ingediendOp", datumTijd(dossier.ingediendOp())),
                new Label("nietCompleet", "Nog niet alles is ingevuld; het dossier kan nog niet worden ingediend.")
                        .setVisible(dossier.status() == DossierStatus.CONCEPT && !dossier.compleet()),
                new ActiesForm("actiesForm"),

                new BookmarkablePageLink<>("ceremonieWijzigen", DossierCeremonieUpdatePage.class, dossierParams),
                new Label("registratieType", dossier.registratieType().getLabel()),
                new Label("ceremonieSoort", dossier.ceremonieSoort().getLabel()),
                new Label("ceremoniePrijs", prijs(dossier.ceremoniePrijs())),
                new Label("locatie", tekst(dossier.locatieNaam())),

                new BookmarkablePageLink<>("afspraakWijzigen", DossierAfspraakUpdatePage.class, dossierParams),
                new Label("datumTijdHuwelijk", dossier.datumTijdHuwelijk() != null
                        ? datumTijd(dossier.datumTijdHuwelijk()) : "Nog niet gekozen"),

                new PartnersListView("partners", dossier.partners()),
                new Label("partner2NietGekoppeld", "Partner 2 is nog niet aan het dossier gekoppeld.")
                        .setVisible(dossier.partners().size() < 2),

                new BookmarkablePageLink<>("getuigenWijzigen", DossierGetuigenUpdatePage.class, dossierParams),
                new Label("getuigenAantal", dossier.getuigen().stream().filter(g -> g.naam() != null).count()
                        + " van " + dossier.ceremonieSoort().getAantalGetuigen() + " ingevuld"),
                new ListView<>("getuigen", dossier.getuigen()) {
                    @Override
                    protected void populateItem(ListItem<GetuigeDto> item) {
                        item.add(new Label("naam", tekst(item.getModelObject().naam())));
                    }
                },

                new BookmarkablePageLink<>("extrasWijzigen", DossierExtrasUpdatePage.class, dossierParams),
                new ListView<>("extras", dossier.extras()) {
                    @Override
                    protected void populateItem(ListItem<SidebarExtraItemDto> item) {
                        item.add(new Label("naam", item.getModelObject().naam()),
                                new Label("prijs", item.getModelObject().prijs() != null
                                        ? prijs(item.getModelObject().prijs()) : ""));
                    }
                },
                new Label("geenExtras", "Geen extra's gekozen").setVisible(dossier.extras().isEmpty()),
                new Label("totaalPrijs", prijs(dossier.totaalPrijs())),

                new BestandenListView("bestanden", dossier.bestanden()),
                new Label("geenBestanden", "Er zijn nog geen bestanden toegevoegd.")
                        .setVisible(dossier.bestanden().isEmpty()),
                new BestandToevoegenForm("bestandToevoegenForm")
        );
    }

    /**
     * Indienen (een concept dat compleet is), en accepteren of afwijzen (een ingediend dossier).
     */
    private class ActiesForm extends Form<Void> {

        ActiesForm(String id) {
            super(id);
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();

            RdButton indienen = new RdButton("indienen", Model.of("Indienen")) {
                @Override
                public void onSubmit() {
                    try {
                        dossierAdministrationService.dienIn(dossierId);
                        naarDetailPagina();
                    } catch (DossierNietCompleetException e) {
                        error("Het dossier is nog niet compleet.");
                    }
                }
            };
            indienen.setAppearance(RdButtonAppearance.PRIMARY_ACTION);
            indienen.setVisible(dossier.status() == DossierStatus.CONCEPT && dossier.compleet());

            RdButton accepteren = new RdButton("accepteren", Model.of("Accepteren")) {
                @Override
                public void onSubmit() {
                    dossierAdministrationService.accepteer(dossierId);
                    naarDetailPagina();
                }
            };
            accepteren.setAppearance(RdButtonAppearance.PRIMARY_ACTION);

            RdButton afwijzen = new RdButton("afwijzen", Model.of("Afwijzen")) {
                @Override
                public void onSubmit() {
                    dossierAdministrationService.wijsAf(dossierId);
                    naarDetailPagina();
                }
            };
            afwijzen.setAppearance(RdButtonAppearance.SECONDARY_ACTION);

            boolean ingediend = dossier.status() == DossierStatus.INGEDIEND;
            accepteren.setVisible(ingediend);
            afwijzen.setVisible(ingediend);

            add(indienen, accepteren, afwijzen);
        }
    }

    private class PartnersListView extends ListView<DetailPartnerDto> {

        PartnersListView(String id, List<DetailPartnerDto> partners) {
            super(id, partners);
        }

        @Override
        protected void populateItem(ListItem<DetailPartnerDto> item) {
            DetailPartnerDto partner = item.getModelObject();
            PartnerGegevensDto gegevens = partner.gegevens();
            boolean heeftBsn = gegevens.bsn() != null;

            item.add(
                    new Label("titel", "Partner " + gegevens.volgorde()),
                    new BookmarkablePageLink<>("wijzigen", DossierPartnerUpdatePage.class,
                            DossierPartnerUpdatePage.parametersVoor(dossierId, gegevens.volgorde())),
                    new Label("identificatieLabel", heeftBsn ? "BSN" : "Persoonsnummer (paspoort)"),
                    new Label("identificatie", heeftBsn
                            ? gegevens.bsn().getValue()
                            : gegevens.buitenlandsPersoonsnummer().getValue()),
                    new Label("bron", heeftBsn ? "Uit de BRP" : "Ingevoerd door medewerker (geen BSN)"),
                    new Label("achternaam", tekst(gegevens.achternaam())),
                    new Label("voornamen", tekst(gegevens.voornamen())),
                    new Label("geboortedatum", datum(gegevens.geboortedatum())),
                    new Label("geboorteplaats", tekst(gegevens.geboorteplaats())),
                    new Label("nationaliteit", tekst(gegevens.nationaliteit())),
                    new Label("burgerlijkeStaat", tekst(gegevens.burgerlijkeStaat())),
                    new Label("gekozenAchternaam", tekst(gegevens.gekozenAchternaam())),
                    new Label("telefoonnummer", gegevens.telefoonnummer() != null ? gegevens.telefoonnummer().getValue() : "-"),
                    new Label("emailadres", gegevens.emailadres() != null ? gegevens.emailadres().getValue() : "-"),
                    new Label("identiteit", partner.identiteitGecontroleerdDoor() != null
                            ? "Gecontroleerd door " + partner.identiteitGecontroleerdDoor()
                              + " op " + datumTijd(partner.identiteitGecontroleerdOp())
                            : "Ingelogd met DigiD")
            );
        }
    }

    private class BestandenListView extends ListView<ListDossierBestandDto> {

        BestandenListView(String id, List<ListDossierBestandDto> bestanden) {
            super(id, bestanden);
        }

        @Override
        protected void populateItem(ListItem<ListDossierBestandDto> item) {
            ListDossierBestandDto bestand = item.getModelObject();
            ResourceLink<Void> download = new ResourceLink<>("download", new BestandResource(bestand.id()));
            download.add(new Label("bestandsnaam", bestand.bestandsnaam()));

            Form<Void> verwijderForm = new Form<>("verwijderForm");
            RdButton verwijderen = new RdButton("verwijderen", Model.of("Verwijderen")) {
                @Override
                public void onSubmit() {
                    dossierAdministrationService.deleteBestand(dossierId, bestand.id());
                    naarDetailPagina();
                }
            };
            verwijderen.setAppearance(RdButtonAppearance.SECONDARY_ACTION);
            verwijderForm.add(verwijderen);

            item.add(
                    download,
                    new Label("grootte", Bytes.bytes(bestand.grootte()).toString(NEDERLAND)),
                    new Label("toegevoegd", bestand.toegevoegdDoor() + ", " + datumTijd(bestand.toegevoegdOp())),
                    verwijderForm
            );
        }
    }

    /**
     * Levert de inhoud van een bestand pas bij het downloaden, zodat de inhoud niet in de pagina (en de
     * Wicket-sessie) terechtkomt, en elke download in het inzagelog komt.
     */
    private class BestandResource extends AbstractResource {

        private final long bestandId;

        BestandResource(long bestandId) {
            this.bestandId = bestandId;
        }

        @Override
        protected ResourceResponse newResourceResponse(Attributes attributes) {
            DossierBestandDto bestand = dossierAdministrationService.findBestand(dossierId, bestandId);
            ResourceResponse response = new ResourceResponse();
            response.setContentType(bestand.bestandType().getContentType());
            response.setFileName(bestand.bestandsnaam());
            response.setContentDisposition(ContentDisposition.ATTACHMENT);
            response.setContentLength(bestand.inhoud().length);
            response.disableCaching();
            response.setWriteCallback(new WriteCallback() {
                @Override
                public void writeData(Attributes attributes) {
                    attributes.getResponse().write(bestand.inhoud());
                }
            });
            return response;
        }
    }

    private class BestandToevoegenForm extends Form<Void> {

        private final IModel<List<FileUpload>> uploads = new ListModel<>();
        private RdFormFieldFileUpload bestand;

        BestandToevoegenForm(String id) {
            super(id);
            setMultiPart(true);
            setMaxSize(Bytes.bytes(DossierBestandType.MAX_GROOTTE + 1024 * 1024));
            setFileMaxSize(Bytes.bytes(DossierBestandType.MAX_GROOTTE));
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            String extensies = DossierBestandType.alleExtensies().stream()
                    .map(extensie -> "." + extensie)
                    .collect(Collectors.joining(","));
            bestand = new RdFormFieldFileUpload("bestand", uploads, Model.of("Bestand"));
            bestand.withInput(input -> input.add(AttributeModifier.replace("accept", extensies)));
            add(
                    bestand,
                    new Label("toegestaan", "Documenten en afbeeldingen ("
                            + String.join(", ", DossierBestandType.alleExtensies()) + "), maximaal "
                            + DossierBestandType.MAX_GROOTTE / (1024 * 1024) + " MB."),
                    new RdButton("toevoegen", Model.of("Toevoegen"))
            );
        }

        @Override
        protected void onSubmit() {
            FileUpload upload = bestand.getFileUpload();
            if (upload == null) {
                error("Kies een bestand.");
                return;
            }
            try {
                dossierAdministrationService.createBestand(dossierId,
                        new CreateDossierBestandDto(upload.getClientFileName(), upload.getBytes()));
                naarDetailPagina();
            } catch (BestandNietToegestaanException e) {
                error(e.getMessage());
            }
        }
    }

    private static String tekst(String waarde) {
        return waarde != null && !waarde.isBlank() ? waarde : "-";
    }

    private static String datum(LocalDate datum) {
        return datum != null ? datum.format(DATUM) : "-";
    }

    static String datumTijd(LocalDateTime datumTijd) {
        return datumTijd != null ? datumTijd.format(DATUM_TIJD) : "-";
    }

    private static String prijs(BigDecimal prijs) {
        return prijs != null ? NumberFormat.getCurrencyInstance(NEDERLAND).format(prijs) : "-";
    }
}
