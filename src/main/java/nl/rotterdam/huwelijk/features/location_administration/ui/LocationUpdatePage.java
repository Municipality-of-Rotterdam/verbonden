package nl.rotterdam.huwelijk.features.location_administration.ui;

import nl.rotterdam.huwelijk.administration_common.AdministrationBasePage;
import nl.rotterdam.huwelijk.features.location_administration.application.LocationAdministrationService;
import nl.rotterdam.huwelijk.features.location_administration.domain.ChangeLocatieDto;
import nl.rotterdam.huwelijk.features.location_administration.domain.ListBeschikbaarheidDto;
import nl.rotterdam.huwelijk.features.location_administration.domain.ListNietBeschikbareDagDto;
import nl.rotterdam.nl_design_system.wicket.components.button.RdButton;
import nl.rotterdam.nl_design_system.wicket.components.form_field_textarea.RdFormFieldTextArea;
import nl.rotterdam.nl_design_system.wicket.components.form_field_text_input.RdFormFieldTextInput;
import nl.rotterdam.nl_design_system.wicket.components.table.RdDataTable;
import org.apache.wicket.Session;
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
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class LocationUpdatePage extends AdministrationBasePage {

    @SpringBean
    private LocationAdministrationService locationAdministrationService;

    public LocationUpdatePage(PageParameters params) {
        Long id = params.get("id").toOptionalLong();
        if (id == null) {
            setResponsePage(LocationAdministrationPage.class);
            return;
        }
        ChangeLocatieDto dto = locationAdministrationService.findById(id).orElse(null);
        if (dto == null) {
            setResponsePage(LocationAdministrationPage.class);
            return;
        }

        FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);

        PageParameters nieuwBeschikbaarheidParams = new PageParameters();
        nieuwBeschikbaarheidParams.add("locatieId", id);

        PageParameters nieuwNietBeschikbareDagParams = new PageParameters();
        nieuwNietBeschikbareDagParams.add("locatieId", id);

        PageParameters importNietBeschikbareDagenParams = new PageParameters();
        importNietBeschikbareDagenParams.add("locatieId", id);

        pageBody.add(
                new BookmarkablePageLink<>("terugLink", LocationAdministrationPage.class),
                feedback,
                new ChangeLocatieForm("locatieForm", dto),
                new BookmarkablePageLink<>("nieuwBeschikbaarheidLink",
                        BeschikbaarheidCreatePage.class, nieuwBeschikbaarheidParams),
                buildBeschikbaarheidTable(id),
                new BookmarkablePageLink<>("nieuwNietBeschikbareDagLink",
                        NietBeschikbareDagCreatePage.class, nieuwNietBeschikbareDagParams),
                new BookmarkablePageLink<>("importNietBeschikbareDagenLink",
                        NietBeschikbareDagImportPage.class, importNietBeschikbareDagenParams),
                buildNietBeschikbareDagenTable(id)
        );
    }

    private RdDataTable<ListBeschikbaarheidDto, String> buildBeschikbaarheidTable(long locatieId) {
        List<IColumn<ListBeschikbaarheidDto, String>> columns = new ArrayList<>();

        columns.add(new AbstractColumn<>(Model.of("Dag"), "dagVanDeWeek") {
            @Override
            public void populateItem(Item<ICellPopulator<ListBeschikbaarheidDto>> cellItem,
                                     String componentId, IModel<ListBeschikbaarheidDto> rowModel) {
                DayOfWeek dag = rowModel.getObject().dagVanDeWeek();
                String label = dag != null ? dag.getDisplayName(TextStyle.FULL,
                        Session.get().getLocale()) : "";
                cellItem.add(new Label(componentId, Model.of(label)));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Start"), "startTijd") {
            @Override
            public void populateItem(Item<ICellPopulator<ListBeschikbaarheidDto>> cellItem,
                                     String componentId, IModel<ListBeschikbaarheidDto> rowModel) {
                LocalTime t = rowModel.getObject().startTijd();
                cellItem.add(new Label(componentId, Model.of(t != null ? t.toString() : "")));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Eind"), "eindTijd") {
            @Override
            public void populateItem(Item<ICellPopulator<ListBeschikbaarheidDto>> cellItem,
                                     String componentId, IModel<ListBeschikbaarheidDto> rowModel) {
                LocalTime t = rowModel.getObject().eindTijd();
                cellItem.add(new Label(componentId, Model.of(t != null ? t.toString() : "")));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Huwelijkstype"), "huwelijkstype") {
            @Override
            public void populateItem(Item<ICellPopulator<ListBeschikbaarheidDto>> cellItem,
                                     String componentId, IModel<ListBeschikbaarheidDto> rowModel) {
                cellItem.add(new Label(componentId,
                        rowModel.map(b -> b.huwelijkstype() != null ? b.huwelijkstype().name() : "")));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Duur (min)"), "duurInMinuten") {
            @Override
            public void populateItem(Item<ICellPopulator<ListBeschikbaarheidDto>> cellItem,
                                     String componentId, IModel<ListBeschikbaarheidDto> rowModel) {
                cellItem.add(new Label(componentId,
                        Model.of(String.valueOf(rowModel.getObject().duurInMinuten()))));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Prijs"), "prijs") {
            @Override
            public void populateItem(Item<ICellPopulator<ListBeschikbaarheidDto>> cellItem,
                                     String componentId, IModel<ListBeschikbaarheidDto> rowModel) {
                var prijs = rowModel.getObject().prijs();
                cellItem.add(new Label(componentId, Model.of(prijs != null ? "€ " + prijs : "")));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Ingangsdatum"), "ingangsdatum") {
            @Override
            public void populateItem(Item<ICellPopulator<ListBeschikbaarheidDto>> cellItem,
                                     String componentId, IModel<ListBeschikbaarheidDto> rowModel) {
                LocalDate d = rowModel.getObject().ingangsdatum();
                cellItem.add(new Label(componentId, Model.of(d != null ? d.toString() : "")));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Einddatum"), "einddatum") {
            @Override
            public void populateItem(Item<ICellPopulator<ListBeschikbaarheidDto>> cellItem,
                                     String componentId, IModel<ListBeschikbaarheidDto> rowModel) {
                LocalDate d = rowModel.getObject().einddatum();
                cellItem.add(new Label(componentId, Model.of(d != null ? d.toString() : "")));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Acties")) {
            @Override
            public void populateItem(Item<ICellPopulator<ListBeschikbaarheidDto>> cellItem,
                                     String componentId, IModel<ListBeschikbaarheidDto> rowModel) {
                cellItem.add(new BeschikbaarheidActiesFragment(componentId, rowModel, locatieId));
            }
        });

        SortableDataProvider<ListBeschikbaarheidDto, String> provider = new SortableDataProvider<>() {
            private transient List<ListBeschikbaarheidDto> cachedList;

            private List<ListBeschikbaarheidDto> getList() {
                if (cachedList == null) {
                    cachedList = new ArrayList<>(locationAdministrationService.findBeschikbaarheden(locatieId));
                }
                return cachedList;
            }

            @Override
            public Iterator<? extends ListBeschikbaarheidDto> iterator(long first, long count) {
                List<ListBeschikbaarheidDto> list = new ArrayList<>(getList());
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
            public IModel<ListBeschikbaarheidDto> model(ListBeschikbaarheidDto dto) {
                return Model.of(dto);
            }
        };
        provider.setSort("dagVanDeWeek", SortOrder.ASCENDING);

        return new RdDataTable<>("beschikbaarheidTable", columns, provider, 100);
    }

    private static Comparator<ListBeschikbaarheidDto> comparatorFor(String property, boolean ascending) {
        Comparator<ListBeschikbaarheidDto> comparator = switch (property) {
            case "dagVanDeWeek" -> Comparator.comparing(ListBeschikbaarheidDto::dagVanDeWeek,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "startTijd" -> Comparator.comparing(ListBeschikbaarheidDto::startTijd,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "eindTijd" -> Comparator.comparing(ListBeschikbaarheidDto::eindTijd,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "huwelijkstype" -> Comparator.comparing(ListBeschikbaarheidDto::huwelijkstype,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "duurInMinuten" -> Comparator.comparingInt(ListBeschikbaarheidDto::duurInMinuten);
            case "prijs" -> Comparator.comparing(ListBeschikbaarheidDto::prijs,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "ingangsdatum" -> Comparator.comparing(ListBeschikbaarheidDto::ingangsdatum,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "einddatum" -> Comparator.comparing(ListBeschikbaarheidDto::einddatum,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(ListBeschikbaarheidDto::dagVanDeWeek,
                    Comparator.nullsLast(Comparator.naturalOrder()));
        };
        return ascending ? comparator : comparator.reversed();
    }

    private final class BeschikbaarheidActiesFragment extends Fragment {

        BeschikbaarheidActiesFragment(String id, IModel<ListBeschikbaarheidDto> model, long locatieId) {
            super(id, "beschikbaarheidActiesFragment", LocationUpdatePage.this, model);

            ListBeschikbaarheidDto b = model.getObject();
            PageParameters bewerkParams = new PageParameters();
            bewerkParams.add("locatieId", locatieId);
            bewerkParams.add("id", b.id());
            add(new BookmarkablePageLink<>("bewerkBeschikbaarheidLink",
                    BeschikbaarheidUpdatePage.class, bewerkParams));

            add(new Link<>("verwijderBeschikbaarheidLink", model) {
                @Override
                public void onClick() {
                    locationAdministrationService.deleteBeschikbaarheid(getModelObject().id());
                    PageParameters params = new PageParameters();
                    params.add("id", locatieId);
                    setResponsePage(LocationUpdatePage.class, params);
                }
            });
        }
    }

    private RdDataTable<ListNietBeschikbareDagDto, String> buildNietBeschikbareDagenTable(long locatieId) {
        List<IColumn<ListNietBeschikbareDagDto, String>> columns = new ArrayList<>();

        columns.add(new AbstractColumn<>(Model.of("Datum"), "datum") {
            @Override
            public void populateItem(Item<ICellPopulator<ListNietBeschikbareDagDto>> cellItem,
                                     String componentId, IModel<ListNietBeschikbareDagDto> rowModel) {
                LocalDate d = rowModel.getObject().datum();
                cellItem.add(new Label(componentId, Model.of(d != null ? d.toString() : "")));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Reden"), "reden") {
            @Override
            public void populateItem(Item<ICellPopulator<ListNietBeschikbareDagDto>> cellItem,
                                     String componentId, IModel<ListNietBeschikbareDagDto> rowModel) {
                cellItem.add(new Label(componentId, rowModel.map(ListNietBeschikbareDagDto::reden)));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Acties")) {
            @Override
            public void populateItem(Item<ICellPopulator<ListNietBeschikbareDagDto>> cellItem,
                                     String componentId, IModel<ListNietBeschikbareDagDto> rowModel) {
                cellItem.add(new NietBeschikbareDagActiesFragment(componentId, rowModel, locatieId));
            }
        });

        SortableDataProvider<ListNietBeschikbareDagDto, String> provider = new SortableDataProvider<>() {
            private transient List<ListNietBeschikbareDagDto> cachedList;

            private List<ListNietBeschikbareDagDto> getList() {
                if (cachedList == null) {
                    cachedList = new ArrayList<>(locationAdministrationService.findNietBeschikbareDagen(locatieId));
                }
                return cachedList;
            }

            @Override
            public Iterator<? extends ListNietBeschikbareDagDto> iterator(long first, long count) {
                List<ListNietBeschikbareDagDto> list = new ArrayList<>(getList());
                if (getSort() != null && "datum".equals(getSort().getProperty())) {
                    Comparator<ListNietBeschikbareDagDto> comparator = Comparator.comparing(
                            ListNietBeschikbareDagDto::datum, Comparator.nullsLast(Comparator.naturalOrder()));
                    list.sort(getSort().isAscending() ? comparator : comparator.reversed());
                }
                return list.stream().skip(first).limit(count > 0 ? count : list.size()).iterator();
            }

            @Override
            public long size() {
                return getList().size();
            }

            @Override
            public IModel<ListNietBeschikbareDagDto> model(ListNietBeschikbareDagDto dto) {
                return Model.of(dto);
            }
        };
        provider.setSort("datum", SortOrder.ASCENDING);

        return new RdDataTable<>("nietBeschikbareDagenTable", columns, provider, 100);
    }

    private final class NietBeschikbareDagActiesFragment extends Fragment {

        NietBeschikbareDagActiesFragment(String id, IModel<ListNietBeschikbareDagDto> model, long locatieId) {
            super(id, "nietBeschikbareDagActiesFragment", LocationUpdatePage.this, model);

            ListNietBeschikbareDagDto dag = model.getObject();
            PageParameters bewerkParams = new PageParameters();
            bewerkParams.add("locatieId", locatieId);
            bewerkParams.add("id", dag.id());
            add(new BookmarkablePageLink<>("bewerkNietBeschikbareDagLink",
                    NietBeschikbareDagUpdatePage.class, bewerkParams));

            add(new Link<>("verwijderNietBeschikbareDagLink", model) {
                @Override
                public void onClick() {
                    locationAdministrationService.deleteNietBeschikbareDag(getModelObject().id());
                    PageParameters params = new PageParameters();
                    params.add("id", locatieId);
                    setResponsePage(LocationUpdatePage.class, params);
                }
            });
        }
    }

    private class ChangeLocatieForm extends Form<LocatieFormDto> {

        private final long locatieId;

        ChangeLocatieForm(String id, ChangeLocatieDto dto) {
            super(id, Model.of(LocatieFormDto.vanDto(dto)));
            locatieId = dto.id();
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            IModel<LocatieFormDto> model = getModel();
            add(
                    new RdFormFieldTextInput<>("naam",
                            LambdaModel.of(model, LocatieFormDto::getNaam, LocatieFormDto::setNaam),
                            Model.of("Naam")).setRequired(true),
                    new RdFormFieldTextInput<>("fotoUrl",
                            LambdaModel.of(model, LocatieFormDto::getFotoUrl, LocatieFormDto::setFotoUrl),
                            Model.of("Foto URL"),
                            Model.of("URL naar de foto van de trouwlocatie")),
                    new RdFormFieldTextArea<>("omschrijving",
                            LambdaModel.of(model, LocatieFormDto::getOmschrijving, LocatieFormDto::setOmschrijving),
                            Model.of("Omschrijving"),
                            Model.of("Beschrijving van de trouwlocatie (HTML toegestaan)")),
                    new RdFormFieldTextInput<>("detailUrl",
                            LambdaModel.of(model, LocatieFormDto::getDetailUrl, LocatieFormDto::setDetailUrl),
                            Model.of("Detail URL"),
                            Model.of("URL naar de detailpagina van de trouwlocatie")),
                    new RdButton("opslaan", Model.of("Opslaan"))
            );
        }

        @Override
        protected void onSubmit() {
            LocatieFormDto f = getModelObject();
            locationAdministrationService.update(new ChangeLocatieDto(
                    locatieId,
                    f.getNaam(),
                    f.getFotoUrl(),
                    f.getOmschrijving(),
                    f.getDetailUrl()
            ));
            PageParameters params = new PageParameters();
            params.add("id", locatieId);
            setResponsePage(LocationUpdatePage.class, params);
        }
    }
}
