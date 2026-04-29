package nl.rotterdam.huwelijk.features.marriage_intake.ui;

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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
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

        List<GetuigeDto> bestaande = marriageIntakeService.findGetuigen(dossierId);
        pageBody.add(new GetuigenForm("getuigenForm", GetuigenFormDto.vanGetuigen(bestaande)));
    }

    private class GetuigenForm extends Form<GetuigenFormDto> {

        private final FileUploadField bestandVeld1 = new FileUploadField("bestandInput");
        private final FileUploadField bestandVeld2 = new FileUploadField("bestandInput");
        private final FileUploadField bestandVeld3 = new FileUploadField("bestandInput");
        private final FileUploadField bestandVeld4 = new FileUploadField("bestandInput");

        GetuigenForm(String id, GetuigenFormDto formDto) {
            super(id, Model.of(formDto));
            setMultiPart(true);
            setMaxSize(Bytes.megabytes(10));
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();

            IModel<GetuigenFormDto> model = getModel();

            DossierSamenvattingDto dossier = marriageIntakeService.findByDossierId(dossierId);
            int maxGetuigen = dossier.ceremonieSoort() == CeremonieSoort.KLEIN ? 2 : 4;
            List<GetuigeDto> bestaande = marriageIntakeService.findGetuigen(dossierId);

            add(
                    maakGetuigeBlok("getuige1", 1, model, bestandVeld1, maxGetuigen, bestaande),
                    maakGetuigeBlok("getuige2", 2, model, bestandVeld2, maxGetuigen, bestaande),
                    maakGetuigeBlok("getuige3", 3, model, bestandVeld3, maxGetuigen, bestaande),
                    maakGetuigeBlok("getuige4", 4, model, bestandVeld4, maxGetuigen, bestaande)
            );
        }

        private WebMarkupContainer maakGetuigeBlok(String id, int volgnummer,
                                                    IModel<GetuigenFormDto> model,
                                                    FileUploadField bestandVeld,
                                                    int maxGetuigen,
                                                    List<GetuigeDto> bestaande) {
            boolean zichtbaar = volgnummer <= maxGetuigen;
            String bestaandeBestandNaam = bestaande.stream()
                    .filter(g -> g.volgnummer() == volgnummer)
                    .map(g -> g.bestand() != null ? g.bestand().bestandNaam() : null)
                    .findFirst()
                    .orElse(null);

            WebMarkupContainer blok = new WebMarkupContainer(id) {
                @Override
                protected void onConfigure() {
                    super.onConfigure();
                    setVisible(zichtbaar);
                }
            };
            blok.add(new Label("getuigeNummer", getString("de.getuigen.getuige") + " " + volgnummer));

            blok.add(new RdFormFieldTextInput<>("naam",
                    naamModel(volgnummer, model),
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

            bestandVeld.add(new AjaxFormSubmitBehavior("change") {
                @Override
                protected void onSubmit(AjaxRequestTarget target) {
                    // form's onSubmit() handles saving
                }
            });
            blok.add(bestandVeld);
            blok.add(new Label("bestandNaamLabel", bestaandeBestandNaam != null ? bestaandeBestandNaam : ""));
            return blok;
        }

        private IModel<PersonFullName> naamModel(int volgnummer, IModel<GetuigenFormDto> model) {
            return switch (volgnummer) {
                case 1 -> LambdaModel.of(model, GetuigenFormDto::getNaam1, GetuigenFormDto::setNaam1);
                case 2 -> LambdaModel.of(model, GetuigenFormDto::getNaam2, GetuigenFormDto::setNaam2);
                case 3 -> LambdaModel.of(model, GetuigenFormDto::getNaam3, GetuigenFormDto::setNaam3);
                case 4 -> LambdaModel.of(model, GetuigenFormDto::getNaam4, GetuigenFormDto::setNaam4);
                default -> throw new IllegalArgumentException("Ongeldig volgnummer: " + volgnummer);
            };
        }

        @Override
        protected void onSubmit() {
            slaAllesOp();
        }

        private void slaAllesOp() {
            GetuigenFormDto formDto = getModelObject();
            DossierSamenvattingDto dossier = marriageIntakeService.findByDossierId(dossierId);
            int maxGetuigen = dossier.ceremonieSoort() == CeremonieSoort.KLEIN ? 2 : 4;

            PersonFullName[] namen = {
                    formDto.getNaam1(), formDto.getNaam2(),
                    formDto.getNaam3(), formDto.getNaam4()
            };
            FileUploadField[] velden = {bestandVeld1, bestandVeld2, bestandVeld3, bestandVeld4};
            LegitimatieFileUpload[] bestanden = {
                    formDto.getBestand1(), formDto.getBestand2(),
                    formDto.getBestand3(), formDto.getBestand4()
            };

            List<SaveGetuigenDto> teOpslaan = new ArrayList<>();
            for (int i = 0; i < maxGetuigen; i++) {
                PersonFullName naam = namen[i];
                if (naam == null) {
                    continue;
                }
                FileUpload upload = velden[i].getFileUpload();
                LegitimatieFileUpload bestand;
                if (upload != null && upload.getClientFileName() != null && !upload.getClientFileName().isBlank()) {
                    bestand = new LegitimatieFileUpload(upload.getClientFileName(), upload.getBytes());
                    // Update FormDto so subsequent saves preserve this file
                    setBestandOpFormDto(formDto, i + 1, bestand);
                } else {
                    bestand = bestanden[i];
                }
                teOpslaan.add(new SaveGetuigenDto(i + 1, naam.getValue(), bestand));
            }

            marriageIntakeService.slaGetuigenOp(dossierId, teOpslaan);
        }

        private void setBestandOpFormDto(GetuigenFormDto formDto, int volgnummer, LegitimatieFileUpload bestand) {
            switch (volgnummer) {
                case 1 -> formDto.setBestand1(bestand);
                case 2 -> formDto.setBestand2(bestand);
                case 3 -> formDto.setBestand3(bestand);
                case 4 -> formDto.setBestand4(bestand);
            }
        }
    }
}

