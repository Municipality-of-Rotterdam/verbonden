package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import nl.rotterdam.verbonden.core.domain.Telefoonnummer;
import nl.rotterdam.verbonden.core.features.location_administration.domain.ListLocatieDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SaveVrijeTrouwlocatieDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.VrijeTrouwlocatieDto;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.ExternalImage;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.io.Serializable;
import java.net.URI;
import java.util.UUID;

import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;

public class LocatieKiezenPage extends IntakeBasePage {

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    @Override
    protected IntakeStep getActiveStep() {
        return IntakeStep.DE_DAG;
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new ResourceModel("locatie.kiezen.page.title");
    }

    @Override
    protected IModel<String> getStepHeadingModel() {
        return new ResourceModel("locatie.kiezen.heading");
    }

    @Override
    protected IModel<String> getStepIntroModel() {
        return new ResourceModel("locatie.kiezen.intro");
    }

    @Override
    protected IModel<DossierSamenvattingDto> getSidebarDossierModel() {
        return LoadableDetachableModel.of(() -> marriageIntakeService.findByDossierId(dossierId));
    }

    @Override
    protected boolean hasStepNavigation() {
        return false;
    }

    @Override
    protected boolean hasIntakeStepLayout() {
        return true;
    }

    @Override
    protected boolean hasIntakeContentBorder() {
        return false;
    }

    public static void respond(UUID dossierId) {
        RequestCycle.get().setResponsePage(LocatieKiezenPage.class, makeDossierPageParameters(dossierId));
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        if (marriageIntakeService.findByDossierId(dossierId).ceremonieSoort() != CeremonieSoort.GROOT) {
            throw new RestartResponseException(DeDagPage.class, makeDossierPageParameters(dossierId));
        }

        pageBody.add(
                new Label("mogelijkheden", new ResourceModel("locatie.kiezen.mogelijkheden.html"))
                        .setEscapeModelStrings(false),
                new ListView<>("locaties",
                        LoadableDetachableModel.of(() -> marriageIntakeService.findBeschikbareLocaties(dossierId))) {
                    @Override
                    protected void populateItem(ListItem<ListLocatieDto> item) {
                        ListLocatieDto locatie = item.getModelObject();
                        boolean hasFoto = isAllowedImageUrl(locatie.fotoUrl());
                        boolean hasDetailUrl = isHttpUrl(locatie.detailUrl());
                        item.add(
                                new ExternalImage("foto", Model.of(hasFoto ? locatie.fotoUrl().trim() : ""))
                                        .add(AttributeModifier.replace("alt", locatie.naam()))
                                        .setVisible(hasFoto),
                                new Label("naam", locatie.naam()),
                                new Label("omschrijving",
                                        Model.of(locatie.omschrijving() != null
                                                ? Jsoup.clean(locatie.omschrijving(), Safelist.basic()) : ""))
                                        .setEscapeModelStrings(false)
                                        .setVisible(locatie.omschrijving() != null
                                                && !locatie.omschrijving().isBlank()),
                                new ExternalLink("meerInfo",
                                        Model.of(hasDetailUrl ? locatie.detailUrl().trim() : ""))
                                        .add(new Label("meerInfoLabel",
                                                new ResourceModel("locatie.kiezen.locatie.meer.informatie")))
                                        .setVisible(hasDetailUrl),
                                new Link<Void>("kies") {
                                    @Override
                                    public void onClick() {
                                        marriageIntakeService.kiesLocatie(dossierId, locatie.id());
                                        DeDagPage.respond(dossierId);
                                    }
                                }.add(new Label("kiesLabel",
                                        new StringResourceModel("locatie.kiezen.locatie.kies", item)
                                                .setParameters(locatie.naam())))
                        );
                    }
                },
                new Label("vrijeLocatieTitel", new ResourceModel("locatie.kiezen.vrije.titel")),
                new Label("vrijeLocatieUitleg", new ResourceModel("locatie.kiezen.vrije.uitleg")),
                new Label("vrijeLocatieKosten", new ResourceModel("locatie.kiezen.vrije.kosten")),
                new Label("vrijeLocatiePrijs", new ResourceModel("locatie.kiezen.vrije.prijs")),
                new Label("vrijeLocatieVerklaring", new ResourceModel("locatie.kiezen.vrije.verklaring")),
                new VrijeTrouwlocatieForm("vrijeTrouwlocatieForm"),
                new Link<Void>("terugLink") {
                    @Override
                    public void onClick() {
                        DeDagPage.respond(dossierId);
                    }
                }.add(new Label("terugLabel", new ResourceModel("locatie.kiezen.terug")))
        );
    }

    private static boolean isHttpUrl(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        try {
            URI uri = URI.create(value.trim());
            return uri.isAbsolute()
                    && uri.getHost() != null
                    && ("http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme()));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean isAllowedImageUrl(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(value.trim());
            return !uri.isAbsolute() || isHttpUrl(value);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private class VrijeTrouwlocatieForm extends Form<VrijeTrouwlocatieFormDto> {

        VrijeTrouwlocatieForm(String id) {
            super(id, Model.of(formulier()));
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            IModel<VrijeTrouwlocatieFormDto> model = getModel();
            add(
                    new RdFormFieldTextInput<>("naam",
                            LambdaModel.of(model, VrijeTrouwlocatieFormDto::getNaam, VrijeTrouwlocatieFormDto::setNaam),
                            new ResourceModel("locatie.kiezen.vrije.naam"))
                            .setRequired(true),
                    new RdFormFieldTextInput<>("adres",
                            LambdaModel.of(model, VrijeTrouwlocatieFormDto::getAdres, VrijeTrouwlocatieFormDto::setAdres),
                            new ResourceModel("locatie.kiezen.vrije.adres"))
                            .setRequired(true),
                    new RdFormFieldTextInput<>("telefoonnummer",
                            LambdaModel.of(model, VrijeTrouwlocatieFormDto::getTelefoonnummer,
                                    VrijeTrouwlocatieFormDto::setTelefoonnummer),
                            new ResourceModel("locatie.kiezen.vrije.telefoonnummer"))
                            .setModelType(Telefoonnummer.class)
                            .setRequired(true),
                    new RdFormFieldTextInput<>("contactpersoon",
                            LambdaModel.of(model, VrijeTrouwlocatieFormDto::getContactpersoon,
                                    VrijeTrouwlocatieFormDto::setContactpersoon),
                            new ResourceModel("locatie.kiezen.vrije.contactpersoon")),
                    new RdButton("opslaan", new ResourceModel("locatie.kiezen.vrije.opslaan"))
            );
        }

        @Override
        protected void onSubmit() {
            VrijeTrouwlocatieFormDto form = getModelObject();
            marriageIntakeService.slaVrijeTrouwlocatieOp(dossierId, new SaveVrijeTrouwlocatieDto(
                    form.getNaam(), form.getAdres(), form.getTelefoonnummer(), form.getContactpersoon()));
            DeDagPage.respond(dossierId);
        }
    }

    private VrijeTrouwlocatieFormDto formulier() {
        VrijeTrouwlocatieDto dto = marriageIntakeService.findVrijeTrouwlocatie(dossierId).orElse(null);
        VrijeTrouwlocatieFormDto form = new VrijeTrouwlocatieFormDto();
        if (dto != null) {
            form.setNaam(dto.naam());
            form.setAdres(dto.adres());
            form.setTelefoonnummer(dto.telefoonnummer());
            form.setContactpersoon(dto.contactpersoon());
        }
        return form;
    }

    private static class VrijeTrouwlocatieFormDto implements Serializable {

        private String naam;
        private String adres;
        private Telefoonnummer telefoonnummer;
        private String contactpersoon;

        public String getNaam() {
            return naam;
        }

        public void setNaam(String naam) {
            this.naam = naam;
        }

        public String getAdres() {
            return adres;
        }

        public void setAdres(String adres) {
            this.adres = adres;
        }

        public Telefoonnummer getTelefoonnummer() {
            return telefoonnummer;
        }

        public void setTelefoonnummer(Telefoonnummer telefoonnummer) {
            this.telefoonnummer = telefoonnummer;
        }

        public String getContactpersoon() {
            return contactpersoon;
        }

        public void setContactpersoon(String contactpersoon) {
            this.contactpersoon = contactpersoon;
        }
    }
}
