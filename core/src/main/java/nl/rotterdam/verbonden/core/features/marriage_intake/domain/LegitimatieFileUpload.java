package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import java.io.Serializable;

/**
 * Value type representing an uploaded legitimacy document (identity proof) for a witness.
 * Holds both the original client file name and the raw file bytes.
 */
public record LegitimatieFileUpload(String bestandNaam, byte[] bestandData) implements Serializable {
}
