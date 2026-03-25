package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

public class IntakeSidebarPanel extends Panel {

    public IntakeSidebarPanel(String id, IModel<DossierSamenvattingDto> dossierModel) {
        super(id, dossierModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        DossierSamenvattingDto dossier = (DossierSamenvattingDto) getDefaultModelObject();

        WebMarkupContainer dossierGegevens = new WebMarkupContainer("dossierGegevens");
        dossierGegevens.setVisible(dossier != null);
        if (dossier != null) {
            dossierGegevens.add(new Label("registratieTypeLabel", dossier.registratieType().getLabel()));
            dossierGegevens.add(new Label("ceremonieSoortLabel", dossier.ceremonieSoort().getLabel()));
            dossierGegevens.add(new Label("ceremoniePrijs", dossier.ceremonieSoort().getPrijs()));
        } else {
            dossierGegevens.add(new Label("registratieTypeLabel", ""));
            dossierGegevens.add(new Label("ceremonieSoortLabel", ""));
            dossierGegevens.add(new Label("ceremoniePrijs", ""));
        }
        add(dossierGegevens);
    }
}
