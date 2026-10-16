package nl.rotterdam.verbonden.core.features.babs_administration.ui;

import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;

import java.time.LocalDate;

/**
 * Controleert dat "actief tot en met" niet vóór "actief vanaf" ligt. Beide datums mogen leeg zijn.
 */
class ActiefPeriodeValidator extends AbstractFormValidator {

    private final TextField<LocalDate> actiefVanaf;
    private final TextField<LocalDate> actiefTotEnMet;

    ActiefPeriodeValidator(RdFormFieldTextInput<LocalDate> actiefVanaf, RdFormFieldTextInput<LocalDate> actiefTotEnMet) {
        this.actiefVanaf = actiefVanaf.getTextField();
        this.actiefTotEnMet = actiefTotEnMet.getTextField();
    }

    @Override
    public FormComponent<?>[] getDependentFormComponents() {
        return new FormComponent<?>[]{actiefVanaf, actiefTotEnMet};
    }

    @Override
    public void validate(Form<?> form) {
        LocalDate vanaf = actiefVanaf.getConvertedInput();
        LocalDate totEnMet = actiefTotEnMet.getConvertedInput();
        if (vanaf != null && totEnMet != null && totEnMet.isBefore(vanaf)) {
            actiefTotEnMet.error("Actief tot en met mag niet vóór actief vanaf liggen");
        }
    }
}
