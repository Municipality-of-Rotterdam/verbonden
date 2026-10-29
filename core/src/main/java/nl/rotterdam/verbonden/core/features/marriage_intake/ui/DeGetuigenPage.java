package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.domain.PersonFullName;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.GetuigeDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SaveGetuigenDto;
import nl.rotterdam.nl_design_system.wicket.components.fieldset.RdFieldset;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.basic.Label;
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

import java.util.List;
import java.util.UUID;

import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;
import static nl.rotterdam.nl_design_system.wicket.components.models.DefaultModels.NULL_STRING_MODEL;

public class DeGetuigenPage extends IntakeBasePage {

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    @Override
    protected IntakeStep getActiveStep() {
        return IntakeStep.DE_GETUIGEN;
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new ResourceModel("intake.page.title.de.getuigen");
    }

    @Override
    protected IModel<String> getStepHeadingModel() {
        return new ResourceModel("intake.step.getuigen");
    }

    @Override
    protected IModel<String> getStepIntroModel() {
        return new ResourceModel("de.getuigen.intro");
    }

    @Override
    protected IModel<DossierSamenvattingDto> getSidebarDossierModel() {
        return LoadableDetachableModel.of(() -> marriageIntakeService.findByDossierId(dossierId));
    }

    public static void respond(UUID dossierId) {
        RequestCycle.get().setResponsePage(
                DeGetuigenPage.class,
                makeDossierPageParameters(dossierId)
        );
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        DossierSamenvattingDto dossier = marriageIntakeService.findByDossierId(dossierId);
        int maxGetuigen = dossier.ceremonieSoort().getAantalGetuigen();
        List<GetuigeDto> bestaande = marriageIntakeService.findGetuigen(dossierId);
        List<GetuigenItemFormDto> items = GetuigenItemFormDto.vanGetuigen(maxGetuigen, bestaande);

        pageBody.add(new ListView<>("getuigen", new ListModel<>(items)) {
            @Override
            protected void populateItem(ListItem<GetuigenItemFormDto> item) {
                item.add(new GetuigeForm("getuigeForm", item.getModel()));
            }
        }.setReuseItems(true));
    }

    /**
     * Slaat een getuige op via Ajax en ververst alleen het veld en de sidebar, zodat de pagina op mobiel niet naar
     * boven springt.
     */
    private class GetuigeForm extends Form<GetuigenItemFormDto> {

        private RdFormFieldTextInput<PersonFullName> naamVeld;

        GetuigeForm(String id, IModel<GetuigenItemFormDto> model) {
            super(id, model);
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();

            IModel<GetuigenItemFormDto> model = getModel();
            int volgnummer = model.getObject().getVolgnummer();

            naamVeld = new RdFormFieldTextInput<>("naam",
                    LambdaModel.of(model, GetuigenItemFormDto::getNaam, GetuigenItemFormDto::setNaam),
                    new ResourceModel("de.getuigen.naam.label"));
            naamVeld.setModelType(PersonFullName.class);
            naamVeld.setOutputMarkupId(true);
            naamVeld.withTextInput((rdTextInput, _) -> rdTextInput.add(
                    new AjaxFormComponentUpdatingBehavior("change") {
                        @Override
                        protected void onUpdate(AjaxRequestTarget target) {
                            slaOp(target);
                        }

                        @Override
                        protected void onError(AjaxRequestTarget target, RuntimeException e) {
                            target.add(naamVeld);
                        }
                    }
            ));

            RdFieldset<String> fieldset = new RdFieldset<>(
                    "getuigeFieldset", NULL_STRING_MODEL,
                    Model.of(getString("de.getuigen.getuige") + " " + volgnummer));
            fieldset.add(naamVeld);
            add(fieldset);

            // Enter of "Gereed" op het mobiele toetsenbord verstuurt het formulier ook via Ajax
            add(new AjaxFormSubmitBehavior(this, "submit") {
                @Override
                protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                    super.updateAjaxAttributes(attributes);
                    attributes.setPreventDefault(true);
                }

                @Override
                protected void onSubmit(AjaxRequestTarget target) {
                    // Opslaan gebeurt al in Form#onSubmit(), dat deze behavior ook aanroept
                    target.add(naamVeld, keuzesSidebar);
                }

                @Override
                protected void onError(AjaxRequestTarget target) {
                    target.add(naamVeld);
                }
            });
        }

        /**
         * Wordt aangeroepen bij zowel de Ajax-submit als een gewone submit zonder JavaScript. Geen redirect: die zou ook
         * het Ajax-antwoord vervangen en de pagina laten herladen.
         */
        @Override
        protected void onSubmit() {
            opslaan();
        }

        private void slaOp(AjaxRequestTarget target) {
            opslaan();
            target.add(naamVeld, keuzesSidebar);
        }

        private void opslaan() {
            GetuigenItemFormDto formDto = getModelObject();
            PersonFullName naam = formDto.getNaam();
            marriageIntakeService.slaGetuigeOp(dossierId,
                    new SaveGetuigenDto(formDto.getVolgnummer(), naam==null ? null : naam.getValue()));
        }
    }
}
