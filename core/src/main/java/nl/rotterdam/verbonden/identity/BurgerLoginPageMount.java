package nl.rotterdam.verbonden.identity;

import org.apache.wicket.markup.html.WebPage;

/**
 * Een burger-inlogpagina die {@code WicketApplication} moet mounten: pad +
 * pagina-klasse. Vervangt de hardcoded {@code mountPage("/inloggen", ...)}-
 * regel, zodat een adapter-module zijn eigen inlogpagina('s) kan aanbieden
 * zonder {@code WicketApplication} aan te passen.
 */
public interface BurgerLoginPageMount {

    String getPath();

    Class<? extends WebPage> getPageClass();
}
