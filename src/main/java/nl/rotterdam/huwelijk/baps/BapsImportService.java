package nl.rotterdam.huwelijk.baps;

import java.util.List;

public interface BapsImportService {

    ImportResult importeerVanRotterdam();

    record ImportResult(int imported, int errors, List<String> messages) {
    }
}
