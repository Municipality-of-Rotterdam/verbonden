package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.administration_common.RdFormFieldFileUpload;
import nl.rotterdam.huwelijk.features.babs_administration.domain.PersonFullName;
import nl.rotterdam.huwelijk.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.GetuigeDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.LegitimatieFileUpload;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.SaveGetuigenDto;
import nl.rotterdam.nl_design_system.wicket.components.fieldset.RdFieldset;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import nl.rotterdam.nl_design_system.wicket.components.heading.RdHeading;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Bytes;

import java.util.List;
import java.util.UUID;

import static nl.rotterdam.huwelijk.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;
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
    protected IModel<DossierSamenvattingDto> getSidebarDossierModel() {
        return Model.of(marriageIntakeService.findByDossierId(dossierId));
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

        pageBody.add(new RdHeading("heading", getString("de.getuigen.heading"), 1));
        pageBody.add(new Label("intro", new ResourceModel("de.getuigen.intro")));

        DossierSamenvattingDto dossier = marriageIntakeService.findByDossierId(dossierId);
        int maxGetuigen = dossier.ceremonieSoort() == CeremonieSoort.KLEIN ? 2 : 4;
        List<GetuigeDto> bestaande = marriageIntakeService.findGetuigen(dossierId);
        List<GetuigenItemFormDto> items = GetuigenItemFormDto.vanGetuigen(maxGetuigen, bestaande);

        pageBody.add(new ListView<GetuigenItemFormDto>("getuigen", new ListModel<>(items)) {
            @Override
            protected void populateItem(ListItem<GetuigenItemFormDto> item) {
                item.add(new GetuigeForm("getuigeForm", item.getModel()));
            }
        }.setReuseItems(true));
    }

    private class GetuigeForm extends Form<GetuigenItemFormDto> {

        private RdFormFieldFileUpload bestandVeld;

        GetuigeForm(String id, IModel<GetuigenItemFormDto> model) {
            super(id, model);
            setMultiPart(true);
            setMaxSize(Bytes.megabytes(10));
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();

            IModel<GetuigenItemFormDto> model = getModel();
            int volgnummer = model.getObject().getVolgnummer();
            String bestaandeBestandNaam = model.getObject().getBestand() != null
                    ? model.getObject().getBestand().bestandNaam() : null;

            bestandVeld = new RdFormFieldFileUpload(
                    "bestandInput", new ListModel<>(), new ResourceModel("de.getuigen.bestand.label"));

            RdFieldset<String> fieldset = new RdFieldset<>(
                    "getuigeFieldset", NULL_STRING_MODEL,
                    Model.of(getString("de.getuigen.getuige") + " " + volgnummer));
            fieldset.add(
                    new RdFormFieldTextInput<>("naam",
                            LambdaModel.of(model, GetuigenItemFormDto::getNaam, GetuigenItemFormDto::setNaam),
                            new ResourceModel("de.getuigen.naam.label"))
                            .setModelType(PersonFullName.class)
                            .withTextInput((rdTextInput, parent) -> rdTextInput.add(
                                    new AjaxFormComponentUpdatingBehavior("change") {
                                        @Override
                                        protected void onUpdate(AjaxRequestTarget target) {
                                            slaOp();
                                        }
                                    }
                            )),
                    bestandVeld.withInput(input -> input.add(new AjaxFormSubmitBehavior("change") {
                        @Override
                        protected void onSubmit(AjaxRequestTarget target) {
                            // form's onSubmit() handles saving
                        }
                    })),
                    new Label("bestandNaamLabel", bestaandeBestandNaam != null ? bestaandeBestandNaam : "")
            );
            add(fieldset);
        }

        @Override
        protected void onSubmit() {
            slaOp();
        }

        private void slaOp() {
            GetuigenItemFormDto formDto = getModelObject();
            PersonFullName naam = formDto.getNaam();
            if (naam == null) {
                return;
            }
            FileUpload upload = bestandVeld.getFileUpload();
            LegitimatieFileUpload bestand;
            if (upload != null && upload.getClientFileName() != null && !upload.getClientFileName().isBlank()) {
                bestand = new LegitimatieFileUpload(upload.getClientFileName(), upload.getBytes());
                formDto.setBestand(bestand);
            } else {
                bestand = formDto.getBestand();
            }
            marriageIntakeService.slaGetuigeOp(dossierId,
                    new SaveGetuigenDto(formDto.getVolgnummer(), naam.getValue(), bestand));
        }
    }
}

