package nl.rotterdam.verbonden.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Configuratie voor het versturen van e-mail. De mailserver zelf stelt u in met de standaard Spring-instellingen
 * ({@code spring.mail.host}, {@code spring.mail.port}, {@code spring.mail.username}, ...).
 * <ul>
 *   <li>{@code verbonden.mail.afzender-adres} — het e-mailadres van de afzender.</li>
 *   <li>{@code verbonden.mail.afzender-naam} — de naam van de afzender.</li>
 *   <li>{@code verbonden.mail.max-pogingen} — hoe vaak het verzenden van één e-mail wordt geprobeerd, de eerste
 *       poging meegeteld (standaard 5).</li>
 *   <li>{@code verbonden.mail.wachttijd} — wachttijd na de eerste mislukte poging (standaard 1 minuut).</li>
 *   <li>{@code verbonden.mail.back-off-factor} — de wachttijd wordt na elke mislukte poging hiermee
 *       vermenigvuldigd (standaard 2).</li>
 *   <li>{@code verbonden.mail.max-wachttijd} — de wachttijd wordt nooit langer dan dit (standaard 1 uur).</li>
 *   <li>{@code verbonden.mail.verwerk-interval} — hoe vaak wordt gezocht naar e-mails die verstuurd moeten
 *       worden (standaard 15 seconden).</li>
 * </ul>
 */
@Component
@ConfigurationProperties(prefix = "verbonden.mail")
public class MailConfig {

    private String afzenderAdres = "noreply@rotterdam.nl";
    private String afzenderNaam = "Gemeente Rotterdam";
    private int maxPogingen = 5;
    private Duration wachttijd = Duration.ofMinutes(1);
    private double backOffFactor = 2.0;
    private Duration maxWachttijd = Duration.ofHours(1);
    private Duration verwerkInterval = Duration.ofSeconds(15);

    public String getAfzenderAdres() {
        return afzenderAdres;
    }

    public void setAfzenderAdres(String afzenderAdres) {
        this.afzenderAdres = afzenderAdres;
    }

    public String getAfzenderNaam() {
        return afzenderNaam;
    }

    public void setAfzenderNaam(String afzenderNaam) {
        this.afzenderNaam = afzenderNaam;
    }

    public int getMaxPogingen() {
        return maxPogingen;
    }

    public void setMaxPogingen(int maxPogingen) {
        this.maxPogingen = maxPogingen;
    }

    public Duration getWachttijd() {
        return wachttijd;
    }

    public void setWachttijd(Duration wachttijd) {
        this.wachttijd = wachttijd;
    }

    public double getBackOffFactor() {
        return backOffFactor;
    }

    public void setBackOffFactor(double backOffFactor) {
        this.backOffFactor = backOffFactor;
    }

    public Duration getMaxWachttijd() {
        return maxWachttijd;
    }

    public void setMaxWachttijd(Duration maxWachttijd) {
        this.maxWachttijd = maxWachttijd;
    }

    public Duration getVerwerkInterval() {
        return verwerkInterval;
    }

    public void setVerwerkInterval(Duration verwerkInterval) {
        this.verwerkInterval = verwerkInterval;
    }
}
