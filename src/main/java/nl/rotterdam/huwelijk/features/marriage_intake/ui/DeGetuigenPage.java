package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.GetuigeDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.SaveGetuigenDto;
import nl.rotterdam.nl_design_system.wicket.components.heading.RdHeading;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.IModel;
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

        pageBody.add(new GetuigenForm("getuigenForm"));
    }

    private class GetuigenForm extends Form<Void> {

        private final List<WebMarkupContainer> getuigenBlokken = new ArrayList<>();

        GetuigenForm(String id) {
            super(id);
            setMultiPart(true);
            setMaxSize(Bytes.megabytes(10));
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();

            DossierSamenvattingDto dossier = marriageIntakeService.findByDossierId(dossierId);
            int maxGetuigen = dossier.ceremonieSoort() == CeremonieSoort.KLEIN ? 2 : 4;

            List<GetuigeDto> bestaandeGetuigen = marriageIntakeService.findGetuigen(dossierId);

            for (int i = 1; i <= 4; i++) {
                final int volgnummer = i;
                boolean zichtbaar = i <= maxGetuigen;

                String bestaandeNaam = bestaandeGetuigen.stream()
                        .filter(g -> g.volgnummer() == volgnummer)
                        .map(GetuigeDto::naam)
                        .findFirst()
                        .orElse(null);

                String bestaandeBestandNaam = bestaandeGetuigen.stream()
                        .filter(g -> g.volgnummer() == volgnummer)
                        .map(GetuigeDto::bestandNaam)
                        .findFirst()
                        .orElse(null);

                WebMarkupContainer blok = new WebMarkupContainer("getuige" + i) {
                    @Override
                    protected void onConfigure() {
                        super.onConfigure();
                        setVisible(zichtbaar);
                    }
                };

                blok.add(new Label("getuigeNummer",
                        getString("de.getuigen.getuige") + " " + i));
                blok.add(new TextField<>("naam", Model.of(bestaandeNaam)));
                blok.add(new FileUploadField("bestand"));
                blok.add(new Label("bestandNaamLabel",
                        bestaandeBestandNaam != null ? bestaandeBestandNaam : ""));

                getuigenBlokken.add(blok);
                add(blok);
            }
        }

        @Override
        protected void onSubmit() {
            DossierSamenvattingDto dossier = marriageIntakeService.findByDossierId(dossierId);
            int maxGetuigen = dossier.ceremonieSoort() == CeremonieSoort.KLEIN ? 2 : 4;

            List<SaveGetuigenDto> teOpslaan = new ArrayList<>();
            for (int i = 0; i < maxGetuigen; i++) {
                int volgnummer = i + 1;
                WebMarkupContainer blok = getuigenBlokken.get(i);

                @SuppressWarnings("unchecked")
                TextField<String> naamVeld = (TextField<String>) blok.get("naam");
                FileUploadField bestandVeld = (FileUploadField) blok.get("bestand");

                String naam = naamVeld != null ? naamVeld.getModelObject() : null;
                FileUpload upload = bestandVeld != null ? bestandVeld.getFileUpload() : null;
                String bestandNaam = upload != null ? upload.getClientFileName() : null;
                byte[] bestandData = upload != null ? upload.getBytes() : null;

                teOpslaan.add(new SaveGetuigenDto(volgnummer, naam, bestandNaam, bestandData));
            }

            marriageIntakeService.slaGetuigenOp(dossierId, teOpslaan);
            DeGetuigenPage.respond(dossierId);
        }
    }
}
