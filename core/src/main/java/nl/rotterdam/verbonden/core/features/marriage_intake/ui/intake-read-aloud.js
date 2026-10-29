(() => {
    // De zichtbare tekst is ook de toegankelijke naam van de knop (WCAG 2.5.3). Tijdens het voorlezen wisselt die
    // naar de stoptekst; daarom geen aria-pressed, want een toggle-knop hoort een vaste naam te houden.
    document.addEventListener("click", event => {
        const button = event.target.closest("[data-intake-read-aloud]");
        if (!button || !window.speechSynthesis || !window.SpeechSynthesisUtterance) {
            return;
        }

        const speech = window.speechSynthesis;
        const label = button.querySelector(".rd-read-aloud__label");
        const play = button.querySelector(".rd-read-aloud__play");
        const reset = () => {
            delete button.dataset.speaking;
            label.textContent = button.dataset.startLabel;
            play.textContent = "▶";
        };
        if (button.dataset.speaking) {
            speech.cancel();
            reset();
            return;
        }

        if (speech.speaking) {
            speech.cancel();
        }

        const content = button.closest(".rd-intake-step")?.querySelector(".rd-intake-content");
        const text = content?.innerText.trim();
        if (!text) {
            return;
        }

        const utterance = new SpeechSynthesisUtterance(text);
        utterance.lang = "nl-NL";
        utterance.onend = reset;
        utterance.onerror = reset;
        button.dataset.speaking = "true";
        label.textContent = button.dataset.stopLabel;
        play.textContent = "■";
        speech.speak(utterance);
    });
})();
