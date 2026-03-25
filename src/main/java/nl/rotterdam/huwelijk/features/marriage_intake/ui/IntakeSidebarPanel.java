package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.RegistratieType;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;

import java.util.function.Function;

public class IntakeSidebarPanel extends Panel {

    public IntakeSidebarPanel(String id, IModel<DossierSamenvattingDto> dossierModel) {
        super(id, dossierModel);
        setOutputMarkupId(true);
    }

    @SuppressWarnings("unchecked")
    private IModel<DossierSamenvattingDto> getDossierModel() {
        return (IModel<DossierSamenvattingDto>) getDefaultModel();
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        WebMarkupContainer dossierGegevens = new WebMarkupContainer("dossierGegevens") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(getDossierModel().getObject() != null);
            }
        };

        IModel<String> registratieModel = LambdaModel.of(
                () -> dossierValue(DossierSamenvattingDto::registratieType, RegistratieType::getLabel));

        dossierGegevens.add(new Label("registratieTypeLabel", registratieModel));
        add(dossierGegevens);
    }

    private <I, O> String dossierValue(Function<DossierSamenvattingDto, I> extractor, Function<I, O> mapper) {
        DossierSamenvattingDto d = getDossierModel().getObject();
        if (d == null) {
            return "";
        }
        return String.valueOf(mapper.apply(extractor.apply(d)));
    }
}
