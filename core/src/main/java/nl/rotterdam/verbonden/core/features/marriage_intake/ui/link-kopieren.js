// Kopieert de waarde van een invoerveld naar het klembord. Een knop met data-kopieer-doel (id van het
// invoerveld) en data-kopieer-melding (id van een aria-live element) activeert dit; de meldingsteksten
// komen uit data-tekst-gekopieerd en data-tekst-mislukt.
document.addEventListener('click', function (event) {
    var knop = event.target.closest('[data-kopieer-doel]');
    if (!knop) {
        return;
    }
    var invoer = document.getElementById(knop.dataset.kopieerDoel);
    var melding = document.getElementById(knop.dataset.kopieerMelding);

    function toonMislukt() {
        // Selecteer de tekst zodat de gebruiker hem zelf kan kopiëren.
        invoer.focus();
        invoer.select();
        melding.textContent = knop.dataset.tekstMislukt;
    }

    if (!navigator.clipboard) {
        toonMislukt();
        return;
    }
    navigator.clipboard.writeText(invoer.value).then(function () {
        melding.textContent = knop.dataset.tekstGekopieerd;
    }, toonMislukt);
});
