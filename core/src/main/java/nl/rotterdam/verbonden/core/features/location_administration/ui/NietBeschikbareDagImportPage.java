package nl.rotterdam.verbonden.core.features.location_administration.ui;

import nl.rotterdam.verbonden.core.administration_common.AdministrationBasePage;
import nl.rotterdam.verbonden.core.administration_common.RdFormFieldFileUpload;
import nl.rotterdam.verbonden.core.features.location_administration.application.NietBeschikbareDagImportService;
import nl.rotterdam.verbonden.core.features.location_administration.domain.NietBeschikbareDagImportResult;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
        private final RdFormFieldFileUpload bestand;
        private final IModel<List<FileUpload>> uploads;

        ImportForm(String id, long locatieId) {
            super(id);
            this.locatieId = locatieId;
            setMultiPart(true);
            uploads = new ListModel<>();
            bestand = new RdFormFieldFileUpload("bestand", uploads, Model.of("xlsx-bestand"));
            add(
                    bestand,
                    new RdButton("importeren", Model.of("Importeren"))
            );
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
