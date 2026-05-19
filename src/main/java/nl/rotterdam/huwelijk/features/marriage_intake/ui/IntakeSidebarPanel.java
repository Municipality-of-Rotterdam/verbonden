package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
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
import static nl.rotterdam.huwelijk.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;

public class IntakeSidebarPanel extends GenericPanel<DossierSamenvattingDto> {

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
                setEnabled(samenvattingDto != null && samenvattingDto.id() != null);
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

        IModel<List<String>> extrasListModel = LambdaModel.of(() -> {
            DossierSamenvattingDto d = getDossierModelObject();
            return (d != null && d.extras() != null) ? d.extras() : List.of();
        });

        ListView<String> extrasListView = new ListView<>("extrasList", new ListModel<>(extrasListModel.getObject())) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModelObject();
                setVisible(d != null && d.extras() != null && !d.extras().isEmpty());
            }

            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("extraLabel", item.getModel()));
            }
        };
        extrasListView.setReuseItems(false);
        add(extrasListView);

        // Bevestig form with RdButton
        Form<Void> bevestigForm = new Form<>("bevestigForm");
        bevestigForm.add(new RdButton("bevestigButton",
                new ResourceModel("intake.sidebar.bevestig")) {
            @Override
            public void onSubmit() {
                // TODO: navigation to confirmation — to be implemented in a future iteration
            }
        });
        add(bevestigForm);
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
                            setEnabled(samenvattingDto != null && samenvattingDto.id() != null);
                        }
                    }
            );
        }
    }
}
