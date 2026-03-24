package nl.rotterdam.huwelijk.features.babs_administration.domain;

import java.util.List;

public record BabsImportResult(int imported, int errors, List<String> messages) {
}
