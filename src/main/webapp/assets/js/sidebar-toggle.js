(() => {
    const shell = document.querySelector(".admin-shell");
    const toggle = document.querySelector("[data-sidebar-toggle]");

    if (!shell || !toggle) {
        return;
    }

    const storageKey = "application-sidebar-collapsed";

    try {
        shell.classList.toggle("sidebar-collapsed", localStorage.getItem(storageKey) === "true");
    } catch {
        // The toggle still works for this page when browser storage is unavailable.
    }

    const updateButton = () => {
        const collapsed = shell.classList.contains("sidebar-collapsed");
        const label = collapsed ? "Agrandir le menu" : "Réduire le menu";
        toggle.setAttribute("aria-expanded", String(!collapsed));
        toggle.setAttribute("aria-label", label);
        toggle.title = label;
    };

    updateButton();

    toggle.addEventListener("click", () => {
        const collapsed = shell.classList.toggle("sidebar-collapsed");
        try {
            localStorage.setItem(storageKey, String(collapsed));
        } catch {
            // Keep the new state until navigation even if persistence is unavailable.
        }
        updateButton();
    });
})();
