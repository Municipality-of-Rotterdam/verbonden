package nl.rotterdam.verbonden.core.config;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.List;
import java.util.Properties;

/**
 * Standaard-timeouts voor de verbinding met de mailserver. Zonder timeout wacht JavaMail eindeloos op een server die
 * niet antwoordt; dat houdt de thread van de mailverwerker (en daarmee andere {@code @Scheduled}-taken) bezet.
 * <p>
 * Elke timeout is 10 seconden, tenzij hij is ingesteld met {@code spring.mail.properties.mail.smtp.*}
 * (bijvoorbeeld {@code spring.mail.properties.mail.smtp.timeout=5000}).
 */
@Configuration
public class SmtpTimeoutConfig {

    private static final String STANDAARD_TIMEOUT_MS = "10000";
    private static final List<String> TIMEOUTS = List.of("connectiontimeout", "timeout", "writetimeout");

    @Bean
    static BeanPostProcessor smtpTimeouts() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) {
                if (bean instanceof JavaMailSenderImpl sender) {
                    Properties instellingen = sender.getJavaMailProperties();
                    for (String protocol : List.of("smtp", "smtps")) {
                        for (String timeout : TIMEOUTS) {
                            instellingen.putIfAbsent("mail." + protocol + "." + timeout, STANDAARD_TIMEOUT_MS);
                        }
                    }
                }
                return bean;
            }
        };
    }
}
