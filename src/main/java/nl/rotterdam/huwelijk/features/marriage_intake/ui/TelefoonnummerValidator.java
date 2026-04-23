package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import java.util.regex.Pattern;

public class TelefoonnummerValidator implements IValidator<String> {

    /**
     * Accepts Dutch phone numbers in common notations:
     * <ul>
     *   <li>06-12345678  (mobile)</li>
     *   <li>0201234567   (landline, 10 digits starting with 0)</li>
     *   <li>+31612345678 (international mobile)</li>
     *   <li>+31201234567 (international landline)</li>
     * </ul>
     * Spaces and dashes used as separators are stripped before matching.
     */
    private static final Pattern PATTERN = Pattern.compile("^(\\+31|0)[1-9][0-9]{8}$");

    @Override
    public void validate(IValidatable<String> validatable) {
        String value = validatable.getValue();
        if (value != null && !value.isBlank()) {
            String normalized = value.replaceAll("[\\s\\-]", "");
            if (!PATTERN.matcher(normalized).matches()) {
                validatable.error(new ValidationError(this));
            }
        }
    }
}
