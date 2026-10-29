package ci.univ.soutenances.controller;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

import ci.univ.soutenances.model.ActionHistoryEntry;
import ci.univ.soutenances.service.HistoriqueActionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

final class HistoryPagination {
    private static final int PAGE_SIZE = 4;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private HistoryPagination() {
    }

    static boolean expose(HttpServletRequest request, HttpServletResponse response,
            HistoriqueActionService historiqueActionService, String screen, String title, String editBase)
            throws IOException {
        int requestedPage = parsePage(request.getParameter("page"));
        long total = historiqueActionService.countByScreen(screen);
        int pages = (int) ((total + PAGE_SIZE - 1) / PAGE_SIZE);
        int current = pages == 0 ? 1 : Math.min(requestedPage, pages);
        long offset = (long) (current - 1) * PAGE_SIZE;
        if (offset > Integer.MAX_VALUE) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Numéro de page trop grand.");
            return false;
        }
        request.setAttribute("historyActions",
                historiqueActionService.findPage(screen, (int) offset, PAGE_SIZE).stream()
                        .map(action -> new ActionHistoryEntry(
                                action.getDateAction().format(DATE_FORMAT),
                                action.getDescription(), actionLabel(action.getTypeAction()),
                                action.getUtilisateur().getLogin(), title))
                        .toList());
        request.setAttribute("historyPage", current);
        request.setAttribute("historyTotalPages", pages);
        request.setAttribute("historyTotalActions", total);
        request.setAttribute("historyFirstPage", Math.max(1, current - 2));
        request.setAttribute("historyLastPage", Math.min(pages, current + 2));
        if (editBase != null) {
            request.setAttribute("historyEditBase", editBase);
        }
        return true;
    }

    private static String actionLabel(String typeAction) {
        if (typeAction.endsWith("_CREATION")) {
            return "Création";
        }
        if (typeAction.endsWith("_MODIFICATION")) {
            return "Modification";
        }
        if (typeAction.endsWith("_SUPPRESSION")) {
            return "Suppression";
        }
        return typeAction;
    }

    private static int parsePage(String raw) {
        if (raw == null || raw.isBlank()) {
            return 1;
        }
        try {
            int page = Integer.parseInt(raw);
            if (page < 1) {
                throw new IllegalArgumentException("Le numéro de page doit être positif.");
            }
            return page;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Numéro de page invalide.");
        }
    }
}
