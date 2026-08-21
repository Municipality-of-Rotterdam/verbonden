package nl.rotterdam.verbonden.features.dossier_administration.ui;

import nl.rotterdam.verbonden.administration_common.AdministrationBasePage;
import nl.rotterdam.verbonden.features.dossier_administration.application.DossierAdministrationService;
import nl.rotterdam.verbonden.features.dossier_administration.domain.ListDossierDto;
import nl.rotterdam.nl_design_system.wicket.components.button.RdAjaxButton;
import nl.rotterdam.nl_design_system.wicket.components.table.RdDataTable;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DossierAdministrationPage extends AdministrationBasePage {

    @SpringBean
    private DossierAdministrationService dossierAdministrationService;

    private static final DateTimeFormatter DATUM_TIJD_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final Model<String> zoektermModel = Model.of("");

    public DossierAdministrationPage() {
        RdDataTable<ListDossierDto, String> dossierTable = buildDossierTable();
        dossierTable.setOutputMarkupId(true);

        Form<?> zoekForm = new Form<>("zoekForm");
        zoekForm.add(new TextField<>("zoektermVeld", zoektermModel));
        zoekForm.add(new RdAjaxButton("zoekButton") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                target.add(dossierTable);
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
            }
        });

        pageBody.add(zoekForm);
        pageBody.add(dossierTable);
    }

    private RdDataTable<ListDossierDto, String> buildDossierTable() {
        List<IColumn<ListDossierDto, String>> columns = new ArrayList<>();

        columns.add(new AbstractColumn<>(Model.of("Dossier ID")) {
            @Override
            public void populateItem(Item<ICellPopulator<ListDossierDto>> cellItem,
                                     String componentId,
                                     IModel<ListDossierDto> rowModel) {
                cellItem.add(new Label(componentId,
                        rowModel.map(dto -> dto.dossierId() != null ? dto.dossierId().toString() : "")));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("BSN 1")) {
            @Override
            public void populateItem(Item<ICellPopulator<ListDossierDto>> cellItem,
                                     String componentId,
                                     IModel<ListDossierDto> rowModel) {
                cellItem.add(new Label(componentId,
                        rowModel.map(dto -> dto.bsn1() != null ? dto.bsn1() : "")));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("BSN 2")) {
            @Override
            public void populateItem(Item<ICellPopulator<ListDossierDto>> cellItem,
                                     String componentId,
                                     IModel<ListDossierDto> rowModel) {
                cellItem.add(new Label(componentId,
                        rowModel.map(dto -> dto.bsn2() != null ? dto.bsn2() : "")));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Registratietype"), "registratieType") {
            @Override
            public void populateItem(Item<ICellPopulator<ListDossierDto>> cellItem,
                                     String componentId,
                                     IModel<ListDossierDto> rowModel) {
                cellItem.add(new Label(componentId,
                        rowModel.map(dto -> dto.registratieType() != null ? dto.registratieType().getLabel() : "")));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Ceremonie"), "ceremonieSoort") {
            @Override
            public void populateItem(Item<ICellPopulator<ListDossierDto>> cellItem,
                                     String componentId,
                                     IModel<ListDossierDto> rowModel) {
                cellItem.add(new Label(componentId,
                        rowModel.map(dto -> dto.ceremonieSoort() != null ? dto.ceremonieSoort().getLabel() : "")));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Aangemaakt op"), "aangemaaktOp") {
            @Override
            public void populateItem(Item<ICellPopulator<ListDossierDto>> cellItem,
                                     String componentId,
                                     IModel<ListDossierDto> rowModel) {
                cellItem.add(new Label(componentId,
                        rowModel.map(dto -> dto.aangemaaktOp() != null
                                ? dto.aangemaaktOp().format(DATUM_TIJD_FORMATTER) : "")));
            }
        });

        SortableDataProvider<ListDossierDto, String> provider = new SortableDataProvider<>() {
            @Override
            public Iterator<? extends ListDossierDto> iterator(long first, long count) {
                boolean ascending = getSort() == null || getSort().isAscending();
                String sortProperty = getSort() != null ? getSort().getProperty() : "aangemaaktOp";
                Sort.Direction direction = ascending ? Sort.Direction.ASC : Sort.Direction.DESC;
                int pageSize = count > 0 ? (int) count : 20;
                int pageNumber = pageSize > 0 ? (int) (first / pageSize) : 0;
                PageRequest pageRequest = PageRequest.of(pageNumber, pageSize,
                        Sort.by(direction, sortProperty));
                Page<ListDossierDto> page = dossierAdministrationService.search(
                        zoektermModel.getObject(), pageRequest);
                return page.iterator();
            }

            @Override
            public long size() {
                return dossierAdministrationService.count(zoektermModel.getObject());
            }

            @Override
            public IModel<ListDossierDto> model(ListDossierDto dto) {
                return Model.of(dto);
            }
        };
        provider.setSort("aangemaaktOp", SortOrder.DESCENDING);

        return new RdDataTable<>("dossierTable", columns, provider, 20);
    }
}
