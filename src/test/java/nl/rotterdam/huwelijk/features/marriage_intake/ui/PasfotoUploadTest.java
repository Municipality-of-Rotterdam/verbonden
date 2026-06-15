package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.PasfotoDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.PartnerGegevensDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.huwelijk.integration_test.BaseWicketTest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.tester.FormTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PasfotoUploadTest extends BaseWicketTest {

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    private UUID createdDossierId;

    @AfterEach
    void cleanup() {
        if (createdDossierId != null) {
            marriageIntakeService.delete(createdDossierId);
            createdDossierId = null;
        }
    }

    @Test
    @WithMockUser(username = "999990007")
    void pasfotoUploaden() {
        createdDossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, "999990007"));

        // Upload a pasfoto via the service
        byte[] testImageData = createMinimalPng();
        marriageIntakeService.slaPasfotoOp(createdDossierId, "999990007", testImageData, "image/png");

        // Verify pasfoto is stored
        Optional<PasfotoDto> pasfoto = marriageIntakeService.findPasfoto(createdDossierId, "999990007");
        assertTrue(pasfoto.isPresent());
        assertEquals("image/png", pasfoto.get().contentType());
        assertArrayEquals(testImageData, pasfoto.get().data());

        // Verify partner gegevens shows heeftPasfoto=true
        List<PartnerGegevensDto> partners = marriageIntakeService.findPartnerGegevens(createdDossierId);
        assertTrue(partners.getFirst().heeftPasfoto());
    }

    @Test
    @WithMockUser(username = "999990007")
    void pasfotoVerwijderen() {
        createdDossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, "999990007"));

        // Upload then delete
        byte[] testImageData = createMinimalPng();
        marriageIntakeService.slaPasfotoOp(createdDossierId, "999990007", testImageData, "image/png");
        marriageIntakeService.verwijderPasfoto(createdDossierId, "999990007");

        // Verify pasfoto is removed
        Optional<PasfotoDto> pasfoto = marriageIntakeService.findPasfoto(createdDossierId, "999990007");
        assertTrue(pasfoto.isEmpty());

        // Verify partner gegevens shows heeftPasfoto=false
        List<PartnerGegevensDto> partners = marriageIntakeService.findPartnerGegevens(createdDossierId);
        assertFalse(partners.getFirst().heeftPasfoto());
    }

    @Test
    @WithMockUser(username = "999990007")
    void pageRenderMetPasfoto() {
        createdDossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, "999990007"));

        byte[] testImageData = createMinimalPng();
        marriageIntakeService.slaPasfotoOp(createdDossierId, "999990007", testImageData, "image/png");

        PageParameters params = new PageParameters();
        params.add("dossierId", createdDossierId.toString());
        tester.startPage(JullieGegevensPage.class, params);
        tester.assertRenderedPage(JullieGegevensPage.class);
    }

    /**
     * Creates a minimal valid PNG file (1x1 pixel, transparent).
     */
    private byte[] createMinimalPng() {
        return new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, // PNG signature
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52, // IHDR chunk
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, // 1x1 pixel
                0x08, 0x02, 0x00, 0x00, 0x00, (byte) 0x90, 0x77, 0x53, // 8-bit RGB
                (byte) 0xDE, 0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41, // IDAT chunk
                0x54, 0x08, (byte) 0xD7, 0x63, (byte) 0xF8, (byte) 0xCF, (byte) 0xC0, 0x00,
                0x00, 0x00, 0x02, 0x00, 0x01, (byte) 0xE2, 0x21, (byte) 0xBC,
                0x33, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, // IEND chunk
                0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
        };
    }
}
