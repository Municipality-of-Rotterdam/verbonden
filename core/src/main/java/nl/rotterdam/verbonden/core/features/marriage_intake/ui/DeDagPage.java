package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.nl_design_system.wicket.components.alert.RdAlert;
import nl.rotterdam.nl_design_system.wicket.components.alert.RdAlertType;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.UUID;

import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;

public class DeDagPage extends IntakeBasePage {

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    private final Model<String> nietBeschikbaarMelding = Model.of();

    private final RdAlert nietBeschikbaarAlert = new RdAlert("nietBeschikbaarAlert", nietBeschikbaarMelding, RdAlertType.INFO) {
        @Override
        protected void onConfigure() {
            super.onConfigure();
            setVisible(nietBeschikbaarMelding.getObject() != null);
        }
    };

    @Override
    protected IntakeStep getActiveStep() {
        return IntakeStep.DE_DAG;
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new ResourceModel("intake.page.title.dedag");
    }

    @Override
    protected IModel<String> getStepHeadingModel() {
        return new ResourceModel("intake.step.dedag");
    }

    @Override
    protected IModel<String> getStepIntroModel() {
        return LoadableDetachableModel.of(() -> {
            CeremonieSoort soort = marriageIntakeService.findByDossierId(dossierId).ceremonieSoort();
            return getString(soort == CeremonieSoort.KLEIN
                    ? "intake.intro.dedag.klein" : "intake.intro.dedag");
        });
    }

    @Override
    protected IModel<DossierSamenvattingDto> getSidebarDossierModel() {
        return LoadableDetachableModel.of(() -> marriageIntakeService.findByDossierId(dossierId));
    }

    public static void respond(UUID dossierId) {
        RequestCycle.get().setResponsePage(
                DeDagPage.class,
                makeDossierPageParameters(dossierId)
        );
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        pageBody.add(new Link<Void>("datumLink") {
            @Override
            public void onClick() {
                DatumKiezenPage.respond(dossierId);
            }
        });

        // Location and BABS cannot be chosen yet; clicking a card explains why instead of doing nothing
        pageBody.add(
                nietBeschikbaarAlert,
                new NietBeschikbaarLink("locatieLink", "intake.card.locatie.niet.beschikbaar"),
                new NietBeschikbaarLink("babsLink", "intake.card.babs.niet.beschikbaar")
        );

    }

    private class NietBeschikbaarLink extends Link<Void> {

        private final String meldingKey;

        NietBeschikbaarLink(String id, String meldingKey) {
            super(id);
            this.meldingKey = meldingKey;
            add(AttributeModifier.append("class", " rd-keuze-card--niet-beschikbaar"));
        }

        @Override
        public void onClick() {
            CeremonieSoort soort = marriageIntakeService.findByDossierId(dossierId).ceremonieSoort();
            String key = soort == CeremonieSoort.GROOT ? meldingKey : meldingKey + ".klein";
            nietBeschikbaarMelding.setObject(getString(key));
        }
    }
}
