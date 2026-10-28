package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.burger_common.BurgerBasePage;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierAccessOutcome;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.GetuigeDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.PartnerGegevensDto;
import nl.rotterdam.nl_design_system.wicket.components.alert.RdAlert;
import nl.rotterdam.nl_design_system.wicket.components.alert.RdAlertType;
import nl.rotterdam.nl_design_system.wicket.components.heading.RdHeading;
import nl.rotterdam.verbonden.core.burger_common.MijnLoketBreadcrumbPanel;
import nl.rotterdam.verbonden.core.burger_common.MijnLoketBreadcrumbPanel.Kruimel;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.List;
import java.util.UUID;

import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierPageParameterUtil.extractDossierId;
import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;
import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierPageParameterUtil.PARAM_EXTRAS_DOORLOPEN;

public abstract class IntakeBasePage extends BurgerBasePage {

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    protected UUID dossierId;

    private boolean showWrongDossierWarning;
    private boolean showInvitationBlockedWarning;
    private boolean showNotAuthorizedWarning;
    private boolean extrasDoorlopen;

    protected IntakeSidebarPanel keuzesSidebar;

    protected abstract IModel<DossierSamenvattingDto> getSidebarDossierModel();

    protected IModel<List<PartnerGegevensDto>> getSidebarPartnerGegevensModel() {
        return LoadableDetachableModel.of(() ->
                dossierId == null ? List.of() : marriageIntakeService.findPartnerGegevens(dossierId));
    }

    protected IModel<List<GetuigeDto>> getSidebarGetuigenModel() {
        return LoadableDetachableModel.of(() ->
                dossierId == null ? List.of() : marriageIntakeService.findGetuigen(dossierId));
    }

    protected abstract IntakeStep getActiveStep();

    protected boolean hasStepNavigation() {
        return true;
    }

    protected boolean hasIntakeStepLayout() {
        return hasStepNavigation();
    }

    protected IModel<String> getStepHeadingModel() {
        return Model.of("");
    }

    protected IModel<String> getStepIntroModel() {
        return Model.of("");
    }

    /**
     * Returns {@code true} when the page requires a valid (non-null) dossier ID to render.
     * Override to return {@code false} on pages that can render without an existing dossier
     * (e.g. the intake form where a new dossier can be created).
     */
    protected boolean requiresDossier() {
        return true;
    }

    /**
     * Returns {@code true} when the page may be shown for a dossier that has already been submitted.
     * All other intake pages change the dossier, so they redirect to the {@link AanvraagStatusPage}
     * once the dossier is no longer a {@link DossierStatus#CONCEPT}.
     */
    protected boolean isToegestaanNaIndienen() {
        return false;
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
                case INVITATION_BLOCKED -> {
                    dossierId = outcome.dossierId();
                    showInvitationBlockedWarning = true;
                }
                case INVITED -> throw new RestartResponseException(
                        DossierUitnodigingPage.class, makeDossierPageParameters(requestedDossierId));
                case NOT_AUTHORIZED -> showNotAuthorizedWarning = true;
            }
        } else {
            marriageIntakeService.findDossierIdByBsn(getCurrentBsn()).ifPresent(id -> dossierId = id);
        }
        extrasDoorlopen = getPageParameters().get(PARAM_EXTRAS_DOORLOPEN).toBoolean(false);

        if (dossierId == null && showNotAuthorizedWarning && requiresDossier()) {
            throw new RestartResponseException(MarriageIntakePage.class, makeDossierPageParameters(extractDossierId(getPageParameters())));
        }

        boolean ingediend = dossierId != null && marriageIntakeService.findStatus(dossierId) != DossierStatus.CONCEPT;
        if (ingediend && !isToegestaanNaIndienen()) {
            throw new RestartResponseException(AanvraagStatusPage.class, makeDossierPageParameters(dossierId));
        }

        pageBody.add(new MijnLoketBreadcrumbPanel("breadcrumb", List.of(
                new Kruimel(getString("intake.breadcrumb.mijndag"), MarriageIntakePage.class,
                        dossierId != null ? pageParametersForDossier(dossierId) : new PageParameters()))));

        RdAlert wrongDossierAlert = new RdAlert("wrongDossierAlert",
                new ResourceModel("intake.alert.wrong.dossier"), RdAlertType.WARNING);
        wrongDossierAlert.setVisible(showWrongDossierWarning);
        pageBody.add(wrongDossierAlert);

        RdAlert invitationBlockedAlert = new RdAlert("invitationBlockedAlert",
                new ResourceModel("intake.alert.invitation.blocked"), RdAlertType.WARNING);
        invitationBlockedAlert.setVisible(showInvitationBlockedWarning);
        pageBody.add(invitationBlockedAlert);

        RdAlert notAuthorizedAlert = new RdAlert("notAuthorizedAlert",
                new ResourceModel("intake.alert.not.authorized"), RdAlertType.WARNING);
        notAuthorizedAlert.setVisible(showNotAuthorizedWarning);
        pageBody.add(notAuthorizedAlert);

        boolean intakeStepLayout = hasIntakeStepLayout();
        if (intakeStepLayout) {
            pageBody.add(AttributeModifier.append("class", "rd-intake-step-layout"));
        }
        WebMarkupContainer intakeHeader = new WebMarkupContainer("intakeHeader");
        intakeHeader.setVisible(intakeStepLayout);
        intakeHeader.add(new RdHeading("intakeHeading", new ResourceModel("intake.heading"), 1));

        WebMarkupContainer stepDetails = new WebMarkupContainer("stepDetails");
        stepDetails.setVisible(intakeStepLayout);
        stepDetails.add(new Label("stepHeading", getStepHeadingModel()),
                new Label("stepIntro", getStepIntroModel()));

        pageBody.add(
                intakeHeader,
                stepDetails,
                createNavigationContainer("vorigeStapContainer", "vorigeStap", vorigeStap(), "intake.button.vorige"),
                createNavigationContainer("volgendeStapContainer", "volgendeStap", volgendeStap(), "intake.button.volgende")
        );

        keuzesSidebar = new IntakeSidebarPanel("keuzesSidebar", getSidebarDossierModel(),
                getSidebarPartnerGegevensModel(), getSidebarGetuigenModel(), getActiveStep(), extrasDoorlopen);
        pageBody.add(keuzesSidebar);
    }

    private WebMarkupContainer createNavigationContainer(String id, String linkId, IntakeStep targetStep, String labelKey) {
        WebMarkupContainer container = new WebMarkupContainer(id);
        container.setVisible(hasIntakeStepLayout() && hasStepNavigation() && targetStep != null);
        container.add(createNavigationLink(linkId, targetStep, labelKey));
        return container;
    }

    private Link<Void> createNavigationLink(String id, IntakeStep targetStep, String labelKey) {
        Link<Void> link = new Link<>(id) {
            @Override
            public void onClick() {
                if (dossierId != null) {
                    if (getActiveStep() == IntakeStep.EXTRAS && targetStep == IntakeStep.CONTROLEREN) {
                        extrasDoorlopen = true;
                    }
                    setResponsePage(targetStep.pageClass(), pageParametersForDossier(dossierId));
                }
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setEnabled(dossierId != null && targetStep != null);
            }
        };
        link.add(new Label("label", new ResourceModel(labelKey)));
        return link;
    }

    private IntakeStep vorigeStap() {
        IntakeStep current = getActiveStep();
        if (current == null || current.ordinal() == 0) {
            return null;
        }
        return IntakeStep.values()[current.ordinal() - 1];
    }

    private IntakeStep volgendeStap() {
        IntakeStep current = getActiveStep();
        if (current == null || current.ordinal() == IntakeStep.values().length - 1) {
            return null;
        }
        return IntakeStep.values()[current.ordinal() + 1];
    }

    protected final PageParameters pageParametersForDossier(UUID id) {
        return makeDossierPageParameters(id, extrasDoorlopen);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(MarriageIntakeHeaderItems.MARRIAGE_INTAKE_CSS);
        response.render(JavaScriptHeaderItem.forReference(
                new PackageResourceReference(IntakeBasePage.class, "intake-read-aloud.js")));
    }
}
