package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.GetuigeDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.OntbrekendOnderdeel;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.PartnerGegevensDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SidebarExtraItemDto;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.jspecify.annotations.Nullable;
import org.wicketstuff.minis.behavior.VisibleModelBehavior;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;

public class IntakeSidebarPanel extends GenericPanel<DossierSamenvattingDto> {

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    private final IntakeStep activeStep;

    public IntakeSidebarPanel(String id, IModel<DossierSamenvattingDto> dossierModel, IntakeStep activeStep) {
        super(id, dossierModel);
        this.activeStep = activeStep;
        setOutputMarkupId(true);
    }

    private List<String> findGekozenNamen() {
        DossierSamenvattingDto dossier = getDossierModelObject();
        if (dossier == null || dossier.id() == null) {
            return List.of();
        }
        return marriageIntakeService.findPartnerGegevens(dossier.id()).stream()
                .map(PartnerGegevensDto::gekozenAchternaam)
                .filter(naam -> naam != null && !naam.isBlank())
                .toList();
    }

    private List<String> findGetuigenNamen() {
        DossierSamenvattingDto dossier = getDossierModelObject();
        if (dossier == null || dossier.id() == null) {
            return List.of();
        }
        return marriageIntakeService.findGetuigen(dossier.id()).stream()
                .map(GetuigeDto::naam)
                .filter(naam -> naam != null && !naam.isBlank())
                .toList();
    }

    @Nullable
    private DossierSamenvattingDto getDossierModelObject() {
        return getModelObject();
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(
                progressIcon("algemeenStatusIcon", 1, IntakeStep.MIJN_DAG, null),
                progressIcon("gegevensProgressIcon", 3, IntakeStep.JULLIE_GEGEVENS, null),
                progressIcon("getuigenProgressIcon", 4, IntakeStep.DE_GETUIGEN, null),
                progressIcon("extrasProgressIcon", 5, IntakeStep.EXTRAS, null),
                progressIcon("controlerenProgressIcon", 6, IntakeStep.CONTROLEREN, null)
        );

        // Dé dag — registratie type row (visible when dossier is present)
        WebMarkupContainer dossierGegevens = new WebMarkupContainer("dossierGegevens") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(getDossierModelObject() != null);
            }
        };

        IModel<String> registratieModel = LambdaModel.of(
                () -> dossierValue(DossierSamenvattingDto::registratieType, RegistratieType::getLabel));
        dossierGegevens.add(new Label("registratieTypeLabel", registratieModel));
        add(stepLink("dedagLink", IntakeStep.MIJN_DAG, MarriageIntakePage.class));
        dossierGegevens.add(stepLink("datumLink", IntakeStep.DE_DAG, DeDagPage.class));

        // Ceremony row — only shown when a persisted dossier exists (id > 0)
        WebMarkupContainer ceremonieDossierGegevens = new WebMarkupContainer("ceremonieDossierGegevens") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModelObject();
                setVisible(d != null && d.id() != null);
            }
        };
        IModel<String> ceremonieSoortModel = LambdaModel.of(
                () -> dossierValue(DossierSamenvattingDto::ceremonieSoort, CeremonieSoort::getLabel));
        IModel<String> ceremoniePrijsModel = LambdaModel.of(() -> {
            DossierSamenvattingDto d = getDossierModelObject();
            if (d == null) {
                return "";
            }
            BigDecimal prijs = d.prijs();
            if (prijs == null) {
                return "";
            }
            return new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.forLanguageTag("nl-NL"))).format(prijs);
        });
        ceremonieDossierGegevens.add(new Label("ceremonieSoortLabel", ceremonieSoortModel));
        ceremonieDossierGegevens.add(new Label("ceremoniePrijs", ceremoniePrijsModel));
        dossierGegevens.add(ceremonieDossierGegevens);
        dossierGegevens.add(progressIcon("datumStatusIcon", 2, IntakeStep.DE_DAG,
                OntbrekendOnderdeel.DATUM));

        // Datum row — shown when datumTijdHuwelijk is set, otherwise "nog kiezen" placeholder
        WebMarkupContainer datumGekozen = new WebMarkupContainer("datumGekozen") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModelObject();
                setVisible(d != null && d.datumTijdHuwelijk() != null);
            }
        };
        IModel<String> datumModel = LambdaModel.of(() -> {
            DossierSamenvattingDto d = getDossierModelObject();
            return (d != null && d.datumTijdHuwelijk() != null)
                    ? d.datumTijdHuwelijk().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    : "";
        });
        datumGekozen.add(new Label("datumLabel", datumModel));

        IModel<String> tijdModel = LambdaModel.of(() -> {
            DossierSamenvattingDto d = getDossierModelObject();
            return (d != null && d.datumTijdHuwelijk() != null)
                    ? d.datumTijdHuwelijk().format(DateTimeFormatter.ofPattern("HH:mm"))
                    : "";
        });
        WebMarkupContainer tijdGekozen = new WebMarkupContainer("tijdGekozen") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModelObject();
                setVisible(d != null && d.datumTijdHuwelijk() != null);
            }
        };
        tijdGekozen.add(new Label("tijdLabel", tijdModel));
        datumGekozen.add(tijdGekozen);

        dossierGegevens.add(datumGekozen);

        WebMarkupContainer datumNogKiezen = new WebMarkupContainer("datumNogKiezen") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModelObject();
                setVisible(d == null || d.datumTijdHuwelijk() == null);
            }
        };
        dossierGegevens.add(datumNogKiezen);

        // Locatie row — shown when locatie is set, otherwise "nog kiezen" placeholder
        WebMarkupContainer locatieGekozen = new WebMarkupContainer("locatieGekozen") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModelObject();
                setVisible(d != null && d.huwelijksLocatie() != null && !d.huwelijksLocatie().isBlank());
            }
        };
        IModel<String> locatieModel = LambdaModel.of(() -> {
            DossierSamenvattingDto d = getDossierModelObject();
            return (d != null && d.huwelijksLocatie() != null) ? d.huwelijksLocatie() : "";
        });
        locatieGekozen.add(new Label("locatieLabel", locatieModel));
        dossierGegevens.add(locatieGekozen);

        WebMarkupContainer locatieNogKiezen = new WebMarkupContainer("locatieNogKiezen") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModelObject();
                setVisible(d == null || d.huwelijksLocatie() == null || d.huwelijksLocatie().isBlank());
            }
        };
        dossierGegevens.add(locatieNogKiezen);

        add(dossierGegevens);

        // Gegevens & Getuigen status icons
        add(stepLink("gegevensLink", IntakeStep.JULLIE_GEGEVENS, JullieGegevensPage.class));
        IModel<List<String>> gekozenNamenModel = LoadableDetachableModel.of(this::findGekozenNamen);
        add(
                new WebMarkupContainer("geenGegevens")
                        .add(new VisibleModelBehavior(gekozenNamenModel.map(List::isEmpty))),
                new ListView<>("gekozenNamen", gekozenNamenModel) {
                    @Override
                    protected void populateItem(ListItem<String> item) {
                        item.add(new Label("gekozenNaam", item.getModel()));
                    }
                }
        );
        add(stepLink("getuigenLink", IntakeStep.DE_GETUIGEN, DeGetuigenPage.class));
        IModel<List<String>> getuigenNamenModel = LoadableDetachableModel.of(this::findGetuigenNamen);
        add(
                new WebMarkupContainer("geenGetuigen")
                        .add(new VisibleModelBehavior(getuigenNamenModel.map(List::isEmpty))),
                new ListView<>("getuigenNamen", getuigenNamenModel) {
                    @Override
                    protected void populateItem(ListItem<String> item) {
                        item.add(new Label("getuigeNaam", item.getModel()));
                    }
                }
        );
        add(new PartnerHerinnering());

        // Extra's list
        WebMarkupContainer extrasNogNiet = new WebMarkupContainer("extrasNogNiet") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModelObject();
                setVisible(d == null || d.extras() == null || d.extras().isEmpty());
            }
        };
        add(extrasNogNiet);

        IModel<List<SidebarExtraItemDto>> extrasListModel = LambdaModel.of(() -> {
            DossierSamenvattingDto d = getDossierModelObject();
            return (d != null && d.extras() != null) ? d.extras() : List.of();
        });

        ListView<SidebarExtraItemDto> extrasListView = new ListView<>("extrasList", extrasListModel) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModelObject();
                setVisible(d != null && d.extras() != null && !d.extras().isEmpty());
            }

            @Override
            protected void populateItem(ListItem<SidebarExtraItemDto> item) {
                SidebarExtraItemDto extra = item.getModelObject();
                item.add(new Label("extraLabel", Model.of(extra.naam())));
                WebMarkupContainer prijsContainer = new WebMarkupContainer("extraPrijsContainer");
                prijsContainer.setVisible(extra.prijs() != null);
                if (extra.prijs() != null) {
                    String prijsText = new DecimalFormat("#,##0.00",
                            new DecimalFormatSymbols(Locale.forLanguageTag("nl-NL"))).format(extra.prijs());
                    prijsContainer.add(new Label("extraPrijs", Model.of(prijsText)));
                } else {
                    prijsContainer.add(new Label("extraPrijs", Model.of("")));
                }
                item.add(prijsContainer);
            }
        };
        extrasListView.setReuseItems(false);
        add(extrasListView);

        add(stepLink("extrasLink", IntakeStep.EXTRAS, ExtrasPage.class));
        add(stepLink("controlerenLink", IntakeStep.CONTROLEREN, ControlerenPage.class));

        // Status — shown once the dossier has been submitted
        WebMarkupContainer statusContainer = new WebMarkupContainer("statusContainer");
        statusContainer.add(new VisibleModelBehavior(LambdaModel.of(() -> !isWijzigbaar())));
        statusContainer.add(new Label("statusLabel", LambdaModel.of(
                () -> dossierValue(DossierSamenvattingDto::status, DossierStatus::getLabel))));
        add(statusContainer);
    }

    private boolean isWijzigbaar() {
        DossierSamenvattingDto d = getDossierModelObject();
        return d == null || d.status() == DossierStatus.CONCEPT;
    }

    private Link<Void> stepLink(String id, IntakeStep step, Class<? extends Page> pageClass) {
        Link<Void> link = new Link<>(id) {
            @Override
            public void onClick() {
                setResponsePage(pageClass, makeDossierPageParameters(requireNonNull(getDossierModelObject()).id()));
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModelObject();
                setEnabled(d != null && d.id() != null && isWijzigbaar());
            }
        };
        setCurrentStep(link, step);
        return link;
    }

    private void setCurrentStep(Link<Void> link, IntakeStep step) {
        if (activeStep == step) {
            link.add(AttributeModifier.append("class", " rd-progress-step--active"));
            link.add(AttributeModifier.replace("aria-current", "step"));
        }
    }

    private Label progressIcon(String id, int number, IntakeStep step, OntbrekendOnderdeel missing) {
        return new Label(id, Model.of("")) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setDefaultModelObject(isStepComplete(step, missing) ? "" : number);
            }

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                String state = isStepComplete(step, missing)
                        ? "rd-status-icon--check"
                        : "rd-status-icon--current";
                tag.put("class", "rd-status-icon " + state);
            }
        };
    }

    private boolean isStepComplete(IntakeStep step, OntbrekendOnderdeel missing) {
        DossierSamenvattingDto d = getDossierModelObject();
        if (d == null || d.id() == null) {
            return false;
        }
        if (missing != null) {
            return !d.ontbrekendeOnderdelen().contains(missing);
        }
        return switch (step) {
            case MIJN_DAG -> d.registratieType() != null && d.ceremonieSoort() != null;
            case DE_DAG -> !d.ontbrekendeOnderdelen().contains(OntbrekendOnderdeel.DATUM)
                    && !d.ontbrekendeOnderdelen().contains(OntbrekendOnderdeel.LOCATIE);
            case JULLIE_GEGEVENS -> !d.ontbrekendeOnderdelen().contains(OntbrekendOnderdeel.PARTNER_GEKOPPELD)
                    && !d.ontbrekendeOnderdelen().contains(OntbrekendOnderdeel.ACHTERNAMEN);
            case DE_GETUIGEN -> !d.ontbrekendeOnderdelen().contains(OntbrekendOnderdeel.GETUIGEN);
            case EXTRAS -> d.status() != DossierStatus.CONCEPT
                    || activeStep != null && activeStep.ordinal() > IntakeStep.EXTRAS.ordinal();
            case CONTROLEREN -> d.status() != DossierStatus.CONCEPT;
        };
    }

    private <I, O> String dossierValue(Function<DossierSamenvattingDto, I> extractor, Function<I, O> mapper) {
        DossierSamenvattingDto d = getDossierModelObject();
        if (d == null) {
            return "";
        }
        return String.valueOf(mapper.apply(extractor.apply(d)));
    }

    private class PartnerHerinnering extends WebMarkupContainer {

        public PartnerHerinnering() {
            super("partnerHerinnering");
        }

        @Override
        protected void onConfigure() {
            super.onConfigure();
            DossierSamenvattingDto d = getDossierModelObject();
            setVisible(d != null && d.id() != null && !d.partnerGekoppeld() && isWijzigbaar());
        }
    }

}
