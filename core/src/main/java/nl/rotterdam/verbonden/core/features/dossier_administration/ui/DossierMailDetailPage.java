package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DossierMailDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DossierMailPogingDto;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValueConversionException;

import java.util.UUID;

import static nl.rotterdam.verbonden.core.features.dossier_administration.ui.DossierDetailPage.datumTijd;

/**
 * Eén e-mail van een dossier: de gegevens, elke verzendpoging en de inhoud zoals die is verstuurd.
 */
public class DossierMailDetailPage extends DossierBeheerBasePage {

    static final String MAIL_ID = "mailId";

    public DossierMailDetailPage(PageParameters params) {
        super(params);
        DossierMailDto mail = laadMail(params);

        pageBody.add(
                new Label("titel", "E-mail - dossier " + dossierId),
                new BookmarkablePageLink<>("terugLink", DossierDetailPage.class, parametersVoor(dossierId)),

                new Label("onderwerp", mail.onderwerp()),
                new Label("afzender", mail.afzender()),
                new Label("ontvangers", mail.ontvangers().isEmpty() ? "-" : String.join(", ", mail.ontvangers())),
                new Label("type", mail.type().getLabel()),
                new Label("aangemaakt", datumTijd(mail.aangemaaktOp())),
                new Label("status", mail.status().getLabel()),
                new Label("volgendePoging", datumTijd(mail.volgendePogingOp())),

                new ListView<>("pogingen", mail.pogingen()) {
                    @Override
                    protected void populateItem(ListItem<DossierMailPogingDto> item) {
                        DossierMailPogingDto poging = item.getModelObject();
                        item.add(
                                new Label("volgnummer", poging.volgnummer()),
                                new Label("tijdstip", datumTijd(poging.tijdstip())),
                                new Label("pogingStatus", poging.status().getLabel()),
                                new Label("foutmelding", poging.foutmelding() != null ? poging.foutmelding() : "")
                        );
                    }
                },
                new Label("geenPogingen", "Er is nog geen poging gedaan om deze e-mail te verzenden.")
                        .setVisible(mail.pogingen().isEmpty()),

                // sandbox zonder rechten: de inhoud kan geen scripts uitvoeren of de beheerpagina aanpassen
                new WebMarkupContainer("inhoud").add(AttributeModifier.replace("srcdoc", mail.inhoud()))
        );
    }

    static PageParameters parametersVoor(UUID dossierId, long mailId) {
        return DossierBeheerBasePage.parametersVoor(dossierId).set(MAIL_ID, mailId);
    }

    private DossierMailDto laadMail(PageParameters params) {
        try {
            return dossierAdministrationService.findMail(dossierId, params.get(MAIL_ID).toLong());
        } catch (IllegalArgumentException | StringValueConversionException e) {
            throw new RestartResponseException(DossierDetailPage.class, parametersVoor(dossierId));
        }
    }
}
