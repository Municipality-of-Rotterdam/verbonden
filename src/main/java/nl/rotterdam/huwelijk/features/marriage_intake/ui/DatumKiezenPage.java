package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.burger_common.BurgerBasePage;
import nl.rotterdam.huwelijk.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.nl_design_system.wicket.components.breadcrumb_nav.RdBreadcrumbNavPanel;
import nl.rotterdam.nl_design_system.wicket.components.breadcrumb_nav.RdBreadcrumbNavRecord;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

public class DatumKiezenPage extends BurgerBasePage {

    private static final DayOfWeek[] KOLOM_VOLGORDE = {
            DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
    };

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    private UUID dossierId;
    private YearMonth huidigeMaand;
    private LocalDate geselecteerdeDatum;
    private LocalTime geselecteerdeTijd;

    private WebMarkupContainer kalenderPanel;
    private WebMarkupContainer tijdslotPanel;
    private WebMarkupContainer bevestigBar;

    /** Fetched once per request cycle; detached afterwards by Wicket. */
    private IModel<Set<LocalDate>> beschikbareDatumsModel;

    @Override
    protected IModel<String> getTitleModel() {
        return new ResourceModel("datum.kiezen.page.title");
    }

    public DatumKiezenPage(PageParameters params) {
        this.dossierId = UUID.fromString(params.get("dossierId").toString());
        this.huidigeMaand = YearMonth.now();
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        beschikbareDatumsModel = new LoadableDetachableModel<>() {
            @Override
            protected Set<LocalDate> load() {
                return marriageIntakeService.findBeschikbareDatums(dossierId, huidigeMaand);
            }
        };

        List<RdBreadcrumbNavRecord<? extends org.apache.wicket.request.component.IRequestablePage>> crumbs = List.of(
                new RdBreadcrumbNavRecord<>(null, getString("intake.breadcrumb.mijnloket"), MarriageIntakePage.class),
                new RdBreadcrumbNavRecord<>(null, getString("intake.tab.dedag"), DeDagPage.class),
                new RdBreadcrumbNavRecord<>(null, getString("datum.kiezen.breadcrumb"), DatumKiezenPage.class)
        );
        pageBody.add(new RdBreadcrumbNavPanel("breadcrumb", crumbs));

        PageParameters terugParams = new PageParameters();
        terugParams.add("dossierId", dossierId.toString());
        pageBody.add(new BookmarkablePageLink<>("terugLink", DeDagPage.class, terugParams));

        pageBody.add(new Label("heading", new ResourceModel("datum.kiezen.heading")));

        kalenderPanel = buildKalenderPanel();
        kalenderPanel.setOutputMarkupId(true);
        pageBody.add(kalenderPanel);

        tijdslotPanel = buildTijdslotPanel();
        tijdslotPanel.setOutputMarkupId(true);
        pageBody.add(tijdslotPanel);

        bevestigBar = buildBevestigBar();
        bevestigBar.setOutputMarkupId(true);
        pageBody.add(bevestigBar);
    }

    // -------------------------------------------------------------------------
    // Calendar panel
    // -------------------------------------------------------------------------

    private WebMarkupContainer buildKalenderPanel() {
        WebMarkupContainer panel = new WebMarkupContainer("kalenderPanel");

        AjaxLink<Void> vorigeMaand = new AjaxLink<>("vorigeMaand") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                huidigeMaand = huidigeMaand.minusMonths(1);
                geselecteerdeDatum = null;
                geselecteerdeTijd = null;
                beschikbareDatumsModel.detach();
                target.add(kalenderPanel, tijdslotPanel, bevestigBar);
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setEnabled(huidigeMaand.isAfter(YearMonth.now()));
            }
        };
        panel.add(vorigeMaand);

        panel.add(new Label("maandLabel", () -> {
            String naam = huidigeMaand.getMonth()
                    .getDisplayName(TextStyle.FULL, new Locale("nl")).toLowerCase();
            return naam + ", " + huidigeMaand.getYear();
        }));

        panel.add(new AjaxLink<Void>("volgendeMaand") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                huidigeMaand = huidigeMaand.plusMonths(1);
                geselecteerdeDatum = null;
                geselecteerdeTijd = null;
                beschikbareDatumsModel.detach();
                target.add(kalenderPanel, tijdslotPanel, bevestigBar);
            }
        });

        panel.add(buildKalenderRijen());
        return panel;
    }

    private ListView<List<LocalDate>> buildKalenderRijen() {
        return new ListView<>("kalenderRijen", () -> bouwMaandRijen(huidigeMaand)) {
            @Override
            protected void populateItem(ListItem<List<LocalDate>> rijItem) {
                Set<LocalDate> beschikbaar = beschikbareDatumsModel.getObject();
                rijItem.add(new ListView<>("dag", rijItem.getModel()) {
                    @Override
                    protected void populateItem(ListItem<LocalDate> dagItem) {
                        LocalDate datum = dagItem.getModelObject();
                        boolean isLeeg = datum == null;
                        boolean isVerleden = datum != null && !datum.isAfter(LocalDate.now());
                        boolean isBeschikbaar = datum != null && !isVerleden && beschikbaar.contains(datum);
                        boolean isGeselecteerd = datum != null && datum.equals(geselecteerdeDatum);
                        boolean isVandaag = datum != null && datum.equals(LocalDate.now());

                        dagItem.add(AttributeModifier.replace("class",
                                buildDagClass(isLeeg, isVerleden, isBeschikbaar, isGeselecteerd)));

                        WebMarkupContainer dagLink = new WebMarkupContainer("dagLink");
                        dagLink.add(new Label("dagNummer",
                                datum != null ? String.valueOf(datum.getDayOfMonth()) : ""));
                        Label vandaagLabel = new Label("vandaagLabel",
                                new ResourceModel("datum.kiezen.vandaag"));
                        vandaagLabel.setVisible(isVandaag);
                        dagLink.add(vandaagLabel);

                        if (isBeschikbaar) {
                            dagLink.add(AttributeModifier.replace("href", "#"));
                            dagLink.add(new AjaxEventBehavior("click") {
                                @Override
                                protected void onEvent(AjaxRequestTarget target) {
                                    geselecteerdeDatum = datum;
                                    geselecteerdeTijd = null;
                                    target.add(kalenderPanel, tijdslotPanel, bevestigBar);
                                }
                            });
                        }

                        dagItem.add(dagLink);
                    }
                });
            }
        };
    }

    // -------------------------------------------------------------------------
    // Time-slot panel
    // -------------------------------------------------------------------------

    private WebMarkupContainer buildTijdslotPanel() {
        WebMarkupContainer panel = new WebMarkupContainer("tijdslotPanel") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(geselecteerdeDatum != null);
            }
        };

        panel.add(new Label("geselecteerdeDatumLabel", () -> {
            if (geselecteerdeDatum == null) return "";
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE d MMMM, yyyy", new Locale("nl"));
            return geselecteerdeDatum.format(fmt);
        }));

        panel.add(new ListView<LocalTime>("tijdsloten",
                () -> geselecteerdeDatum != null
                        ? marriageIntakeService.findBeschikbareTijden(dossierId, geselecteerdeDatum)
                        : List.of()) {
            @Override
            protected void populateItem(ListItem<LocalTime> item) {
                LocalTime tijd = item.getModelObject();
                boolean isGeselecteerd = tijd.equals(geselecteerdeTijd);

                AjaxLink<LocalTime> link = new AjaxLink<>("tijdslotLink", Model.of(tijd)) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        geselecteerdeTijd = getModelObject();
                        target.add(tijdslotPanel, bevestigBar);
                    }
                };
                link.add(AttributeModifier.replace("class",
                        isGeselecteerd ? "tijdslot tijdslot--geselecteerd" : "tijdslot"));
                link.add(new Label("tijdLabel",
                        tijd.format(DateTimeFormatter.ofPattern("HH:mm"))));
                item.add(link);
            }
        });

        return panel;
    }

    // -------------------------------------------------------------------------
    // Confirm bar
    // -------------------------------------------------------------------------

    private WebMarkupContainer buildBevestigBar() {
        WebMarkupContainer bar = new WebMarkupContainer("bevestigBar") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(geselecteerdeDatum != null && geselecteerdeTijd != null);
            }
        };

        bar.add(new Label("samenvattingLabel", () -> {
            if (geselecteerdeDatum == null || geselecteerdeTijd == null) return "";
            DateTimeFormatter datumFmt = DateTimeFormatter.ofPattern("EEEE d MMMM", new Locale("nl"));
            return getString("datum.kiezen.samenvatting.prefix")
                    + " " + geselecteerdeDatum.format(datumFmt)
                    + " " + getString("datum.kiezen.samenvatting.om")
                    + " " + geselecteerdeTijd.format(DateTimeFormatter.ofPattern("HH:mm"))
                    + " " + getString("datum.kiezen.samenvatting.uur");
        }));

        Form<Void> bevestigForm = new Form<>("bevestigForm") {
            @Override
            protected void onSubmit() {
                marriageIntakeService.slaAfspraakOp(dossierId, geselecteerdeDatum, geselecteerdeTijd);
                PageParameters params = new PageParameters();
                params.add("dossierId", dossierId.toString());
                setResponsePage(DeDagPage.class, params);
            }
        };
        bar.add(bevestigForm);

        return bar;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static List<List<LocalDate>> bouwMaandRijen(YearMonth maand) {
        LocalDate eerste = maand.atDay(1);
        int verschuiving = kolomIndex(eerste.getDayOfWeek());

        List<LocalDate> cells = new ArrayList<>();
        for (int i = 0; i < verschuiving; i++) cells.add(null);
        for (int dag = 1; dag <= maand.lengthOfMonth(); dag++) cells.add(maand.atDay(dag));
        while (cells.size() % 7 != 0) cells.add(null);

        List<List<LocalDate>> rijen = new ArrayList<>();
        for (int i = 0; i < cells.size(); i += 7) rijen.add(cells.subList(i, i + 7));
        return rijen;
    }

    private static int kolomIndex(DayOfWeek dag) {
        for (int i = 0; i < KOLOM_VOLGORDE.length; i++) {
            if (KOLOM_VOLGORDE[i] == dag) return i;
        }
        return 0;
    }

    private static String buildDagClass(boolean leeg, boolean verleden,
                                        boolean beschikbaar, boolean geselecteerd) {
        if (leeg) return "kalender-dag kalender-dag--leeg";
        if (geselecteerd) return "kalender-dag kalender-dag--geselecteerd";
        if (beschikbaar) return "kalender-dag kalender-dag--beschikbaar";
        return "kalender-dag kalender-dag--onbeschikbaar";
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(
                new PackageResourceReference(IntakeBasePage.class, "mijn-dag.css")));
    }
}
