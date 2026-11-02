package nl.rotterdam.verbonden.core.integration_test;

import jakarta.mail.internet.MimeMessage;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Een mailserver voor tests: verstuurt niets, maar bewaart de berichten, en kan een storing nabootsen. Roep
 * {@link #reset} aan vóór elke test, want de Spring-context wordt gedeeld tussen testklassen.
 */
public class OpvangendeMailSender extends JavaMailSenderImpl {

    private final List<MimeMessage> berichten = new ArrayList<>();
    private final Queue<RuntimeException> fouten = new LinkedList<>();
    private RuntimeException altijdFout;

    public List<MimeMessage> berichten() {
        return berichten;
    }

    public void reset() {
        berichten.clear();
        fouten.clear();
        altijdFout = null;
    }

    public void faalAltijdMet(RuntimeException fout) {
        altijdFout = fout;
    }

    public void faalEenKeerMet(RuntimeException fout) {
        fouten.add(fout);
    }

    @Override
    protected void doSend(MimeMessage[] mimeMessages, Object[] originalMessages) {
        if (altijdFout != null) {
            throw altijdFout;
        }
        RuntimeException fout = fouten.poll();
        if (fout != null) {
            throw fout;
        }
        berichten.addAll(List.of(mimeMessages));
    }

    @TestConfiguration
    static class Config {

        @Bean
        OpvangendeMailSender opvangendeMailSender() {
            return new OpvangendeMailSender();
        }
    }
}
