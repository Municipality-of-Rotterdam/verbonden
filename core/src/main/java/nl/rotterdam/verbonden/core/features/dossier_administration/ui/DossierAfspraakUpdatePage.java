package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import nl.rotterdam.verbonden.core.administration_common.RdFormFieldSelect;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.LambdaChoiceRenderer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Verzet het huwelijk naar een ander vrij tijdslot, ook als het dossier al is ingediend.
 */
public class DossierAfspraakUpdatePage extends DossierBeheerBasePage {

    public DossierAfspraakUpdatePage(PageParameters params) {
        super(params);
        pageBody.add(
                new Label("titel", "Datum en tijd wijzigen - dossier " + dossierId),
                new BookmarkablePageLink<>("terugLink", DossierDetailPage.class, parametersVoor(dossierId)),
                new Label("huidig", dossier.datumTijdHuwelijk() != null
                        ? DossierDetailPage.datumTijd(dossier.datumTijdHuwelijk()) : "nog niet gekozen"),
                new FeedbackPanel("feedback"),
                new ChangeAfspraakForm("afspraakForm")
        );
    }

    private class ChangeAfspraakForm extends Form<AfspraakFormDto> {

        ChangeAfspraakForm(String id) {
            super(id, Model.of(new AfspraakFormDto()));
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            IModel<AfspraakFormDto> model = getModel();

            IModel<List<LocalTime>> tijden = LoadableDetachableModel.of(() -> {
                LocalDate datum = model.getObject().getDatum();
                return datum != null ? dossierAdministrationService.findBeschikbareTijden(dossierId, datum) : List.of();
            });
            RdFormFieldSelect<LocalTime> startTijd = new RdFormFieldSelect<>("startTijd",
                    LambdaModel.of(model, AfspraakFormDto::getStartTijd, AfspraakFormDto::setStartTijd),
                    Model.of("Tijd"),
                    tijden,
                    new LambdaChoiceRenderer<>(LocalTime::toString, LocalTime::toString))
                    .setRequired(true);
            startTijd.setOutputMarkupId(true);

            add(
                    new RdFormFieldTextInput<>("datum",
                            LambdaModel.of(model, AfspraakFormDto::getDatum, AfspraakFormDto::setDatum),
                            Model.of("Datum"),
                            Model.of("Na het kiezen van een datum verschijnen de vrije tijden"))
                            .setHtmlInputType("date")
                            .setModelType(LocalDate.class)
                            .setRequired(true)
                            .withTextInput((textField, formField) -> textField.add(new AjaxFormComponentUpdatingBehavior("change") {
                                @Override
                                protected void onUpdate(AjaxRequestTarget target) {
                                    model.getObject().setStartTijd(null);
                                    target.add(startTijd);
                                }
                            })),
                    startTijd,
                    new RdButton("opslaan", Model.of("Opslaan"))
            );
        }

        @Override
        protected void onSubmit() {
            AfspraakFormDto f = getModelObject();
            try {
                dossierAdministrationService.updateAfspraak(dossierId, f.getDatum(), f.getStartTijd());
                naarDetailPagina();
            } catch (IllegalStateException e) {
                error("Dit tijdslot is intussen niet meer vrij. Kies een andere tijd.");
            }
        }
    }
}
