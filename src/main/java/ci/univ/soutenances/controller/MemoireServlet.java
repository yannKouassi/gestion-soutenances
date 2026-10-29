package ci.univ.soutenances.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import ci.univ.soutenances.config.AdminAuthorizationFilter;
import ci.univ.soutenances.model.Memoire;
import ci.univ.soutenances.model.SessionUser;
import ci.univ.soutenances.repository.EncadreurRepository;
import ci.univ.soutenances.service.HistoriqueActionService;
import ci.univ.soutenances.service.MemoireService;
import ci.univ.soutenances.util.FlashMessages;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = { "/admin/memoires/*" })
public class MemoireServlet extends HttpServlet {
    private static final String TYPE = "memoires";
    private static final String TITLE = "Mémoires";
    private static final String SCREEN = "MEMOIRE";
    private static final String LIST_VIEW = "/WEB-INF/views/admin/memoire-list.jsp";
    private static final String FORM_VIEW = "/WEB-INF/views/admin/memoire-form.jsp";
    private static final String DETAIL_VIEW = "/WEB-INF/views/admin/memoire-detail.jsp";

    @EJB
    private MemoireService memoireService;

    @EJB
    private EncadreurRepository encadreurRepository;

    @EJB
    private HistoriqueActionService historiqueActionService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("type", TYPE);
        request.setAttribute("titre", TITLE);
        String[] path = pathParts(request);

        try {
            if (path.length == 0 && "detail".equals(request.getParameter("vue"))) {
                Memoire element = memoireService.findById(parseId(request.getParameter("id"))).orElse(null);
                if (element == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                request.setAttribute("element", element);
                request.getRequestDispatcher(DETAIL_VIEW).forward(request, response);
                return;
            }

            if (path.length == 0 && "form".equals(request.getParameter("vue"))) {
                request.setAttribute("dateDepotMax", LocalDate.now());
                String rawId = request.getParameter("id");
                Memoire element = rawId == null || rawId.isBlank()
                        ? Memoire.create()
                        : memoireService.findById(parseId(rawId)).orElse(null);
                if (element == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                request.setAttribute("element", element);
                FlashMessages.exposer(request);
                setFormOptions(request);
                if (!setHistoryPage(request, response)) {
                    return;
                }
                forwardForm(request, response);
                return;
            }

            if (path.length == 0) {
                request.setAttribute("elements", memoireService.findAll());
                request.setAttribute("encadreurs", encadreurRepository.findAll());
                FlashMessages.exposer(request);
                request.getRequestDispatcher(LIST_VIEW).forward(request, response);
                return;
            }

            if (path.length == 2 && "modifier".equals(path[1])) {
                request.setAttribute("dateDepotMax", LocalDate.now());
                Memoire element = memoireService.findById(parseId(path[0])).orElse(null);
                if (element == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                request.setAttribute("element", element);
                setFormOptions(request);
                if (!setHistoryPage(request, response)) {
                    return;
                }
                forwardForm(request, response);
                return;
            }

            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        } catch (EJBException exception) {
            getServletContext().log("Impossible de charger l’écran des mémoires " + TYPE, exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Impossible de charger cette page pour le moment.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String[] path = pathParts(request);
        if (path.length != 1 || !("enregistrer".equals(path[0]) || "supprimer".equals(path[0]))) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            if ("supprimer".equals(path[0])) {
                Memoire memoire = memoireService.findById(parseId(request.getParameter("id")))
                        .orElseThrow(() -> new IllegalArgumentException("Mémoire introuvable."));
                String label = entityLabel(memoire);
                memoireService.delete(memoire.getId());
                if (recordAction(request, "SUPPRESSION", label)) {
                    redirect(request, response, "message", "Mémoire supprimé.");
                } else {
                    redirect(request, response, "erreur",
                            "Mémoire supprimé, mais l’historique de l’action n’a pas pu être enregistré.");
                }
                return;
            }

            boolean creation = isCreation(request);
            String label = requestLabel(request);
            saveFromRequest(request);
            String action = creation ? "CREATION" : "MODIFICATION";
            if (recordAction(request, action, label)) {
                redirect(request, response, "message",
                        creation ? "Mémoire créé." : "Modifications enregistrées.");
            } else {
                redirect(request, response, "erreur", creation
                        ? "Mémoire créé, mais l’historique de l’action n’a pas pu être enregistré."
                        : "Modifications enregistrées, mais l’historique de l’action n’a pas pu être enregistré.");
            }
        } catch (IllegalArgumentException exception) {
            if ("supprimer".equals(path[0])) {
                redirect(request, response, "erreur", exception.getMessage());
                return;
            }
            showFormError(request, response, exception.getMessage());
        } catch (EJBException exception) {
            String validationError = ValidationErrors.find(exception);
            if (validationError != null && !"supprimer".equals(path[0])) {
                showFormError(request, response, validationError);
                return;
            }
            getServletContext().log("Échec d’une opération sur les mémoires " + TYPE, exception);
            if ("supprimer".equals(path[0])) {
                redirect(request, response, "erreur",
                        "Suppression impossible : ce mémoire est peut-être encore utilisé.");
                return;
            }
            showFormError(request, response,
                    "Enregistrement impossible. Vérifie les champs et les éléments liés.");
        }
    }

    private void saveFromRequest(HttpServletRequest request) {
        String rawId = request.getParameter("id");
        UUID id = rawId == null || rawId.isBlank() ? null : parseId(rawId);
        Memoire memoire = id == null
                ? Memoire.create()
                : memoireService.findById(id).orElseThrow(
                        () -> new IllegalArgumentException("Mémoire introuvable."));
        memoire.setTitre(request.getParameter("titre"));
        memoire.setDescription(request.getParameter("description"));
        memoire.setDateDepot(parseDate(request.getParameter("dateDepot")));
        String encadreurId = request.getParameter("encadreurId");
        memoireService.save(memoire,
                encadreurId == null || encadreurId.isBlank() ? null : parseId(encadreurId));
    }

    private void setFormOptions(HttpServletRequest request) {
        request.setAttribute("encadreurs", encadreurRepository.findAll());
        Memoire memoire = (Memoire) request.getAttribute("element");
        request.setAttribute("selectedEncadreurId", selectedId(request.getParameter("encadreurId"),
                memoire == null || memoire.getEncadreur() == null ? null : memoire.getEncadreur().getId()));
    }

    private void showFormError(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        request.setAttribute("type", TYPE);
        request.setAttribute("titre", TITLE);
        request.setAttribute("dateDepotMax", LocalDate.now());
        request.setAttribute("erreur", error);
        Memoire element = Memoire.create();
        String rawId = request.getParameter("id");
        if (rawId != null && !rawId.isBlank()) {
            try {
                Memoire existing = memoireService.findById(parseId(rawId)).orElse(null);
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
        return HistoryPagination.expose(request, response, historiqueActionService, SCREEN, TITLE, null);
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

    private static String requestLabel(HttpServletRequest request) {
        return "Mémoire : " + clean(request.getParameter("titre"));
    }

    private static String entityLabel(Memoire memoire) {
        return "Mémoire : " + clean(memoire.getTitre());
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
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

    private static UUID parseId(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("Identifiant invalide.");
        }
    }

    private static LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("La date de dépôt est invalide.");
        }
    }

    private static void forwardForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
    }

    private static void redirect(HttpServletRequest request, HttpServletResponse response,
            String parameter, String message) throws IOException {
        if ("erreur".equals(parameter)) {
            FlashMessages.erreur(request, message);
        } else {
            FlashMessages.succes(request, message);
        }
        response.sendRedirect(request.getContextPath() + "/admin/" + TYPE);
    }
}
