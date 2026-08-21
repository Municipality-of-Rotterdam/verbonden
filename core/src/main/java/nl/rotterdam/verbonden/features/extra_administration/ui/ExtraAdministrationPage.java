package nl.rotterdam.verbonden.features.extra_administration.ui;

import nl.rotterdam.verbonden.administration_common.AdministrationBasePage;
import nl.rotterdam.verbonden.features.extra_administration.application.ExtraAdministrationService;
import nl.rotterdam.verbonden.features.extra_administration.domain.ListExtraDto;
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

public class ExtraAdministrationPage extends AdministrationBasePage {

    @SpringBean
    private ExtraAdministrationService extraAdministrationService;

    public ExtraAdministrationPage() {
        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        pageBody.add(feedback);
        pageBody.add(new BookmarkablePageLink<>("nieuwExtraLink", ExtraCreatePage.class));
        pageBody.add(buildExtrasTable());
    }

    private Form<Void> buildExtrasTable() {
        List<IColumn<ListExtraDto, String>> columns = new ArrayList<>();

        columns.add(new AbstractColumn<>(Model.of("Type"), "type") {
            @Override
            public void populateItem(Item<ICellPopulator<ListExtraDto>> cellItem,
                                     String componentId,
                                     IModel<ListExtraDto> rowModel) {
                cellItem.add(new Label(componentId, rowModel.map(dto -> dto.type().getLabel())));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Naam"), "naam") {
            @Override
            public void populateItem(Item<ICellPopulator<ListExtraDto>> cellItem,
                                     String componentId,
                                     IModel<ListExtraDto> rowModel) {
                cellItem.add(new Label(componentId, rowModel.map(ListExtraDto::naam)));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Prijs"), "prijs") {
            @Override
            public void populateItem(Item<ICellPopulator<ListExtraDto>> cellItem,
                                     String componentId,
                                     IModel<ListExtraDto> rowModel) {
                cellItem.add(new Label(componentId,
                        rowModel.map(dto -> dto.prijs() != null ? dto.prijs().toPlainString() : "")));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Startdatum"), "startdatum") {
            @Override
            public void populateItem(Item<ICellPopulator<ListExtraDto>> cellItem,
                                     String componentId,
                                     IModel<ListExtraDto> rowModel) {
                cellItem.add(new Label(componentId,
                        rowModel.map(dto -> dto.startdatum() != null ? dto.startdatum().toString() : "")));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Einddatum"), "einddatum") {
            @Override
            public void populateItem(Item<ICellPopulator<ListExtraDto>> cellItem,
                                     String componentId,
                                     IModel<ListExtraDto> rowModel) {
                cellItem.add(new Label(componentId,
                        rowModel.map(dto -> dto.einddatum() != null ? dto.einddatum().toString() : "")));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Acties")) {
            @Override
            public void populateItem(Item<ICellPopulator<ListExtraDto>> cellItem,
                                     String componentId,
                                     IModel<ListExtraDto> rowModel) {
                cellItem.add(new ActiesFragment(componentId, rowModel));
            }
        });

        SortableDataProvider<ListExtraDto, String> provider = new SortableDataProvider<>() {
            private transient List<ListExtraDto> cachedList;

            private List<ListExtraDto> getList() {
                if (cachedList == null) {
                    cachedList = new ArrayList<>(extraAdministrationService.findAll());
                }
                return cachedList;
            }

            @Override
            public Iterator<? extends ListExtraDto> iterator(long first, long count) {
                List<ListExtraDto> list = new ArrayList<>(getList());
                if (getSort() != null) {
                    list.sort(comparatorFor(getSort().getProperty(), getSort().isAscending()));
                }
                return list.stream().skip(first).limit(count > 0 ? count : list.size()).iterator();
            }

            @Override
            public long size() {
                return extraAdministrationService.count();
            }

            @Override
            public IModel<ListExtraDto> model(ListExtraDto dto) {
                return Model.of(dto);
            }
        };
        provider.setSort("naam", SortOrder.ASCENDING);

        Form<Void> form = new Form<>("actionsForm");
        form.add(new RdDataTable<>("extrasTable", columns, provider, 20));
        return form;
    }

    private static Comparator<ListExtraDto> comparatorFor(String property, boolean ascending) {
        Comparator<ListExtraDto> comparator = switch (property) {
            case "type" -> Comparator.comparing(dto -> dto.type().getLabel(), Comparator.nullsLast(Comparator.naturalOrder()));
            case "prijs" -> Comparator.comparing(ListExtraDto::prijs, Comparator.nullsLast(Comparator.naturalOrder()));
            case "startdatum" -> Comparator.comparing(ListExtraDto::startdatum, Comparator.nullsLast(Comparator.naturalOrder()));
            case "einddatum" -> Comparator.comparing(ListExtraDto::einddatum, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(ListExtraDto::naam, Comparator.nullsLast(Comparator.naturalOrder()));
        };
        return ascending ? comparator : comparator.reversed();
    }

    private final class ActiesFragment extends Fragment {

        ActiesFragment(String id, IModel<ListExtraDto> model) {
            super(id, "actiesFragment", ExtraAdministrationPage.this, model);

            ListExtraDto dto = model.getObject();
            PageParameters params = new PageParameters().add("id", dto.id());

            add(new BookmarkablePageLink<>("bewerkLink", ExtraUpdatePage.class, params));

            add(new Link<>("verwijderLink", model) {
                @Override
                public void onClick() {
                    try {
                        extraAdministrationService.delete(getModelObject().id());
                    } catch (DataIntegrityViolationException e) {
                        getPage().error(getString("extra.verwijder.fout.ingebruik"));
                        return;
                    }
                    setResponsePage(ExtraAdministrationPage.class);
                }
            });
        }
    }
}
