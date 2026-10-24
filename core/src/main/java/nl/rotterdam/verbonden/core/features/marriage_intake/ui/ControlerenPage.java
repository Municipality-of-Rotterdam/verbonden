package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietWijzigbaarException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.GetuigeDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.OntbrekendOnderdeel;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.PartnerGegevensDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SidebarExtraItemDto;
import nl.rotterdam.nl_design_system.rotterdam_extensions.wicket.components.rotterdam_icon.RotterdamIconBehavior;
import nl.rotterdam.nl_design_system.wicket.components.action_group.RdActionGroup;
import nl.rotterdam.nl_design_system.wicket.components.button.RdAjaxButton;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButtonAppearance;
import nl.rotterdam.nl_design_system.wicket.components.dialog.RdDialogBorder;
import nl.rotterdam.nl_design_system.wicket.components.dialog.RdDialogHeadingLevel;
import nl.rotterdam.nl_design_system.wicket.components.heading.RdHeading;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.jspecify.annotations.NonNull;
import org.wicketstuff.minis.behavior.VisibleModelBehavior;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;

public class ControlerenPage extends IntakeBasePage {

    private static final DateTimeFormatter DATUM_FORMAT =
            DateTimeFormatter.ofPattern("d MMMM yyyy 'om' HH:mm", Locale.forLanguageTag("nl-NL"));

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    private IModel<DossierSamenvattingDto> dossierModel;

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
        return LoadableDetachableModel.of(() -> marriageIntakeService.findByDossierId(dossierId));
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        dossierModel = LoadableDetachableModel.of(() -> marriageIntakeService.findByDossierId(dossierId));
        DossierSamenvattingDto dossier = dossierModel.getObject();

        pageBody.add(
                new RdHeading("heading", getString("controleren.heading"), 1),
                new Label("intro", new ResourceModel("controleren.intro")),
                new BookmarkablePageLink<>("wijzigDag", DeDagPage.class, parameters()),
                new BookmarkablePageLink<>("wijzigGegevens", JullieGegevensPage.class, parameters()),
                new BookmarkablePageLink<>("wijzigGetuigen", DeGetuigenPage.class, parameters()),
                new BookmarkablePageLink<>("wijzigExtras", ExtrasPage.class, parameters()),
                new Label("registratieType", dossier.registratieType().getLabel()),
                new Label("ceremonieSoort", dossier.ceremonieSoort().getLabel()),
                new Label("ceremoniePrijs", formatPrijs(dossier.prijs())),
                new Label("datum", dossier.datumTijdHuwelijk() == null ? "" : dossier.datumTijdHuwelijk().format(DATUM_FORMAT)),
                new Label("locatie", dossier.huwelijksLocatie() == null ? "" : dossier.huwelijksLocatie()),
                new ListView<>("partners", marriageIntakeService.findPartnerGegevens(dossierId)) {
                    @Override
                    protected void populateItem(ListItem<PartnerGegevensDto> item) {
                        item.add(new Label("naamkeuze", naamkeuze(item.getModelObject())));
                    }
                },
                new ListView<>("getuigen", marriageIntakeService.findGetuigen(dossierId)) {
                    @Override
                    protected void populateItem(ListItem<GetuigeDto> item) {
                        item.add(new Label("getuigeNaam", item.getModelObject().naam()));
                    }
                },
                new ListView<>("extras", dossier.extras() == null ? List.of() : dossier.extras()) {
                    @Override
                    protected void populateItem(ListItem<SidebarExtraItemDto> item) {
                        SidebarExtraItemDto extra = item.getModelObject();
                        item.add(new Label("extraNaam", extra.naam()));
                        item.add(new Label("extraPrijs", extra.prijs() == null ? "" : "€ " + formatPrijs(extra.prijs())));
                    }
                },
                new WebMarkupContainer("geenExtras") {
                    @Override
                    protected void onConfigure() {
                        super.onConfigure();
                        setVisible(dossier.extras() == null || dossier.extras().isEmpty());
                    }
                },
                new Label("totaalPrijs", formatPrijs(dossier.totalPrijs())),
                new BevestigForm("bevestigForm")
        );
    }

    private PageParameters parameters() {
        return makeDossierPageParameters(dossierId);
    }

    private String naamkeuze(PartnerGegevensDto partner) {
        String naam = String.join(" ", List.of(
                partner.voornamen() == null ? "" : partner.voornamen(),
                partner.achternaam() == null ? "" : partner.achternaam())).trim();
        return partner.gekozenAchternaam() == null || partner.gekozenAchternaam().isBlank()
                ? naam
                : naam + " - " + partner.gekozenAchternaam();
    }

    private String formatPrijs(BigDecimal prijs) {
        return prijs == null ? "" : new DecimalFormat(
                "#,##0.00", new DecimalFormatSymbols(Locale.forLanguageTag("nl-NL"))).format(prijs);
    }

    private boolean isCompleet() {
        DossierSamenvattingDto dossier = dossierModel.getObject();
        return dossier != null && dossier.id() != null && dossier.compleet();
    }

    private class BevestigForm extends Form<Void> {

        private final RdDialogBorder bevestigDialog = new RdDialogBorder("bevestigDialog",
                new ResourceModel("intake.bevestig.dialog.titel"),
                RotterdamIconBehavior.supplyOf(RotterdamIconBehavior.CLOSE),
                RdDialogHeadingLevel.LEVEL_2) {
            @Override
            protected @NonNull Component newFooterContent(@NonNull String id) {
                return new BevestigDialogActies(id, this);
            }

            @Override
            protected void onClose(@NonNull AjaxRequestTarget target) {
                close(target);
            }
        };

        BevestigForm(String id) {
            super(id);
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            add(
                    new RdAjaxButton("bevestigButton", new ResourceModel("controleren.bevestig")) {
                        @Override
                        protected void onSubmit(AjaxRequestTarget target) {
                            bevestigDialog.showModal(target);
                        }

                        @Override
                        protected void onConfigure() {
                            super.onConfigure();
                            setEnabled(isCompleet());
                        }
                    }.setDefaultFormProcessing(false),
                    new NogNietCompleetHint(),
                    bevestigDialog
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
                    setResponsePage(pageVoor(onderdeel), parameters());
                }
            }.add(new Label("label", new ResourceModel("intake.sidebar.ontbrekend." + onderdeel.name()))));
        }

        private Class<? extends Page> pageVoor(OntbrekendOnderdeel onderdeel) {
            return switch (onderdeel) {
                case DATUM -> DatumKiezenPage.class;
                case LOCATIE -> DeDagPage.class;
                case PARTNER_GEKOPPELD, ACHTERNAMEN -> JullieGegevensPage.class;
                case GETUIGEN -> DeGetuigenPage.class;
            };
        }
    }

    private class BevestigDialogActies extends Fragment {

        private final RdDialogBorder dialog;

        BevestigDialogActies(String id, RdDialogBorder dialog) {
            super(id, "bevestigDialogActies", ControlerenPage.this);
            this.dialog = dialog;
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            RdButton indienenButton = new RdButton("indienenButton",
                    new ResourceModel("intake.bevestig.dialog.bevestigen")) {
                @Override
                public void onSubmit() {
                    UUID id = requireNonNull(dossierModel.getObject()).id();
                    try {
                        marriageIntakeService.dienIn(id);
                    } catch (DossierNietWijzigbaarException ignored) {
                        // Already submitted by the partner; show the current status.
                    }
                    AanvraagStatusPage.respond(id);
                }
            };
            indienenButton.setDefaultFormProcessing(false);
            indienenButton.setAppearance(RdButtonAppearance.PRIMARY_ACTION);
            RdAjaxButton annulerenButton = new RdAjaxButton("annulerenButton",
                    new ResourceModel("intake.bevestig.dialog.annuleren")) {
                @Override
                protected void onSubmit(AjaxRequestTarget target) {
                    dialog.close(target);
                }
            };
            annulerenButton.setDefaultFormProcessing(false);
            add(new RdActionGroup("acties").add(indienenButton, annulerenButton));
        }
    }
}
