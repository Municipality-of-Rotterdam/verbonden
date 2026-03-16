package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.burger_common.BurgerBasePage;
import nl.rotterdam.nl_design_system.rotterdam_extensions.wicket.components.rotterdam_icon.RotterdamIconBehavior;
import nl.rotterdam.nl_design_system.rotterdam_extensions.wicket.components.rotterdam_icon.RotterdamIconType;
import nl.rotterdam.nl_design_system.wicket.components.breadcrumb_nav.RdBreadcrumbNavPanel;
import nl.rotterdam.nl_design_system.wicket.components.breadcrumb_nav.RdBreadcrumbNavRecord;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButtonAppearance;
import nl.rotterdam.nl_design_system.wicket.components.heading.RdHeading;
import nl.rotterdam.nl_design_system.wicket.components.radio_button.RdRadioButton;
import nl.rotterdam.nl_design_system.wicket.components.radio_group.RdRadioGroup;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;

import java.util.List;

public class MarriageIntakePage extends BurgerBasePage {

    private String registrationType = "geregistreerd_partnerschap";
    private String ceremonyType = "eenvoudig";

    public MarriageIntakePage() {
        this(new PageParameters());
    }

    public MarriageIntakePage(PageParameters parameters) {
        // Breadcrumb
        List<RdBreadcrumbNavRecord<? extends org.apache.wicket.request.component.IRequestablePage>> breadcrumbs = List.of(
                new RdBreadcrumbNavRecord<>(null, "Mijn Loket", MarriageIntakePage.class),
                new RdBreadcrumbNavRecord<>(null, "Mijn dag", MarriageIntakePage.class)
        );
        pageBody.add(new RdBreadcrumbNavPanel("breadcrumb", breadcrumbs));

        // Page heading
        pageBody.add(new RdHeading("heading", "Maak jullie dag om nooit te vergeten", 1));

        // Form
        Form<Void> form = new Form<>("form");
        pageBody.add(form);

        // Registratie radio group
        RdRadioGroup<String> registrationGroup = new RdRadioGroup<>(
                "registrationGroup",
                new PropertyModel<>(this, "registrationType"),
                Model.of("Registratie"),
                Model.of("Wil je weten wat het verschil is tussen een huwelijk en geregistreerd partnerschap,"
                        + " kijk dan even op onze pagina op rotterdam.nl/registratie")
        );
        form.add(registrationGroup);
        RadioGroup<String> regRadioGroup = registrationGroup.getRadioGroup();
        registrationGroup.add(new RdRadioButton<>("huwelijk", Model.of("huwelijk"), regRadioGroup));
        registrationGroup.add(new RdRadioButton<>("geregistreerdPartnerschap",
                Model.of("geregistreerd_partnerschap"), regRadioGroup));

        // Soort radio group
        RdRadioGroup<String> ceremonyGroup = new RdRadioGroup<>(
                "ceremonyGroup",
                new PropertyModel<>(this, "ceremonyType"),
                Model.of("Soort"),
                Model.of("Wil je weten wat het verschil is tussen een huwelijk en geregistreerd partnerschap,"
                        + " kijk dan even op onze pagina op rotterdam.nl/registratie")
        );
        form.add(ceremonyGroup);
        RadioGroup<String> cerRadioGroup = ceremonyGroup.getRadioGroup();
        ceremonyGroup.add(new RdRadioButton<>("gratis", Model.of("gratis"), cerRadioGroup));
        ceremonyGroup.add(new RdRadioButton<>("eenvoudig", Model.of("eenvoudig"), cerRadioGroup));
        ceremonyGroup.add(new RdRadioButton<>("regulier", Model.of("regulier"), cerRadioGroup));

        // Submit button
        RdButton submitButton = new RdButton("submitButton") {
            @Override
            public void onSubmit() {
                // Navigate to next step (to be implemented)
            }
        };
        submitButton.setAppearance(RdButtonAppearance.PRIMARY_ACTION);
        submitButton.add(new WebMarkupContainer("icon")
                .add(new RotterdamIconBehavior(RotterdamIconType.RING)));

        form.add(submitButton);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssReferenceHeaderItem.forReference(
                new PackageResourceReference(MarriageIntakePage.class, "mijn-dag.css")));
    }
}
