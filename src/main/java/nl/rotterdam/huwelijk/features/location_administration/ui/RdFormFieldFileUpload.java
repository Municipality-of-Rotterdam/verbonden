package nl.rotterdam.huwelijk.features.location_administration.ui;

import nl.rotterdam.nl_design_system.wicket.components.form_field.RdFormFieldBorder;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.List;

class RdFormFieldFileUpload extends RdFormFieldBorder<List<FileUpload>, FileUploadField> {

    RdFormFieldFileUpload(String id, IModel<String> label) {
        super(id, Model.of(null), label);
    }

    @Override
    protected FileUploadField newInput(IModel<List<FileUpload>> model) {
        return new FileUploadField("input");
    }

    FileUpload getFileUpload() {
        return getInput().getFileUpload();
    }
}
