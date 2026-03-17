package nl.rotterdam.huwelijk.features.location_administration.ui;

import nl.rotterdam.nl_design_system.wicket.components.form_field.RdFormFieldBorder;
import nl.rotterdam.nl_design_system.wicket.components.select.RdSelect;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;

import java.util.List;

class RdFormFieldSelect<T> extends RdFormFieldBorder<T, RdSelect<T>> {

    private final List<? extends T> choices;
    private final IChoiceRenderer<? super T> renderer;
    private boolean required;

    RdFormFieldSelect(String id, IModel<T> model, IModel<String> label,
                      List<? extends T> choices, IChoiceRenderer<? super T> renderer) {
        super(id, model, label);
        this.choices = choices;
        this.renderer = renderer;
    }

    RdFormFieldSelect<T> setRequired(boolean required) {
        this.required = required;
        return this;
    }

    @Override
    protected RdSelect<T> newInput(IModel<T> model) {
        return new RdSelect<>("input", model, choices, renderer);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        getInput().setRequired(required);
    }
}
