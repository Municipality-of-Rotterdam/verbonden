package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.GetuigeDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SaveGetuigenDto;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.ArrayList;
import java.util.List;

public class DossierGetuigenUpdatePage extends DossierBeheerBasePage {

    public DossierGetuigenUpdatePage(PageParameters params) {
        super(params);
        pageBody.add(
                new Label("titel", "Getuigen wijzigen - dossier " + dossierId),
                new BookmarkablePageLink<>("terugLink", DossierDetailPage.class, parametersVoor(dossierId)),
                new Label("aantal", "Bij een " + dossier.ceremonieSoort().getLabel().toLowerCase()
                        + " ceremonie horen " + dossier.ceremonieSoort().getAantalGetuigen() + " getuigen."),
                new FeedbackPanel("feedback"),
                new ChangeGetuigenForm("getuigenForm")
        );
    }

    /**
     * Een veld per getuige die bij de ceremonie hoort, gevuld met de getuigen die er al zijn.
     */
    private List<GetuigeFormDto> getuigenVoorFormulier() {
        List<GetuigeFormDto> getuigen = new ArrayList<>();
        for (int volgnummer = 1; volgnummer <= dossier.ceremonieSoort().getAantalGetuigen(); volgnummer++) {
            int nummer = volgnummer;
            String naam = dossier.getuigen().stream()
                    .filter(g -> g.volgnummer() == nummer)
                    .map(GetuigeDto::naam)
                    .findFirst()
                    .orElse(null);
            getuigen.add(new GetuigeFormDto(volgnummer, naam));
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
                    item.add(new RdFormFieldTextInput<>("naam",
                            LambdaModel.of(item.getModel(), GetuigeFormDto::getNaam, GetuigeFormDto::setNaam),
                            Model.of("Getuige " + item.getModelObject().getVolgnummer()),
                            Model.of("Voornaam en achternaam")));
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
            dossierAdministrationService.updateGetuigen(dossierId, getModelObject().stream()
                    .map(g -> new SaveGetuigenDto(g.getVolgnummer(), g.getNaam()))
                    .toList());
            naarDetailPagina();
        }
    }
}
