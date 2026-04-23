package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import java.util.regex.Pattern;

public class EmailadresValidator implements IValidator<String> {

    /**
     * Accepts common email formats: local-part@domain.tld.
     * Rejects addresses with consecutive dots, missing TLD, or whitespace.
     */
    private static final Pattern PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s.][^@\\s]*\\.[^@\\s.][^@\\s]*$");

    @Override
    public void validate(IValidatable<String> validatable) {
        String value = validatable.getValue();
        if (value != null && !value.isBlank()) {
            if (!PATTERN.matcher(value).matches()) {
                validatable.error(new ValidationError(this));
            }
        }
    }
}
