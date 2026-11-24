package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import nl.rotterdam.verbonden.core.domain.Postcode;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ChangeGetuigeDto;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DossierGetuigenUpdatePage extends DossierBeheerBasePage {

    private static final DateTimeFormatter DATUM = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public DossierGetuigenUpdatePage(PageParameters params) {
        super(params);
        pageBody.add(
                new Label("titel", "Getuigen wijzigen - dossier " + dossierId),
                new BookmarkablePageLink<>("terugLink", DossierDetailPage.class, parametersVoor(dossierId)),
                new Label("aantal", "Bij een " + dossier.ceremonieSoort().getLabel().toLowerCase()
                        + " ceremonie horen " + dossier.ceremonieSoort().getAantalGetuigen() + " getuigen. "
                        + "Een getuige moet op de dag van de ceremonie minstens "
                        + ChangeGetuigeDto.MINIMUM_LEEFTIJD + " jaar zijn."),
                new FeedbackPanel("feedback"),
                new ChangeGetuigenForm("getuigenForm")
        );
    }

    /**
     * Een formulierdeel per getuige die bij de ceremonie hoort, gevuld met de getuigen die er al zijn.
     */
    private List<GetuigeFormDto> getuigenVoorFormulier() {
        List<GetuigeFormDto> getuigen = new ArrayList<>();
        for (int volgnummer = 1; volgnummer <= dossier.ceremonieSoort().getAantalGetuigen(); volgnummer++) {
            int nummer = volgnummer;
            getuigen.add(dossier.getuigen().stream()
                    .filter(g -> g.volgnummer() == nummer)
                    .map(GetuigeFormDto::vanDto)
                    .findFirst()
                    .orElseGet(() -> new GetuigeFormDto(nummer)));
        }
        return getuigen;
    }

    private class ChangeGetuigenForm extends Form<List<GetuigeFormDto>> {

        ChangeGetuigenForm(String id) {
            super(id, new ListModel<>(getuigenVoorFormulier()));
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            ListView<GetuigeFormDto> getuigen = new ListView<>("getuigen", getModel()) {
                @Override
                protected void populateItem(ListItem<GetuigeFormDto> item) {
                    IModel<GetuigeFormDto> model = item.getModel();
                    item.add(
                            new Label("kop", "Getuige " + model.getObject().getVolgnummer()),
                            new RdFormFieldTextInput<>("naam",
                                    LambdaModel.of(model, GetuigeFormDto::getNaam, GetuigeFormDto::setNaam),
                                    Model.of("Naam"),
                                    Model.of("Voornaam en achternaam")),
                            new RdFormFieldTextInput<>("voornamen",
                                    LambdaModel.of(model, GetuigeFormDto::getVoornamen, GetuigeFormDto::setVoornamen),
                                    Model.of("Voornamen")),
                            new RdFormFieldTextInput<>("achternaam",
                                    LambdaModel.of(model, GetuigeFormDto::getAchternaam, GetuigeFormDto::setAchternaam),
                                    Model.of("Achternaam")),
                            new RdFormFieldTextInput<>("geboortedatum",
                                    LambdaModel.of(model, GetuigeFormDto::getGeboortedatum, GetuigeFormDto::setGeboortedatum),
                                    Model.of("Geboortedatum"))
                                    .setHtmlInputType("date")
                                    .setModelType(LocalDate.class),
                            new RdFormFieldTextInput<>("postcode",
                                    LambdaModel.of(model, GetuigeFormDto::getPostcode, GetuigeFormDto::setPostcode),
                                    Model.of("Postcode"),
                                    Model.of("In Nederland bijvoorbeeld 3011 AD"))
                                    .setModelType(Postcode.class),
                            new RdFormFieldTextInput<>("woonplaats",
                                    LambdaModel.of(model, GetuigeFormDto::getWoonplaats, GetuigeFormDto::setWoonplaats),
                                    Model.of("Woonplaats")),
                            new RdFormFieldTextInput<>("land",
                                    LambdaModel.of(model, GetuigeFormDto::getLand, GetuigeFormDto::setLand),
                                    Model.of("Land"))
                    );
                }
            };
            getuigen.setReuseItems(true);
            add(
                    getuigen,
                    new RdButton("opslaan", Model.of("Opslaan"))
            );
        }

        @Override
        protected void onSubmit() {
            List<ChangeGetuigeDto> getuigen = getModelObject().stream().map(GetuigeFormDto::naarDto).toList();
            List<ChangeGetuigeDto> metOngeldigePostcode = getuigen.stream()
                    .filter(ChangeGetuigeDto::heeftOngeldigeNederlandsePostcode)
                    .toList();
            if (!metOngeldigePostcode.isEmpty()) {
                metOngeldigePostcode.forEach(g -> error("Getuige " + g.volgnummer()
                        + ": geef een Nederlandse postcode op (bijv. 3011 AD) of kies een ander land."));
                return;
            }
            dossierAdministrationService.updateGetuigen(dossierId, getuigen);
            waarschuwVoorTeJongeGetuigen(getuigen);
            naarDetailPagina();
        }

        /**
         * Waarschuwt, zonder het opslaan tegen te houden, voor elke getuige die op de dag van de ceremonie te jong
         * is. Zonder gekozen datum valt dat nog niet te zeggen.
         */
        private void waarschuwVoorTeJongeGetuigen(List<ChangeGetuigeDto> getuigen) {
            if (dossier.datumTijdHuwelijk() == null) {
                return;
            }
            LocalDate ceremonieDatum = dossier.datumTijdHuwelijk().toLocalDate();
            getuigen.stream()
                    .filter(g -> g.isTeJongOp(ceremonieDatum))
                    .forEach(g -> getSession().warn("Getuige " + g.volgnummer() + " is op de dag van de ceremonie ("
                            + DATUM.format(ceremonieDatum) + ") jonger dan " + ChangeGetuigeDto.MINIMUM_LEEFTIJD
                            + " jaar."));
        }
    }
}
