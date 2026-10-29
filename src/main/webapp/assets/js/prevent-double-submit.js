// Anti double-soumission : désactive les boutons submit après le premier clic
(function () {
    function disableButtons(form) {
        form.querySelectorAll('button[type="submit"], input[type="submit"]').forEach(function (btn) {
            btn.disabled = true;
            btn.setAttribute('aria-disabled', 'true');
        });
    }
    document.addEventListener('submit', function (event) {
        var form = event.target;
        if (form && form.tagName === 'FORM') {
            // Laisser les validations natives se faire ; désactiver au prochain tick
            setTimeout(function () { disableButtons(form); }, 0);
        }
    }, true);
})();
