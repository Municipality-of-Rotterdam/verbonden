package nl.rotterdam.huwelijk.baps;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class BapsImportServiceImplTestIntegration {

    @Autowired
    BapsImportServiceImpl importService;

    @Test
    void testImportAll() {
        BapsImportResult bapsImportResult = importService.importeerVanRotterdam();

        System.out.println("result: " + bapsImportResult);
    }

    @Test
    void importOne() throws IOException {
        BapsEntity bapsEntity = importService.parseerBapsVanPagina("https://www.rotterdam.nl/yvonne-kien");
        System.out.println(bapsEntity);
    }
}