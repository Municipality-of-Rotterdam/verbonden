package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.PartnerGegevensDto;
import nl.rotterdam.nl_design_system.wicket.components.heading.RdHeading;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static nl.rotterdam.huwelijk.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;

public class JullieGegevensPage extends IntakeBasePage {

    private static final DateTimeFormatter DATUM_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

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

                item.add(new Label("achternaam", partner.achternaam()));
                item.add(new Label("voornamen", partner.voornamen()));
                item.add(new Label("geboortedatum",
                        partner.geboortedatum() != null ? partner.geboortedatum().format(DATUM_FORMAT) : ""));
                item.add(new Label("geboorteplaats", partner.geboorteplaats()));
                item.add(new Label("nationaliteit", partner.nationaliteit()));
                item.add(new Label("burgerlijkeStaat", partner.burgerlijkeStaat()));
                item.add(new Label("telefoonnummer", partner.telefoonnummer()));
                item.add(new Label("emailadres", partner.emailadres()));

                // "Gekozen achternaam" display section (with edit icon inside)
                WebMarkupContainer gekozenAchternaamSection = new WebMarkupContainer("gekozenAchternaamSection");
                gekozenAchternaamSection.setVisible(partner.gekozenAchternaam() != null);
                gekozenAchternaamSection.add(new Label("gekozenAchternaamWaarde", partner.gekozenAchternaam()));
                item.add(gekozenAchternaamSection);

                // Edit icon — inside gekozenAchternaamSection
                WebMarkupContainer editIconContainer = new WebMarkupContainer("editIconContainer");
                editIconContainer.setVisible(kanKiezen);
                gekozenAchternaamSection.add(editIconContainer);

                // "Kies je achternaam" button section — only shown when no name chosen yet
                WebMarkupContainer kiesAchternaamSection = new WebMarkupContainer("kiesAchternaamSection");
                kiesAchternaamSection.setVisible(kanKiezen && partner.gekozenAchternaam() == null);
                item.add(kiesAchternaamSection);

                // Dialog with name-selection form
                WebMarkupContainer dialogContainer = new WebMarkupContainer("naamKiezenDialog");
                dialogContainer.setOutputMarkupId(true);
                dialogContainer.setVisible(kanKiezen);
                item.add(dialogContainer);

                if (kanKiezen) {
                    List<String> naamOpties = berekenNaamOpties(partner, partners);
                    String dialogId = dialogContainer.getMarkupId();

                    kiesAchternaamSection.add(new WebMarkupContainer("kiesAchternaamButton") {
                        {
                            add(new AttributeAppender("onclick",
                                    "document.getElementById('" + dialogId + "').showModal();return false;"));
                        }
                    });

                    editIconContainer.add(new WebMarkupContainer("editButton") {
                        {
                            add(new AttributeAppender("onclick",
                                    "document.getElementById('" + dialogId + "').showModal();return false;"));
                        }
                    });

                    dialogContainer.add(new Label("dialogVoornamen", partner.voornamen()));
                    dialogContainer.add(new NaamKiezenForm("naamKiezenForm", partner, naamOpties, dialogId));
                } else {
                    kiesAchternaamSection.add(new WebMarkupContainer("kiesAchternaamButton"));
                    editIconContainer.add(new WebMarkupContainer("editButton"));
                    dialogContainer.add(new Label("dialogVoornamen", ""));
                    dialogContainer.add(new Form<Void>("naamKiezenForm") {});
                }
            }
        });

        WebMarkupContainer partnerNogBevestigenCard = new WebMarkupContainer("partnerNogBevestigenCard");
        partnerNogBevestigenCard.setVisible(partners.size() < 2);
        pageBody.add(partnerNogBevestigenCard);
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

        NaamKiezenForm(String id, PartnerGegevensDto partner, List<String> naamOpties, String dialogId) {
            super(id);
            String initialNaam = partner.gekozenAchternaam() != null ? partner.gekozenAchternaam() : naamOpties.get(0);

            naamRadioGroup = new RadioGroup<>("naamRadioGroup", Model.of(initialNaam));
            naamRadioGroup.setRequired(true);
            naamRadioGroup.add(new ListView<String>("naamOptie", naamOpties) {
                @Override
                protected void populateItem(ListItem<String> optieItem) {
                    optieItem.add(new Radio<>("radio", optieItem.getModel(), naamRadioGroup));
                    optieItem.add(new Label("label", optieItem.getModelObject()));
                }
            });
            add(naamRadioGroup);

            add(new WebMarkupContainer("sluitenButton") {
                {
                    add(new AttributeAppender("onclick",
                            "document.getElementById('" + dialogId + "').close();return false;"));
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
}
