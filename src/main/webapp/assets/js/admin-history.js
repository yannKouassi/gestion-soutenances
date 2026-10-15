document.addEventListener("click", (event) => {
    const detailButton = event.target.closest("[data-history-detail]");
    if (detailButton) {
        const section = detailButton.closest(".admin-main");
        const dialog = section?.querySelector(".history-modal");
        if (dialog) {
            dialog.querySelector("[data-history-modal-date]").textContent = detailButton.dataset.historyDate;
            dialog.querySelector("[data-history-modal-action]").textContent = detailButton.dataset.historyAction;
            dialog.querySelector("[data-history-modal-user]").textContent = detailButton.dataset.historyUser;
            const role = dialog.querySelector("[data-history-modal-role]");
            if (role) {
                role.textContent = detailButton.dataset.historyRole || "";
            }
            dialog.querySelector("[data-history-modal-screen]").textContent = detailButton.dataset.historyScreen;
            dialog.querySelector("[data-history-modal-content]").textContent = detailButton.dataset.historyDetail;
            dialog.showModal();
        }
        return;
    }

    const closeButton = event.target.closest("[data-history-close]");
    if (closeButton) {
        closeButton.closest(".history-modal")?.close();
        return;
    }

    if (event.target.matches(".history-modal")) {
        event.target.close();
    }
});
