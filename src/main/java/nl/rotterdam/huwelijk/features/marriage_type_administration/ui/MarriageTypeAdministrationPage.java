package nl.rotterdam.huwelijk.features.marriage_type_administration.ui;

import nl.rotterdam.huwelijk.administration_common.AdministrationBasePage;
import nl.rotterdam.huwelijk.features.marriage_type_administration.application.MarriageTypeAdministrationService;
import nl.rotterdam.huwelijk.features.marriage_type_administration.domain.ListMarriageTypeDto;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.List;

public class MarriageTypeAdministrationPage extends AdministrationBasePage {

    @SpringBean
    private MarriageTypeAdministrationService marriageTypeAdministrationService;

    public MarriageTypeAdministrationPage() {
        pageBody.add(new BookmarkablePageLink<>("nieuwHuwelijkstypeLink", MarriageTypeCreatePage.class));

        pageBody.add(new ListView<>("huwelijkstypen",
                new LoadableDetachableModel<List<ListMarriageTypeDto>>() {
                    @Override
                    protected List<ListMarriageTypeDto> load() {
                        return marriageTypeAdministrationService.findAll();
                    }
                }) {
            @Override
            protected void populateItem(ListItem<ListMarriageTypeDto> item) {
                ListMarriageTypeDto dto = item.getModelObject();

                PageParameters params = new PageParameters();
                params.add("id", dto.id());

                item.add(new Label("soort", dto.soort().name()));
                item.add(new Label("titel", dto.titel()));
                item.add(new Label("prijs", dto.prijs() != null ? dto.prijs().toPlainString() : ""));
                item.add(new BookmarkablePageLink<>("bewerkLink",
                        MarriageTypeUpdatePage.class, params));
                item.add(new Link<>("verwijderLink", item.getModel()) {
                    @Override
                    public void onClick() {
                        marriageTypeAdministrationService.delete(getModelObject().id());
                        setResponsePage(MarriageTypeAdministrationPage.class);
                    }
                });
            }
        });
    }
}
