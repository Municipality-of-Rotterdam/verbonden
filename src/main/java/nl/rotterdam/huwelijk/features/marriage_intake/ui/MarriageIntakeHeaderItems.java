package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;
import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.request.resource.PackageResourceReference;

class MarriageIntakeHeaderItems {
    static final CssReferenceHeaderItem MARRIAGE_INTAKE_CSS =
            CssHeaderItem.forReference(new PackageResourceReference(IntakeBasePage.class, "marriage-intake.css"));

    static final CssReferenceHeaderItem CROPPER_CSS =
            CssHeaderItem.forReference(new WebjarsCssResourceReference("cropperjs/current/dist/cropper.min.css"));

    static final JavaScriptReferenceHeaderItem CROPPER_JS =
            JavaScriptHeaderItem.forReference(new WebjarsJavaScriptResourceReference("cropperjs/current/dist/cropper.min.js"));

    static final JavaScriptReferenceHeaderItem PASFOTO_CROP_JS =
            JavaScriptHeaderItem.forReference(new PackageResourceReference(JullieGegevensPage.class, "pasfoto-crop.js"));
}
