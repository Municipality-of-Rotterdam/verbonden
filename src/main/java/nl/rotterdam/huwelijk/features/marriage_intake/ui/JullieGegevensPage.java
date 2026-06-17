package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.domain.ValueHolder;
import nl.rotterdam.huwelijk.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.Emailadres;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.PartnerGegevensDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.Telefoonnummer;
import nl.rotterdam.nl_design_system.wicket.components.data_summary.RdDataSummary;
import nl.rotterdam.nl_design_system.wicket.components.data_summary.SummaryItem;
import nl.rotterdam.nl_design_system.wicket.components.data_summary.SummaryItemValue;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import nl.rotterdam.nl_design_system.wicket.components.heading.RdHeading;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.jspecify.annotations.NonNull;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static nl.rotterdam.huwelijk.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;

public class JullieGegevensPage extends IntakeBasePage {

    private static final DateTimeFormatter DATUM_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final int QR_CODE_SIZE = 200;

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    @Override
    protected IntakeStep getActiveStep() {
        return IntakeStep.JULLIE_GEGEVENS;
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new ResourceModel("intake.page.title.jullie.gegevens");
    }

    @Override
    protected IModel<DossierSamenvattingDto> getSidebarDossierModel() {
        return Model.of(marriageIntakeService.findByDossierId(dossierId));
    }


    @Override
    protected void onInitialize() {
        super.onInitialize();

        pageBody.add(new RdHeading("heading", getString("jullie.gegevens.heading"), 1));

        List<PartnerGegevensDto> partners = marriageIntakeService.findPartnerGegevens(dossierId);
        String currentBsn = getCurrentBsn();
        boolean tweeBsns = partners.size() == 2;

        pageBody.add(new ListView<>("partnerCards", partners) {
            @Override
            protected void populateItem(ListItem<PartnerGegevensDto> item) {
                PartnerGegevensDto partner = item.getModelObject();
                boolean kanKiezen = tweeBsns && partner.bsn().equals(currentBsn);
                boolean kanContactBewerken = partner.bsn().equals(currentBsn);

                item.add(
                        new Label("achternaam", partner.achternaam()),
                        new Label("voornamen", partner.voornamen()),
                        new Label("geboortedatum", partner.geboortedatum() != null ? partner.geboortedatum().format(DATUM_FORMAT) : ""),
                        new Label("geboorteplaats", partner.geboorteplaats()),
                        new Label("nationaliteit", partner.nationaliteit()),
                        new Label("burgerlijkeStaat", partner.burgerlijkeStaat())
                );

                // Pasfoto display (shown when a photo exists)
                WebMarkupContainer pasfotoDisplay = new WebMarkupContainer("pasfotoDisplay");
                pasfotoDisplay.setVisible(partner.pasfotoAanwezig());
                WebMarkupContainer pasfotoImg = new WebMarkupContainer("pasfotoImg") {
                    @Override
                    protected void onComponentTag(ComponentTag tag) {
                        super.onComponentTag(tag);
                        tag.put("src", "/pasfoto/" + dossierId + "/" + partner.bsn());
                    }
                };
                pasfotoDisplay.add(pasfotoImg);
                item.add(pasfotoDisplay);

                // Pasfoto upload form (only shown for the current user's own card)
                PasfotoUploadForm pasfotoUploadForm = new PasfotoUploadForm("pasfotoUploadForm");
                pasfotoUploadForm.setVisible(kanContactBewerken);
                item.add(pasfotoUploadForm);

                // Contact gegevens: read-only display (shown for partner's card)
                WebMarkupContainer contactGegevensReadOnly = new WebMarkupContainer("contactGegevensReadOnly");
                contactGegevensReadOnly.setVisible(!kanContactBewerken);

                contactGegevensReadOnly.add(
                        new RdDataSummary("contactGegevensSummary",
                                new ListModel<>(
                                        List.of(
                                                new SummaryItem(new ResourceModel("jullie.gegevens.telefoonnummer"), new SummaryItemValue(partner.telefoonnummer(), false)),
                                                new SummaryItem(new ResourceModel("jullie.gegevens.emailadres"), new SummaryItemValue(partner.emailadres(), false))
                                        ))));

                item.add(contactGegevensReadOnly);

                // Contact gegevens: editable form (shown for current user's card)
                ContactGegevensForm contactGegevensForm = new ContactGegevensForm("contactGegevensForm", partner);
                contactGegevensForm.setVisible(kanContactBewerken);
                item.add(contactGegevensForm);

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
                    });

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
                    stubForm.add(new WebMarkupContainer("sluitenButton"));
                    dialogContainer.add(stubForm);
                }
            }
        });

        WebMarkupContainer partnerNogBevestigenCard = new WebMarkupContainer("partnerNogBevestigenCard");
        partnerNogBevestigenCard.setVisible(partners.size() < 2);

        String loginUrl = RequestCycle.get().getUrlRenderer().renderFullUrl(
                    Url.parse(urlFor(MarriageIntakePage.class, makeDossierPageParameters(requireNonNull(dossierId))).toString()));

        String qrDataUri = !loginUrl.isEmpty() && partners.size() < 2
                ? QrCodeUtil.generateQrCodeDataUri(loginUrl, QR_CODE_SIZE, QR_CODE_SIZE)
                : "";

        WebMarkupContainer qrCodeImg = new WebMarkupContainer("partnerQrCode") {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("src", qrDataUri);
            }
        };
        qrCodeImg.setVisible(!qrDataUri.isEmpty());
        partnerNogBevestigenCard.add(qrCodeImg);

        WebMarkupContainer loginLink = new WebMarkupContainer("partnerLoginLink") {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("href", loginUrl);
            }
        };
        loginLink.setVisible(!loginUrl.isEmpty());
        partnerNogBevestigenCard.add(loginLink);

        pageBody.add(partnerNogBevestigenCard);

        pageBody.add(new BookmarkablePageLink<>("deGetuigenLink", DeGetuigenPage.class, makeDossierPageParameters(dossierId)));
    }

    private List<String> berekenNaamOpties(PartnerGegevensDto eigenPartner, List<PartnerGegevensDto> allePartners) {
        String eigenAchternaam = eigenPartner.achternaam();
        String andereAchternaam = allePartners.stream()
                .filter(p -> !p.bsn().equals(eigenPartner.bsn()))
                .map(PartnerGegevensDto::achternaam)
                .findFirst()
                .orElse("");

        List<String> opties = new ArrayList<>();
        opties.add(andereAchternaam);
        opties.add(andereAchternaam + " - " + eigenAchternaam);
        opties.add(eigenAchternaam + " - " + andereAchternaam);
        opties.add(eigenAchternaam);
        return opties;
    }

    private class NaamKiezenForm extends Form<Void> {

        private final RadioGroup<String> naamRadioGroup;

        NaamKiezenForm(String id, PartnerGegevensDto partner, List<String> naamOpties,
                       WebMarkupContainer dialogContainer, IModel<Boolean> dialogTonen) {
            super(id);
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
        }

        @Override
        protected void onSubmit() {
            String gekozenNaam = naamRadioGroup.getModelObject();
            marriageIntakeService.slaGekozenAchternaamOp(dossierId, getCurrentBsn(), gekozenNaam);
            setResponsePage(JullieGegevensPage.class, makeDossierPageParameters(dossierId));
        }
    }

    private class PasfotoUploadForm extends Form<Void> {

        private final FileUploadField pasfotoField;

        PasfotoUploadForm(String id) {
            super(id);
            setMultiPart(true);
            pasfotoField = new FileUploadField("pasfotoField");
            add(pasfotoField);
        }

        @Override
        protected void onSubmit() {
            FileUpload upload = pasfotoField.getFileUpload();
            if (upload == null) {
                return;
            }
            String contentType = upload.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                error(getString("jullie.gegevens.pasfoto.fout.geen.afbeelding"));
                return;
            }
            byte[] data;
            try {
                data = upload.getBytes();
            } catch (Exception e) {
                error(getString("jullie.gegevens.pasfoto.fout.lezen"));
                return;
            }
            marriageIntakeService.slaPasfotoOp(dossierId, getCurrentBsn(), data, contentType);
            setResponsePage(JullieGegevensPage.class, makeDossierPageParameters(dossierId));
        }
    }

    private class ContactGegevensForm extends Form<ContactGegevensFormDto> {

        ContactGegevensForm(String id, PartnerGegevensDto partner) {
            super(id);
            ContactGegevensFormDto dto = new ContactGegevensFormDto();
            dto.setTelefoonnummer(partner.telefoonnummer());
            dto.setEmailadres(partner.emailadres());
            setDefaultModel(Model.of(dto));
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
                    marriageIntakeService.slaContactGegevensOp(dossierId, getCurrentBsn(), f.getTelefoonnummer(), f.getEmailadres());
                    target.add(rdFormFieldTextInput);
                }
            };
        }
    }
}
