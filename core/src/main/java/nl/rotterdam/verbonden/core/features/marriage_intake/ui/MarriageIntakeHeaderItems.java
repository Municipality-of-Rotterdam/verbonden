package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.request.resource.PackageResourceReference;

class MarriageIntakeHeaderItems {
    static final CssReferenceHeaderItem MARRIAGE_INTAKE_CSS =
            CssHeaderItem.forReference(new PackageResourceReference(IntakeBasePage.class, "marriage-intake.css"));

}
