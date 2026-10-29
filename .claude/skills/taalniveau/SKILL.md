---
name: taalniveau
description: Bepaal het taalniveau van een Nederlandse tekst volgens het CEFR/ERK-framework (A1–C2), met nadruk op de B1-norm voor gemeentelijke en publiekgerichte webteksten. Gebruik deze skill altijd wanneer de gebruiker vraagt om het taalniveau van een tekst te bepalen, een tekst te analyseren op leesbaarheid, een tekst te herschrijven naar B1-niveau, of te controleren of een tekst begrijpelijk is voor een breed publiek. Trigger ook bij vragen als "is deze tekst begrijpelijk?", "hoe moeilijk is deze tekst?", "maak dit eenvoudiger", of "schrijf dit op B1-niveau".
---

# Taalniveau Skill

Analyseer Nederlandse teksten op CEFR-niveau (ERK) en geef concrete herschrijfsuggesties, gerangschikt naar impact.

## Streefniveau

**Standaard: B1** — bereikbaar voor 95% van de Nederlandse bevolking. Dit is het streefniveau voor gemeentelijke websites en publiekgerichte communicatie.

**Uitzonderingen:**
- Doelgroep zijn laaggeletterden, inburgeraars of kinderen → streef **A2**
- Doelgroep is een specifiek vakpubliek dat bekend is met het jargon → **C1 toegestaan**
- Vermeld altijd in de beoordeling als een uitzondering van toepassing is.

---

## CEFR-niveaus

| Niveau | Zinslengte | Woordenschat | Kenmerken |
|--------|-----------|--------------|-----------|
| A1 | gem. <10 woorden | Zeer eenvoudig, alledaags | Korte enkelvoudige zinnen, bekende situaties |
| A2 | gem. <10 woorden | Eenvoudig, veelgebruikt | Korte zinnen, vertrouwde onderwerpen |
| B1 | gem. 10–15 woorden | Gangbaar, geen vaktaal | Actief, direct, één gedachte per zin |
| B2 | gem. >15 woorden | Gevarieerd, enige vaktaal | Complexere zinnen, abstracte onderwerpen |
| C1 | lang, complex | Breed incl. vaktaal | Ingebedde bijzinnen, specialistische thema's |
| C2 | zeer lang/complex | Gespecialiseerd | Academisch, juridisch, wetenschappelijk |

---

## Stap 1: Beoordeel het taalniveau

Analyseer op basis van:

**Zinslengte**
- Bereken de gemiddelde zinslengte
- Zinnen >20 woorden zijn te complex voor B1

**Woordenschat**
- Vakjargon en juridische termen (bijv. "bezwaarschrift", "conform", "vergunninghouder")
- Samengestelde woorden met 3+ delen
- Abstracte begrippen

**Zinsstructuur**
- Passieve constructies ("wordt verleend" i.p.v. "krijgt u")
- Diep geneste bijzinnen
- Dubbele ontkenningen
- Nominalisaties ("de uitvoering van" i.p.v. "uitvoeren")

Bij grensgevallen: benoem dit expliciet. Bijv.: *"Deze tekst scoort tussen B1 en B2: de zinsbouw is B1, maar bevat meerdere juridische termen."*

---

## Stap 2: Verbeterpunten (indien niet B1 of doelniveau)

Rangschik naar impact:

**Hoge impact** (grootste obstakel voor leesbaarheid)
> **Origineel:** [exacte zin]
> **B1-versie:** [herschreven zin]
> **Waarom beter:** [korte toelichting]

**Gemiddelde impact**
> [zelfde format]

**Lage impact**
> [zelfde format]

---

## Stap 3: Samenvatting

- **Belangrijkste knelpunt:** [grootste obstakel voor B1]
- **Snelste winst:** [makkelijkste verbetering met groot effect]
- **Geschat verbetertraject:** klein / gemiddeld / groot

---

## Outputstructuur

```
## Taalniveau: [NIVEAU]

**Motivering:**
- Gemiddelde zinslengte: [X woorden]
- Vakjargon/juridische termen: [voorbeelden]
- Overige observaties: [passief, nominalisaties, etc.]

---

### Verbeterpunten

**Hoge impact:**
...

**Gemiddelde impact:**
...

**Lage impact:**
...

---

### Samenvatting
- Belangrijkste knelpunt: ...
- Snelste winst: ...
- Geschat verbetertraject: klein / gemiddeld / groot
```

---

## Aandachtspunten

- Beoordeel op **taalkenmerken**, niet op inhoudelijke mening over het onderwerp.
- Wees **eerlijk**: als een tekst al B1 is, zeg dat dan. Geef dan suggesties voor verdere verfijning als zinvol.
- Houd herschrijfsuggesties **inhoudelijk trouw** aan het origineel. Vereenvoudig de taal, niet de boodschap.
- Bij korte teksten (<50 woorden): geef aan dat de analyse beperkt betrouwbaar is.
- Vermeld altijd als een uitzondering op het B1-streefniveau van toepassing is (A2 of C1).
