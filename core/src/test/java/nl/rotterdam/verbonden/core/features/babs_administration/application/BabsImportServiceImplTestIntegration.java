package nl.rotterdam.verbonden.core.features.babs_administration.application;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import nl.rotterdam.verbonden.core.features.babs_administration.domain.BabsImportResult;
import nl.rotterdam.verbonden.core.integration_test.VerbondenIntegrationTest;
import nl.rotterdam.verbonden.core.persistence.BabsEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;


@VerbondenIntegrationTest
class BabsImportServiceImplTestIntegration {

    @Autowired
    BabsImportServiceImpl importService;

    @Test
    void testImportAll() {
        BabsImportResult babsImportResult = importService.importeerVanRotterdam();

        System.out.println("result: " + babsImportResult);
    }

    @Test
    void importOne() throws IOException {
        BabsEntity babsEntity = importService.parseerBabsVanPagina("https://www.rotterdam.nl/yvonne-kien");
        System.out.println(babsEntity);
    }
}
