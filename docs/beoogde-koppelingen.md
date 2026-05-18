# Beoogde koppelingen

Dit document beschrijft de beoogde koppelingen met externe voorzieningen voor de applicatie huwelijk.

## Haal Centraal

De applicatie koppelt met Haal Centraal om voor de met DigiD ingelogde gebruiker aanvullende gegevens op te halen, zoals:

- adresgegevens;
- ouders (voor controle op familiebanden tussen partners);
- partners (voor controle op bestaande verbintenissen).

## E-mailserver (SMTP)

De applicatie verstuurt e-mails, bijvoorbeeld naar dossierbeheerders wanneer actie nodig is.
Hiervoor wordt een koppeling met een SMTP-server via een beveiligde verbinding gerealiseerd.

## Zaaksysteem

Voor een dossier wordt een zaak aangemaakt in een lopend zaaksysteem.
Binnen Rotterdam zijn meerdere versies van zaaksystemen in omloop. Afhankelijk van de gekozen implementatieroute wordt aangesloten op de passende ZGW API-versie.

## Inloggen met DigiD

Voor inloggen met DigiD wordt OIDC-ondersteuning ingebouwd.
Toegang met een Europees middel of eHerkenning wordt geweigerd. Anonieme toegang wordt geblokkeerd.

Een DigiD koppeling zorgt er automatisch voor dat de applicatie ook meeloopt in de periodieke DigiD audit. Hier zijn kosten aan verbonden.

## Medewerkersportaal

Medewerkers loggen in met hun gemeentelijke account.
Hiervoor wordt een OIDC-koppeling voorzien via SailPoint / Microsoft Entra.

## Virusscanner voor uploads

Bestanden die worden geüpload worden gescand op virussen.
Naar verwachting wordt hiervoor Scanii gebruikt, zoals ook in andere applicaties met bestandsuploads binnen Rotterdam.

## Online betaalmodule

Kosten worden afgerekend via een online betaalmodule.
De koppeling wordt beïnvloed door Common Ground. Er wordt onderzocht welke standaard kan worden gebruikt en hoe aangesloten kan worden op de online betaalmodule die Rotterdam al gebruikt.

## Berichtenbox

Communicatie naar partners kan via e-mail of via de berichtenbox plaatsvinden.
