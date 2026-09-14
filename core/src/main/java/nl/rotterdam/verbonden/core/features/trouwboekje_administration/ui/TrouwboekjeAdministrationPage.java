package nl.rotterdam.verbonden.core.features.trouwboekje_administration.ui;

import nl.rotterdam.verbonden.core.administration_common.AdministrationBasePage;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.application.TrouwboekjeAdministrationService;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain.ListTrouwboekjeDto;
import nl.rotterdam.nl_design_system.wicket.components.table.RdDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
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
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class TrouwboekjeAdministrationPage extends AdministrationBasePage {

    @SpringBean
    private TrouwboekjeAdministrationService trouwboekjeAdministrationService;

    public TrouwboekjeAdministrationPage() {
        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        pageBody.add(feedback);
        pageBody.add(new BookmarkablePageLink<>("nieuwExtraLink", TrouwboekjeCreatePage.class));
        pageBody.add(buildExtrasTable());
    }

    private Form<Void> buildExtrasTable() {
        List<IColumn<ListTrouwboekjeDto, String>> columns = new ArrayList<>();

        columns.add(new AbstractColumn<>(Model.of("Naam"), "naam") {
            @Override
            public void populateItem(Item<ICellPopulator<ListTrouwboekjeDto>> cellItem,
                                     String componentId,
                                     IModel<ListTrouwboekjeDto> rowModel) {
                cellItem.add(new Label(componentId, rowModel.map(ListTrouwboekjeDto::naam)));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Prijs"), "prijs") {
            @Override
            public void populateItem(Item<ICellPopulator<ListTrouwboekjeDto>> cellItem,
                                     String componentId,
                                     IModel<ListTrouwboekjeDto> rowModel) {
                cellItem.add(new Label(componentId,
                        rowModel.map(dto -> dto.prijs() != null ? dto.prijs().toPlainString() : "")));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Startdatum"), "startdatum") {
            @Override
            public void populateItem(Item<ICellPopulator<ListTrouwboekjeDto>> cellItem,
                                     String componentId,
                                     IModel<ListTrouwboekjeDto> rowModel) {
                cellItem.add(new Label(componentId,
                        rowModel.map(dto -> dto.startdatum() != null ? dto.startdatum().toString() : "")));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Einddatum"), "einddatum") {
            @Override
            public void populateItem(Item<ICellPopulator<ListTrouwboekjeDto>> cellItem,
                                     String componentId,
                                     IModel<ListTrouwboekjeDto> rowModel) {
                cellItem.add(new Label(componentId,
                        rowModel.map(dto -> dto.einddatum() != null ? dto.einddatum().toString() : "")));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Acties")) {
            @Override
            public void populateItem(Item<ICellPopulator<ListTrouwboekjeDto>> cellItem,
                                     String componentId,
                                     IModel<ListTrouwboekjeDto> rowModel) {
                cellItem.add(new ActiesFragment(componentId, rowModel));
            }
        });

        SortableDataProvider<ListTrouwboekjeDto, String> provider = new SortableDataProvider<>() {
            private transient List<ListTrouwboekjeDto> cachedList;

            private List<ListTrouwboekjeDto> getList() {
                if (cachedList == null) {
                    cachedList = new ArrayList<>(trouwboekjeAdministrationService.findAll());
                }
                return cachedList;
            }

            @Override
            public Iterator<? extends ListTrouwboekjeDto> iterator(long first, long count) {
                List<ListTrouwboekjeDto> list = new ArrayList<>(getList());
                if (getSort() != null) {
                    list.sort(comparatorFor(getSort().getProperty(), getSort().isAscending()));
                }
                return list.stream().skip(first).limit(count > 0 ? count : list.size()).iterator();
            }

            @Override
            public long size() {
                return trouwboekjeAdministrationService.count();
            }

            @Override
            public IModel<ListTrouwboekjeDto> model(ListTrouwboekjeDto dto) {
                return Model.of(dto);
            }
        };
        provider.setSort("naam", SortOrder.ASCENDING);

        Form<Void> form = new Form<>("actionsForm");
        form.add(new RdDataTable<>("extrasTable", columns, provider, 20));
        return form;
    }

    private static Comparator<ListTrouwboekjeDto> comparatorFor(String property, boolean ascending) {
        Comparator<ListTrouwboekjeDto> comparator = switch (property) {
            case "prijs" -> Comparator.comparing(ListTrouwboekjeDto::prijs, Comparator.nullsLast(Comparator.naturalOrder()));
            case "startdatum" -> Comparator.comparing(ListTrouwboekjeDto::startdatum, Comparator.nullsLast(Comparator.naturalOrder()));
            case "einddatum" -> Comparator.comparing(ListTrouwboekjeDto::einddatum, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(ListTrouwboekjeDto::naam, Comparator.nullsLast(Comparator.naturalOrder()));
        };
        return ascending ? comparator : comparator.reversed();
    }

    private final class ActiesFragment extends Fragment {

        ActiesFragment(String id, IModel<ListTrouwboekjeDto> model) {
            super(id, "actiesFragment", TrouwboekjeAdministrationPage.this, model);

            ListTrouwboekjeDto dto = model.getObject();
            PageParameters params = new PageParameters().add("id", dto.id());

            add(new BookmarkablePageLink<>("bewerkLink", TrouwboekjeUpdatePage.class, params));

            add(new Link<>("verwijderLink", model) {
                @Override
                public void onClick() {
                    try {
                        trouwboekjeAdministrationService.delete(getModelObject().id());
                    } catch (DataIntegrityViolationException e) {
                        getPage().error(getString("extra.verwijder.fout.ingebruik"));
                        return;
                    }
                    setResponsePage(TrouwboekjeAdministrationPage.class);
                }
            });
        }
    }
}
