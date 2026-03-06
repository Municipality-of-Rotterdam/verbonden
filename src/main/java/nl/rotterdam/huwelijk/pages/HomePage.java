package nl.rotterdam.huwelijk.pages;

import nl.rotterdam.nl_design_system.rotterdam_css.wicket.NldsRotterdamDesignSystemThemeBehavior;
import nl.rotterdam.nl_design_system.rotterdam_extensions.wicket.components.rotterdam_icon.RotterdamIconBehavior;
import nl.rotterdam.nl_design_system.rotterdam_extensions.wicket.components.rotterdam_icon.RotterdamIconType;
import nl.rotterdam.nl_design_system.rotterdam_extensions.wicket.components.rotterdam_logo.RotterdamLogoImage;
import nl.rotterdam.nl_design_system.wicket.components.body.RdBodyTransparentContainer;
import nl.rotterdam.nl_design_system.wicket.components.breadcrumb_nav.RdBreadcrumbNavPanel;
import nl.rotterdam.nl_design_system.wicket.components.breadcrumb_nav.RdBreadcrumbNavRecord;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButtonAppearance;
import nl.rotterdam.nl_design_system.wicket.components.heading.RdHeading;
import nl.rotterdam.nl_design_system.wicket.components.logo.RdLogoBorder;
import nl.rotterdam.nl_design_system.wicket.components.page_body.RdPageBodyBorder;
import nl.rotterdam.nl_design_system.wicket.components.page_footer.RdPageFooterBorder;
import nl.rotterdam.nl_design_system.wicket.components.page_header.RdPageHeaderBorder;
import nl.rotterdam.nl_design_system.wicket.components.page_layout.RdPageLayoutBorder;
import nl.rotterdam.nl_design_system.wicket.components.radio_button.RdRadioButton;
import nl.rotterdam.nl_design_system.wicket.components.radio_group.RdRadioGroup;
import nl.rotterdam.nl_design_system.wicket.components.root.RdRootTransparentContainer;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;

import java.util.List;

public class HomePage extends WebPage {

    private String registrationType = "geregistreerd_partnerschap";
    private String ceremonyType = "eenvoudig";

    public HomePage() {
        this(new PageParameters());
    }

    public HomePage(PageParameters parameters) {
        // Root html element with Rotterdam theme
        RdRootTransparentContainer root = new RdRootTransparentContainer("root");
        root.add(NldsRotterdamDesignSystemThemeBehavior.INSTANCE);
        add(root);

        // Body element
        add(new RdBodyTransparentContainer("body"));

        // Page Layout
        RdPageLayoutBorder pageLayout = new RdPageLayoutBorder("pageLayout");
        add(pageLayout);

        // Page Header
        RdPageHeaderBorder pageHeader = new RdPageHeaderBorder("pageHeader");
        pageLayout.add(pageHeader);

        // Logo inside header
        RdLogoBorder logo = new RdLogoBorder("logo");
        logo.add(new RotterdamLogoImage("rotterdamLogoImage"));
        pageHeader.add(logo);

        // Top bar icons
        pageHeader.add(new WebMarkupContainer("globeIcon")
                .add(new RotterdamIconBehavior(RotterdamIconType.GLOBE)));
        pageHeader.add(new WebMarkupContainer("userIcon")
                .add(new RotterdamIconBehavior(RotterdamIconType.USER)));
        pageHeader.add(new WebMarkupContainer("logOutIcon")
                .add(new RotterdamIconBehavior(RotterdamIconType.LOG_OUT)));

        // Page Body
        RdPageBodyBorder pageBody = new RdPageBodyBorder("pageBody");
        pageLayout.add(pageBody);

        // Breadcrumb
        List<RdBreadcrumbNavRecord<? extends org.apache.wicket.request.component.IRequestablePage>> breadcrumbs = List.of(
                new RdBreadcrumbNavRecord<>(null, "Mijn Loket", HomePage.class),
                new RdBreadcrumbNavRecord<>(null, "Mijn dag", HomePage.class)
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

        // Page Footer
        RdPageFooterBorder pageFooter = new RdPageFooterBorder("pageFooter");
        pageFooter.add(new RotterdamLogoImage("footerLogoImage"));
        pageLayout.add(pageFooter);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssReferenceHeaderItem.forReference(
                new PackageResourceReference(HomePage.class, "mijn-dag.css")));
    }
}
