package ci.univ.soutenances.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import ci.univ.soutenances.config.AdminAuthorizationFilter;
import ci.univ.soutenances.model.ActionHistoryEntry;
import ci.univ.soutenances.model.Memoire;
import ci.univ.soutenances.model.SessionUser;
import ci.univ.soutenances.model.Soutenance;
import ci.univ.soutenances.repository.SalleRepository;
import ci.univ.soutenances.service.HistoriqueActionService;
import ci.univ.soutenances.service.MemoireService;
import ci.univ.soutenances.service.SoutenanceService;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = { "/admin/planification/*" })
public class PlanificationServlet extends HttpServlet {
    private static final String TYPE = "planification";
    private static final String TITLE = "Planification des soutenances";
    private static final String SCREEN = "SOUTENANCE";
    private static final String FORM_VIEW = "/WEB-INF/views/admin/soutenance-form.jsp";
    private static final int HISTORY_PAGE_SIZE = 4;
    private static final DateTimeFormatter HISTORY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @EJB
    private SoutenanceService soutenanceService;

    @EJB
    private MemoireService memoireService;

    @EJB
    private SalleRepository salleRepository;

    @EJB
    private HistoriqueActionService historiqueActionService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("type", TYPE);
        request.setAttribute("titre", TITLE);
        String[] path = pathParts(request);

        try {
            if (path.length != 0) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String rawId = request.getParameter("id");
            Soutenance element = rawId == null || rawId.isBlank()
                    ? Soutenance.create()
                    : soutenanceService.findById(parseId(rawId)).orElse(null);
            if (element == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("element", element);
            request.setAttribute("message", request.getParameter("message"));
            request.setAttribute("erreur", request.getParameter("erreur"));
            setFormOptions(request);
            if (!setHistoryPage(request, response)) {
                return;
            }
            forwardForm(request, response);
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        } catch (EJBException exception) {
            getServletContext().log("Impossible de charger l’écran de planification des soutenances.", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Impossible de charger cette page pour le moment.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String[] path = pathParts(request);
        if (path.length != 1 || !"enregistrer".equals(path[0])) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            boolean creation = isCreation(request);
            Soutenance soutenance = saveFromRequest(request);
            String label = "Soutenance : " + clean(soutenance.getMemoire().getTitre()) + " — "
                    + soutenance.getDateHeure().format(HISTORY_DATE_FORMAT);
            String action = creation ? "CREATION" : "MODIFICATION";
            boolean recorded = recordAction(request, action, label);
            String message = creation ? "Soutenance planifiée." : "Modifications enregistrées.";
            if (!recorded) {
                message = message + " L’historique de l’action n’a pas pu être enregistré.";
            }
            if (creation) {
                redirectJury(request, response, soutenance.getId(), recorded, message);
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/planification?id=" + soutenance.getId()
                        + (recorded ? "&message=" : "&erreur=") + encode(message));
            }
        } catch (IllegalArgumentException exception) {
            showFormError(request, response, exception.getMessage());
        } catch (EJBException exception) {
            getServletContext().log("Échec de la planification d’une soutenance.", exception);
            showFormError(request, response,
                    "Enregistrement impossible. Vérifie la mémoire, la salle et le créneau choisis.");
        }
    }

    private Soutenance saveFromRequest(HttpServletRequest request) {
        String rawId = request.getParameter("id");
        UUID id = rawId == null || rawId.isBlank() ? null : parseId(rawId);
        Soutenance soutenance = id == null
                ? Soutenance.create()
                : soutenanceService.findById(id).orElseThrow(
                        () -> new IllegalArgumentException("Soutenance introuvable."));
        soutenance.setDateHeure(parseDateHeure(request.getParameter("dateHeure")));
        String memoireId = request.getParameter("memoireId");
        String salleId = request.getParameter("salleId");
        return soutenanceService.save(soutenance,
                memoireId == null || memoireId.isBlank() ? null : parseId(memoireId),
                salleId == null || salleId.isBlank() ? null : parseId(salleId));
    }

    private void setFormOptions(HttpServletRequest request) {
        Soutenance element = (Soutenance) request.getAttribute("element");
        List<Memoire> memoires = new ArrayList<>(memoireService.findNonSoutenus());
        if (element != null && element.getMemoire() != null
                && memoires.stream().noneMatch(memoire -> memoire.getId().equals(element.getMemoire().getId()))) {
            memoires.add(element.getMemoire());
        }
        memoires.sort(Comparator.comparing(Memoire::getTitre, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
        request.setAttribute("memoires", memoires);
        request.setAttribute("salles", salleRepository.findAll());
        request.setAttribute("selectedMemoireId", selectedId(request.getParameter("memoireId"),
                element == null || element.getMemoire() == null ? null : element.getMemoire().getId()));
        request.setAttribute("selectedSalleId", selectedId(request.getParameter("salleId"),
                element == null || element.getSalle() == null ? null : element.getSalle().getId()));
        request.setAttribute("dateHeureValue", dateHeureValue(request.getParameter("dateHeure"), element));
    }

    private void showFormError(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        request.setAttribute("type", TYPE);
        request.setAttribute("titre", TITLE);
        request.setAttribute("erreur", error);
        Soutenance element = Soutenance.create();
        String rawId = request.getParameter("id");
        if (rawId != null && !rawId.isBlank()) {
            try {
                Soutenance existing = soutenanceService.findById(parseId(rawId)).orElse(null);
                if (existing != null) {
                    element = existing;
                }
            } catch (IllegalArgumentException ignored) {
                request.setAttribute("idInvalide", true);
            }
        }
        request.setAttribute("element", element);
        setFormOptions(request);
        if (!setHistoryPage(request, response)) {
            return;
        }
        forwardForm(request, response);
    }

    private boolean setHistoryPage(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int requestedPage = parsePage(request.getParameter("page"));
        long total = historiqueActionService.countByScreen(SCREEN);
        int pages = (int) ((total + HISTORY_PAGE_SIZE - 1) / HISTORY_PAGE_SIZE);
        int current = pages == 0 ? 1 : Math.min(requestedPage, pages);
        long offset = (long) (current - 1) * HISTORY_PAGE_SIZE;
        if (offset > Integer.MAX_VALUE) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Numéro de page trop grand.");
            return false;
        }
        request.setAttribute("historyActions",
                historiqueActionService.findPage(SCREEN, (int) offset, HISTORY_PAGE_SIZE).stream()
                        .map(action -> new ActionHistoryEntry(
                                action.getDateAction().format(HISTORY_DATE_FORMAT),
                                action.getDescription(), actionLabel(action.getTypeAction()),
                                action.getUtilisateur().getLogin(), TITLE))
                        .toList());
        request.setAttribute("historyPage", current);
        request.setAttribute("historyTotalPages", pages);
        request.setAttribute("historyTotalActions", total);
        request.setAttribute("historyFirstPage", Math.max(1, current - 2));
        request.setAttribute("historyLastPage", Math.min(pages, current + 2));
        Soutenance element = (Soutenance) request.getAttribute("element");
        String elementId = element == null || element.getId() == null ? "" : element.getId().toString();
        request.setAttribute("historyEditBase",
                request.getContextPath() + "/admin/planification?id=" + elementId);
        return true;
    }

    private boolean recordAction(HttpServletRequest request, String action, String label) {
        Object user = request.getSession(false).getAttribute(AdminAuthorizationFilter.SESSION_USER_ATTRIBUTE);
        if (!(user instanceof SessionUser sessionUser)) {
            getServletContext().log("Impossible d’enregistrer l’historique : utilisateur de session absent.");
            return false;
        }
        try {
            historiqueActionService.record(SCREEN, action, label, sessionUser.id());
            return true;
        } catch (EJBException | IllegalArgumentException exception) {
            getServletContext().log("Impossible d’enregistrer l’action " + action + " sur " + SCREEN, exception);
            return false;
        }
    }

    private static boolean isCreation(HttpServletRequest request) {
        String id = request.getParameter("id");
        return id == null || id.isBlank();
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
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

    private static String[] pathParts(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null || path.equals("/") ? new String[0] : path.substring(1).split("/");
    }

    private static UUID selectedId(String rawId, UUID currentId) {
        if (rawId == null || rawId.isBlank()) {
            return currentId;
        }
        try {
            return parseId(rawId);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static String dateHeureValue(String raw, Soutenance element) {
        if (raw != null && !raw.isBlank()) {
            return raw;
        }
        if (element == null || element.getDateHeure() == null) {
            return "";
        }
        return element.getDateHeure().format(INPUT_FORMAT);
    }

    private static UUID parseId(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("Identifiant invalide.");
        }
    }

    private static LocalDateTime parseDateHeure(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("La date et l’heure de la soutenance sont obligatoires.");
        }
        try {
            return LocalDateTime.parse(raw, INPUT_FORMAT);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("La date et l’heure de la soutenance sont invalides.");
        }
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

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static void forwardForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
    }

    private static void redirectJury(HttpServletRequest request, HttpServletResponse response,
            UUID soutenanceId, boolean recorded, String message) throws IOException {
        response.sendRedirect(request.getContextPath() + "/admin/jury?soutenance=" + soutenanceId
                + (recorded ? "&message=" : "&erreur=") + encode(message));
    }
}
