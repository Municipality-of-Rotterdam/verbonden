package nl.rotterdam.verbonden.core;

import org.apache.wicket.Session;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;

/**
 * Maakt de Wicket-sessie aan in plaats van {@code WebApplication#newSession}. Een adapter-module
 * kan zo een eigen {@code WebSession}-subklasse aanbieden (bijvoorbeeld een sessie die page-id's
 * en page locks in een gedeelde cache bijhoudt, voor meerdere nodes) zonder
 * {@link WicketApplication} aan te passen. Zonder zo'n bean gebruikt Wicket z'n standaardsessie.
 */
public interface WicketSessionFactory {

    Session newSession(Request request, Response response);
}
