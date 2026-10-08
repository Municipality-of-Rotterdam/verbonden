package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import nl.rotterdam.verbonden.core.domain.BuitenlandsPersoonsnummer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;

import java.time.LocalDate;

/**
 * Invoervelden voor het persoonsnummer en de persoonsgegevens van een partner zonder BSN. Hoort binnen een
 * formulier; de velden zijn alleen verplicht zolang het panel zichtbaar is.
 */
class PersoonsgegevensPanel extends GenericPanel<PersoonsgegevensFormDto> {

    PersoonsgegevensPanel(String id, IModel<PersoonsgegevensFormDto> model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        IModel<PersoonsgegevensFormDto> model = getModel();
        add(
                new RdFormFieldTextInput<>("persoonsnummer",
                        LambdaModel.of(model, PersoonsgegevensFormDto::getPersoonsnummer, PersoonsgegevensFormDto::setPersoonsnummer),
                        Model.of("Persoonsnummer"),
                        Model.of("Het persoonsnummer (bijvoorbeeld social security number) uit het paspoort"))
                        .setModelType(BuitenlandsPersoonsnummer.class)
                        .setRequired(true),
                new RdFormFieldTextInput<>("achternaam",
                        LambdaModel.of(model, PersoonsgegevensFormDto::getAchternaam, PersoonsgegevensFormDto::setAchternaam),
                        Model.of("Achternaam"))
                        .setRequired(true),
                new RdFormFieldTextInput<>("voornamen",
                        LambdaModel.of(model, PersoonsgegevensFormDto::getVoornamen, PersoonsgegevensFormDto::setVoornamen),
                        Model.of("Voornamen"))
                        .setRequired(true),
                new RdFormFieldTextInput<>("geboortedatum",
                        LambdaModel.of(model, PersoonsgegevensFormDto::getGeboortedatum, PersoonsgegevensFormDto::setGeboortedatum),
                        Model.of("Geboortedatum"))
                        .setHtmlInputType("date")
                        .setModelType(LocalDate.class)
                        .setRequired(true),
                new RdFormFieldTextInput<>("geboorteplaats",
                        LambdaModel.of(model, PersoonsgegevensFormDto::getGeboorteplaats, PersoonsgegevensFormDto::setGeboorteplaats),
                        Model.of("Geboorteplaats")),
                new RdFormFieldTextInput<>("nationaliteit",
                        LambdaModel.of(model, PersoonsgegevensFormDto::getNationaliteit, PersoonsgegevensFormDto::setNationaliteit),
                        Model.of("Nationaliteit")),
                new RdFormFieldTextInput<>("burgerlijkeStaat",
                        LambdaModel.of(model, PersoonsgegevensFormDto::getBurgerlijkeStaat, PersoonsgegevensFormDto::setBurgerlijkeStaat),
                        Model.of("Burgerlijke staat"))
        );
    }
}
