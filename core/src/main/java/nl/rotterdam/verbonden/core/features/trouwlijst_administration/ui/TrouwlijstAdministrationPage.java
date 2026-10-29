package nl.rotterdam.verbonden.core.features.trouwlijst_administration.ui;

import nl.rotterdam.nl_design_system.wicket.components.table.RdDataTable;
import nl.rotterdam.verbonden.core.administration_common.AdministrationBasePage;
import nl.rotterdam.verbonden.core.administration_common.RdFormFieldSelect;
import nl.rotterdam.verbonden.core.features.trouwlijst_administration.application.TrouwlijstAdministrationService;
import nl.rotterdam.verbonden.core.features.trouwlijst_administration.domain.ListTrouwlijstDossierDto;
import nl.rotterdam.verbonden.core.features.trouwlijst_administration.domain.TrouwlijstPartnerDto;
import nl.rotterdam.verbonden.core.features.trouwlijst_administration.domain.TrouwlijstWeekDto;
import nl.rotterdam.verbonden.core.identity.Roles;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.LambdaChoiceRenderer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.danekja.java.util.function.serializable.SerializableFunction;
import java.util.stream.Stream;

@AuthorizeInstantiation({Roles.BEHEER, Roles.SUPERUSER, Roles.PLANNER})
public class TrouwlijstAdministrationPage extends AdministrationBasePage {

    private static final DateTimeFormatter DATUM_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIJD_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String EXCEL_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @SpringBean
    private TrouwlijstAdministrationService trouwlijstAdministrationService;

    private final Model<TrouwlijstWeekDto> weekModel = new Model<>();

    private final IModel<List<TrouwlijstWeekDto>> wekenModel = new LoadableDetachableModel<>() {
        @Override
        protected List<TrouwlijstWeekDto> load() {
            return trouwlijstAdministrationService.findWeken();
        }
    };

    private final IModel<List<ListTrouwlijstDossierDto>> dossiersModel = new LoadableDetachableModel<>() {
        @Override
        protected List<ListTrouwlijstDossierDto> load() {
            TrouwlijstWeekDto week = weekModel.getObject();
            return week == null ? List.of()
                    : trouwlijstAdministrationService.findBevestigdeDossiers(week.van(), week.tot());
        }
    };

    private final WebMarkupContainer resultaat = new WebMarkupContainer("resultaat");

    public TrouwlijstAdministrationPage() {
        resultaat.setOutputMarkupId(true);
        resultaat.add(
                new RdDataTable<>("dossierTable", buildColumns(), new DossierDataProvider(), 50),
                new ExporteerLink("exporteerLink")
        );
        pageBody.add(new FilterForm("filterForm"), resultaat);
    }

    @Override
    protected void onDetach() {
        wekenModel.detach();
        dossiersModel.detach();
        super.onDetach();
    }

    private static List<IColumn<ListTrouwlijstDossierDto, String>> buildColumns() {
        List<IColumn<ListTrouwlijstDossierDto, String>> columns = new ArrayList<>();
        columns.add(kolom("Trouwdatum en tijd", dto -> dto.datum().format(DATUM_FORMATTER) + " "
                + dto.startTijd().format(TIJD_FORMATTER)));
        columns.add(kolom("Locatie", ListTrouwlijstDossierDto::locatie));
        columns.add(kolom("Babs", dto -> dto.babs() != null ? dto.babs() : ""));
        columns.add(kolom("Type huwelijk", dto -> dto.registratieType().getLabel() + " - "
                + dto.ceremonieSoort().getLabel()));
        columns.add(kolom("Partner 1", dto -> partnerTekst(dto, 0)));
        columns.add(kolom("Partner 2", dto -> partnerTekst(dto, 1)));
        columns.add(kolom("Taal", dto -> dto.taal() != null ? dto.taal() : ""));
        return columns;
    }

    private static IColumn<ListTrouwlijstDossierDto, String> kolom(String titel,
                                                                    SerializableFunction<ListTrouwlijstDossierDto, String> waarde) {
        return new AbstractColumn<>(Model.of(titel)) {
            @Override
            public void populateItem(Item<ICellPopulator<ListTrouwlijstDossierDto>> cellItem,
                                     String componentId,
                                     IModel<ListTrouwlijstDossierDto> rowModel) {
                cellItem.add(new Label(componentId, rowModel.map(waarde)));
            }
        };
    }

    private static String partnerTekst(ListTrouwlijstDossierDto dto, int index) {
        if (dto.partners().size() <= index) {
            return "";
        }
        TrouwlijstPartnerDto partner = dto.partners().get(index);
        return String.join(", ", Stream.of(partner.naam(), partner.emailadres(), partner.telefoonnummer())
                .filter(deel -> deel != null && !deel.isBlank())
                .toList());
    }

    private class DossierDataProvider extends SortableDataProvider<ListTrouwlijstDossierDto, String> {

        @Override
        public Iterator<? extends ListTrouwlijstDossierDto> iterator(long first, long count) {
            List<ListTrouwlijstDossierDto> dossiers = dossiersModel.getObject();
            return dossiers.subList((int) first, (int) Math.min(first + count, dossiers.size())).iterator();
        }

        @Override
        public long size() {
            return dossiersModel.getObject().size();
        }

        @Override
        public IModel<ListTrouwlijstDossierDto> model(ListTrouwlijstDossierDto dto) {
            return Model.of(dto);
        }
    }

    private class FilterForm extends Form<Void> {

        FilterForm(String id) {
            super(id);
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            add(new RdFormFieldWeekSelect("week"));
        }
    }

    private class RdFormFieldWeekSelect extends RdFormFieldSelect<TrouwlijstWeekDto> {

        RdFormFieldWeekSelect(String id) {
            super(id, weekModel, Model.of("Week"), wekenModel,
                    new LambdaChoiceRenderer<>(
                            week -> week.van().format(DATUM_FORMATTER) + " t/m " + week.tot().format(DATUM_FORMATTER),
                            week -> week.van().toString()));
            setRequired(true);
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            getInput().add(new AjaxFormComponentUpdatingBehavior("change") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.add(resultaat);
                }
            });
        }
    }

    private class ExporteerLink extends Link<Void> {

        ExporteerLink(String id) {
            super(id);
        }

        @Override
        public boolean isVisible() {
            return weekModel.getObject() != null;
        }

        @Override
        public void onClick() {
            TrouwlijstWeekDto week = weekModel.getObject();
            LocalDate van = week.van();
            byte[] excel = trouwlijstAdministrationService.exporteer(van, week.tot());
            AbstractResourceStreamWriter stream = new AbstractResourceStreamWriter() {
                @Override
                public void write(OutputStream output) throws IOException {
                    output.write(excel);
                }

                @Override
                public String getContentType() {
                    return EXCEL_CONTENT_TYPE;
                }
            };
            getRequestCycle().scheduleRequestHandlerAfterCurrent(
                    new ResourceStreamRequestHandler(stream, "trouwlijst-" + van + ".xlsx")
                            .setContentDisposition(ContentDisposition.ATTACHMENT));
        }
    }
}
