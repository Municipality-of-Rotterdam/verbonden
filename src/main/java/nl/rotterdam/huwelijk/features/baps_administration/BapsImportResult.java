package nl.rotterdam.huwelijk.features.baps_administration;

import java.util.List;

public record BapsImportResult(int imported, int errors, List<String> messages) {
}
