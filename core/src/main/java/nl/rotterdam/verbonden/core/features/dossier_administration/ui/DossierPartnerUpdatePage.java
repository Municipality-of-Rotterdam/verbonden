package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import nl.rotterdam.verbonden.core.administration_common.RdFormFieldSelect;
import nl.rotterdam.verbonden.core.domain.Emailadres;
import nl.rotterdam.verbonden.core.domain.Telefoonnummer;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ChangePartnerDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DetailPartnerDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.NaamgebruikOpties;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.PartnerGegevensDto;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.LambdaChoiceRenderer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Wijzigt de gekozen achternaam en contactgegevens van een partner. Van een partner zonder BSN kunnen ook het
 * persoonsnummer en de persoonsgegevens worden gewijzigd; die van een partner met BSN komen uit de BRP.
 */
public class DossierPartnerUpdatePage extends DossierBeheerBasePage {

    static final String VOLGORDE = "volgorde";

    private final PartnerGegevensDto partner;

    public DossierPartnerUpdatePage(PageParameters params) {
        super(params);
        int volgorde = params.get(VOLGORDE).toInt(0);
        partner = dossier.partners().stream()
                .map(DetailPartnerDto::gegevens)
                .filter(p -> p.volgorde() == volgorde)
                .findFirst()
                .orElseThrow(() -> new RestartResponseException(DossierDetailPage.class, parametersVoor(dossierId)));

        pageBody.add(
                new Label("titel", "Partner " + volgorde + " wijzigen - dossier " + dossierId),
                new BookmarkablePageLink<>("terugLink", DossierDetailPage.class, parametersVoor(dossierId)),
                new Label("naam", partner.voornamen() + " " + partner.achternaam()),
                new Label("bron", partner.bsn() != null
                        ? "Persoonsgegevens uit de BRP (BSN " + partner.bsn().getValue() + ")"
                        : "Partner zonder BSN: persoonsgegevens ingevoerd door een medewerker"),
                new FeedbackPanel("feedback"),
                new ChangePartnerForm("partnerForm")
        );
    }

    static PageParameters parametersVoor(UUID dossierId, int volgorde) {
        return parametersVoor(dossierId).set(VOLGORDE, volgorde);
    }

    /**
     * Dezelfde keuzes als de burger online heeft, plus de huidige keuze als die daar (na een naamswijziging) niet
     * meer tussen staat.
     */
    private List<String> naamOpties() {
        Set<String> opties = new LinkedHashSet<>();
        dossier.partners().stream()
                .map(DetailPartnerDto::gegevens)
                .filter(p -> p.volgorde() != partner.volgorde())
                .findFirst()
                .ifPresentOrElse(
                        ander -> opties.addAll(NaamgebruikOpties.voor(partner.achternaam(), ander.achternaam())),
                        () -> opties.add(partner.achternaam()));
        if (partner.gekozenAchternaam() != null) {
            opties.add(partner.gekozenAchternaam());
        }
        return new ArrayList<>(opties);
    }

    private class ChangePartnerForm extends Form<PartnerFormDto> {

        ChangePartnerForm(String id) {
            super(id, Model.of(PartnerFormDto.vanDto(partner)));
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            IModel<PartnerFormDto> model = getModel();
            add(
                    new RdFormFieldSelect<>("gekozenAchternaam",
                            LambdaModel.of(model, PartnerFormDto::getGekozenAchternaam, PartnerFormDto::setGekozenAchternaam),
                            Model.of("Gekozen achternaam"),
                            naamOpties(),
                            new LambdaChoiceRenderer<>(naam -> naam, naam -> naam))
                            .setNullValid(true),
                    new RdFormFieldTextInput<>("telefoonnummer",
                            LambdaModel.of(model, PartnerFormDto::getTelefoonnummer, PartnerFormDto::setTelefoonnummer),
                            Model.of("Telefoonnummer"))
                            .setModelType(Telefoonnummer.class),
                    new RdFormFieldTextInput<>("emailadres",
                            LambdaModel.of(model, PartnerFormDto::getEmailadres, PartnerFormDto::setEmailadres),
                            Model.of("E-mailadres"))
                            .setModelType(Emailadres.class),
                    new PersoonsgegevensPanel("persoonsgegevens", model.map(PartnerFormDto::getPersoonsgegevens))
                            .setVisible(partner.bsn() == null),
                    new RdButton("opslaan", Model.of("Opslaan"))
            );
        }

        @Override
        protected void onSubmit() {
            PartnerFormDto f = getModelObject();
            boolean zonderBsn = partner.bsn() == null;
            try {
                dossierAdministrationService.updatePartner(dossierId, new ChangePartnerDto(
                        partner.volgorde(),
                        f.getVersie(),
                        f.getGekozenAchternaam(),
                        f.getTelefoonnummer(),
                        f.getEmailadres(),
                        zonderBsn ? f.getPersoonsgegevens().getPersoonsnummer() : null,
                        zonderBsn ? f.getPersoonsgegevens().naarDto() : null));
                naarDetailPagina();
            } catch (OptimisticLockingFailureException e) {
                error("De gegevens van deze partner zijn intussen door iemand anders gewijzigd. "
                        + "Ga terug naar het dossier en probeer het opnieuw.");
            }
        }
    }
}
