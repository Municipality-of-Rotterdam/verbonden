package nl.rotterdam.verbonden.core.administration_common;

import nl.rotterdam.nl_design_system.wicket.components.form_field.RdFormFieldBorder;
import nl.rotterdam.nl_design_system.wicket.components.select.RdSelect;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;

import java.util.Collections;
import java.util.List;

public class RdFormFieldSelect<T> extends RdFormFieldBorder<T, RdSelect<T>> {

    private final List<? extends T> choices;
    private final IChoiceRenderer<? super T> renderer;
    private boolean required;
    private boolean nullValid;

    public RdFormFieldSelect(String id, IModel<T> model, IModel<String> label,
                             List<? extends T> choices, IChoiceRenderer<? super T> renderer) {
        super(id, model, label);
        this.choices = choices;
        this.renderer = renderer;
    }

    public RdFormFieldSelect<T> setRequired(boolean required) {
        this.required = required;
        return this;
    }

    public RdFormFieldSelect<T> setNullValid(boolean nullValid) {
        this.nullValid = nullValid;
        return this;
    }

    @Override
    protected RdSelect<T> newInput(IModel<T> model) {
        // choices and renderer are not yet assigned at this point (super() is still running),
        // so we create the RdSelect with an empty placeholder and configure it in onInitialize().
        return new RdSelect<>("input", model, Collections.emptyList());
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        getInput().setChoices(choices);
        getInput().setChoiceRenderer(renderer);
        getInput().setRequired(required);
        getInput().setNullValid(nullValid);
    }
}
