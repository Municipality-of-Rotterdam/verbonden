package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

public class IntakeSidebarPanel extends Panel {

    public IntakeSidebarPanel(String id, IModel<DossierSamenvattingDto> dossierModel) {
        super(id, dossierModel);
        setOutputMarkupId(true);
    }

    @SuppressWarnings("unchecked")
    private IModel<DossierSamenvattingDto> getDossierModel() {
        return (IModel<DossierSamenvattingDto>) getDefaultModel();
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        // Dé dag — registratie type row (visible when dossier is present)
        WebMarkupContainer dossierGegevens = new WebMarkupContainer("dossierGegevens") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(getDossierModel().getObject() != null);
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
                DossierSamenvattingDto d = getDossierModel().getObject();
                setVisible(d != null && d.id() != null);
            }
        };
        IModel<String> ceremonieSoortModel = LambdaModel.of(
                () -> dossierValue(DossierSamenvattingDto::ceremonieSoort, CeremonieSoort::getLabel));
        IModel<String> ceremoniePrijsModel = LambdaModel.of(
                () -> dossierValue(DossierSamenvattingDto::ceremonieSoort, CeremonieSoort::getPrijs));
        ceremonieDossierGegevens.add(new Label("ceremonieSoortLabel", ceremonieSoortModel));
        ceremonieDossierGegevens.add(new Label("ceremoniePrijs", ceremoniePrijsModel));
        dossierGegevens.add(ceremonieDossierGegevens);

        // Datum row — shown when datum is set, otherwise "nog kiezen" placeholder
        WebMarkupContainer datumGekozen = new WebMarkupContainer("datumGekozen") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModel().getObject();
                setVisible(d != null && d.datumHuwelijk() != null);
            }
        };
        IModel<String> datumModel = LambdaModel.of(() -> {
            DossierSamenvattingDto d = getDossierModel().getObject();
            return (d != null && d.datumHuwelijk() != null)
                    ? d.datumHuwelijk().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    : "";
        });
        datumGekozen.add(new Label("datumLabel", datumModel));
        dossierGegevens.add(datumGekozen);

        WebMarkupContainer datumNogKiezen = new WebMarkupContainer("datumNogKiezen") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModel().getObject();
                setVisible(d == null || d.datumHuwelijk() == null);
            }
        };
        dossierGegevens.add(datumNogKiezen);

        // Locatie row — shown when locatie is set, otherwise "nog kiezen" placeholder
        WebMarkupContainer locatieGekozen = new WebMarkupContainer("locatieGekozen") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModel().getObject();
                setVisible(d != null && d.huwelijksLocatie() != null && !d.huwelijksLocatie().isBlank());
            }
        };
        IModel<String> locatieModel = LambdaModel.of(() -> {
            DossierSamenvattingDto d = getDossierModel().getObject();
            return (d != null && d.huwelijksLocatie() != null) ? d.huwelijksLocatie() : "";
        });
        locatieGekozen.add(new Label("locatieLabel", locatieModel));
        dossierGegevens.add(locatieGekozen);

        WebMarkupContainer locatieNogKiezen = new WebMarkupContainer("locatieNogKiezen") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModel().getObject();
                setVisible(d == null || d.huwelijksLocatie() == null || d.huwelijksLocatie().isBlank());
            }
        };
        dossierGegevens.add(locatieNogKiezen);

        add(dossierGegevens);

        // Gegevens & Getuigen status icons
        WebMarkupContainer gegevensStatusIcon = new WebMarkupContainer("gegevensStatusIcon");
        gegevensStatusIcon.add(new WebMarkupContainer("gegevensStatusCheck") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModel().getObject();
                setVisible(d != null && d.gegevensBevestigd());
            }
        });
        gegevensStatusIcon.add(new WebMarkupContainer("gegevensStatusEmpty") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModel().getObject();
                setVisible(d == null || !d.gegevensBevestigd());
            }
        });
        add(gegevensStatusIcon);

        WebMarkupContainer getuigenStatusIcon = new WebMarkupContainer("getuigenStatusIcon");
        getuigenStatusIcon.add(new WebMarkupContainer("getuigenStatusCheck") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModel().getObject();
                setVisible(d != null && d.getuigenBevestigd());
            }
        });
        getuigenStatusIcon.add(new WebMarkupContainer("getuigenStatusEmpty") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModel().getObject();
                setVisible(d == null || !d.getuigenBevestigd());
            }
        });
        add(getuigenStatusIcon);

        // Extra's list
        WebMarkupContainer extrasNogNiet = new WebMarkupContainer("extrasNogNiet") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModel().getObject();
                setVisible(d == null || d.extras() == null || d.extras().isEmpty());
            }
        };
        add(extrasNogNiet);

        IModel<List<String>> extrasListModel = LambdaModel.of(() -> {
            DossierSamenvattingDto d = getDossierModel().getObject();
            return (d != null && d.extras() != null) ? d.extras() : List.of();
        });

        ListView<String> extrasListView = new ListView<>("extrasList", new ListModel<>(extrasListModel.getObject())) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                DossierSamenvattingDto d = getDossierModel().getObject();
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
        DossierSamenvattingDto d = getDossierModel().getObject();
        if (d == null) {
            return "";
        }
        return String.valueOf(mapper.apply(extractor.apply(d)));
    }
}
