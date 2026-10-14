"use strict";

(() => {
    const search = document.querySelector("[data-list-search]");
    const body = document.querySelector("[data-list-body]");

    if (!(search instanceof HTMLInputElement) || !(body instanceof HTMLTableSectionElement)) {
        return;
    }

    const rows = Array.from(body.querySelectorAll("[data-list-row]"));
    const count = document.querySelector("[data-list-count]");
    const clear = document.querySelector("[data-search-clear]");
    const noResults = body.querySelector("[data-search-no-results]");

    const normalize = (value) => value
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toLocaleLowerCase("fr")
        .trim();

    const update = () => {
        const term = normalize(search.value);
        let visible = 0;

        rows.forEach((row) => {
            const text = Array.from(row.querySelectorAll("[data-searchable]"))
                .map((cell) => cell.textContent)
                .join(" ");
            const matches = normalize(text).includes(term);
            row.hidden = !matches;
            visible += matches ? 1 : 0;
        });

        if (noResults instanceof HTMLTableRowElement) {
            noResults.hidden = visible > 0 || rows.length === 0;
        }
        if (count) {
            count.textContent = `${visible} résultat${visible === 1 ? "" : "s"} sur ${rows.length}`;
        }
        if (clear instanceof HTMLButtonElement) {
            clear.hidden = term.length === 0;
        }
    };

    search.addEventListener("input", update);
    clear?.addEventListener("click", () => {
        search.value = "";
        update();
        search.focus();
    });

    update();
})();
