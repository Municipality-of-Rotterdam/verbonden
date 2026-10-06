package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietWijzigbaarException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SidebarExtraItemDto;
import nl.rotterdam.nl_design_system.rotterdam_extensions.wicket.components.rotterdam_icon.RotterdamIconBehavior;
import nl.rotterdam.nl_design_system.wicket.components.action_group.RdActionGroup;
import nl.rotterdam.nl_design_system.wicket.components.button.RdAjaxButton;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButtonAppearance;
import nl.rotterdam.nl_design_system.wicket.components.dialog.RdDialogBorder;
import nl.rotterdam.nl_design_system.wicket.components.dialog.RdDialogHeadingLevel;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.jspecify.annotations.Nullable;
import org.wicketstuff.minis.behavior.EnabledModelBehavior;
import org.wicketstuff.minis.behavior.VisibleModelBehavior;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;

public class IntakeSidebarPanel extends GenericPanel<DossierSamenvattingDto> {

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    private final IModel<DossierSamenvattingDto> dossierModel;
    public IntakeSidebarPanel(String id, IModel<DossierSamenvattingDto> dossierModel) {
        super(id, dossierModel);
        this.dossierModel = dossierModel;
        setOutputMarkupId(true);
    }

    @Nullable
    private DossierSamenvattingDto getDossierModelObject() {
        return getModelObject();
    }
    
    

    @Override
    protected void onInitialize() {
        super.onInitialize();

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
        WebMarkupContainer gegevensRegel = new WebMarkupContainer("gegevensStatusIcon");
        gegevensRegel.add(new WebMarkupContainer("gegevensStatusCheckGreen") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModelObject();
                setVisible(d != null && d.aantalGekozenAchternamen() == 2);
            }
        });
        gegevensRegel.add(new WebMarkupContainer("gegevensStatusCheckGrey") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModelObject();
                setVisible(d != null && d.aantalGekozenAchternamen() == 1);
            }
        });
        gegevensRegel.add(new WebMarkupContainer("gegevensStatusEmpty") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModelObject();
                setVisible(d == null || d.aantalGekozenAchternamen() == 0);
            }
        });
        add(gegevensRegel);
        gegevensRegel.add(new Link<Void>("jullieGegevensLink") {
            @Override
            public void onClick() {
                setResponsePage(JullieGegevensPage.class,
                        makeDossierPageParameters(requireNonNull(getDossierModelObject()).id()));
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto samenvattingDto = getDossierModelObject();
                setEnabled(samenvattingDto != null && samenvattingDto.id() != null && isWijzigbaar());
            }
        });


        add(new GetuigenRegel());

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

        // Totaal (ceremony price + extras prices)
        WebMarkupContainer totaalContainer = new WebMarkupContainer("totaalContainer") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModelObject();
                setVisible(d != null && d.totalPrijs() != null);
            }
        };
        IModel<String> totaalModel = LambdaModel.of(() -> {
            DossierSamenvattingDto d = getDossierModelObject();
            if (d == null || d.totalPrijs() == null) {
                return "";
            }
            return new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.forLanguageTag("nl-NL"))).format(d.totalPrijs());
        });
        totaalContainer.add(new Label("totaalPrijs", totaalModel));
        add(totaalContainer);

        add(new BevestigForm());

        // Reservation info — only relevant while the dossier can still be changed
        add(new WebMarkupContainer("reserveringInfo")
                .add(new VisibleModelBehavior(LambdaModel.of(this::isWijzigbaar))));

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

    private boolean isCompleet() {
        DossierSamenvattingDto d = getDossierModelObject();
        return d != null && d.id() != null && d.compleet();
    }

    /**
     * "Bevestig jullie keuzes": opens a modal dialog in which the citizen confirms that the choices
     * are final. Only enabled when the dossier is complete.
     */
    private class BevestigForm extends Form<Void> {

        private final RdDialogBorder bevestigDialog = new RdDialogBorder("bevestigDialog",
                new ResourceModel("intake.bevestig.dialog.titel"),
                RotterdamIconBehavior.supplyOf(RotterdamIconBehavior.CLOSE),
                RdDialogHeadingLevel.LEVEL_2) {

            @Override
            protected Component newFooterContent(String id) {
                return new BevestigDialogActies(id, this);
            }

            @Override
            protected void onClose(AjaxRequestTarget target) {
                close(target);
            }
        };

        BevestigForm() {
            super("bevestigForm");
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            add(new VisibleModelBehavior(LambdaModel.of(IntakeSidebarPanel.this::isWijzigbaar)));

            add(
                    new RdAjaxButton("bevestigButton", new ResourceModel("intake.sidebar.bevestig")) {

                        @Override
                        protected void onSubmit(AjaxRequestTarget target) {
                            bevestigDialog.showModal(target);
                        }

                        @Override
                        protected void onConfigure() {
                            super.onConfigure();
                            setEnabled(isCompleet());
                        }
                    }
                    .setDefaultFormProcessing(false)
                    .add(new EnabledModelBehavior(dossierModel.map(DossierSamenvattingDto::compleet))),
                    new WebMarkupContainer("nogNietCompleetHint")
                            .add(new VisibleModelBehavior(LambdaModel.of(() -> !isCompleet()))),
                    bevestigDialog
            );
        }
    }

    private class BevestigDialogActies extends Fragment {

        private final RdDialogBorder dialog;

        BevestigDialogActies(String id, RdDialogBorder dialog) {
            super(id, "bevestigDialogActies", IntakeSidebarPanel.this);
            this.dialog = dialog;
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();

            RdButton indienenButton = new RdButton("indienenButton",
                    new ResourceModel("intake.bevestig.dialog.bevestigen")) {
                @Override
                public void onSubmit() {
                    UUID dossierId = requireNonNull(getDossierModelObject()).id();
                    try {
                        marriageIntakeService.dienIn(dossierId);
                    } catch (DossierNietWijzigbaarException e) {
                        // Already submitted (e.g. by the partner); the status page shows the current state
                    }
                    AanvraagStatusPage.respond(dossierId);
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

    private <I, O> String dossierValue(Function<DossierSamenvattingDto, I> extractor, Function<I, O> mapper) {
        DossierSamenvattingDto d = getDossierModelObject();
        if (d == null) {
            return "";
        }
        return String.valueOf(mapper.apply(extractor.apply(d)));
    }

    private class GetuigenRegel extends WebMarkupContainer {

        public GetuigenRegel() {
            super("getuigenRegel");
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            add(
                    new WebMarkupContainer("getuigenStatusCheck")
                            .add(new VisibleModelBehavior(dossierModel.map(DossierSamenvattingDto::getuigenBevestigd))),

                    new WebMarkupContainer("getuigenStatusPartial")
                            .add(new VisibleModelBehavior(dossierModel.map(DossierSamenvattingDto::getuigenGedeeltelijkIngevuld))),

                    new WebMarkupContainer("getuigenStatusEmpty") {
                        @Override
                        protected void onConfigure() {
                            super.onConfigure();
                            DossierSamenvattingDto d = getDossierModelObject();
                            setVisible(d == null || (!d.getuigenBevestigd() && !d.getuigenGedeeltelijkIngevuld()));
                        }
                    },

                    new Link<Void>("getuigenLink") {

                        @Override
                        public void onClick() {
                            DeGetuigenPage.respond(requireNonNull(getDossierModelObject()).id());
                        }

                        @Override
                        protected void onConfigure() {
                            super.onConfigure();
                            DossierSamenvattingDto samenvattingDto = getDossierModelObject();
                            setEnabled(samenvattingDto != null && samenvattingDto.id() != null && isWijzigbaar());
                        }
                    }
            );
        }
    }
}
