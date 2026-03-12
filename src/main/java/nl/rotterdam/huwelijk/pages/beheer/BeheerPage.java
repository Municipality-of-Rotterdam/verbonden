package nl.rotterdam.huwelijk.pages.beheer;

import nl.rotterdam.huwelijk.baps.BapsDto;
import nl.rotterdam.huwelijk.baps.BapsImportResult;
import nl.rotterdam.huwelijk.baps.BapsImportService;
import nl.rotterdam.huwelijk.baps.BapsService;
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
import nl.rotterdam.huwelijk.pages.BasePage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BeheerPage extends BasePage {

    @Serial
    private static final long serialVersionUID = 1L;

    @SpringBean
    private BapsService bapsService;

    @SpringBean
    private BapsImportService bapsImportService;

    public BeheerPage() {
        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);

        add(new BookmarkablePageLink<>("nieuwBapsLink", BapsToevoegenPage.class));

        Form<?> importForm = new Form<>("importForm");
        importForm.add(new RdAjaxButton("importeerButton") {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                BapsImportResult result = bapsImportService.importeerVanRotterdam();
                if (result.errors() == 0) {
                    success("Import voltooid: " + result.imported() + " BAPS geïmporteerd.");
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
        add(importForm);

        add(buildBapsTable());
    }

    private RdDataTable<BapsDto, String> buildBapsTable() {
        List<IColumn<BapsDto, String>> columns = new ArrayList<>();

        columns.add(new AbstractColumn<BapsDto, String>(Model.of("Naam"), "naam") {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(Item<ICellPopulator<BapsDto>> cellItem,
                                     String componentId,
                                     IModel<BapsDto> rowModel) {
                cellItem.add(new Label(componentId, Model.of(rowModel.getObject().naam())));
            }
        });

        columns.add(new AbstractColumn<BapsDto, String>(Model.of("Status")) {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(Item<ICellPopulator<BapsDto>> cellItem,
                                     String componentId,
                                     IModel<BapsDto> rowModel) {
                cellItem.add(new Label(componentId,
                        Model.of(rowModel.getObject().actief() ? "Actief" : "Inactief")));
            }
        });

        columns.add(new AbstractColumn<BapsDto, String>(Model.of("Actief Vanaf")) {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(Item<ICellPopulator<BapsDto>> cellItem,
                                     String componentId,
                                     IModel<BapsDto> rowModel) {
                BapsDto dto = rowModel.getObject();
                String waarde = dto.actiefVanaf() != null ? dto.actiefVanaf().toString() : "";
                cellItem.add(new Label(componentId, Model.of(waarde)));
            }
        });

        columns.add(new AbstractColumn<BapsDto, String>(Model.of("Actief Tot en Met")) {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(Item<ICellPopulator<BapsDto>> cellItem,
                                     String componentId,
                                     IModel<BapsDto> rowModel) {
                BapsDto dto = rowModel.getObject();
                String waarde = dto.actiefTotEnMet() != null ? dto.actiefTotEnMet().toString() : "";
                cellItem.add(new Label(componentId, Model.of(waarde)));
            }
        });

        columns.add(new AbstractColumn<BapsDto, String>(Model.of("Acties")) {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(Item<ICellPopulator<BapsDto>> cellItem,
                                     String componentId,
                                     IModel<BapsDto> rowModel) {
                cellItem.add(new ActiesFragment(componentId, rowModel));
            }
        });

        SortableDataProvider<BapsDto, String> provider = new SortableDataProvider<>() {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public Iterator<? extends BapsDto> iterator(long first, long count) {
                boolean ascending = getSort() == null || getSort().isAscending();
                String sortProperty = getSort() != null ? getSort().getProperty() : "naam";
                Sort.Direction direction = ascending ? Sort.Direction.ASC : Sort.Direction.DESC;
                int pageSize = count > 0 ? (int) count : 20;
                int pageNumber = pageSize > 0 ? (int) (first / pageSize) : 0;
                PageRequest pageRequest = PageRequest.of(pageNumber, pageSize,
                        Sort.by(direction, sortProperty));
                Page<BapsDto> page = bapsService.findAll(pageRequest);
                return page.iterator();
            }

            @Override
            public long size() {
                return bapsService.count();
            }

            @Override
            public IModel<BapsDto> model(BapsDto dto) {
                Long id = dto.id();
                return new IModel<>() {
                    @Serial
                    private static final long serialVersionUID = 1L;

                    @Override
                    public BapsDto getObject() {
                        return bapsService.findById(id).orElse(null);
                    }
                };
            }
        };
        provider.setSort("naam", SortOrder.ASCENDING);

        return new RdDataTable<>("bapsTable", columns, provider, 20);
    }

    // ---------------------------------------------------------------------------
    // Fragment for row action buttons, defined in BeheerPage.html
    // ---------------------------------------------------------------------------

    private final class ActiesFragment extends Fragment {

        @Serial
        private static final long serialVersionUID = 1L;

        ActiesFragment(String id, IModel<BapsDto> model) {
            super(id, "actiesFragment", BeheerPage.this, model);

            BapsDto dto = model.getObject();
            PageParameters params = new PageParameters();
            params.add("id", dto.id());

            add(new BookmarkablePageLink<>("bewerkLink", BapsWijzigenPage.class, params));

            add(new Link<BapsDto>("toggleActiefLink", model) {
                @Serial
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick() {
                    BapsDto b = getModelObject();
                    bapsService.save(b.withActief(!b.actief()));
                    setResponsePage(BeheerPage.class);
                }

                @Override
                public IModel<String> getBody() {
                    return Model.of(getModelObject().actief() ? "Inactief maken" : "Actief maken");
                }
            });
        }
    }
}
