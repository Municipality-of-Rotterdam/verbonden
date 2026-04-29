package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.administration_common.RdFormFieldFileUpload;
import nl.rotterdam.huwelijk.features.babs_administration.domain.PersonFullName;
import nl.rotterdam.huwelijk.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.GetuigeDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.LegitimatieFileUpload;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.SaveGetuigenDto;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static nl.rotterdam.huwelijk.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;

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

        pageBody.add(new GetuigenForm("getuigenForm", items));
    }

    private class GetuigenForm extends Form<List<GetuigenItemFormDto>> {

        private final List<RdFormFieldFileUpload> bestandVelden = new ArrayList<>();

        GetuigenForm(String id, List<GetuigenItemFormDto> items) {
            super(id, new ListModel<>(items));
            setMultiPart(true);
            setMaxSize(Bytes.megabytes(10));
            for (int i = 0; i < items.size(); i++) {
                bestandVelden.add(new RdFormFieldFileUpload(
                        "bestandInput", new ListModel<>(), new ResourceModel("de.getuigen.bestand.label")));
            }
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();

            IModel<List<GetuigenItemFormDto>> model = getModel();

            add(new ListView<GetuigenItemFormDto>("getuigen", model) {
                @Override
                protected void populateItem(ListItem<GetuigenItemFormDto> item) {
                    int index = item.getIndex();
                    int volgnummer = item.getModelObject().getVolgnummer();
                    RdFormFieldFileUpload bestandVeld = bestandVelden.get(index);

                    String bestaandeBestandNaam = item.getModelObject().getBestand() != null
                            ? item.getModelObject().getBestand().bestandNaam() : null;

                    item.add(new Label("getuigeNummer", getString("de.getuigen.getuige") + " " + volgnummer));

                    item.add(new RdFormFieldTextInput<>("naam",
                            LambdaModel.of(item.getModel(), GetuigenItemFormDto::getNaam, GetuigenItemFormDto::setNaam),
                            new ResourceModel("de.getuigen.naam.label"))
                            .setModelType(PersonFullName.class)
                            .withTextInput((rdTextInput, parent) -> rdTextInput.add(
                                    new AjaxFormComponentUpdatingBehavior("change") {
                                        @Override
                                        protected void onUpdate(AjaxRequestTarget target) {
                                            slaAllesOp();
                                        }
                                    }
                            ))
                    );

                    bestandVeld.withInput(input -> input.add(new AjaxFormSubmitBehavior("change") {
                        @Override
                        protected void onSubmit(AjaxRequestTarget target) {
                            // form's onSubmit() handles saving
                        }
                    }));
                    item.add(bestandVeld);
                    item.add(new Label("bestandNaamLabel", bestaandeBestandNaam != null ? bestaandeBestandNaam : ""));
                }
            }.setReuseItems(true));
        }

        @Override
        protected void onSubmit() {
            slaAllesOp();
        }

        private void slaAllesOp() {
            List<GetuigenItemFormDto> items = getModelObject();
            List<SaveGetuigenDto> teOpslaan = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                GetuigenItemFormDto item = items.get(i);
                PersonFullName naam = item.getNaam();
                if (naam == null) {
                    continue;
                }
                FileUpload upload = bestandVelden.get(i).getFileUpload();
                LegitimatieFileUpload bestand;
                if (upload != null && upload.getClientFileName() != null && !upload.getClientFileName().isBlank()) {
                    bestand = new LegitimatieFileUpload(upload.getClientFileName(), upload.getBytes());
                    item.setBestand(bestand);
                } else {
                    bestand = item.getBestand();
                }
                teOpslaan.add(new SaveGetuigenDto(item.getVolgnummer(), naam.getValue(), bestand));
            }

            marriageIntakeService.slaGetuigenOp(dossierId, teOpslaan);
        }
    }
}

