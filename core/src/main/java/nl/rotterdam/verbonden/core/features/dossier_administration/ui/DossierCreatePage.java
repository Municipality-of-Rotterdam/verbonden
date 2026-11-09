package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButtonAppearance;
import nl.rotterdam.nl_design_system.wicket.components.form_field_checkbox.RdFormFieldCheckbox;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import nl.rotterdam.verbonden.core.administration_common.AdministrationBasePage;
import nl.rotterdam.verbonden.core.administration_common.RdFormFieldSelect;
import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.domain.Telefoonnummer;
import nl.rotterdam.verbonden.core.features.dossier_administration.application.DossierAdministrationService;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.CreateBalieDossierDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.PartnerHeeftAlDossierException;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.PartnerNietInBrpException;
import nl.rotterdam.verbonden.core.features.location_administration.domain.ListLocatieDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.AanmaakKanaal;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.LambdaChoiceRenderer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wicketstuff.minis.behavior.VisibleModelBehavior;

import java.util.List;
import java.util.UUID;

/**
 * Een dossier aanmaken voor twee partners die aan de balie of in een videogesprek aanwezig zijn. De medewerker
 * controleert de identiteit van beide partners; partner 2 hoeft geen BSN te hebben.
 */
public class DossierCreatePage extends AdministrationBasePage {

    @SpringBean
    private DossierAdministrationService dossierAdministrationService;

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    public DossierCreatePage() {
        pageBody.add(
                new BookmarkablePageLink<>("terugLink", DossierAdministrationPage.class),
                new FeedbackPanel("feedback"),
                new CreateDossierForm("dossierForm")
        );
    }

    private class CreateDossierForm extends Form<BalieDossierFormDto> {

        CreateDossierForm(String id) {
            super(id, Model.of(new BalieDossierFormDto()));
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            IModel<BalieDossierFormDto> model = getModel();

            WebMarkupContainer partner2 = new WebMarkupContainer("partner2");
            partner2.setOutputMarkupId(true);

            WebMarkupContainer locatieKeuze = new WebMarkupContainer("locatieKeuze");
            locatieKeuze.setOutputMarkupId(true);
            locatieKeuze.setOutputMarkupPlaceholderTag(true);
            WebMarkupContainer vrijeLocatieVelden = new WebMarkupContainer("vrijeLocatieVelden");
            vrijeLocatieVelden.add(
                    new RdFormFieldTextInput<>("vrijeLocatieNaam",
                            LambdaModel.of(model, BalieDossierFormDto::getVrijeLocatieNaam,
                                    BalieDossierFormDto::setVrijeLocatieNaam),
                            Model.of("Naam van de locatie")),
                    new RdFormFieldTextInput<>("vrijeLocatieAdres",
                            LambdaModel.of(model, BalieDossierFormDto::getVrijeLocatieAdres,
                                    BalieDossierFormDto::setVrijeLocatieAdres),
                            Model.of("Adres van de locatie")),
                    new RdFormFieldTextInput<>("vrijeLocatieTelefoonnummer",
                            LambdaModel.of(model, BalieDossierFormDto::getVrijeLocatieTelefoonnummer,
                                    BalieDossierFormDto::setVrijeLocatieTelefoonnummer),
                            Model.of("Telefoonnummer van de locatie"))
                            .setModelType(Telefoonnummer.class),
                    new RdFormFieldTextInput<>("vrijeLocatieContactpersoon",
                            LambdaModel.of(model, BalieDossierFormDto::getVrijeLocatieContactpersoon,
                                    BalieDossierFormDto::setVrijeLocatieContactpersoon),
                            Model.of("Contactpersoon (optioneel)"))
            );
            vrijeLocatieVelden.add(new VisibleModelBehavior(model.map(BalieDossierFormDto::isVrijeLocatie)));

            RdFormFieldCheckbox vrijeLocatieCheckbox = new RdFormFieldCheckbox("vrijeLocatie",
                    LambdaModel.of(model, BalieDossierFormDto::isVrijeLocatie, BalieDossierFormDto::setVrijeLocatie),
                    Model.of("Vrije locatie"));
            vrijeLocatieCheckbox.getControl().add(new AjaxFormComponentUpdatingBehavior("change") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.add(locatieKeuze);
                }
            });

            locatieKeuze.add(
                    vrijeLocatieCheckbox,
                    new RdFormFieldSelect<>("locatie",
                            LambdaModel.of(model, BalieDossierFormDto::getLocatie, BalieDossierFormDto::setLocatie),
                            Model.of("Trouwlocatie"),
                            marriageIntakeService.findLocaties(),
                            new LambdaChoiceRenderer<>(ListLocatieDto::naam, ListLocatieDto::id))
                            .add(new VisibleModelBehavior(model.map(f -> !f.isVrijeLocatie()))),
                    vrijeLocatieVelden
            );
            locatieKeuze.add(new VisibleModelBehavior(model.map(f -> f.getCeremonieSoort() == CeremonieSoort.GROOT)));
            RdFormFieldCheckbox zonderBsn = new RdFormFieldCheckbox("partner2ZonderBsn",
                    LambdaModel.of(model, BalieDossierFormDto::isPartner2ZonderBsn, BalieDossierFormDto::setPartner2ZonderBsn),
                    Model.of("Partner 2 heeft geen BSN"),
                    Model.of("Neem dan het persoonsnummer en de persoonsgegevens over van het paspoort"));
            zonderBsn.getControl().add(new AjaxFormComponentUpdatingBehavior("change") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.add(partner2);
                }
            });

            partner2.add(
                    zonderBsn,
                    new RdFormFieldTextInput<>("bsnPartner2",
                            LambdaModel.of(model, BalieDossierFormDto::getBsnPartner2, BalieDossierFormDto::setBsnPartner2),
                            Model.of("BSN"))
                            .setModelType(BurgerServiceNummer.class)
                            .setRequired(true)
                            .add(new VisibleModelBehavior(model.map(f -> !f.isPartner2ZonderBsn()))),
                    new PersoonsgegevensPanel("persoonsgegevensPartner2",
                            model.map(BalieDossierFormDto::getPartner2))
                            .add(new VisibleModelBehavior(model.map(BalieDossierFormDto::isPartner2ZonderBsn))),
                    new RdFormFieldCheckbox("identiteitPartner2Gecontroleerd",
                            LambdaModel.of(model, BalieDossierFormDto::isIdentiteitPartner2Gecontroleerd,
                                    BalieDossierFormDto::setIdentiteitPartner2Gecontroleerd),
                            Model.of("Identiteit van partner 2 gecontroleerd"))
            );

            RdButton aanmaken = new RdButton("aanmaken", Model.of("Dossier aanmaken"));
            aanmaken.setAppearance(RdButtonAppearance.PRIMARY_ACTION);

            RdFormFieldSelect<CeremonieSoort> ceremonieSoort = new RdFormFieldSelect<>("ceremonieSoort",
                    LambdaModel.of(model, BalieDossierFormDto::getCeremonieSoort, BalieDossierFormDto::setCeremonieSoort),
                    Model.of("Ceremonie"),
                    List.of(CeremonieSoort.values()),
                    new LambdaChoiceRenderer<>(CeremonieSoort::getLabel, CeremonieSoort::name));
            ceremonieSoort.getInput().add(new AjaxFormComponentUpdatingBehavior("change") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.add(locatieKeuze);
                }
            });

            add(
                    new RdFormFieldSelect<>("kanaal",
                            LambdaModel.of(model, BalieDossierFormDto::getKanaal, BalieDossierFormDto::setKanaal),
                            Model.of("Gesprek"),
                            List.of(AanmaakKanaal.BALIE, AanmaakKanaal.VIDEO),
                            new LambdaChoiceRenderer<>(AanmaakKanaal::getLabel, AanmaakKanaal::name))
                            .setRequired(true),
                    new RdFormFieldSelect<>("registratieType",
                            LambdaModel.of(model, BalieDossierFormDto::getRegistratieType, BalieDossierFormDto::setRegistratieType),
                            Model.of("Soort registratie"),
                            List.of(RegistratieType.values()),
                            new LambdaChoiceRenderer<>(RegistratieType::getLabel, RegistratieType::name))
                            .setRequired(true),
                    ceremonieSoort.setRequired(true),
                    locatieKeuze,
                    new RdFormFieldTextInput<>("bsnPartner1",
                            LambdaModel.of(model, BalieDossierFormDto::getBsnPartner1, BalieDossierFormDto::setBsnPartner1),
                            Model.of("BSN"))
                            .setModelType(BurgerServiceNummer.class)
                            .setRequired(true),
                    new RdFormFieldCheckbox("identiteitPartner1Gecontroleerd",
                            LambdaModel.of(model, BalieDossierFormDto::isIdentiteitPartner1Gecontroleerd,
                                    BalieDossierFormDto::setIdentiteitPartner1Gecontroleerd),
                            Model.of("Identiteit van partner 1 gecontroleerd")),
                    partner2,
                    aanmaken
            );
        }

        @Override
        protected void onSubmit() {
            BalieDossierFormDto f = getModelObject();
            if (!f.isIdentiteitPartner1Gecontroleerd() || !f.isIdentiteitPartner2Gecontroleerd()) {
                error("Controleer de identiteit van beide partners voordat u het dossier aanmaakt.");
                return;
            }
            if (f.getCeremonieSoort() == CeremonieSoort.GROOT
                    && (f.isVrijeLocatie()
                    ? isLeeg(f.getVrijeLocatieNaam()) || isLeeg(f.getVrijeLocatieAdres())
                    || f.getVrijeLocatieTelefoonnummer() == null
                    : f.getLocatie() == null)) {
                error("Kies een trouwlocatie of vul alle verplichte gegevens van uw vrije locatie in.");
                return;
            }
            try {
                boolean vrijeLocatie = f.getCeremonieSoort() == CeremonieSoort.GROOT && f.isVrijeLocatie();
                UUID dossierId = dossierAdministrationService.create(new CreateBalieDossierDto(
                        f.getKanaal(),
                        f.getRegistratieType(),
                        f.getCeremonieSoort(),
                        f.getBsnPartner1(),
                        f.isPartner2ZonderBsn() ? null : f.getBsnPartner2(),
                        f.isPartner2ZonderBsn() ? f.getPartner2().getPersoonsnummer() : null,
                        f.isPartner2ZonderBsn() ? f.getPartner2().naarDto() : null,
                        f.getCeremonieSoort() == CeremonieSoort.GROOT && !vrijeLocatie
                                ? f.getLocatie().id() : null,
                        vrijeLocatie ? f.getVrijeLocatieNaam() : null,
                        vrijeLocatie ? f.getVrijeLocatieAdres() : null,
                        vrijeLocatie ? f.getVrijeLocatieTelefoonnummer() : null,
                        vrijeLocatie ? f.getVrijeLocatieContactpersoon() : null));
                setResponsePage(DossierDetailPage.class, DossierBeheerBasePage.parametersVoor(dossierId));
            } catch (PartnerNietInBrpException | PartnerHeeftAlDossierException e) {
                error(e.getMessage());
            }
        }

        private boolean isLeeg(String waarde) {
            return waarde == null || waarde.isBlank();
        }
    }
}
