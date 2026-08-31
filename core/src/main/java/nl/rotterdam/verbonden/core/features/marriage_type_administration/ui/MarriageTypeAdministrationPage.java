package nl.rotterdam.verbonden.core.features.marriage_type_administration.ui;

import nl.rotterdam.verbonden.core.administration_common.AdministrationBasePage;
import nl.rotterdam.verbonden.core.features.marriage_type_administration.application.MarriageTypeAdministrationService;
import nl.rotterdam.verbonden.core.features.marriage_type_administration.domain.ListMarriageTypeDto;
import nl.rotterdam.nl_design_system.rotterdam_extensions.wicket.components.rotterdam_icon.RotterdamIconBehavior;
import nl.rotterdam.nl_design_system.wicket.components.icon_button.RdIconAjaxButtonBorder;
import nl.rotterdam.nl_design_system.wicket.components.table.RdDataTable;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class MarriageTypeAdministrationPage extends AdministrationBasePage {

    @SpringBean
    private MarriageTypeAdministrationService marriageTypeAdministrationService;

    public MarriageTypeAdministrationPage() {
        pageBody.add(new BookmarkablePageLink<>("nieuwHuwelijkstypeLink", MarriageTypeCreatePage.class));
        pageBody.add(buildHuwelijkstypeTable());
    }

    private Component buildHuwelijkstypeTable() {
        List<IColumn<ListMarriageTypeDto, String>> columns = new ArrayList<>();

        columns.add(new AbstractColumn<>(Model.of("Soort"), "soort") {
            @Override
            public void populateItem(Item<ICellPopulator<ListMarriageTypeDto>> cellItem,
                                     String componentId,
                                     IModel<ListMarriageTypeDto> rowModel) {
                cellItem.add(new Label(componentId,
                        rowModel.map(dto -> dto.soort().getLabel())));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Titel"), "titel") {
            @Override
            public void populateItem(Item<ICellPopulator<ListMarriageTypeDto>> cellItem,
                                     String componentId,
                                     IModel<ListMarriageTypeDto> rowModel) {
                cellItem.add(new Label(componentId, rowModel.map(ListMarriageTypeDto::titel)));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Prijs"), "prijs") {
            @Override
            public void populateItem(Item<ICellPopulator<ListMarriageTypeDto>> cellItem,
                                     String componentId,
                                     IModel<ListMarriageTypeDto> rowModel) {
                cellItem.add(new Label(componentId,
                        rowModel.map(dto -> dto.prijs() != null ? dto.prijs().toPlainString() : "")));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Vaste locatie"), "locatieNaam") {
            @Override
            public void populateItem(Item<ICellPopulator<ListMarriageTypeDto>> cellItem,
                                     String componentId,
                                     IModel<ListMarriageTypeDto> rowModel) {
                cellItem.add(new Label(componentId,
                        rowModel.map(dto -> dto.locatieNaam() != null ? dto.locatieNaam() : "")));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Actief"), "active") {
            @Override
            public void populateItem(Item<ICellPopulator<ListMarriageTypeDto>> cellItem,
                                     String componentId,
                                     IModel<ListMarriageTypeDto> rowModel) {
                cellItem.add(new Label(componentId,
                        rowModel.map(dto -> dto.active() ? "Ja" : "Nee")));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Acties")) {
            @Override
            public void populateItem(Item<ICellPopulator<ListMarriageTypeDto>> cellItem,
                                     String componentId,
                                     IModel<ListMarriageTypeDto> rowModel) {
                cellItem.add(new ActiesFragment(componentId, rowModel));
            }
        });

        SortableDataProvider<ListMarriageTypeDto, String> provider = new SortableDataProvider<>() {
            private transient List<ListMarriageTypeDto> cachedList;

            private List<ListMarriageTypeDto> getList() {
                if (cachedList == null) {
                    cachedList = new ArrayList<>(marriageTypeAdministrationService.findAll());
                }
                return cachedList;
            }

            @Override
            public Iterator<? extends ListMarriageTypeDto> iterator(long first, long count) {
                List<ListMarriageTypeDto> list = new ArrayList<>(getList());
                if (getSort() != null) {
                    list.sort(comparatorFor(getSort().getProperty(), getSort().isAscending()));
                }
                return list.stream().skip(first).limit(count > 0 ? count : list.size()).iterator();
            }

            @Override
            public long size() {
                return getList().size();
            }

            @Override
            public IModel<ListMarriageTypeDto> model(ListMarriageTypeDto dto) {
                return Model.of(dto);
            }
        };
        provider.setSort("titel", SortOrder.ASCENDING);

        return new Form<Void>("actionsForm")
                .add(new RdDataTable<>("huwelijkstypeTable", columns, provider, 20));
    }

    private static Comparator<ListMarriageTypeDto> comparatorFor(String property, boolean ascending) {
        Comparator<ListMarriageTypeDto> comparator = switch (property) {
            case "soort" -> Comparator.comparing(
                    dto -> dto.soort().getLabel(), Comparator.nullsLast(Comparator.naturalOrder()));
            case "prijs" -> Comparator.comparing(
                    ListMarriageTypeDto::prijs, Comparator.nullsLast(Comparator.naturalOrder()));
            case "locatieNaam" -> Comparator.comparing(
                    ListMarriageTypeDto::locatieNaam, Comparator.nullsLast(Comparator.naturalOrder()));
            case "active" -> Comparator.comparing(ListMarriageTypeDto::active);
            default -> Comparator.comparing(
                    ListMarriageTypeDto::titel, Comparator.nullsLast(Comparator.naturalOrder()));
        };
        return ascending ? comparator : comparator.reversed();
    }

    private final class ActiesFragment extends Fragment {

        ActiesFragment(String id, IModel<ListMarriageTypeDto> dtoModel) {
            super(id, "actiesFragment", MarriageTypeAdministrationPage.this, dtoModel);

            add(
                    new RdIconAjaxButtonBorder("bewerkLink", Model.of("Wijzigen")) {
                        @Override
                        protected void onInitialize() {
                            super.onInitialize();
                            add(new WebMarkupContainer("icon")
                                    .add(RotterdamIconBehavior.EDIT));
                        }

                        @Override
                        protected void onSubmit(AjaxRequestTarget target) {
                            ListMarriageTypeDto d = dtoModel.getObject();
                            PageParameters p = new PageParameters().add("id", d.id());
                            setResponsePage(MarriageTypeUpdatePage.class, p);
                        }
                    },

                    new RdIconAjaxButtonBorder("verwijderLink", Model.of("Verwijderen")) {
                        @Override
                        protected void onInitialize() {
                            super.onInitialize();
                            add(new WebMarkupContainer("icon")
                                    .add(RotterdamIconBehavior.TRASH));
                        }

                        @Override
                        protected void onSubmit(AjaxRequestTarget target) {
                            marriageTypeAdministrationService.delete(dtoModel.getObject().id());
                            setResponsePage(MarriageTypeAdministrationPage.class);
                        }
                    }.add(AttributeModifier.replace("aria-label", Model.of("Verwijderen")))
            );
        }
    }
}
