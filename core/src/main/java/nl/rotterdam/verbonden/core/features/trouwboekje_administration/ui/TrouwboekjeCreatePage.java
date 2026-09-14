package nl.rotterdam.verbonden.core.features.trouwboekje_administration.ui;

import nl.rotterdam.verbonden.core.administration_common.AdministrationBasePage;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.application.TrouwboekjeAdministrationService;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain.CreateTrouwboekjeDto;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import nl.rotterdam.nl_design_system.wicket.components.form_field_textarea.RdFormFieldTextArea;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TrouwboekjeCreatePage extends AdministrationBasePage {

    @SpringBean
    private TrouwboekjeAdministrationService trouwboekjeAdministrationService;

    public TrouwboekjeCreatePage() {
        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        pageBody.add(
                new BookmarkablePageLink<>("terugLink", TrouwboekjeAdministrationPage.class),
                feedback,
                new CreateTrouwboekjeForm("extraForm")
        );
    }

    private class CreateTrouwboekjeForm extends Form<TrouwboekjeFormDto> {

        CreateTrouwboekjeForm(String id) {
            super(id, Model.of(TrouwboekjeFormDto.leeg()));
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            IModel<TrouwboekjeFormDto> model = getModel();
            add(
                    new RdFormFieldTextInput<>("naam",
                            LambdaModel.of(model, TrouwboekjeFormDto::getNaam, TrouwboekjeFormDto::setNaam),
                            Model.of("Naam")).setRequired(true),
                    new RdFormFieldTextArea<>("omschrijving",
                            LambdaModel.of(model, TrouwboekjeFormDto::getOmschrijving, TrouwboekjeFormDto::setOmschrijving),
                            Model.of("Omschrijving"),
                            Model.of("Korte beschrijving van het trouwboekje")),
                    new RdFormFieldTextInput<>("afbeelding",
                            LambdaModel.of(model, TrouwboekjeFormDto::getAfbeelding, TrouwboekjeFormDto::setAfbeelding),
                            Model.of("Afbeelding URL"),
                            Model.of("URL naar de afbeelding")),
                    new RdFormFieldTextInput<>("prijs",
                            LambdaModel.of(model, TrouwboekjeFormDto::getPrijs, TrouwboekjeFormDto::setPrijs),
                            Model.of("Prijs"),
                            Model.of("Prijs in euro's, bijv. 40.50"))
                            .setModelType(BigDecimal.class),
                    new RdFormFieldTextInput<>("startdatum",
                            LambdaModel.of(model, TrouwboekjeFormDto::getStartdatum, TrouwboekjeFormDto::setStartdatum),
                            Model.of("Startdatum"),
                            Model.of("Datum vanaf wanneer het trouwboekje beschikbaar is")).setHtmlInputType("date").setModelType(LocalDate.class),
                    new RdFormFieldTextInput<>("einddatum",
                            LambdaModel.of(model, TrouwboekjeFormDto::getEinddatum, TrouwboekjeFormDto::setEinddatum),
                            Model.of("Einddatum"),
                            Model.of("Datum tot wanneer het trouwboekje beschikbaar is")).setHtmlInputType("date").setModelType(LocalDate.class),
                    new RdButton("opslaan", Model.of("Toevoegen"))
            );
        }

        @Override
        protected void onSubmit() {
            TrouwboekjeFormDto f = getModelObject();
            trouwboekjeAdministrationService.create(new CreateTrouwboekjeDto(
                    f.getNaam(),
                    f.getOmschrijving(),
                    f.getAfbeelding(),
                    f.getPrijs(),
                    f.getStartdatum(),
                    f.getEinddatum()
            ));
            setResponsePage(TrouwboekjeAdministrationPage.class);
        }
    }
}
