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
import nl.rotterdam.nl_design_system.wicket.components.heading.RdHeading;
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
import org.wicketstuff.minis.behavior.VisibleModelBehavior;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class ControlerenPage extends IntakeBasePage {

    private static final DateTimeFormatter DATUM_FORMAT =
            DateTimeFormatter.ofPattern("d MMMM yyyy 'om' HH:mm", Locale.forLanguageTag("nl-NL"));

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    private IModel<DossierSamenvattingDto> dossierModel;
    private IModel<List<PartnerGegevensDto>> partnerGegevensModel;
    private IModel<List<GetuigeDto>> getuigenModel;

    @Override
    protected IntakeStep getActiveStep() {
        return IntakeStep.CONTROLEREN;
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new ResourceModel("intake.page.title.controleren");
    }

    @Override
    protected IModel<DossierSamenvattingDto> getSidebarDossierModel() {
        return dossierModel();
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

        IModel<List<SidebarExtraItemDto>> extrasModel =
                LambdaModel.of(() -> dossierModel().getObject().extras() == null
                        ? List.of() : dossierModel().getObject().extras());

        pageBody.add(
                new RdHeading("heading", getString("controleren.heading"), 1),
                new Label("intro", new ResourceModel("controleren.intro")),
                new RdAlert("betalingMislukt", new ResourceModel("controleren.betaling.mislukt"), RdAlertType.WARNING)
                        .add(new VisibleModelBehavior(LambdaModel.of(
                                () -> getPageParameters().get("betalingMislukt").toBoolean(false)))),
                new BookmarkablePageLink<>("wijzigDag", DeDagPage.class, parameters()),
                new BookmarkablePageLink<>("wijzigGegevens", JullieGegevensPage.class, parameters()),
                new BookmarkablePageLink<>("wijzigGetuigen", DeGetuigenPage.class, parameters()),
                new BookmarkablePageLink<>("wijzigExtras", ExtrasPage.class, parameters()),
                new Label("registratieType", LambdaModel.of(() ->
                        dossierModel().getObject().registratieType().getLabel())),
                new Label("ceremonieSoort", LambdaModel.of(() ->
                        dossierModel().getObject().ceremonieSoort().getLabel())),
                new WebMarkupContainer("ceremoniePrijsContainer")
                        .add(new VisibleModelBehavior(LambdaModel.of(() -> dossierModel().getObject().prijs() != null)))
                        .add(new Label("ceremoniePrijs", LambdaModel.of(() ->
                                IntakePriceFormatter.format(Objects.requireNonNull(dossierModel().getObject().prijs()))))),
                new WebMarkupContainer("datumRij")
                        .add(new VisibleModelBehavior(LambdaModel.of(() ->
                                dossierModel().getObject().datumTijdHuwelijk() != null)))
                        .add(new Label("datum", LambdaModel.of(() -> {
                    var datum = dossierModel().getObject().datumTijdHuwelijk();
                    return Objects.requireNonNull(datum).format(DATUM_FORMAT);
                }))),
                new WebMarkupContainer("locatieRij")
                        .add(new VisibleModelBehavior(LambdaModel.of(() -> {
                            String locatie = dossierModel().getObject().huwelijksLocatie();
                            return locatie != null && !locatie.isBlank();
                        })))
                        .add(new Label("locatie", LambdaModel.of(() ->
                                Objects.requireNonNull(dossierModel().getObject().huwelijksLocatie())))),
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
                        item.add(new WebMarkupContainer("extraPrijsContainer")
                                .add(new VisibleModelBehavior(LambdaModel.of(() -> extra.prijs() != null)))
                                .add(new Label("extraPrijs", LambdaModel.of(() ->
                                        IntakePriceFormatter.format(Objects.requireNonNull(extra.prijs()))))));
                    }
                },
                new WebMarkupContainer("geenExtras")
                        .add(new VisibleModelBehavior(extrasModel.map(List::isEmpty))),
                new WebMarkupContainer("totaalPrijsRij")
                        .add(new VisibleModelBehavior(LambdaModel.of(() ->
                                dossierModel().getObject().totalPrijs() != null)))
                        .add(new Label("totaalPrijs", LambdaModel.of(() ->
                                IntakePriceFormatter.format(Objects.requireNonNull(dossierModel().getObject().totalPrijs()))))),
                new BevestigForm("bevestigForm")
        );
    }

    private IModel<DossierSamenvattingDto> dossierModel() {
        if (dossierModel == null) {
            dossierModel = LoadableDetachableModel.of(() -> marriageIntakeService.findByDossierId(dossierId));
        }
        return dossierModel;
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
        return pageParametersForDossier(dossierId);
    }

    private String naamkeuze(PartnerGegevensDto partner) {
        String naam = java.util.stream.Stream.of(partner.voornamen(), partner.achternaam())
                .filter(Objects::nonNull)
                .filter(waarde -> !waarde.isBlank())
                .collect(Collectors.joining(" "));
        return java.util.Optional.ofNullable(partner.gekozenAchternaam())
                .filter(gekozen -> !gekozen.isBlank())
                .map(gekozen -> naam + " - " + gekozen)
                .orElse(naam);
    }

    private boolean isCompleet() {
        DossierSamenvattingDto dossier = dossierModel.getObject();
        return dossier != null && dossier.id() != null && dossier.compleet();
    }

    private class BevestigForm extends Form<Void> {

        BevestigForm(String id) {
            super(id);
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

                        @Override
                        protected void onConfigure() {
                            super.onConfigure();
                            setEnabled(isCompleet());
                        }
                    },
                    new NogNietCompleetHint()
            );
        }
    }

    private class NogNietCompleetHint extends WebMarkupContainer {

        NogNietCompleetHint() {
            super("nogNietCompleetHint");
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            IModel<List<OntbrekendOnderdeel>> ontbrekendModel =
                    LambdaModel.of(() -> dossierModel.getObject().ontbrekendeOnderdelen());
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
            setVisible(!isCompleet());
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
