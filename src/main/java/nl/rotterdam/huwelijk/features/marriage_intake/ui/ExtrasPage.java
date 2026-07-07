package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.extra_administration.domain.ExtraType;
import nl.rotterdam.huwelijk.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.ExtraDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.SaveExtrasDto;
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
import java.util.UUID;

import static nl.rotterdam.huwelijk.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;

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
        return Model.of(marriageIntakeService.findByDossierId(dossierId));
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
                }
            });
            muziekSection.add(muziekCheckbox);
            add(muziekSection);

            // --- Trouwboekje ---
            List<ExtraDto> trouwboekjes = marriageIntakeService.findActiefExtras(ExtraType.TROUWBOEKJE);
            WebMarkupContainer trouwboekjeSection = new WebMarkupContainer("trouwboekjeSection");
            trouwboekjeSection.add(bouwExtraKeuzeList("trouwboekjeKeuzes", trouwboekjes,
                    LambdaModel.of(model, ExtrasFormDto::getTrouwboekjeId, ExtrasFormDto::setTrouwboekjeId)));
            add(trouwboekjeSection);

            // --- Internationale akte ---
            List<ExtraDto> internationaleAktes = marriageIntakeService.findActiefExtras(ExtraType.INTERNATIONALE_AKTE);
            WebMarkupContainer internationaleAkteSection = new WebMarkupContainer("internationaleAkteSection") {
                @Override
                protected void onConfigure() {
                    super.onConfigure();
                    setVisible(isHuwelijk);
                }
            };
            internationaleAkteSection.add(bouwExtraKeuzeList("internationaleAkteKeuzes", internationaleAktes,
                    LambdaModel.of(model, ExtrasFormDto::getInternationaleAkteId, ExtrasFormDto::setInternationaleAkteId)));
            add(internationaleAkteSection);
        }

        private ListView<ExtraDto> bouwExtraKeuzeList(String id, List<ExtraDto> extras,
                                                       IModel<Long> geselecteerdIdModel) {
            return new ListView<>(id, new ListModel<>(extras)) {
                @Override
                protected void populateItem(ListItem<ExtraDto> item) {
                    ExtraDto extra = item.getModelObject();

                    WebMarkupContainer keuzeItem = new WebMarkupContainer("keuzeItem");
                    boolean isGeselecteerd = Long.valueOf(extra.id()).equals(geselecteerdIdModel.getObject());
                    if (isGeselecteerd) {
                        keuzeItem.add(AttributeModifier.append("class", " rd-extra-keuze--geselecteerd"));
                    }

                    if (extra.afbeelding() != null && !extra.afbeelding().isBlank()) {
                        keuzeItem.add(new WebMarkupContainer("extraAfbeelding")
                                .add(AttributeModifier.replace("src", extra.afbeelding())));
                    } else {
                        keuzeItem.add(new WebMarkupContainer("extraAfbeelding").setVisible(false));
                    }

                    keuzeItem.add(new Label("extraNaam", Model.of(extra.naam())));

                    WebMarkupContainer prijsContainer = new WebMarkupContainer("extraPrijsContainer");
                    prijsContainer.setVisible(extra.prijs() != null);
                    if (extra.prijs() != null) {
                        prijsContainer.add(new Label("extraPrijs", Model.of(formatPrijs(extra.prijs()))));
                    } else {
                        prijsContainer.add(new Label("extraPrijs", Model.of("")));
                    }
                    keuzeItem.add(prijsContainer);

                    if (extra.omschrijving() != null) {
                        keuzeItem.add(new Label("extraOmschrijving", Model.of(extra.omschrijving())));
                    } else {
                        keuzeItem.add(new Label("extraOmschrijving", Model.of("")).setVisible(false));
                    }

                    CheckBox selectCheckbox = new CheckBox("selectCheckbox", Model.of(isGeselecteerd));
                    selectCheckbox.setOutputMarkupId(true);
                    selectCheckbox.add(new AjaxFormComponentUpdatingBehavior("change") {
                        @Override
                        protected void onUpdate(AjaxRequestTarget target) {
                            Boolean checked = (Boolean) getComponent().getDefaultModelObject();
                            if (Boolean.TRUE.equals(checked)) {
                                geselecteerdIdModel.setObject(extra.id());
                            } else {
                                geselecteerdIdModel.setObject(null);
                            }
                            slaOp();
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
        }

        private void slaOp() {
            ExtrasFormDto f = getModelObject();
            marriageIntakeService.slaExtrasOp(dossierId, new SaveExtrasDto(
                    f.isRingenUitwisselen(),
                    f.isMuziek(),
                    f.getTrouwboekjeId(),
                    f.getInternationaleAkteId()
            ));
            setResponsePage(ExtrasPage.class, makeDossierPageParameters(dossierId));
        }
    }
}
