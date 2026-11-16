package nl.rotterdam.verbonden.core.features.dossier_mail.application;

import jakarta.mail.Address;
import jakarta.mail.SendFailedException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import nl.rotterdam.verbonden.core.config.MailConfig;
import nl.rotterdam.verbonden.core.domain.Emailadres;
import nl.rotterdam.verbonden.core.features.dossier_mail.domain.DossierMailPogingStatus;
import nl.rotterdam.verbonden.core.features.dossier_mail.domain.DossierMailStatus;
import nl.rotterdam.verbonden.core.features.dossier_mail.domain.DossierMailType;
import nl.rotterdam.verbonden.core.features.dossier_mail.repository.DossierMailRepository;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.GetuigeDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.PartnerGegevensDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SidebarExtraItemDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.repository.DossierRepository;
import nl.rotterdam.verbonden.core.persistence.DossierMailEntity;
import nl.rotterdam.verbonden.core.persistence.HuwelijksDossierEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.util.HtmlUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
class DossierMailServiceImpl implements DossierMailService {

    private static final Logger LOG = LoggerFactory.getLogger(DossierMailServiceImpl.class);

    private static final DateTimeFormatter DATUM_FORMAT =
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("nl-NL"));
    private static final DateTimeFormatter TIJD_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private static final String BEVESTIGING_TEMPLATE = "bevestiging-indiening.html";
    private static final int MAX_FOUTMELDING_LENGTE = 1000;
    private static final Pattern TITEL = Pattern.compile("<title>(.*?)</title>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** Zolang een poging bezig is, pakt geen andere verwerker dezelfde e-mail op. */
    private static final Duration POGING_TIMEOUT = Duration.ofMinutes(5);

    private final DossierMailRepository dossierMailRepository;
    private final DossierRepository dossierRepository;
    private final MarriageIntakeService marriageIntakeService;
    private final ObjectProvider<JavaMailSender> mailSender;
    private final MailConfig mailConfig;
    private final Clock clock;
    private final TransactionTemplate transactionTemplate;
    private final MailTemplate bevestigingTemplate = MailTemplate.laad(BEVESTIGING_TEMPLATE);

    DossierMailServiceImpl(DossierMailRepository dossierMailRepository,
                           DossierRepository dossierRepository,
                           MarriageIntakeService marriageIntakeService,
                           ObjectProvider<JavaMailSender> mailSender,
                           MailConfig mailConfig,
                           Clock clock,
                           PlatformTransactionManager transactionManager) {
        this.dossierMailRepository = dossierMailRepository;
        this.dossierRepository = dossierRepository;
        this.marriageIntakeService = marriageIntakeService;
        this.mailSender = mailSender;
        this.mailConfig = mailConfig;
        this.clock = clock;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    @Transactional
    public void maakBevestigingsmail(UUID dossierId) {
        HuwelijksDossierEntity dossier = dossierRepository.findByUuid(dossierId)
                .orElseThrow(() -> new IllegalArgumentException("Dossier niet gevonden: " + dossierId));
        DossierSamenvattingDto samenvatting = marriageIntakeService.findByDossierId(dossierId);
        List<PartnerGegevensDto> partners = marriageIntakeService.findPartnerGegevens(dossierId);
        List<GetuigeDto> getuigen = marriageIntakeService.findGetuigen(dossierId);
        LocalDateTime nu = LocalDateTime.now(clock);

        // Een fout bij het opstellen mag het indienen niet tegenhouden: de e-mail wordt dan als mislukt vastgelegd.
        String onderwerp = DossierMailType.BEVESTIGING_INDIENING.getLabel();
        String html = "";
        String fout = null;
        try {
            html = bevestigingTemplate.render(bevestigingWaarden(samenvatting, partners, getuigen));
            onderwerp = titel(html);
        } catch (RuntimeException e) {
            LOG.error("Opstellen van de bevestigingsmail voor dossier {} is mislukt", dossierId, e);
            fout = "Opstellen van de e-mail is mislukt: " + foutmelding(e);
        }

        Set<String> ontvangers = partners.stream()
                .map(PartnerGegevensDto::emailadres)
                .filter(Objects::nonNull)
                .map(Emailadres::getValue)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (ontvangers.isEmpty()) {
            DossierMailEntity mail = new DossierMailEntity(dossier, DossierMailType.BEVESTIGING_INDIENING, afzender(),
                    null, onderwerp, html, nu);
            markeerMislukt(mail, nu, fout != null ? fout : "Geen van de partners heeft een e-mailadres.");
            dossierMailRepository.save(mail);
            return;
        }
        // Eén e-mail per ontvanger, zodat een afgewezen adres of een nieuwe poging de andere partner niet raakt.
        for (String ontvanger : ontvangers) {
            DossierMailEntity mail = new DossierMailEntity(dossier, DossierMailType.BEVESTIGING_INDIENING, afzender(),
                    ontvanger, onderwerp, html, nu);
            if (fout != null) {
                markeerMislukt(mail, nu, fout);
            }
            dossierMailRepository.save(mail);
        }
    }

    /** Het onderwerp van een e-mail is de {@code <title>} van de template. */
    private static String titel(String html) {
        Matcher titel = TITEL.matcher(html);
        if (!titel.find() || titel.group(1).isBlank()) {
            throw new IllegalStateException("De e-mailtemplate heeft geen <title>");
        }
        return HtmlUtils.htmlUnescape(titel.group(1).strip());
    }

    private static void markeerMislukt(DossierMailEntity mail, LocalDateTime nu, String foutmelding) {
        mail.voegPogingToe(nu, DossierMailPogingStatus.MISLUKT, foutmelding);
        mail.setStatus(DossierMailStatus.MISLUKT);
        mail.setVolgendePogingOp(null);
    }

    @Override
    public int verwerkOpenstaandeMails() {
        List<Long> ids = transactionTemplate.execute(status ->
                dossierMailRepository.findIdsKlaarVoorVerzending(DossierMailStatus.IN_WACHT, LocalDateTime.now(clock)));
        int verwerkt = 0;
        for (long id : Objects.requireNonNull(ids)) {
            try {
                if (verwerk(id)) {
                    verwerkt++;
                }
            } catch (RuntimeException e) {
                // Eén e-mail die misgaat, mag de rest van de e-mails niet tegenhouden.
                LOG.error("Verwerken van e-mail {} is mislukt", id, e);
            }
        }
        return verwerkt;
    }

    private boolean verwerk(long mailId) {
        Zendopdracht opdracht;
        try {
            opdracht = transactionTemplate.execute(status -> claim(mailId));
        } catch (OptimisticLockingFailureException e) {
            return false;
        }
        if (opdracht == null) {
            return false;
        }

        String fout = null;
        boolean permanent = false;
        try {
            verstuur(opdracht);
        } catch (Exception e) {
            fout = foutmelding(e);
            permanent = isPermanent(e);
            LOG.warn("Verzenden van e-mail {} is mislukt: {}", mailId, fout);
        }
        String foutmelding = fout;
        boolean kanHerhalen = !permanent;
        try {
            transactionTemplate.executeWithoutResult(status -> registreerPoging(mailId, foutmelding, kanHerhalen));
        } catch (RuntimeException e) {
            if (foutmelding == null) {
                LOG.error("E-mail {} is verstuurd, maar dat kon niet worden vastgelegd. Na {} kan hij nog een keer "
                        + "worden verstuurd.", mailId, POGING_TIMEOUT, e);
            } else {
                LOG.error("De mislukte poging voor e-mail {} kon niet worden vastgelegd", mailId, e);
            }
        }
        return true;
    }

    private Zendopdracht claim(long mailId) {
        LocalDateTime nu = LocalDateTime.now(clock);
        DossierMailEntity mail = dossierMailRepository.findById(mailId).orElse(null);
        if (mail == null || mail.getStatus() != DossierMailStatus.IN_WACHT
                || mail.getVolgendePogingOp() == null || mail.getVolgendePogingOp().isAfter(nu)) {
            return null;
        }
        mail.setVolgendePogingOp(nu.plus(POGING_TIMEOUT));
        dossierMailRepository.saveAndFlush(mail);
        return new Zendopdracht(mail.getAfzender(), mail.getOntvanger(), mail.getOnderwerp(), mail.getInhoud());
    }

    private void verstuur(Zendopdracht opdracht) throws Exception {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            throw new IllegalStateException("Er is geen mailserver ingesteld (spring.mail.host).");
        }
        MimeMessage bericht = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(bericht, "UTF-8");
        helper.setFrom(new InternetAddress(opdracht.afzender()));
        helper.setTo(new InternetAddress(opdracht.ontvanger(), true));
        helper.setSubject(opdracht.onderwerp());
        helper.setText(opdracht.inhoud(), true);
        sender.send(bericht);
    }

    private void registreerPoging(long mailId, String foutmelding, boolean kanHerhalen) {
        LocalDateTime nu = LocalDateTime.now(clock);
        DossierMailEntity mail = dossierMailRepository.findById(mailId).orElseThrow();
        if (foutmelding == null) {
            mail.voegPogingToe(nu, DossierMailPogingStatus.GESLAAGD, null);
            mail.setStatus(DossierMailStatus.VERZONDEN);
            mail.setVolgendePogingOp(null);
            return;
        }
        int aantalPogingen = mail.getPogingen().size() + 1;
        if (kanHerhalen && aantalPogingen < mailConfig.getMaxPogingen()) {
            mail.voegPogingToe(nu, DossierMailPogingStatus.MISLUKT, foutmelding);
            mail.setVolgendePogingOp(nu.plus(wachttijdNaPoging(aantalPogingen)));
        } else {
            markeerMislukt(mail, nu, foutmelding);
        }
    }

    /**
     * De wachttijd na de {@code aantalPogingen}-e poging: de basiswachttijd, na elke poging vermenigvuldigd met de
     * back-off-factor, tot ten hoogste de maximale wachttijd.
     */
    private Duration wachttijdNaPoging(int aantalPogingen) {
        double millis = mailConfig.getWachttijd().toMillis()
                * Math.pow(Math.max(1.0, mailConfig.getBackOffFactor()), aantalPogingen - 1);
        long max = mailConfig.getMaxWachttijd().toMillis();
        return Duration.ofMillis(millis >= max ? max : (long) millis);
    }

    /**
     * Opnieuw proberen heeft geen zin als de mailserver het adres of het bericht zelf afwijst.
     * {@link org.springframework.mail.javamail.JavaMailSenderImpl} geeft een fout per bericht door in een
     * {@link MailSendException} zonder oorzaak, met de fout in {@link MailSendException#getMessageExceptions()}.
     */
    private static boolean isPermanent(Throwable fout) {
        for (Throwable oorzaak = fout; oorzaak != null; oorzaak = oorzaak.getCause()) {
            if (oorzaak instanceof MailParseException || oorzaak instanceof MailPreparationException
                    || oorzaak instanceof AddressException) {
                return true;
            }
            if (oorzaak instanceof SendFailedException sendFailed) {
                Address[] ongeldig = sendFailed.getInvalidAddresses();
                if (ongeldig != null && ongeldig.length > 0) {
                    return true;
                }
            }
            if (oorzaak instanceof MailSendException sendException
                    && Arrays.stream(sendException.getMessageExceptions()).anyMatch(DossierMailServiceImpl::isPermanent)) {
                return true;
            }
        }
        return false;
    }

    private static String foutmelding(Throwable fout) {
        Throwable oorzaak = fout;
        while (oorzaak.getCause() != null && oorzaak.getCause() != oorzaak) {
            oorzaak = oorzaak.getCause();
        }
        String melding = fout.getClass().getSimpleName() + ": " + fout.getMessage();
        if (oorzaak != fout) {
            melding += " (oorzaak: " + oorzaak.getClass().getSimpleName() + ": " + oorzaak.getMessage() + ")";
        }
        return melding.length() > MAX_FOUTMELDING_LENGTE ? melding.substring(0, MAX_FOUTMELDING_LENGTE) : melding;
    }

    private String afzender() {
        try {
            return new InternetAddress(mailConfig.getAfzenderAdres(), mailConfig.getAfzenderNaam(), "UTF-8")
                    .toUnicodeString();
        } catch (java.io.UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Map<String, Object> bevestigingWaarden(DossierSamenvattingDto dossier,
                                                          List<PartnerGegevensDto> partners,
                                                          List<GetuigeDto> getuigen) {
        List<Map<String, Object>> partnerNamen = partners.stream()
                .map(partner -> Map.<String, Object>of("naam", naamkeuze(partner)))
                .toList();
        List<Map<String, Object>> extras = new ArrayList<>();
        for (SidebarExtraItemDto extra : dossier.extras() == null ? List.<SidebarExtraItemDto>of() : dossier.extras()) {
            Map<String, Object> item = new HashMap<>();
            item.put("naam", extra.naam());
            item.put("prijs", extra.prijs() != null ? extra.prijs().format() : null);
            extras.add(item);
        }

        Map<String, Object> waarden = new HashMap<>();
        waarden.put("aanhef", partners.stream()
                .map(partner -> naam(partner.voornamen(), partner.achternaam()))
                .filter(naam -> !naam.isBlank())
                .collect(Collectors.collectingAndThen(Collectors.joining(" en "),
                        aanhef -> aanhef.isBlank() ? "aanvrager" : aanhef)));
        waarden.put("registratieType", dossier.registratieType().getLabel());
        waarden.put("ceremonieSoort", dossier.ceremonieSoort().getLabel());
        waarden.put("ceremoniePrijs", dossier.prijs() != null ? dossier.prijs().format() : null);
        waarden.put("datum", dossier.datumTijdHuwelijk() != null ? dossier.datumTijdHuwelijk().format(DATUM_FORMAT) : null);
        waarden.put("tijd", dossier.datumTijdHuwelijk() != null ? dossier.datumTijdHuwelijk().format(TIJD_FORMAT) : null);
        waarden.put("locatie", dossier.huwelijksLocatie());
        waarden.put("partners", partnerNamen);
        waarden.put("getuigen", getuigen.stream().map(g -> Map.<String, Object>of("naam", g.naam() == null ? "" : g.naam())).toList());
        waarden.put("extras", extras);
        waarden.put("totaalPrijs", dossier.totalPrijs() != null ? dossier.totalPrijs().format() : null);
        return waarden;
    }

    /**
     * De voornamen met de gekozen achternaam, die al de volledige keuze is (bijvoorbeeld "Jansen - Dëhlano");
     * zonder keuze de achternaam uit de BRP.
     */
    private static String naamkeuze(PartnerGegevensDto partner) {
        boolean heeftKeuze = partner.gekozenAchternaam() != null && !partner.gekozenAchternaam().isBlank();
        return naam(partner.voornamen(), heeftKeuze ? partner.gekozenAchternaam() : partner.achternaam());
    }

    /** De niet-lege delen van een naam, gescheiden door een spatie. */
    private static String naam(String... delen) {
        return Stream.of(delen)
                .filter(Objects::nonNull)
                .filter(deel -> !deel.isBlank())
                .collect(Collectors.joining(" "));
    }

    private record Zendopdracht(String afzender, String ontvanger, String onderwerp, String inhoud) {
    }
}
