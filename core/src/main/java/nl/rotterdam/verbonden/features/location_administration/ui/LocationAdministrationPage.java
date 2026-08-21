package nl.rotterdam.verbonden.features.location_administration.ui;

import nl.rotterdam.verbonden.administration_common.AdministrationBasePage;
import nl.rotterdam.verbonden.features.location_administration.application.LocationAdministrationService;
import nl.rotterdam.verbonden.features.location_administration.domain.ListLocatieDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LocationAdministrationPage extends AdministrationBasePage {

    @SpringBean
    private LocationAdministrationService locationAdministrationService;

    public LocationAdministrationPage() {
        pageBody.add(new BookmarkablePageLink<>("nieuweLocatieLink", LocationCreatePage.class));
        pageBody.add(buildLocatieTable());
    }

    private Component buildLocatieTable() {
        List<IColumn<ListLocatieDto, String>> columns = new ArrayList<>();

        columns.add(new AbstractColumn<>(Model.of("Naam"), "naam") {
            @Override
            public void populateItem(Item<ICellPopulator<ListLocatieDto>> cellItem,
                                     String componentId,
                                     IModel<ListLocatieDto> rowModel) {
                cellItem.add(new Label(componentId, rowModel.map(ListLocatieDto::naam)));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Acties")) {
            @Override
            public void populateItem(Item<ICellPopulator<ListLocatieDto>> cellItem,
                                     String componentId,
                                     IModel<ListLocatieDto> rowModel) {
                cellItem.add(new ActiesFragment(componentId, rowModel));
            }
        });

        SortableDataProvider<ListLocatieDto, String> provider = new SortableDataProvider<>() {
            @Override
            public Iterator<? extends ListLocatieDto> iterator(long first, long count) {
                boolean ascending = getSort() == null || getSort().isAscending();
                String sortProperty = getSort() != null ? getSort().getProperty() : "naam";
                Sort.Direction direction = ascending ? Sort.Direction.ASC : Sort.Direction.DESC;
                int pageSize = count > 0 ? (int) count : 20;
                int pageNumber = pageSize > 0 ? (int) (first / pageSize) : 0;
                PageRequest pageRequest = PageRequest.of(pageNumber, pageSize,
                        Sort.by(direction, sortProperty));
                Page<ListLocatieDto> page = locationAdministrationService.findAll(pageRequest);
                return page.iterator();
            }

            @Override
            public long size() {
                return locationAdministrationService.count();
            }

            @Override
            public IModel<ListLocatieDto> model(ListLocatieDto dto) {
                return Model.of(dto);
            }
        };
        provider.setSort("naam", SortOrder.ASCENDING);

        return new Form<Void>("actionsForm").add(new RdDataTable<>("locatieTable", columns, provider, 20));
    }

    private final class ActiesFragment extends Fragment {

        ActiesFragment(String id, IModel<ListLocatieDto> dtoModel) {
            super(id, "actiesFragment", LocationAdministrationPage.this, dtoModel);

            ListLocatieDto dto = dtoModel.getObject();
            PageParameters params = new PageParameters();
            params.add("id", dto.id());

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

                            ListLocatieDto dto = dtoModel.getObject();
                            PageParameters params = new PageParameters()
                                    .add("id", dto.id());

                            setResponsePage(LocationUpdatePage.class, params);
                        }
                    },

                    new RdIconAjaxButtonBorder("verwijderLink", Model.of("Verwijderen")) {
                        @Override
                        protected void onSubmit(AjaxRequestTarget target) {
                            locationAdministrationService.delete(dtoModel.getObject().id());
                            setResponsePage(LocationAdministrationPage.class);
                        }


                        @Override
                        protected void onInitialize() {
                            super.onInitialize();
                            add(new WebMarkupContainer("icon")
                                    .add(RotterdamIconBehavior.TRASH));
                        }
                    }.add(AttributeModifier.replace("aria-label", Model.of("Verwijderen")))
            );
        }
    }
}
