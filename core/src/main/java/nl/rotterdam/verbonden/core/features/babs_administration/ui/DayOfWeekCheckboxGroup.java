package nl.rotterdam.verbonden.core.features.babs_administration.ui;

import nl.rotterdam.nl_design_system.wicket.components.checkbox.RdCheckboxBehavior;
import nl.rotterdam.nl_design_system.wicket.components.checkbox_group.RdCheckboxGroup;
import nl.rotterdam.nl_design_system.wicket.components.form_field_label.RdFormFieldLabelBehavior;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Collection;

import static nl.rotterdam.nl_design_system.wicket.components.form_field_label.RdFormFieldLabelCheckableInputType.CHECKBOX;

public class DayOfWeekCheckboxGroup extends RdCheckboxGroup<DayOfWeek> {

        private final IModel<Collection<DayOfWeek>> selectedItems;

        public DayOfWeekCheckboxGroup(String id, IModel<Collection<DayOfWeek>> selectedItems, IModel<String> labelModel) {
            super(id, selectedItems, labelModel);
            this.selectedItems = selectedItems;
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();

            getCheckGroup()
                .setOutputMarkupId(true)
                .add(AjaxFormChoiceComponentUpdatingBehavior
                    .onUpdateChoice(target -> target.add(getCheckGroup()))
                );

            add(new ListView<>("day", Arrays.asList(DayOfWeek.values())) {
                {
                    setReuseItems(true);
                }

                @Override
                protected void populateItem(ListItem<DayOfWeek> item) {

                    IModel<String> labelModel = item.
                        getModel().map(day -> day.getDisplayName(TextStyle.FULL, getSession().getLocale()));

                    item.add(
                        new Check<>("checkbox", item.getModel())
                            .setLabel(labelModel)
                            .add(RdCheckboxBehavior.INSTANCE),

                        new Label("label", labelModel)
                    );

                    item.add(new RdFormFieldLabelBehavior(CHECKBOX,
                        selectedItems.map(selected -> selected.contains(item.getModelObject()))));
                }
            });

        }
    }
