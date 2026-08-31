package nl.rotterdam.verbonden.core.features.location_administration.ui;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.List;

class DayOfWeekChoiceRenderer implements IChoiceRenderer<DayOfWeek> {

    @Override
    public Object getDisplayValue(DayOfWeek dayOfWeek) {
        return dayOfWeek.getDisplayName(TextStyle.FULL, Session.get().getLocale());
    }

    @Override
    public String getIdValue(DayOfWeek dayOfWeek, int index) {
        return dayOfWeek.name();
    }

    @Override
    public DayOfWeek getObject(String id, IModel<? extends List<? extends DayOfWeek>> choices) {
        return id != null && !id.isEmpty() ? DayOfWeek.valueOf(id) : null;
    }
}
