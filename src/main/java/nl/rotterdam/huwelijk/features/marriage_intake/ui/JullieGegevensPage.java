package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.PartnerGegevensDto;
import nl.rotterdam.nl_design_system.wicket.components.heading.RdHeading;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class JullieGegevensPage extends IntakeBasePage {

    private static final DateTimeFormatter DATUM_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    private final UUID dossierId;

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

    public JullieGegevensPage(PageParameters parameters) {
        this.dossierId = UUID.fromString(parameters.get("dossierId").toString());
        marriageIntakeService.ensureBsnAccess(dossierId, getCurrentBsn());

        pageBody.add(new RdHeading("heading", getString("jullie.gegevens.heading"), 1));

        List<PartnerGegevensDto> partners = marriageIntakeService.findPartnerGegevens(dossierId,
                SecurityContextHolder.getContext().getAuthentication().getName());

        pageBody.add(new ListView<>("partnerCards", partners) {
            @Override
            protected void populateItem(ListItem<PartnerGegevensDto> item) {
                PartnerGegevensDto partner = item.getModelObject();

                item.add(new Label("achternaam", partner.achternaam()));
                item.add(new Label("voornamen", partner.voornamen()));
                item.add(new Label("geboortedatum",
                        partner.geboortedatum() != null ? partner.geboortedatum().format(DATUM_FORMAT) : ""));
                item.add(new Label("geboorteplaats", partner.geboorteplaats()));
                item.add(new Label("nationaliteit", partner.nationaliteit()));
                item.add(new Label("burgerlijkeStaat", partner.burgerlijkeStaat()));
                item.add(new Label("telefoonnummer", partner.telefoonnummer()));
                item.add(new Label("emailadres", partner.emailadres()));
            }
        });
    }
}
