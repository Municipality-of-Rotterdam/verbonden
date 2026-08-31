package nl.rotterdam.verbonden.core.features.babs_administration.domain;

import java.util.List;

public record BabsImportResult(int imported, int errors, List<String> messages) {
}
