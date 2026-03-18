package nl.rotterdam.huwelijk.features.location_administration.ui;

import nl.rotterdam.huwelijk.administration_common.AdministrationBasePage;
import nl.rotterdam.huwelijk.features.location_administration.application.NietBeschikbareDagImportService;
import nl.rotterdam.huwelijk.features.location_administration.domain.NietBeschikbareDagImportResult;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.io.IOException;
import java.io.InputStream;

public class NietBeschikbareDagImportPage extends AdministrationBasePage {

    @SpringBean
    private NietBeschikbareDagImportService importService;

    public NietBeschikbareDagImportPage(PageParameters params) {
        Long locatieId = params.get("locatieId").toOptionalLong();
        if (locatieId == null) {
            setResponsePage(LocationAdministrationPage.class);
            return;
        }

        PageParameters terugParams = new PageParameters();
        terugParams.add("id", locatieId);

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);

        pageBody.add(
                new BookmarkablePageLink<>("terugLink", LocationUpdatePage.class, terugParams),
                feedback,
                new ImportForm("importForm", locatieId)
        );
    }

    private class ImportForm extends Form<Void> {

        private final long locatieId;
        private final FileUploadField bestand;

        ImportForm(String id, long locatieId) {
            super(id);
            this.locatieId = locatieId;
            setMultiPart(true);
            bestand = new FileUploadField("bestand");
            bestand.setRequired(true);
            add(bestand);
        }

        @Override
        protected void onSubmit() {
            FileUpload upload = bestand.getFileUpload();
            if (upload == null) {
                error("Selecteer een xlsx-bestand.");
                return;
            }
            if (!upload.getClientFileName().toLowerCase().endsWith(".xlsx")) {
                error("Alleen xlsx-bestanden worden ondersteund.");
                return;
            }

            NietBeschikbareDagImportResult result;
            try (InputStream is = upload.getInputStream()) {
                result = importService.importeerVanXlsx(locatieId, is);
            } catch (IOException e) {
                error("Kon het bestand niet lezen: " + e.getMessage());
                return;
            }

            if (result.fouten() == 0) {
                info("Import voltooid: " + result.geimporteerd() + " geïmporteerd, "
                        + result.overgeslagen() + " overgeslagen.");
            } else {
                warn("Import voltooid met fouten: " + result.geimporteerd() + " geïmporteerd, "
                        + result.overgeslagen() + " overgeslagen, " + result.fouten() + " fouten.");
            }
            for (String melding : result.meldingen()) {
                info(melding);
            }
        }
    }
}
