package nl.rotterdam.huwelijk.features.marriage_intake.domain;

import java.io.Serializable;

/**
 * DTO representing a stored passport photo with its content type.
 */
public record PasfotoDto(byte[] data, String contentType) implements Serializable {
}
