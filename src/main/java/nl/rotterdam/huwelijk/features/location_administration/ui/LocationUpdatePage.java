package nl.rotterdam.huwelijk.features.location_administration.ui;

import nl.rotterdam.huwelijk.administration_common.AdministrationBasePage;
import nl.rotterdam.huwelijk.features.location_administration.application.LocationAdministrationService;
import nl.rotterdam.huwelijk.features.location_administration.domain.ChangeLocatieDto;
import nl.rotterdam.huwelijk.features.location_administration.domain.ListBeschikbaarheidDto;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.List;

public class LocationUpdatePage extends AdministrationBasePage {

    @SpringBean
    private LocationAdministrationService locationAdministrationService;

    public LocationUpdatePage(PageParameters params) {
        Long id = params.get("id").toOptionalLong();
        if (id == null) {
            setResponsePage(LocationAdministrationPage.class);
            return;
        }
        ChangeLocatieDto dto = locationAdministrationService.findById(id).orElse(null);
        if (dto == null) {
            setResponsePage(LocationAdministrationPage.class);
            return;
        }

        List<ListBeschikbaarheidDto> beschikbaarheden = locationAdministrationService.findBeschikbaarheden(id);

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);

        PageParameters nieuwBeschikbaarheidParams = new PageParameters();
        nieuwBeschikbaarheidParams.add("locatieId", id);

        add(
                new BookmarkablePageLink<>("terugLink", LocationAdministrationPage.class),
                feedback,
                new ChangeLocatieForm("locatieForm", dto),
                new BookmarkablePageLink<>("nieuwBeschikbaarheidLink",
                        BeschikbaarheidCreatePage.class, nieuwBeschikbaarheidParams),
                buildBeschikbaarheidList(id, beschikbaarheden)
        );
    }

    private ListView<ListBeschikbaarheidDto> buildBeschikbaarheidList(long locatieId,
                                                                       List<ListBeschikbaarheidDto> beschikbaarheden) {
        return new ListView<>("beschikbaarheden", beschikbaarheden) {
            @Override
            protected void populateItem(ListItem<ListBeschikbaarheidDto> item) {
                ListBeschikbaarheidDto b = item.getModelObject();

                item.add(new Label("huwelijkstype", b.huwelijkstype().name()));
                item.add(new Label("dagVanDeWeek", b.dagVanDeWeek().name()));
                item.add(new Label("startTijd", b.startTijd() != null ? b.startTijd().toString() : ""));
                item.add(new Label("eindTijd", b.eindTijd() != null ? b.eindTijd().toString() : ""));
                item.add(new Label("duurInMinuten", String.valueOf(b.duurInMinuten())));
                item.add(new Label("prijs", b.prijs() != null ? "€ " + b.prijs() : ""));
                item.add(new Label("ingangsdatum", b.ingangsdatum() != null ? b.ingangsdatum().toString() : ""));
                item.add(new Label("einddatum", b.einddatum() != null ? b.einddatum().toString() : ""));

                PageParameters bewerkParams = new PageParameters();
                bewerkParams.add("locatieId", locatieId);
                bewerkParams.add("id", b.id());
                item.add(new BookmarkablePageLink<>("bewerkBeschikbaarheidLink",
                        BeschikbaarheidUpdatePage.class, bewerkParams));

                item.add(new Link<>("verwijderBeschikbaarheidLink", item.getModel()) {
                    @Override
                    public void onClick() {
                        locationAdministrationService.deleteBeschikbaarheid(getModelObject().id());
                        PageParameters params = new PageParameters();
                        params.add("id", locatieId);
                        setResponsePage(LocationUpdatePage.class, params);
                    }
                });
            }
        };
    }

    private class ChangeLocatieForm extends Form<LocatieFormDto> {

        private final long locatieId;

        ChangeLocatieForm(String id, ChangeLocatieDto dto) {
            super(id, Model.of(LocatieFormDto.vanDto(dto)));
            locatieId = dto.id();
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            IModel<LocatieFormDto> model = getModel();
            add(
                    new RdFormFieldTextInput<>("naam",
                            LambdaModel.of(model, LocatieFormDto::getNaam, LocatieFormDto::setNaam),
                            Model.of("Naam")).setRequired(true),
                    new RdFormFieldTextInput<>("fotoUrl",
                            LambdaModel.of(model, LocatieFormDto::getFotoUrl, LocatieFormDto::setFotoUrl),
                            Model.of("Foto URL"),
                            Model.of("URL naar de foto van de trouwlocatie")),
                    new RdButton("opslaan", Model.of("Opslaan"))
            );
        }

        @Override
        protected void onSubmit() {
            LocatieFormDto f = getModelObject();
            locationAdministrationService.update(new ChangeLocatieDto(
                    locatieId,
                    f.getNaam(),
                    f.getFotoUrl()
            ));
            PageParameters params = new PageParameters();
            params.add("id", locatieId);
            setResponsePage(LocationUpdatePage.class, params);
        }
    }
}
