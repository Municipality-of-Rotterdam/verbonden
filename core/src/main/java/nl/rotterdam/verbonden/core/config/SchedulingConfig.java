package nl.rotterdam.verbonden.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Zet het uitvoeren van {@code @Scheduled}-methoden aan, bijvoorbeeld het verzenden van e-mails die in de wacht staan.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
