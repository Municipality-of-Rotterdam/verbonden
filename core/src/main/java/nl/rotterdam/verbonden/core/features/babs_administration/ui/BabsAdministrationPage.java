package nl.rotterdam.verbonden.core.features.babs_administration.ui;

import nl.rotterdam.verbonden.core.administration_common.AdministrationBasePage;
import nl.rotterdam.verbonden.core.features.babs_administration.application.BabsAdministrationService;
import nl.rotterdam.verbonden.core.features.babs_administration.application.BabsImportService;
import nl.rotterdam.verbonden.core.features.babs_administration.domain.BabsImportResult;
import nl.rotterdam.verbonden.core.features.babs_administration.domain.ListBabsDto;
import nl.rotterdam.nl_design_system.wicket.components.button.RdAjaxButton;
import nl.rotterdam.nl_design_system.wicket.components.table.RdDataTable;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BabsAdministrationPage extends AdministrationBasePage {

    @SpringBean
    private BabsAdministrationService babsAdministrationService;

    @SpringBean
    private BabsImportService babsImportService;

    public BabsAdministrationPage() {
        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        pageBody.add(feedback);

        pageBody.add(new BookmarkablePageLink<>("nieuwBabsLink", BabsCreatePage.class));

        Form<?> importForm = new Form<>("importForm");
        importForm.add(new RdAjaxButton("importeerButton") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                BabsImportResult result = babsImportService.importeerVanRotterdam();
                if (result.errors() == 0) {
                    success("Import voltooid: " + result.imported() + " BABS geïmporteerd.");
                } else {
                    warn("Import klaar: " + result.imported() + " geïmporteerd, "
                            + result.errors() + " fouten.");
                }
                target.add(feedback);
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                target.add(feedback);
            }
        });
        pageBody.add(importForm);

        pageBody.add(buildBabsTable());
    }

    private RdDataTable<ListBabsDto, String> buildBabsTable() {
        List<IColumn<ListBabsDto, String>> columns = new ArrayList<>();

        columns.add(new AbstractColumn<>(Model.of("Naam"), "naam") {
            @Override
            public void populateItem(Item<ICellPopulator<ListBabsDto>> cellItem,
                                     String componentId,
                                     IModel<ListBabsDto> rowModel) {
                cellItem.add(new Label(componentId, rowModel.map(ListBabsDto::naam)));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Status")) {
            @Override
            public void populateItem(Item<ICellPopulator<ListBabsDto>> cellItem,
                                     String componentId,
                                     IModel<ListBabsDto> rowModel) {
                cellItem.add(new Label(componentId,
                        Model.of(rowModel.getObject().actief() ? "Actief" : "Inactief")));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Actief Vanaf")) {
            @Override
            public void populateItem(Item<ICellPopulator<ListBabsDto>> cellItem,
                                     String componentId,
                                     IModel<ListBabsDto> rowModel) {
                ListBabsDto dto = rowModel.getObject();
                String waarde = dto.actiefVanaf() != null ? dto.actiefVanaf().toString() : "";
                cellItem.add(new Label(componentId, Model.of(waarde)));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Actief Tot en Met")) {
            @Override
            public void populateItem(Item<ICellPopulator<ListBabsDto>> cellItem,
                                     String componentId,
                                     IModel<ListBabsDto> rowModel) {
                ListBabsDto dto = rowModel.getObject();
                String waarde = dto.actiefTotEnMet() != null ? dto.actiefTotEnMet().toString() : "";
                cellItem.add(new Label(componentId, Model.of(waarde)));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Acties")) {
            @Override
            public void populateItem(Item<ICellPopulator<ListBabsDto>> cellItem,
                                     String componentId,
                                     IModel<ListBabsDto> rowModel) {
                cellItem.add(new ActiesFragment(componentId, rowModel));
            }
        });

        SortableDataProvider<ListBabsDto, String> provider = new SortableDataProvider<>() {
            @Override
            public Iterator<? extends ListBabsDto> iterator(long first, long count) {
                boolean ascending = getSort() == null || getSort().isAscending();
                String sortProperty = getSort() != null ? getSort().getProperty() : "naam";
                Sort.Direction direction = ascending ? Sort.Direction.ASC : Sort.Direction.DESC;
                int pageSize = count > 0 ? (int) count : 20;
                int pageNumber = pageSize > 0 ? (int) (first / pageSize) : 0;
                PageRequest pageRequest = PageRequest.of(pageNumber, pageSize,
                        Sort.by(direction, sortProperty));
                Page<ListBabsDto> page = babsAdministrationService.findAll(pageRequest);
                return page.iterator();
            }

            @Override
            public long size() {
                return babsAdministrationService.count();
            }

            @Override
            public IModel<ListBabsDto> model(ListBabsDto dto) {
                return Model.of(dto);
            }
        };
        provider.setSort("naam", SortOrder.ASCENDING);

        return new RdDataTable<>("babsTable", columns, provider, 20);
    }

    // ---------------------------------------------------------------------------
    // Fragment for row action buttons, defined in BabsAdministrationPage.html
    // ---------------------------------------------------------------------------

    private final class ActiesFragment extends Fragment {

        ActiesFragment(String id, IModel<ListBabsDto> model) {
            super(id, "actiesFragment", BabsAdministrationPage.this, model);

            ListBabsDto dto = model.getObject();
            PageParameters params = new PageParameters();
            params.add("id", dto.id());

            add(new BookmarkablePageLink<>("bewerkLink", BabsUpdatePage.class, params));

            add(new Link<>("toggleActiefLink", model) {
                @Override
                public void onClick() {
                    babsAdministrationService.toggleActief(getModelObject().id());
                    setResponsePage(BabsAdministrationPage.class);
                }

                @Override
                public IModel<String> getBody() {
                    return Model.of(getModelObject().actief() ? "Inactief maken" : "Actief maken");
                }
            });
        }
    }
}
