(() => {
    const shell = document.querySelector(".admin-shell");
    const toggle = document.querySelector("[data-sidebar-toggle]");

    if (!shell || !toggle) {
        return;
    }

    const storageKey = "application-sidebar-collapsed";
    const drawerQuery = window.matchMedia("(max-width: 900px)");
    const sidebar = shell.querySelector(".admin-sidebar");

    // Marqueur « JS actif » : sans lui, le menu reste dans le flux de la page
    // au lieu d'être transformé en tiroir hors écran.
    shell.classList.add("js-nav");

    let backdrop = shell.querySelector(".admin-sidebar-backdrop");
    if (!backdrop) {
        backdrop = document.createElement("button");
        backdrop.type = "button";
        backdrop.className = "admin-sidebar-backdrop";
        backdrop.setAttribute("aria-label", "Fermer le menu");
        shell.appendChild(backdrop);
    }

    try {
        shell.classList.toggle("sidebar-collapsed", localStorage.getItem(storageKey) === "true");
    } catch {
        // The toggle still works for this page when browser storage is unavailable.
    }

    const isDrawer = () => drawerQuery.matches;
    const isDrawerOpen = () => shell.classList.contains("sidebar-open");

    const setLabel = (label) => {
        toggle.setAttribute("aria-label", label);
        toggle.title = label;
    };

    const updateButton = () => {
        if (isDrawer()) {
            toggle.setAttribute("aria-expanded", String(isDrawerOpen()));
            setLabel(isDrawerOpen() ? "Fermer le menu" : "Ouvrir le menu");
            return;
        }
        const collapsed = shell.classList.contains("sidebar-collapsed");
        toggle.setAttribute("aria-expanded", String(!collapsed));
        setLabel(collapsed ? "Agrandir le menu" : "Réduire le menu");
    };

    const openDrawer = () => {
        shell.classList.add("sidebar-open");
        document.body.classList.add("sidebar-lock");
        updateButton();
    };

    const closeDrawer = () => {
        shell.classList.remove("sidebar-open");
        document.body.classList.remove("sidebar-lock");
        updateButton();
    };

    updateButton();

    toggle.addEventListener("click", () => {
        if (isDrawer()) {
            if (isDrawerOpen()) {
                closeDrawer();
            } else {
                openDrawer();
            }
            return;
        }
        const collapsed = shell.classList.toggle("sidebar-collapsed");
        try {
            localStorage.setItem(storageKey, String(collapsed));
        } catch {
            // Keep the new state until navigation even if persistence is unavailable.
        }
        updateButton();
    });

    backdrop.addEventListener("click", closeDrawer);

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape" && isDrawerOpen()) {
            closeDrawer();
        }
    });

    if (sidebar) {
        sidebar.addEventListener("click", (event) => {
            if (isDrawer() && event.target.closest("a")) {
                closeDrawer();
            }
        });
    }

    drawerQuery.addEventListener("change", () => {
        closeDrawer();
        updateButton();
    });
})();
