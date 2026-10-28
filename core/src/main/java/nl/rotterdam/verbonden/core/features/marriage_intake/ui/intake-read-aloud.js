(() => {
    document.addEventListener("click", event => {
        const button = event.target.closest("[data-intake-read-aloud]");
        if (!button || !window.speechSynthesis || !window.SpeechSynthesisUtterance) {
            return;
        }

        const speech = window.speechSynthesis;
        if (speech.speaking) {
            speech.cancel();
            button.setAttribute("aria-pressed", "false");
            return;
        }

        const content = button.closest(".rd-intake-step")?.querySelector(".rd-intake-content");
        const text = content?.innerText.trim();
        if (!text) {
            return;
        }

        const utterance = new SpeechSynthesisUtterance(text);
        utterance.lang = "nl-NL";
        const reset = () => button.setAttribute("aria-pressed", "false");
        utterance.onend = reset;
        utterance.onerror = reset;
        button.setAttribute("aria-pressed", "true");
        speech.speak(utterance);
    });
})();
