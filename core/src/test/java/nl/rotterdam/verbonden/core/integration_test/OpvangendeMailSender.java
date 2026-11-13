package nl.rotterdam.verbonden.core.integration_test;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.SendFailedException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Een mailserver voor tests: verstuurt niets, maar bewaart de berichten, en kan een storing of een afgewezen adres
 * nabootsen. Roep
 * {@link #reset} aan vóór elke test, want de Spring-context wordt gedeeld tussen testklassen.
 */
public class OpvangendeMailSender extends JavaMailSenderImpl {

    private final List<MimeMessage> berichten = new ArrayList<>();
    private final Queue<RuntimeException> fouten = new LinkedList<>();
    private final Set<String> geweigerdeAdressen = new HashSet<>();
    private RuntimeException altijdFout;

    public List<MimeMessage> berichten() {
        return berichten;
    }

    public void reset() {
        berichten.clear();
        fouten.clear();
        geweigerdeAdressen.clear();
        altijdFout = null;
    }

    public void faalAltijdMet(RuntimeException fout) {
        altijdFout = fout;
    }

    public void faalEenKeerMet(RuntimeException fout) {
        fouten.add(fout);
    }

    /**
     * De mailserver wijst dit adres definitief af (bijvoorbeeld {@code 550 user unknown}). De fout komt door zoals
     * {@link JavaMailSenderImpl} hem doorgeeft: een {@link MailSendException} per bericht, zonder oorzaak.
     */
    public void weigerAdres(String adres) {
        geweigerdeAdressen.add(adres);
    }

    @Override
    protected void doSend(MimeMessage[] mimeMessages, Object[] originalMessages) {
        for (MimeMessage bericht : mimeMessages) {
            for (Address ontvanger : ontvangers(bericht)) {
                if (geweigerdeAdressen.contains(((InternetAddress) ontvanger).getAddress())) {
                    SendFailedException afwijzing = new SendFailedException("550 user unknown", null,
                            new Address[0], new Address[0], new Address[]{ontvanger});
                    throw new MailSendException(Map.of(bericht, afwijzing));
                }
            }
        }
        if (altijdFout != null) {
            throw altijdFout;
        }
        RuntimeException fout = fouten.poll();
        if (fout != null) {
            throw fout;
        }
        berichten.addAll(List.of(mimeMessages));
    }

    private static Address[] ontvangers(MimeMessage bericht) {
        try {
            Address[] ontvangers = bericht.getRecipients(Message.RecipientType.TO);
            return ontvangers != null ? ontvangers : new Address[0];
        } catch (MessagingException e) {
            throw new IllegalStateException(e);
        }
    }

    @TestConfiguration
    static class Config {

        @Bean
        OpvangendeMailSender opvangendeMailSender() {
            return new OpvangendeMailSender();
        }
    }
}
