package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SaveExtrasDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.TrouwboekjeKeuzeDto;
import nl.rotterdam.nl_design_system.wicket.components.heading.RdHeading;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;

public class ExtrasPage extends IntakeBasePage {

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    @Override
    protected IntakeStep getActiveStep() {
        return IntakeStep.EXTRAS;
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new ResourceModel("intake.page.title.extras");
    }

    @Override
    protected IModel<DossierSamenvattingDto> getSidebarDossierModel() {
        return LoadableDetachableModel.of(() -> marriageIntakeService.findByDossierId(dossierId));
    }

    public static void respond(UUID dossierId) {
        RequestCycle.get().setResponsePage(
                ExtrasPage.class,
                makeDossierPageParameters(dossierId)
        );
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        pageBody.add(new RdHeading("heading", getString("extras.heading"), 1));

        DossierSamenvattingDto dossier = marriageIntakeService.findByDossierId(dossierId);
        SaveExtrasDto selecties = marriageIntakeService.findExtrasSelecties(dossierId);
        boolean isGroot = dossier.ceremonieSoort() == CeremonieSoort.GROOT;
        boolean isHuwelijk = dossier.registratieType() == RegistratieType.HUWELIJK;

        pageBody.add(new ExtrasForm("extrasForm", selecties, isGroot, isHuwelijk));
    }

    private class ExtrasForm extends Form<ExtrasFormDto> {

        private final boolean isGroot;
        private final boolean isHuwelijk;
        private WebMarkupContainer trouwboekjeSection;

        ExtrasForm(String id, SaveExtrasDto selecties, boolean isGroot, boolean isHuwelijk) {
            super(id, Model.of(ExtrasFormDto.vanSelecties(selecties)));
            this.isGroot = isGroot;
            this.isHuwelijk = isHuwelijk;
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();

            IModel<ExtrasFormDto> model = getModel();

            // --- Ringen uitwisselen ---
            WebMarkupContainer ringenSection = new WebMarkupContainer("ringenSection");
            CheckBox ringenCheckbox = new CheckBox("ringenUitwisselenCheckbox",
                    LambdaModel.of(model, ExtrasFormDto::isRingenUitwisselen, ExtrasFormDto::setRingenUitwisselen));
            ringenCheckbox.add(new AjaxFormComponentUpdatingBehavior("change") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    slaOp();
                    target.add(keuzesSidebar);
                }
            });
            ringenSection.add(ringenCheckbox);
            add(ringenSection);

            // --- Muziek ---
            WebMarkupContainer muziekSection = new WebMarkupContainer("muziekSection") {
                @Override
                protected void onConfigure() {
                    super.onConfigure();
                    setVisible(isGroot);
                }
            };
            CheckBox muziekCheckbox = new CheckBox("muziekCheckbox",
                    LambdaModel.of(model, ExtrasFormDto::isMuziek, ExtrasFormDto::setMuziek));
            muziekCheckbox.add(new AjaxFormComponentUpdatingBehavior("change") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    slaOp();
                    target.add(keuzesSidebar);
                }
            });
            muziekSection.add(muziekCheckbox);
            add(muziekSection);

            // --- Trouwboekje ---
            List<TrouwboekjeKeuzeDto> trouwboekjes = marriageIntakeService.findActieveTrouwboekjes();
            trouwboekjeSection = new WebMarkupContainer("trouwboekjeSection");
            trouwboekjeSection.setOutputMarkupId(true);
            trouwboekjeSection.add(bouwTrouwboekjeKeuzeList("trouwboekjeKeuzes", trouwboekjes,
                    LambdaModel.of(model, ExtrasFormDto::getTrouwboekjeId, ExtrasFormDto::setTrouwboekjeId)));
            add(trouwboekjeSection);

            // --- Internationale akte ---
            WebMarkupContainer internationaleAkteSection = new WebMarkupContainer("internationaleAkteSection") {
                @Override
                protected void onConfigure() {
                    super.onConfigure();
                    setVisible(isHuwelijk);
                }
            };
            CheckBox internationaleAkteCheckbox = new CheckBox("internationaleAkteCheckbox",
                    LambdaModel.of(model, ExtrasFormDto::isInternationaleAkte, ExtrasFormDto::setInternationaleAkte));
            internationaleAkteCheckbox.add(new AjaxFormComponentUpdatingBehavior("change") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    slaOp();
                    target.add(keuzesSidebar);
                }
            });
            internationaleAkteSection.add(internationaleAkteCheckbox, new Label("internationaleAktePrijs",
                    formatPrijs(marriageIntakeService.findInternationaleAktePrijs(dossierId))));
            add(internationaleAkteSection);
        }

        private ListView<TrouwboekjeKeuzeDto> bouwTrouwboekjeKeuzeList(String id, List<TrouwboekjeKeuzeDto> trouwboekjes,
                                                                       IModel<Long> geselecteerdIdModel) {
            return new ListView<>(id, new ListModel<>(trouwboekjes)) {
                @Override
                protected void populateItem(ListItem<TrouwboekjeKeuzeDto> item) {
                    TrouwboekjeKeuzeDto trouwboekje = item.getModelObject();

                    WebMarkupContainer keuzeItem = new WebMarkupContainer("keuzeItem");
                    boolean isGeselecteerd = Objects.equals(trouwboekje.id(), geselecteerdIdModel.getObject());

                    if (trouwboekje.afbeelding() != null && !trouwboekje.afbeelding().isBlank()) {
                        keuzeItem.add(new WebMarkupContainer("afbeelding")
                                .add(AttributeModifier.replace("src", trouwboekje.afbeelding())));
                    } else {
                        keuzeItem.add(new WebMarkupContainer("afbeelding").setVisible(false));
                    }

                    keuzeItem.add(new Label("naam", Model.of(trouwboekje.naam())));

                    WebMarkupContainer prijsContainer = new WebMarkupContainer("prijsContainer");
                    prijsContainer.setVisible(trouwboekje.prijs() != null);
                    if (trouwboekje.prijs() != null) {
                        prijsContainer.add(new Label("prijs", Model.of(formatPrijs(trouwboekje.prijs()))));
                    } else {
                        prijsContainer.add(new Label("prijs", Model.of("")));
                    }
                    keuzeItem.add(prijsContainer);

                    if (trouwboekje.omschrijving() != null) {
                        keuzeItem.add(new Label("omschrijving", Model.of(trouwboekje.omschrijving())));
                    } else {
                        keuzeItem.add(new Label("omschrijving", Model.of("")).setVisible(false));
                    }

                    CheckBox selectCheckbox = new CheckBox("selectCheckbox", Model.of(isGeselecteerd));
                    // Vaste markup-id, zodat Wicket de focus na het verversen van de keuzes terugzet
                    selectCheckbox.setMarkupId("trouwboekje-" + trouwboekje.id());
                    selectCheckbox.add(new AjaxFormComponentUpdatingBehavior("change") {
                        @Override
                        protected void onUpdate(AjaxRequestTarget target) {
                            Boolean checked = ((CheckBox) getComponent()).getModelObject();
                            if (Boolean.TRUE.equals(checked)) {
                                geselecteerdIdModel.setObject(trouwboekje.id());
                            } else {
                                geselecteerdIdModel.setObject(null);
                            }
                            slaOp();
                            // Andere kaarten opnieuw renderen zodat er maximaal één trouwboekje gekozen is
                            target.add(trouwboekjeSection, keuzesSidebar);
                        }
                    });
                    keuzeItem.add(selectCheckbox);
                    item.add(keuzeItem);
                }
            };
        }

        private String formatPrijs(BigDecimal prijs) {
            return new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.forLanguageTag("nl-NL"))).format(prijs);
        }

        @Override
        protected void onSubmit() {
            slaOp();
            setResponsePage(ExtrasPage.class, makeDossierPageParameters(dossierId));
        }

        private void slaOp() {
            ExtrasFormDto f = getModelObject();
            marriageIntakeService.slaExtrasOp(dossierId, new SaveExtrasDto(
                    f.isRingenUitwisselen(),
                    f.isMuziek(),
                    f.getTrouwboekjeId(),
                    f.isInternationaleAkte()
            ));
        }
    }
}
