package nl.rotterdam.verbonden.core.identity;

import org.apache.wicket.markup.html.WebPage;

/**
 * Een pagina uit een adapter-module die {@code WicketApplication} moet mounten: pad + pagina-klasse. Bijvoorbeeld
 * de mock-betaalpagina in {@code remote-local}. Voor inlogpagina's is er {@link BurgerLoginPageMount}: die bepaalt
 * ook de security-configuratie.
 */
public interface AdapterPageMount {

    String getPath();

    Class<? extends WebPage> getPageClass();
}
