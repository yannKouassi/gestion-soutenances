// Anti double-soumission + auto-fermeture des alertes de succès
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
            setTimeout(function () { disableButtons(form); }, 0);
        }
    }, true);

    function autoDismiss() {
        document.querySelectorAll('.alert.alert-success, .alert-success').forEach(function (alert) {
            setTimeout(function () {
                alert.classList.add('is-dismissing');
                setTimeout(function () { alert.remove(); }, 280);
            }, 5000);
        });
    }
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', autoDismiss);
    } else {
        autoDismiss();
    }
})();
