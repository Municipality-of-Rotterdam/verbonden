package nl.rotterdam.verbonden.core.features.betaling_administration.ui;

import nl.rotterdam.nl_design_system.wicket.components.button.RdAjaxButton;
import nl.rotterdam.nl_design_system.wicket.components.table.RdDataTable;
import nl.rotterdam.verbonden.core.administration_common.AdministrationBasePage;
import nl.rotterdam.verbonden.core.domain.Bedrag;
import nl.rotterdam.verbonden.core.features.betaling_administration.application.BetalingAdministrationService;
import nl.rotterdam.verbonden.core.features.betaling_administration.domain.ListBetalingDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.ui.DossierDetailPage;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetalingStatus;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.danekja.java.util.function.serializable.SerializableFunction;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Alle online betalingen, nieuwste eerst, om de status te kunnen inzien. Crediteren valt buiten de applicatie.
 */
public class BetalingAdministrationPage extends AdministrationBasePage {

    private static final DateTimeFormatter DATUM_TIJD = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final int REGELS_PER_PAGINA = 20;

    @SpringBean
    private BetalingAdministrationService betalingAdministrationService;

    private final Model<String> zoektermModel = Model.of("");

    public BetalingAdministrationPage() {
        RdDataTable<ListBetalingDto, String> tabel = new RdDataTable<>("betalingTable", kolommen(),
                new BetalingenProvider(), REGELS_PER_PAGINA);
        tabel.setOutputMarkupId(true);

        Form<Void> zoekForm = new Form<>("zoekForm");
        zoekForm.add(
                new TextField<>("zoektermVeld", zoektermModel),
                new RdAjaxButton("zoekButton") {
                    @Override
                    protected void onSubmit(AjaxRequestTarget target) {
                        tabel.setCurrentPage(0);
                        target.add(tabel);
                    }
                });

        pageBody.add(zoekForm, tabel);
    }

    /** De status zoals een medewerker die leest. */
    public static String statusLabel(BetalingStatus status) {
        return switch (status) {
            case GESTART -> "Wacht op uitslag";
            case GELUKT -> "Gelukt";
            case MISLUKT -> "Mislukt of afgebroken";
        };
    }

    private List<IColumn<ListBetalingDto, String>> kolommen() {
        List<IColumn<ListBetalingDto, String>> kolommen = new ArrayList<>();
        kolommen.add(tekstKolom("Gestart op", betaling -> datumTijd(betaling.gestartOp())));
        kolommen.add(new AbstractColumn<>(Model.of("Dossier ID")) {
            @Override
            public void populateItem(Item<ICellPopulator<ListBetalingDto>> cellItem, String componentId,
                                     IModel<ListBetalingDto> rowModel) {
                cellItem.add(new DossierLinkFragment(componentId, rowModel));
            }
        });
        kolommen.add(tekstKolom("Waarvoor", ListBetalingDto::waarvoor));
        kolommen.add(tekstKolom("Bedrag", betaling -> bedrag(betaling.bedrag())));
        kolommen.add(tekstKolom("Status", betaling -> statusLabel(betaling.status())));
        kolommen.add(tekstKolom("Afgerond op", betaling -> datumTijd(betaling.afgerondOp())));
        kolommen.add(tekstKolom("Betalingskenmerk", ListBetalingDto::betalingskenmerk));
        kolommen.add(tekstKolom("Kenmerk betaalprovider",
                betaling -> betaling.externeReferentie() != null ? betaling.externeReferentie() : "-"));
        return kolommen;
    }

    /** Met een {@link SerializableFunction}: de kolom wordt met de pagina geserialiseerd (pagestore). */
    private static IColumn<ListBetalingDto, String> tekstKolom(String titel,
                                                              SerializableFunction<ListBetalingDto, String> waarde) {
        return new AbstractColumn<>(Model.of(titel)) {
            @Override
            public void populateItem(Item<ICellPopulator<ListBetalingDto>> cellItem, String componentId,
                                     IModel<ListBetalingDto> rowModel) {
                cellItem.add(new Label(componentId, rowModel.map(waarde)));
            }
        };
    }

    private static String datumTijd(LocalDateTime datumTijd) {
        return datumTijd != null ? datumTijd.format(DATUM_TIJD) : "-";
    }

    private static String bedrag(Bedrag bedrag) {
        return "€ " + bedrag.format();
    }

    private class DossierLinkFragment extends Fragment {

        DossierLinkFragment(String id, IModel<ListBetalingDto> rowModel) {
            super(id, "dossierLinkFragment", BetalingAdministrationPage.this);
            BookmarkablePageLink<Void> link = new BookmarkablePageLink<>("link", DossierDetailPage.class,
                    new PageParameters().set("dossierId", rowModel.getObject().dossierId().toString()));
            link.add(new Label("dossierId", rowModel.map(betaling -> betaling.dossierId().toString())));
            add(link);
        }
    }

    private class BetalingenProvider extends SortableDataProvider<ListBetalingDto, String> {

        @Override
        public Iterator<? extends ListBetalingDto> iterator(long first, long count) {
            int pageSize = count > 0 ? (int) count : REGELS_PER_PAGINA;
            return betalingAdministrationService
                    .search(zoektermModel.getObject(), PageRequest.of((int) (first / pageSize), pageSize))
                    .iterator();
        }

        @Override
        public long size() {
            return betalingAdministrationService.count(zoektermModel.getObject());
        }

        @Override
        public IModel<ListBetalingDto> model(ListBetalingDto betaling) {
            return Model.of(betaling);
        }
    }
}
