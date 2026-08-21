package nl.rotterdam.verbonden.features.marriage_intake.ui;

import nl.rotterdam.verbonden.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.features.marriage_intake.domain.ChangeIntakeDto;
import nl.rotterdam.verbonden.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.verbonden.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.verbonden.features.marriage_intake.domain.IntakeMarriageTypeDto;
import nl.rotterdam.verbonden.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.heading.RdHeading;
import nl.rotterdam.nl_design_system.wicket.components.radio_button.RdRadioButton;
import nl.rotterdam.nl_design_system.wicket.components.radio_group.RdRadioGroup;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class MarriageIntakePage extends IntakeBasePage {

    private static final DecimalFormat PRIJS_FORMAT =
            new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.forLanguageTag("nl-NL")));

    private static final DateTimeFormatter DATUM_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    private RegistratieType registratieType = RegistratieType.GEREGISTREERD_PARTNERSCHAP;

    private final RdRadioGroup<RegistratieType> registrationGroup = new RdRadioGroup<>(
            "registrationGroup",
            new PropertyModel<>(this, "registratieType"),
            new ResourceModel("intake.registratie.legend"),
            new ResourceModel("intake.registratie.description")
    );


    @Override
    protected IntakeStep getActiveStep() {
        return IntakeStep.DE_DAG;
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new ResourceModel("intake.page.title.marriageintake");
    }

    @Override
    protected boolean requiresDossier() {
        return false;
    }

    @Override
    protected IModel<DossierSamenvattingDto> getSidebarDossierModel() {
        if (dossierId != null) {
            return () -> {
                DossierSamenvattingDto d = marriageIntakeService.findByDossierId(dossierId);
                return new DossierSamenvattingDto(d.id(), registratieType, d.ceremonieSoort(),
                        d.prijs(), d.datumTijdHuwelijk(), d.huwelijksLocatie(),
                        d.gegevensBevestigd(), d.getuigenBevestigd(), d.getuigenGedeeltelijkIngevuld(), d.extras(),
                        d.aantalGekozenAchternamen(), d.totalPrijs());
            };
        }
        return () -> new DossierSamenvattingDto(null, registratieType, CeremonieSoort.KLEIN, null, null, null, false, false, false, List.of(), 0, null);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        if (dossierId != null) {
            registratieType = marriageIntakeService.findByDossierId(dossierId).registratieType();
        }

        pageBody.add(new RdHeading("heading", getString("intake.heading"), 1));

        Form<Void> form = new Form<>("form");
        pageBody.add(form);


        form.add(registrationGroup);
        RadioGroup<RegistratieType> regRadioGroup = registrationGroup.getRadioGroup();
        registrationGroup.add(new RdRadioButton<>("huwelijk", Model.of(RegistratieType.HUWELIJK), regRadioGroup));
        registrationGroup.add(new RdRadioButton<>("geregistreerdPartnerschap",
                Model.of(RegistratieType.GEREGISTREERD_PARTNERSCHAP), regRadioGroup));

        // Load ceremony types from database
        List<IntakeMarriageTypeDto> marriageTypes = marriageIntakeService.findAllMarriageTypes();

        form.add(new ListView<>("ceremonyTypesList", marriageTypes) {
            @Override
            protected void populateItem(ListItem<IntakeMarriageTypeDto> item) {
                IntakeMarriageTypeDto dto = item.getModelObject();

                item.add(new Label("titel", dto.titel()));

                // "Vanaf" prefix — only rendered for GROOT
                Label prijsPrefix = new Label("prijsPrefix", dto.prijsPrefix());
                prijsPrefix.setVisible(dto.prijsPrefix() != null);
                item.add(prijsPrefix);

                // Price badge (text only; CSS class utrecht-status-badge applied in HTML)
                item.add(new Label("prijs", "€\u00a0" + formatPrijs(dto.prijs())));

                // Bullet points
                item.add(new ListView<>("bullets", dto.bulletPoints()) {
                    @Override
                    protected void populateItem(ListItem<String> bulletItem) {
                        bulletItem.add(new Label("bullet", bulletItem.getModel()));
                    }
                });

                // "Eerste mogelijkheid" block — hidden when no date is available
                WebMarkupContainer eersteGelegenheidBox =
                        new WebMarkupContainer("eersteGelegenheidBox");
                boolean heeftDatum = dto.eersteGelegenheid() != null;
                eersteGelegenheidBox.setVisible(heeftDatum);
                eersteGelegenheidBox.add(new Label("eersteGelegenheid",
                        heeftDatum ? dto.eersteGelegenheid().format(DATUM_FORMAT) : ""));
                item.add(eersteGelegenheidBox);

                // Submit button — creates a new dossier or updates an existing one
                item.add(new RdButton("kiesButton", Model.of(dto.titel())) {

                    {
                        setEnabled(dto.active());
                    }
                    @Override
                    public void onSubmit() {
                        if (dossierId != null) {
                            marriageIntakeService.updateIntake(dossierId,
                                    new ChangeIntakeDto(registratieType, dto.soort(), dto.locatieId()));
                        } else {
                            dossierId = marriageIntakeService.create(
                                    new CreateDossierDto(registratieType, dto.soort(), dto.locatieId(), getCurrentBsn()));
                        }

                        DeDagPage.respond(dossierId);
                    }
                });
            }
        });

        // "Verder" button — only shown when a dossier already exists
        WebMarkupContainer verderContainer = new WebMarkupContainer("verderContainer");
        verderContainer.setVisible(dossierId != null);
        verderContainer.add(new Link<Void>("verderLink") {
            @Override
            public void onClick() {
                DeDagPage.respond(dossierId);
            }
        });
        pageBody.add(verderContainer);

        registrationGroup.getRadioGroup().add(new AjaxFormChoiceComponentUpdatingBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(keuzesSidebar);
            }
        });
    }

    private static String formatPrijs(BigDecimal prijs) {
        if (prijs == null) {
            return "";
        }
        return PRIJS_FORMAT.format(prijs);
    }

}
