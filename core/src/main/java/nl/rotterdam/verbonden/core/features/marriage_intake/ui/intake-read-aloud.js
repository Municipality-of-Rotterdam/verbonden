(() => {
    document.addEventListener("click", event => {
        const button = event.target.closest("[data-intake-read-aloud]");
        if (!button || !window.speechSynthesis || !window.SpeechSynthesisUtterance) {
            return;
        }

        const speech = window.speechSynthesis;
        const label = button.querySelector(".rd-read-aloud__label");
        const play = button.querySelector(".rd-read-aloud__play");
        const startLabel = label.textContent;
        const reset = () => {
            button.setAttribute("aria-pressed", "false");
            label.textContent = startLabel;
            play.textContent = "▶";
        };
        if (button.getAttribute("aria-pressed") === "true") {
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
        button.setAttribute("aria-pressed", "true");
        label.textContent = button.dataset.stopLabel;
        play.textContent = "■";
        speech.speak(utterance);
    });
})();
