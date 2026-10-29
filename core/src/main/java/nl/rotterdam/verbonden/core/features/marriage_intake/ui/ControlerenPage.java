package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.GetuigeDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.OntbrekendOnderdeel;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.PartnerGegevensDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SidebarExtraItemDto;
import nl.rotterdam.nl_design_system.wicket.components.alert.RdAlert;
import nl.rotterdam.nl_design_system.wicket.components.alert.RdAlertType;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wicketstuff.minis.behavior.EnabledModelBehavior;
import org.wicketstuff.minis.behavior.VisibleModelBehavior;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;

public class ControlerenPage extends IntakeBasePage {

    private static final DateTimeFormatter DATUM_FORMAT =
            DateTimeFormatter.ofPattern("d MMMM yyyy 'om' HH:mm", Locale.forLanguageTag("nl-NL"));

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    private final IModel<DossierSamenvattingDto> dossierModel =
            LoadableDetachableModel.of(() -> marriageIntakeService.findByDossierId(dossierId));
    private IModel<List<PartnerGegevensDto>> partnerGegevensModel;
    private IModel<List<GetuigeDto>> getuigenModel;
    // Eenmalige melding: wordt na het renderen gewist, zodat hij na een refresh niet opnieuw verschijnt
    private boolean betalingMislukt;

    /**
     * De controleerpagina met een melding dat de betaling niet is gelukt.
     */
    static ControlerenPage naMislukteBetaling(UUID dossierId) {
        ControlerenPage page = new ControlerenPage();
        page.getPageParameters().mergeWith(makeDossierPageParameters(dossierId));
        page.betalingMislukt = true;
        return page;
    }

    @Override
    protected IntakeStep getActiveStep() {
        return IntakeStep.CONTROLEREN;
    }

    @Override
    protected boolean hasIntakeContentBorder() {
        return false;
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new ResourceModel("intake.page.title.controleren");
    }

    @Override
    protected IModel<String> getStepHeadingModel() {
        return new ResourceModel("controleren.heading");
    }

    @Override
    protected IModel<String> getStepIntroModel() {
        return new ResourceModel("controleren.intro");
    }

    @Override
    protected IModel<DossierSamenvattingDto> getSidebarDossierModel() {
        return dossierModel;
    }

    @Override
    protected IModel<List<PartnerGegevensDto>> getSidebarPartnerGegevensModel() {
        return partnerGegevensModel();
    }

    @Override
    protected IModel<List<GetuigeDto>> getSidebarGetuigenModel() {
        return getuigenModel();
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        IModel<List<SidebarExtraItemDto>> extrasModel = dossierModel.map(dossier ->
                dossier.extras() == null ? List.of() : dossier.extras());
        IModel<Boolean> compleetModel = dossierModel.map(dossier ->
                dossier.id() != null && dossier.compleet());

        pageBody.add(
                new RdAlert("betalingMislukt", new ResourceModel("controleren.betaling.mislukt"), RdAlertType.WARNING)
                        .add(new VisibleModelBehavior(LambdaModel.of(() -> betalingMislukt))),
                new BookmarkablePageLink<>("wijzigDag", DeDagPage.class, parameters()),
                new BookmarkablePageLink<>("wijzigGegevens", JullieGegevensPage.class, parameters()),
                new BookmarkablePageLink<>("wijzigGetuigen", DeGetuigenPage.class, parameters()),
                new BookmarkablePageLink<>("wijzigExtras", ExtrasPage.class, parameters()),
                new Label("registratieType", dossierModel.map(dossier -> dossier.registratieType().getLabel())),
                new Label("ceremonieSoort", dossierModel.map(dossier -> dossier.ceremonieSoort().getLabel())),
                visibleWhen("ceremoniePrijsContainer", dossierModel.map(dossier -> dossier.prijs() != null),
                        new Label("ceremoniePrijs", dossierModel.map(dossier ->
                                Objects.requireNonNull(dossier.prijs()).format()))),
                visibleWhen("datumRij", dossierModel.map(dossier -> dossier.datumTijdHuwelijk() != null),
                        new Label("datum", dossierModel.map(dossier ->
                                Objects.requireNonNull(dossier.datumTijdHuwelijk()).format(DATUM_FORMAT)))),
                visibleWhen("locatieRij", dossierModel.map(dossier ->
                                dossier.huwelijksLocatie() != null && !dossier.huwelijksLocatie().isBlank()),
                        new Label("locatie", dossierModel.map(dossier ->
                                Objects.requireNonNull(dossier.huwelijksLocatie())))),
                new ListView<>("partners", partnerGegevensModel()) {
                    @Override
                    protected void populateItem(ListItem<PartnerGegevensDto> item) {
                        item.add(new Label("naamkeuze", naamkeuze(item.getModelObject())));
                    }
                },
                new ListView<>("getuigen", getuigenModel()) {
                    @Override
                    protected void populateItem(ListItem<GetuigeDto> item) {
                        item.add(new Label("getuigeNaam", item.getModelObject().naam()));
                    }
                },
                new ListView<>("extras", extrasModel) {
                    @Override
                    protected void populateItem(ListItem<SidebarExtraItemDto> item) {
                        SidebarExtraItemDto extra = item.getModelObject();
                        item.add(new Label("extraNaam", extra.naam()));
                        item.add(visibleWhen("extraPrijsContainer",
                                item.getModel().map(waarde -> waarde.prijs() != null),
                                new Label("extraPrijs", item.getModel().map(waarde ->
                                        Objects.requireNonNull(waarde.prijs()).format()))));
                    }
                },
                new WebMarkupContainer("geenExtras")
                        .add(new VisibleModelBehavior(extrasModel.map(List::isEmpty))),
                visibleWhen("totaalPrijsRij", dossierModel.map(dossier -> dossier.totalPrijs() != null),
                        new Label("totaalPrijs", dossierModel.map(dossier ->
                                Objects.requireNonNull(dossier.totalPrijs()).format()))),
                new BevestigForm("bevestigForm", compleetModel)
        );
    }

    @Override
    protected void onAfterRender() {
        super.onAfterRender();
        betalingMislukt = false;
    }

    private WebMarkupContainer visibleWhen(String id, IModel<Boolean> visible, Component child) {
        WebMarkupContainer container = new WebMarkupContainer(id);
        container.add(new VisibleModelBehavior(visible));
        container.add(child);
        return container;
    }

    private IModel<List<PartnerGegevensDto>> partnerGegevensModel() {
        if (partnerGegevensModel == null) {
            partnerGegevensModel = LoadableDetachableModel.of(() -> marriageIntakeService.findPartnerGegevens(dossierId));
        }
        return partnerGegevensModel;
    }

    private IModel<List<GetuigeDto>> getuigenModel() {
        if (getuigenModel == null) {
            getuigenModel = LoadableDetachableModel.of(() -> marriageIntakeService.findGetuigen(dossierId));
        }
        return getuigenModel;
    }

    private PageParameters parameters() {
        return makeDossierPageParameters(dossierId);
    }

    private String naamkeuze(PartnerGegevensDto partner) {
        String naam = Stream.of(partner.voornamen(), partner.achternaam())
                .filter(Objects::nonNull)
                .filter(waarde -> !waarde.isBlank())
                .collect(Collectors.joining(" "));
        return Optional.ofNullable(partner.gekozenAchternaam())
                .filter(gekozen -> !gekozen.isBlank())
                .map(gekozen -> naam + " - " + gekozen)
                .orElse(naam);
    }

    private class BevestigForm extends Form<Void> {

        private final IModel<Boolean> compleetModel;

        BevestigForm(String id, IModel<Boolean> compleetModel) {
            super(id);
            this.compleetModel = compleetModel;
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            add(
                    new RdButton("bevestigButton", new ResourceModel("controleren.bevestig")) {
                        @Override
                        public void onSubmit() {
                            setResponsePage(BetalingPage.class, parameters());
                        }

                    }.add(new EnabledModelBehavior(compleetModel)),
                    new NogNietCompleetHint(compleetModel)
            );
        }
    }

    private class NogNietCompleetHint extends WebMarkupContainer {

        private final IModel<Boolean> compleetModel;

        NogNietCompleetHint(IModel<Boolean> compleetModel) {
            super("nogNietCompleetHint");
            this.compleetModel = compleetModel;
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            IModel<List<OntbrekendOnderdeel>> ontbrekendModel =
                    dossierModel.map(DossierSamenvattingDto::ontbrekendeOnderdelen);
            add(
                    new WebMarkupContainer("algemeen")
                            .add(new VisibleModelBehavior(ontbrekendModel.map(List::isEmpty))),
                    new WebMarkupContainer("specifiek")
                            .add(new OntbrekendeOnderdelen(ontbrekendModel))
                            .add(new VisibleModelBehavior(ontbrekendModel.map(l -> !l.isEmpty())))
            );
        }

        @Override
        protected void onConfigure() {
            super.onConfigure();
            setVisible(!compleetModel.getObject());
        }
    }

    private class OntbrekendeOnderdelen extends ListView<OntbrekendOnderdeel> {

        OntbrekendeOnderdelen(IModel<List<OntbrekendOnderdeel>> model) {
            super("onderdelen", model);
        }

        @Override
        protected void populateItem(ListItem<OntbrekendOnderdeel> item) {
            OntbrekendOnderdeel onderdeel = item.getModelObject();
            item.add(new Link<Void>("link") {
                @Override
                public void onClick() {
                    setResponsePage(IntakeStep.pageVoor(onderdeel), parameters());
                }
            }.add(new Label("label", new ResourceModel("intake.sidebar.ontbrekend." + onderdeel.name()))));
        }

    }

}
