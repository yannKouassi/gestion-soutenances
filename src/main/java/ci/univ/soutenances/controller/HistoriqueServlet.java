package ci.univ.soutenances.controller;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ci.univ.soutenances.model.HistoriqueFiltre;
import ci.univ.soutenances.model.HistoriqueJournalEntry;
import ci.univ.soutenances.service.HistoriqueActionService;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/historique")
public class HistoriqueServlet extends HttpServlet {
    private static final int DEFAULT_PAGE_SIZE = 15;
    private static final Set<Integer> PAGE_SIZES = Set.of(10, 15, 20, 50);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Map<String, String> SCREENS = Map.of(
            "GRADE", "Grades",
            "DEPARTEMENT", "Départements",
            "FILIERE", "Filières",
            "SALLE", "Salles");
    private static final Map<String, String> ACTIONS = Map.of(
            "CREATION", "Création",
            "MODIFICATION", "Modification",
            "SUPPRESSION", "Suppression");

    @EJB
    private HistoriqueActionService historiqueActionService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String ecran = blankToNull(request.getParameter("ecran"));
            String action = blankToNull(request.getParameter("action"));
            String auteur = blankToNull(request.getParameter("auteur"));
            LocalDate du = parseDate(request.getParameter("du"));
            LocalDate au = parseDate(request.getParameter("au"));
            if (du != null && au != null && du.isAfter(au)) {
                throw new IllegalArgumentException("La période de l’historique est invalide.");
            }
            int pageSize = parsePageSize(request.getParameter("taille"));
            int requestedPage = parsePage(request.getParameter("page"));

            HistoriqueFiltre filtre = new HistoriqueFiltre(ecran, action, auteur,
                    du == null ? null : du.atStartOfDay(),
                    finExclusiveDe(au));

            long totalActions = historiqueActionService.count(filtre);
            int totalPages = (int) ((totalActions + pageSize - 1) / pageSize);
            int currentPage = totalPages == 0 ? 1 : Math.min(requestedPage, totalPages);
            long offset = (long) (currentPage - 1) * pageSize;
            if (offset > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Numéro de page trop grand.");
            }

            request.setAttribute("type", "historique");
            request.setAttribute("titre", "Journal d’audit");
            request.setAttribute("entries",
                    historiqueActionService.search(filtre, (int) offset, pageSize).stream()
                            .map(historique -> new HistoriqueJournalEntry(
                                    historique.getDateAction().format(DATE_FORMAT),
                                    historique.getDescription(),
                                    actionLabel(historique.getAction(), historique.getTypeAction()),
                                    historique.getUtilisateur().getLogin(),
                                    historique.getUtilisateur().getRole().name(),
                                    screenLabel(historique.getEcran(), historique.getTypeAction())))
                            .toList());
            request.setAttribute("auteurs", auteursAvecFiltreApplique(auteur));
            request.setAttribute("screens", orderedScreens());
            request.setAttribute("actions", orderedActions());
            request.setAttribute("filtreEcran", ecran == null ? "" : ecran);
            request.setAttribute("filtreAction", action == null ? "" : action);
            request.setAttribute("filtreAuteur", auteur == null ? "" : auteur);
            request.setAttribute("filtreDu", du == null ? "" : du.toString());
            request.setAttribute("filtreAu", au == null ? "" : au.toString());
            request.setAttribute("taille", pageSize);
            request.setAttribute("tailles", PAGE_SIZES.stream().sorted().toList());
            request.setAttribute("page", currentPage);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalActions", totalActions);
            request.setAttribute("firstPage", Math.max(1, currentPage - 2));
            request.setAttribute("lastPage", Math.min(totalPages, currentPage + 2));
            request.getRequestDispatcher("/WEB-INF/views/admin/historique-list.jsp").forward(request, response);
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        } catch (EJBException exception) {
            getServletContext().log("Impossible de charger le journal d’audit.", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Impossible de charger cette page pour le moment.");
        }
    }

    private static String screenLabel(String ecran, String typeAction) {
        if (ecran != null && SCREENS.containsKey(ecran)) {
            return SCREENS.get(ecran);
        }
        String code = screenCodeOf(typeAction);
        if (code != null && SCREENS.containsKey(code)) {
            return SCREENS.get(code);
        }
        return typeAction;
    }

    private static String actionLabel(String action, String typeAction) {
        if (action != null && ACTIONS.containsKey(action)) {
            return ACTIONS.get(action);
        }
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

    private static String screenCodeOf(String typeAction) {
        int separator = typeAction.indexOf('_');
        return separator < 1 ? null : typeAction.substring(0, separator);
    }

    private static Map<String, String> orderedScreens() {
        Map<String, String> screens = new LinkedHashMap<>();
        screens.put("GRADE", SCREENS.get("GRADE"));
        screens.put("DEPARTEMENT", SCREENS.get("DEPARTEMENT"));
        screens.put("FILIERE", SCREENS.get("FILIERE"));
        screens.put("SALLE", SCREENS.get("SALLE"));
        return screens;
    }

    private static Map<String, String> orderedActions() {
        Map<String, String> actions = new LinkedHashMap<>();
        actions.put("CREATION", ACTIONS.get("CREATION"));
        actions.put("MODIFICATION", ACTIONS.get("MODIFICATION"));
        actions.put("SUPPRESSION", ACTIONS.get("SUPPRESSION"));
        return actions;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private List<String> auteursAvecFiltreApplique(String auteur) {
        List<String> auteurs = new ArrayList<>(historiqueActionService.findDistinctAuteurs());
        if (auteur != null && !auteurs.contains(auteur)) {
            auteurs.add(auteur);
            Collections.sort(auteurs);
        }
        return auteurs;
    }

    private static LocalDateTime finExclusiveDe(LocalDate au) {
        if (au == null) {
            return null;
        }
        try {
            return au.plusDays(1).atStartOfDay();
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException("Date de période invalide.");
        }
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Date de période invalide.");
        }
    }

    private static int parsePage(String value) {
        if (value == null || value.isBlank()) {
            return 1;
        }
        try {
            int page = Integer.parseInt(value.trim());
            if (page < 1) {
                throw new IllegalArgumentException("Le numéro de page doit être positif.");
            }
            return page;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Numéro de page invalide.");
        }
    }

    private static int parsePageSize(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_PAGE_SIZE;
        }
        try {
            int pageSize = Integer.parseInt(value.trim());
            if (!PAGE_SIZES.contains(pageSize)) {
                throw new IllegalArgumentException("Taille de page invalide.");
            }
            return pageSize;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Taille de page invalide.");
        }
    }
}
