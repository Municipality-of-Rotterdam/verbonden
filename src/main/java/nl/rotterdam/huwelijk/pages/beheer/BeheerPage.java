package nl.rotterdam.huwelijk.pages.beheer;

import nl.rotterdam.huwelijk.baps.Baps;
import nl.rotterdam.huwelijk.baps.BapsImportService;
import nl.rotterdam.huwelijk.baps.BapsService;
import nl.rotterdam.nl_design_system.wicket.components.button.RdAjaxButton;
import nl.rotterdam.nl_design_system.wicket.components.table.RdDataTable;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebPage;
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

import java.io.Serial;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BeheerPage extends WebPage {

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

        add(new BookmarkablePageLink<>("nieuwBapsLink", BapsFormPage.class));

        Form<?> importForm = new Form<>("importForm");
        importForm.add(new RdAjaxButton("importeerButton") {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                BapsImportService.ImportResult result = bapsImportService.importeerVanRotterdam();
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

    private RdDataTable<Baps, String> buildBapsTable() {
        List<IColumn<Baps, String>> columns = new ArrayList<>();

        columns.add(new PropertyColumn<>(Model.of("Naam"), "naam", "naam"));

        columns.add(new AbstractColumn<Baps, String>(Model.of("Status")) {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(Item<ICellPopulator<Baps>> cellItem,
                                     String componentId,
                                     IModel<Baps> rowModel) {
                cellItem.add(new Label(componentId,
                        Model.of(rowModel.getObject().isActief() ? "Actief" : "Inactief")));
            }
        });

        columns.add(new PropertyColumn<>(Model.of("Actief Vanaf"), "actiefVanaf"));
        columns.add(new PropertyColumn<>(Model.of("Actief Tot en Met"), "actiefTotEnMet"));

        columns.add(new AbstractColumn<Baps, String>(Model.of("Acties")) {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(Item<ICellPopulator<Baps>> cellItem,
                                     String componentId,
                                     IModel<Baps> rowModel) {
                cellItem.add(new ActiesFragment(componentId, rowModel));
            }
        });

        SortableDataProvider<Baps, String> provider = new SortableDataProvider<>() {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public Iterator<? extends Baps> iterator(long first, long count) {
                boolean ascending = getSort() == null || getSort().isAscending();
                String sortProperty = getSort() != null ? getSort().getProperty() : "naam";
                Sort.Direction direction = ascending ? Sort.Direction.ASC : Sort.Direction.DESC;
                int pageSize = count > 0 ? (int) count : 20;
                int pageNumber = pageSize > 0 ? (int) (first / pageSize) : 0;
                PageRequest pageRequest = PageRequest.of(pageNumber, pageSize,
                        Sort.by(direction, sortProperty));
                Page<Baps> page = bapsService.findAll(pageRequest);
                return page.iterator();
            }

            @Override
            public long size() {
                return bapsService.count();
            }

            @Override
            public IModel<Baps> model(Baps baps) {
                Long id = baps.getId();
                return new IModel<>() {
                    @Serial
                    private static final long serialVersionUID = 1L;

                    @Override
                    public Baps getObject() {
                        return bapsService.findById(id).orElse(null);
                    }
                };
            }
        };
        provider.setSort("naam", SortOrder.ASCENDING);

        // RdDataTable automatically adds RdHeadersToolbar and RdAjaxNavigationToolbar
        return new RdDataTable<>("bapsTable", columns, provider, 20);
    }

    // ---------------------------------------------------------------------------
    // Fragment for row action buttons, defined in BeheerPage.html
    // ---------------------------------------------------------------------------

    private final class ActiesFragment extends Fragment {

        @Serial
        private static final long serialVersionUID = 1L;

        ActiesFragment(String id, IModel<Baps> model) {
            super(id, "actiesFragment", BeheerPage.this, model);

            Baps baps = model.getObject();
            PageParameters params = new PageParameters();
            params.add("id", baps.getId());

            add(new BookmarkablePageLink<>("bewerkLink", BapsFormPage.class, params));

            add(new Link<Baps>("toggleActiefLink", model) {
                @Serial
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick() {
                    Baps b = getModelObject();
                    b.setActief(!b.isActief());
                    bapsService.save(b);
                    setResponsePage(BeheerPage.class);
                }

                @Override
                public IModel<String> getBody() {
                    return Model.of(getModelObject().isActief() ? "Inactief maken" : "Actief maken");
                }
            });
        }
    }
}
