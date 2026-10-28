package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.GetuigeDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.OntbrekendOnderdeel;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.PartnerGegevensDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SidebarExtraItemDto;
import org.apache.wicket.AttributeModifier;
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
import org.jspecify.annotations.Nullable;
import org.wicketstuff.minis.behavior.VisibleModelBehavior;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;

public class IntakeSidebarPanel extends GenericPanel<DossierSamenvattingDto> {

    private final IntakeStep activeStep;
    private final IModel<List<PartnerGegevensDto>> partnerGegevensModel;
    private final IModel<List<GetuigeDto>> getuigenModel;
    private final boolean extrasDoorlopen;

    public IntakeSidebarPanel(String id, IModel<DossierSamenvattingDto> dossierModel,
                              IModel<List<PartnerGegevensDto>> partnerGegevensModel,
                              IModel<List<GetuigeDto>> getuigenModel, IntakeStep activeStep, boolean extrasDoorlopen) {
        super(id, dossierModel);
        this.activeStep = activeStep;
        this.partnerGegevensModel = partnerGegevensModel;
        this.getuigenModel = getuigenModel;
        this.extrasDoorlopen = extrasDoorlopen;
        setOutputMarkupId(true);
    }

    @Nullable
    private DossierSamenvattingDto getDossierModelObject() {
        return getModelObject();
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(
                progressIcon("algemeenStatusIcon", IntakeStep.MIJN_DAG),
                progressIcon("gegevensProgressIcon", IntakeStep.JULLIE_GEGEVENS),
                progressIcon("getuigenProgressIcon", IntakeStep.DE_GETUIGEN),
                progressIcon("extrasProgressIcon", IntakeStep.EXTRAS)
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
        add(stepLink("dedagLink", IntakeStep.MIJN_DAG));
        dossierGegevens.add(stepLink("datumLink", IntakeStep.DE_DAG));

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
            return IntakePriceFormatter.format(prijs);
        });
        ceremonieDossierGegevens.add(new Label("ceremonieSoortLabel", ceremonieSoortModel));
        ceremonieDossierGegevens.add(new Label("ceremoniePrijs", ceremoniePrijsModel));
        dossierGegevens.add(ceremonieDossierGegevens);
        dossierGegevens.add(progressIcon("datumStatusIcon", IntakeStep.DE_DAG));

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
        add(stepLink("gegevensLink", IntakeStep.JULLIE_GEGEVENS));
        IModel<List<String>> gekozenNamenModel = LambdaModel.of(() -> partnerGegevensModel.getObject().stream()
                .map(PartnerGegevensDto::gekozenAchternaam)
                .filter(naam -> naam != null && !naam.isBlank())
                .toList());
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
        add(stepLink("getuigenLink", IntakeStep.DE_GETUIGEN));
        IModel<List<String>> getuigenNamenModel = LambdaModel.of(() -> getuigenModel.getObject().stream()
                .map(GetuigeDto::naam)
                .filter(naam -> naam != null && !naam.isBlank())
                .toList());
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
                    String prijsText = IntakePriceFormatter.format(extra.prijs());
                    prijsContainer.add(new Label("extraPrijs", Model.of(prijsText)));
                } else {
                    prijsContainer.add(new Label("extraPrijs", Model.of("")));
                }
                item.add(prijsContainer);
            }
        };
        extrasListView.setReuseItems(false);
        add(extrasListView);

        add(stepLink("extrasLink", IntakeStep.EXTRAS));
        WebMarkupContainer controlerenSection = new WebMarkupContainer("controlerenSection");
        WebMarkupContainer statusContainer = new WebMarkupContainer("statusContainer");
        statusContainer.add(new VisibleModelBehavior(LambdaModel.of(() -> !isWijzigbaar())));
        statusContainer.add(new Label("statusLabel", LambdaModel.of(
                () -> dossierValue(DossierSamenvattingDto::status, DossierStatus::getLabel))));
        controlerenSection.add(
                progressIcon("controlerenProgressIcon", IntakeStep.CONTROLEREN),
                stepLink("controlerenLink", IntakeStep.CONTROLEREN),
                statusContainer
        );
        add(controlerenSection);
    }

    private boolean isWijzigbaar() {
        DossierSamenvattingDto d = getDossierModelObject();
        return d == null || d.status() == DossierStatus.CONCEPT;
    }

    private Link<Void> stepLink(String id, IntakeStep step) {
        Link<Void> link = new Link<>(id) {
            @Override
            public void onClick() {
                setResponsePage(step.pageClass(), makeDossierPageParameters(
                        requireNonNull(getDossierModelObject()).id(), extrasDoorlopen));
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

    private Label progressIcon(String id, IntakeStep step) {
        return new Label(id, Model.of("")) {
            {
                setOutputMarkupId(true);
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setDefaultModelObject(isStepComplete(step) ? "" : step.ordinal() + 1);
            }

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                boolean complete = isStepComplete(step);
                boolean current = activeStep == step;
                String state = complete
                        ? current ? "rd-status-icon--current-check" : "rd-status-icon--complete"
                        : current ? "rd-status-icon--current" : "rd-status-icon--upcoming";
                tag.put("class", "rd-status-icon " + state);
            }
        };
    }

    private boolean isStepComplete(IntakeStep step) {
        DossierSamenvattingDto d = getDossierModelObject();
        if (d == null || d.id() == null) {
            return false;
        }
        return switch (step) {
            case MIJN_DAG -> d.registratieType() != null && d.ceremonieSoort() != null;
            case DE_DAG -> !d.ontbrekendeOnderdelen().contains(OntbrekendOnderdeel.DATUM)
                    && !d.ontbrekendeOnderdelen().contains(OntbrekendOnderdeel.LOCATIE);
            case JULLIE_GEGEVENS -> !d.ontbrekendeOnderdelen().contains(OntbrekendOnderdeel.PARTNER_GEKOPPELD)
                    && !d.ontbrekendeOnderdelen().contains(OntbrekendOnderdeel.ACHTERNAMEN);
            case DE_GETUIGEN -> !d.ontbrekendeOnderdelen().contains(OntbrekendOnderdeel.GETUIGEN);
            case EXTRAS -> d.status() != DossierStatus.CONCEPT
                    || extrasDoorlopen;
            case CONTROLEREN -> d.compleet();
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
