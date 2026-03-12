package nl.rotterdam.huwelijk.baps;

import java.util.List;

public record BapsImportResult(int imported, int errors, List<String> messages) {
}
