package nl.rotterdam.verbonden.features.babs_administration.domain;

import java.util.List;

public record BabsImportResult(int imported, int errors, List<String> messages) {
}
