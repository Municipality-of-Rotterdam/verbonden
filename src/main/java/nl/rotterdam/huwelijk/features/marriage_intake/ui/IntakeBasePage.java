package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.burger_common.BurgerBasePage;
import nl.rotterdam.huwelijk.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierAccessOutcome;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.nl_design_system.wicket.components.alert.RdAlert;
import nl.rotterdam.nl_design_system.wicket.components.alert.RdAlertType;
import nl.rotterdam.nl_design_system.wicket.components.breadcrumb_nav.RdBreadcrumbNavPanel;
import nl.rotterdam.nl_design_system.wicket.components.breadcrumb_nav.RdBreadcrumbNavRecord;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.List;
import java.util.UUID;

import static nl.rotterdam.huwelijk.features.marriage_intake.ui.DossierPageParameterUtil.extractDossierId;
import static nl.rotterdam.huwelijk.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;

public abstract class IntakeBasePage extends BurgerBasePage {

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    protected UUID dossierId;

    private boolean showWrongDossierWarning;
    private boolean showNotAuthorizedWarning;

    protected IntakeSidebarPanel keuzesSidebar;

    protected abstract IModel<DossierSamenvattingDto> getSidebarDossierModel();

    protected abstract IntakeStep getActiveStep();

    /**
     * Returns {@code true} when the page requires a valid (non-null) dossier ID to render.
     * Override to return {@code false} on pages that can render without an existing dossier
     * (e.g. the intake form where a new dossier can be created).
     */
    protected boolean requiresDossier() {
        return true;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        UUID requestedDossierId = extractDossierId(getPageParameters());
        if (requestedDossierId != null) {
            DossierAccessOutcome outcome = marriageIntakeService.resolveAccess(requestedDossierId, getCurrentBsn());
            switch (outcome.scenario()) {
                case GRANTED -> dossierId = outcome.dossierId();
                case SWITCHED_DOSSIER -> {
                    dossierId = outcome.dossierId();
                    showWrongDossierWarning = true;
                }
                case NOT_AUTHORIZED -> showNotAuthorizedWarning = true;
            }
        } else {
            marriageIntakeService.findDossierIdByBsn(getCurrentBsn()).ifPresent(id -> dossierId = id);
        }

        if (dossierId == null && showNotAuthorizedWarning && requiresDossier()) {
            throw new RestartResponseException(MarriageIntakePage.class, makeDossierPageParameters(extractDossierId(getPageParameters())));
        }

        List<RdBreadcrumbNavRecord<? extends org.apache.wicket.request.component.IRequestablePage>> breadcrumbs = List.of(
                new RdBreadcrumbNavRecord<>(null, getString("intake.breadcrumb.mijnloket"), MarriageIntakePage.class),
                new RdBreadcrumbNavRecord<>(null, getString("intake.breadcrumb.mijndag"), MarriageIntakePage.class)
        );
        pageBody.add(new RdBreadcrumbNavPanel("breadcrumb", breadcrumbs));

        RdAlert wrongDossierAlert = new RdAlert("wrongDossierAlert",
                new ResourceModel("intake.alert.wrong.dossier"), RdAlertType.WARNING);
        wrongDossierAlert.setVisible(showWrongDossierWarning);
        pageBody.add(wrongDossierAlert);

        RdAlert notAuthorizedAlert = new RdAlert("notAuthorizedAlert",
                new ResourceModel("intake.alert.not.authorized"), RdAlertType.WARNING);
        notAuthorizedAlert.setVisible(showNotAuthorizedWarning);
        pageBody.add(notAuthorizedAlert);

        pageBody.add(
                createTabLink("tabDedag", IntakeStep.DE_DAG, DeDagPage.class),
                createTabLink("tabJullieGegevens", IntakeStep.JULLIE_GEGEVENS, JullieGegevensPage.class),
                createTabLink("tabGetuigen", IntakeStep.DE_GETUIGEN, DeGetuigenPage.class),
                createDisabledTab("tabExtras", IntakeStep.EXTRAS)
        );

        keuzesSidebar = new IntakeSidebarPanel("keuzesSidebar", getSidebarDossierModel());
        pageBody.add(keuzesSidebar);
    }

    private Link<Void> createTabLink(String id, IntakeStep step, Class<? extends IntakeBasePage> pageClass) {
        Link<Void> tab = new Link<>(id) {
            @Override
            public void onClick() {
                if (dossierId != null) {
                    setResponsePage(pageClass, makeDossierPageParameters(dossierId));
                }
            }

            @Override
            public boolean isEnabled() {
                return dossierId != null;
            }
        };
        boolean isActive = getActiveStep() == step;
        tab.add(AttributeModifier.replace("aria-selected", String.valueOf(isActive)));
        if (isActive) {
            tab.add(AttributeModifier.append("class", " rd-tab--active"));
            tab.add(AttributeModifier.replace("aria-current", "step"));
        }
        return tab;
    }

    private WebMarkupContainer createDisabledTab(String id, IntakeStep step) {
        WebMarkupContainer tab = new WebMarkupContainer(id);
        boolean isActive = getActiveStep() == step;
        tab.add(AttributeModifier.replace("aria-selected", String.valueOf(isActive)));
        if (isActive) {
            tab.add(AttributeModifier.append("class", " rd-tab--active"));
            tab.add(AttributeModifier.replace("aria-current", "step"));
        }
        return tab;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(MarriageIntakeHeaderItems.MARRIAGE_INTAKE_CSS);
    }
}
