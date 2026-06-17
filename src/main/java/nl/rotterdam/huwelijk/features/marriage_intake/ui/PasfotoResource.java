package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.PasfotoDto;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.UUID;

public class PasfotoResource extends AbstractResource {

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    public PasfotoResource() {
        Injector.get().inject(this);
    }

    @Override
    protected ResourceResponse newResourceResponse(Attributes attributes) {
        String dossierIdStr = attributes.getParameters().get("dossierId").toString(null);
        String bsn = attributes.getParameters().get("bsn").toString(null);

        ResourceResponse response = new ResourceResponse();

        if (dossierIdStr == null || bsn == null) {
            response.setError(404);
            return response;
        }

        UUID dossierId;
        try {
            dossierId = UUID.fromString(dossierIdStr);
        } catch (IllegalArgumentException e) {
            response.setError(404);
            return response;
        }

        PasfotoDto pasfoto;
        try {
            pasfoto = marriageIntakeService.findPasfoto(dossierId, bsn);
        } catch (Exception e) {
            response.setError(404);
            return response;
        }

        if (pasfoto == null) {
            response.setError(404);
            return response;
        }

        byte[] data = pasfoto.data();
        String contentType = pasfoto.contentType() != null ? pasfoto.contentType() : "image/jpeg";

        response.setContentType(contentType);
        response.setContentLength(data.length);
        response.setWriteCallback(new WriteCallback() {
            @Override
            public void writeData(Attributes attributes) throws java.io.IOException {
                attributes.getResponse().getOutputStream().write(data);
            }
        });

        return response;
    }
}
