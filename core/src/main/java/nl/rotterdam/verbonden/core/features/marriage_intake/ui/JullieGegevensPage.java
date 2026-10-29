package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.domain.ValueHolder;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.NaamgebruikOpties;
import nl.rotterdam.verbonden.core.domain.Emailadres;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.PartnerGegevensDto;
import nl.rotterdam.verbonden.core.domain.Telefoonnummer;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.OptimisticLockingFailureException;
import org.wicketstuff.minis.behavior.VisibleModelBehavior;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;

public class JullieGegevensPage extends IntakeBasePage {

    private static final DateTimeFormatter DATUM_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final int QR_CODE_SIZE = 200;

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    private final IModel<DossierSamenvattingDto> dossierModel =
            LoadableDetachableModel.of(() -> marriageIntakeService.findByDossierId(dossierId));
    private final IModel<List<PartnerGegevensDto>> partnersModel =
            LoadableDetachableModel.of(() -> marriageIntakeService.findPartnerGegevens(dossierId));

    private WebMarkupContainer partnerKaarten;

    @Override
    protected IntakeStep getActiveStep() {
        return IntakeStep.JULLIE_GEGEVENS;
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new ResourceModel("intake.page.title.jullie.gegevens");
    }

    @Override
    protected IModel<String> getStepHeadingModel() {
        return new ResourceModel("intake.step.jullie.gegevens");
    }

    @Override
    protected IModel<String> getStepIntroModel() {
        return new ResourceModel("jullie.gegevens.intro");
    }

    @Override
    protected IModel<DossierSamenvattingDto> getSidebarDossierModel() {
        return dossierModel;
    }

    @Override
    protected IModel<List<PartnerGegevensDto>> getSidebarPartnerGegevensModel() {
        return partnersModel;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        // Wordt na het kiezen van een achternaam via Ajax ververst, zodat de scrollpositie behouden blijft
        partnerKaarten = new WebMarkupContainer("partnerKaarten");
        partnerKaarten.setOutputMarkupId(true);
        pageBody.add(partnerKaarten);

        partnerKaarten.add(new ListView<>("partnerCards", partnersModel) {
            @Override
            protected void populateItem(ListItem<PartnerGegevensDto> item) {
                PartnerGegevensDto partner = item.getModelObject();
                List<PartnerGegevensDto> partners = getList();
                // Zodra beide partners gekoppeld zijn, mag elke partner ook de gegevens van de ander invullen
                boolean kanKiezen = partners.size() == 2;

                item.add(
                        new Label("achternaam", partner.achternaam()),
                        new Label("voornamen", partner.voornamen()),
                        new Label("geboortedatum", partner.geboortedatum() != null ? partner.geboortedatum().format(DATUM_FORMAT) : ""),
                        new Label("geboorteplaats", partner.geboorteplaats()),
                        new Label("nationaliteit", partner.nationaliteit()),
                        new Label("burgerlijkeStaat", partner.burgerlijkeStaat())
                );

                item.add(new ContactGegevensForm("contactGegevensForm", partner));

                // "Gekozen achternaam" display section (with edit icon inside)
                WebMarkupContainer gekozenAchternaamSection = new WebMarkupContainer("gekozenAchternaamSection");
                gekozenAchternaamSection.setVisible(partner.gekozenAchternaam() != null);
                gekozenAchternaamSection.add(new Label("gekozenAchternaamWaarde", partner.gekozenAchternaam()));
                item.add(gekozenAchternaamSection);

                // Dialog overlay — always rendered; shown/hidden via CSS class to avoid inline style
                IModel<Boolean> dialogTonen = Model.of(false);
                WebMarkupContainer dialogContainer = new WebMarkupContainer("naamKiezenDialog") {
                    @Override
                    protected void onComponentTag(ComponentTag tag) {
                        super.onComponentTag(tag);
                        if (!dialogTonen.getObject()) {
                            tag.append("class", "d-none", " ");
                        }
                    }
                };
                dialogContainer.setOutputMarkupId(true);
                item.add(dialogContainer);

                // Edit icon — inside gekozenAchternaamSection
                WebMarkupContainer editIconContainer = new WebMarkupContainer("editIconContainer");
                editIconContainer.setVisible(kanKiezen);
                gekozenAchternaamSection.add(editIconContainer);

                // "Kies je achternaam" button section — shown whenever this user can choose
                WebMarkupContainer kiesAchternaamSection = new WebMarkupContainer("kiesAchternaamSection");
                kiesAchternaamSection.setVisible(kanKiezen);
                item.add(kiesAchternaamSection);

                if (kanKiezen) {
                    List<String> naamOpties = berekenNaamOpties(partner, partners);

                    // "Kies je achternaam" button — opens dialog via Ajax
                    kiesAchternaamSection.add(new AjaxLink<Void>("kiesAchternaamButton") {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            dialogTonen.setObject(true);
                            target.add(dialogContainer);
                        }
                    });

                    // Edit icon — opens dialog via Ajax
                    editIconContainer.add(new AjaxLink<Void>("editButton") {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            dialogTonen.setObject(true);
                            target.add(dialogContainer);
                        }
                    }.setMarkupId(editButtonMarkupId(partner.volgorde())));

                    dialogContainer.add(new Label("dialogVoornamen", partner.voornamen()));
                    dialogContainer.add(new NaamKiezenForm("naamKiezenForm", partner, naamOpties, dialogContainer, dialogTonen));
                } else {
                    kiesAchternaamSection.add(new WebMarkupContainer("kiesAchternaamButton"));
                    editIconContainer.add(new WebMarkupContainer("editButton"));
                    dialogContainer.add(new Label("dialogVoornamen", ""));
                    Form<Void> stubForm = new Form<>("naamKiezenForm");
                    WebMarkupContainer stubGroup = new WebMarkupContainer("naamRadioGroup");
                    stubGroup.add(new ListView<String>("naamOptie", List.of()) {
                        @Override
                        protected void populateItem(ListItem<String> optieItem) {
                            optieItem.add(new WebMarkupContainer("radio"));
                            optieItem.add(new Label("label", ""));
                        }
                    });
                    stubForm.add(stubGroup);
                    stubForm.add(new WebMarkupContainer("sluitenButton"), new WebMarkupContainer("bevestigButton"));
                    dialogContainer.add(stubForm);
                }
            }
        });

        WebMarkupContainer partnerNogBevestigenCard = new WebMarkupContainer("partnerNogBevestigenCard");
        partnerNogBevestigenCard.add(new VisibleModelBehavior(partnersModel.map(partners -> partners.size() < 2)));

        String loginUrl = RequestCycle.get().getUrlRenderer().renderFullUrl(
                    Url.parse(urlFor(MarriageIntakePage.class, makeDossierPageParameters(requireNonNull(dossierId))).toString()));

        // De QR-code wordt alleen gegenereerd wanneer de uitnodigingskaart zichtbaar is
        partnerNogBevestigenCard.add(new WebMarkupContainer("partnerQrCode") {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("src", QrCodeUtil.generateQrCodeDataUri(loginUrl, QR_CODE_SIZE, QR_CODE_SIZE));
            }
        });

        partnerNogBevestigenCard.add(new WebMarkupContainer("partnerUitnodigingUrl") {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("value", loginUrl);
            }
        });

        partnerKaarten.add(partnerNogBevestigenCard);

    }

    private static String editButtonMarkupId(int volgorde) {
        return "achternaamWijzigen" + volgorde;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(JullieGegevensPage.class, "link-kopieren.js")));
    }

    private List<String> berekenNaamOpties(PartnerGegevensDto eigenPartner, List<PartnerGegevensDto> allePartners) {
        String andereAchternaam = allePartners.stream()
                .filter(p -> p.volgorde() != eigenPartner.volgorde())
                .map(PartnerGegevensDto::achternaam)
                .findFirst()
                .orElse("");
        return NaamgebruikOpties.voor(eigenPartner.achternaam(), andereAchternaam);
    }

    private class NaamKiezenForm extends Form<Void> {

        private final int volgorde;
        private final RadioGroup<String> naamRadioGroup;

        NaamKiezenForm(String id, PartnerGegevensDto partner, List<String> naamOpties,
                       WebMarkupContainer dialogContainer, IModel<Boolean> dialogTonen) {
            super(id);
            this.volgorde = partner.volgorde();
            String initialNaam = partner.gekozenAchternaam() != null ? partner.gekozenAchternaam() : naamOpties.getFirst();

            naamRadioGroup = new RadioGroup<>("naamRadioGroup", Model.of(initialNaam));
            naamRadioGroup.setRequired(true);
            naamRadioGroup.add(new ListView<>("naamOptie", naamOpties) {
                @Override
                protected void populateItem(ListItem<String> optieItem) {
                    optieItem.add(new Radio<>("radio", optieItem.getModel(), naamRadioGroup));
                    optieItem.add(new Label("label", optieItem.getModelObject()));
                }
            });
            add(naamRadioGroup);

            // Cancel button — hides dialog via Ajax without submitting
            add(new AjaxLink<Void>("sluitenButton") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    dialogTonen.setObject(false);
                    target.add(dialogContainer);
                }
            });

            // Ajax in plaats van een volledige page-request, zodat de pagina niet naar boven springt
            AjaxButton bevestigButton = new AjaxButton("bevestigButton") {
                @Override
                protected void onSubmit(AjaxRequestTarget target) {
                    marriageIntakeService.slaPartnerGegevensOp(dossierId, volgorde, naamRadioGroup.getModelObject());
                    partnersModel.detach();
                    dossierModel.detach();
                    target.add(partnerKaarten, keuzesSidebar);
                    // Het dialoog verdwijnt; geef de focus aan de wijzigknop op dezelfde kaart zonder te scrollen
                    target.appendJavaScript("document.getElementById('" + editButtonMarkupId(volgorde)
                            + "')?.focus({preventScroll: true});");
                }
            };
            add(bevestigButton);
            setDefaultButton(bevestigButton);
        }
    }

    private class ContactGegevensForm extends Form<ContactGegevensFormDto> {

        private final int volgorde;

        ContactGegevensForm(String id, PartnerGegevensDto partner) {
            super(id);
            this.volgorde = partner.volgorde();
            ContactGegevensFormDto dto = new ContactGegevensFormDto();
            dto.setTelefoonnummer(partner.telefoonnummer());
            dto.setEmailadres(partner.emailadres());
            dto.setVersie(partner.versie());
            setDefaultModel(Model.of(dto));
            setOutputMarkupId(true);
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            IModel<ContactGegevensFormDto> model = getModel();

            add(
                    new RdFormFieldTextInput<>("telefoonnummerInput",
                            LambdaModel.of(model, ContactGegevensFormDto::getTelefoonnummer, ContactGegevensFormDto::setTelefoonnummer),
                            new ResourceModel("jullie.gegevens.telefoonnummer"))
                            .setModelType(Telefoonnummer.class)
                            .withTextInput((rdTextInput, rdFormFieldTextInput) -> rdTextInput.add(newContactUpdateBehavior(rdFormFieldTextInput))),
                    new RdFormFieldTextInput<>("emailadresInput",
                            LambdaModel.of(model, ContactGegevensFormDto::getEmailadres,
                                    ContactGegevensFormDto::setEmailadres),
                            new ResourceModel("jullie.gegevens.emailadres"))
                            .setModelType(Emailadres.class)
                            .withTextInput((rdTextInput, rdFormFieldTextInput) -> rdTextInput.add(newContactUpdateBehavior(rdFormFieldTextInput)))
            );
        }

        private @NonNull AjaxFormComponentUpdatingBehavior newContactUpdateBehavior(RdFormFieldTextInput<? extends ValueHolder<?>> rdFormFieldTextInput) {
            return new AjaxFormComponentUpdatingBehavior("change") {

                @Override
                protected void onError(AjaxRequestTarget target, RuntimeException e) {
                    target.add(rdFormFieldTextInput);
                }

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    ContactGegevensFormDto f = getModelObject();
                    try {
                        f.setVersie(marriageIntakeService.slaContactGegevensOp(
                                dossierId, volgorde, f.getVersie(), f.getTelefoonnummer(), f.getEmailadres()));
                        target.add(rdFormFieldTextInput);
                    } catch (OptimisticLockingFailureException e) {
                        laadActueleContactGegevens();
                        rdFormFieldTextInput.getTextField().error(getString("jullie.gegevens.contact.intussen.gewijzigd"));
                        target.add(ContactGegevensForm.this);
                    }
                }
            };
        }

        /**
         * Vervangt de ingevoerde contactgegevens door wat er nu is opgeslagen, nadat de (andere) partner
         * ze intussen heeft gewijzigd.
         */
        private void laadActueleContactGegevens() {
            PartnerGegevensDto actueel = marriageIntakeService.findPartnerGegevens(dossierId).stream()
                    .filter(p -> p.volgorde() == volgorde)
                    .findFirst()
                    .orElseThrow();
            ContactGegevensFormDto f = getModelObject();
            f.setTelefoonnummer(actueel.telefoonnummer());
            f.setEmailadres(actueel.emailadres());
            f.setVersie(actueel.versie());
        }
    }
}
