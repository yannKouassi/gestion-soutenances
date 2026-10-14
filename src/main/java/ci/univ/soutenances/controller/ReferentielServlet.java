package ci.univ.soutenances.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

import ci.univ.soutenances.config.AdminAuthorizationFilter;
import ci.univ.soutenances.model.ActionHistoryEntry;
import ci.univ.soutenances.model.BaseEntity;
import ci.univ.soutenances.model.Departement;
import ci.univ.soutenances.model.Filiere;
import ci.univ.soutenances.model.Grade;
import ci.univ.soutenances.model.Salle;
import ci.univ.soutenances.model.SessionUser;
import ci.univ.soutenances.service.DepartementService;
import ci.univ.soutenances.service.FiliereService;
import ci.univ.soutenances.service.GradeService;
import ci.univ.soutenances.service.HistoriqueActionService;
import ci.univ.soutenances.service.SalleService;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {
        "/admin/departements/*",
        "/admin/filieres/*",
        "/admin/grades/*",
        "/admin/salles/*"
})
public class ReferentielServlet extends HttpServlet {
    private static final int HISTORY_PAGE_SIZE = 4;
    private static final DateTimeFormatter HISTORY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Map<String, String> TITLES = Map.of(
            "departements", "Départements",
            "filieres", "Filières",
            "grades", "Grades",
            "salles", "Salles");

    @EJB
    private DepartementService departementService;

    @EJB
    private FiliereService filiereService;

    @EJB
    private GradeService gradeService;

    @EJB
    private SalleService salleService;

    @EJB
    private HistoriqueActionService historiqueActionService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String type = typeFromRequest(request);
        String[] path = pathParts(request);
        request.setAttribute("type", type);
        request.setAttribute("titre", TITLES.get(type));

        try {
            if (path.length == 0) {
                if ("form".equals(request.getParameter("vue"))) {
                    String rawId = request.getParameter("id");
                    BaseEntity element = rawId == null || rawId.isBlank()
                            ? newEmptyEntity(type)
                            : findById(type, parseId(rawId));
                    if (element == null) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return;
                    }
                    request.setAttribute("element", element);
                    setFormOptions(request, type);
                    if (!setHistoryPage(request, response, type)) {
                        return;
                    }
                    forwardForm(request, response, type);
                    return;
                }

                request.setAttribute("elements", findAll(type));
                if (!setHistoryPage(request, response, type)) {
                    return;
                }
                request.setAttribute("message", request.getParameter("message"));
                request.setAttribute("erreur", request.getParameter("erreur"));
                request.getRequestDispatcher(listView(type)).forward(request, response);
                return;
            }

            if (path.length == 2 && "modifier".equals(path[1])) {
                BaseEntity element = findById(type, parseId(path[0]));
                if (element == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                request.setAttribute("element", element);
                setFormOptions(request, type);
                if (!setHistoryPage(request, response, type)) {
                    return;
                }
                forwardForm(request, response, type);
                return;
            }

            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        } catch (EJBException exception) {
            getServletContext().log("Impossible de charger le référentiel ou son historique : " + type, exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Impossible de charger cette page pour le moment.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String type = typeFromRequest(request);
        String[] path = pathParts(request);
        if (path.length != 1 || !("enregistrer".equals(path[0]) || "supprimer".equals(path[0]))) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            if ("supprimer".equals(path[0])) {
                BaseEntity element = findById(type, parseId(request.getParameter("id")));
                if (element == null) {
                    throw new IllegalArgumentException("Élément introuvable.");
                }
                String label = entityLabel(type, element);
                delete(type, element.getId());
                if (recordAction(request, type, "SUPPRESSION", label)) {
                    redirect(request, response, type, "message", "Élément supprimé.");
                } else {
                    redirect(request, response, type, "erreur",
                            "Élément supprimé, mais l’historique de l’action n’a pas pu être enregistré.");
                }
                return;
            }

            String action = isCreation(request) ? "CREATION" : "MODIFICATION";
            String label = requestLabel(type, request);
            saveFromRequest(request, type);
            if (recordAction(request, type, action, label)) {
                redirect(request, response, type, "message", "Modifications enregistrées.");
            } else {
                redirect(request, response, type, "erreur",
                        "Modifications enregistrées, mais l’historique de l’action n’a pas pu être enregistré.");
            }
        } catch (IllegalArgumentException exception) {
            if ("supprimer".equals(path[0])) {
                redirect(request, response, type, "erreur", exception.getMessage());
                return;
            }
            showFormError(request, response, type, exception.getMessage());
        } catch (EJBException exception) {
            getServletContext().log("Échec de l’opération sur le référentiel " + type, exception);
            if ("supprimer".equals(path[0])) {
                redirect(request, response, type, "erreur",
                        "Suppression impossible : cet élément est peut-être encore utilisé.");
                return;
            }
            showFormError(request, response, type,
                    "Enregistrement impossible. Vérifie les codes uniques et les éléments liés.");
        }
    }

    private Object findAll(String type) {
        return switch (type) {
            case "departements" -> departementService.findAll();
            case "filieres" -> filiereService.findAll();
            case "grades" -> gradeService.findAll();
            case "salles" -> salleService.findAll();
            default -> throw new IllegalArgumentException("Type de référentiel inconnu.");
        };
    }

    private BaseEntity findById(String type, UUID id) {
        return switch (type) {
            case "departements" -> departementService.findById(id).orElse(null);
            case "filieres" -> filiereService.findById(id).orElse(null);
            case "grades" -> gradeService.findById(id).orElse(null);
            case "salles" -> salleService.findById(id).orElse(null);
            default -> throw new IllegalArgumentException("Type de référentiel inconnu.");
        };
    }

    private BaseEntity newEmptyEntity(String type) {
        return switch (type) {
            case "departements" -> new Departement();
            case "filieres" -> new Filiere();
            case "grades" -> new Grade();
            case "salles" -> new Salle();
            default -> throw new IllegalArgumentException("Type de référentiel inconnu.");
        };
    }

    private void saveFromRequest(HttpServletRequest request, String type) {
        String rawId = request.getParameter("id");
        UUID id = rawId == null || rawId.isBlank() ? null : parseId(rawId);
        switch (type) {
            case "departements" -> {
                Departement departement = id == null
                        ? new Departement()
                        : departementService.findById(id).orElseThrow(
                                () -> new IllegalArgumentException("Département introuvable."));
                departement.setCode(request.getParameter("code"));
                departement.setNom(request.getParameter("nom"));
                departementService.save(departement);
            }
            case "filieres" -> {
                Filiere filiere = id == null
                        ? new Filiere()
                        : filiereService.findById(id).orElseThrow(
                                () -> new IllegalArgumentException("Filière introuvable."));
                filiere.setCode(request.getParameter("code"));
                filiere.setLibelle(request.getParameter("libelle"));
                filiereService.save(filiere, parseId(request.getParameter("departementId")));
            }
            case "grades" -> {
                Grade grade = id == null
                        ? new Grade()
                        : gradeService.findById(id).orElseThrow(
                                () -> new IllegalArgumentException("Grade introuvable."));
                grade.setCode(request.getParameter("code"));
                grade.setLibelle(request.getParameter("libelle"));
                gradeService.save(grade);
            }
            case "salles" -> {
                Salle salle = id == null
                        ? new Salle()
                        : salleService.findById(id).orElseThrow(
                                () -> new IllegalArgumentException("Salle introuvable."));
                salle.setNomSalle(request.getParameter("nomSalle"));
                salle.setCapacite(parseCapacity(request.getParameter("capacite")));
                salleService.save(salle, parseId(request.getParameter("departementId")));
            }
            default -> throw new IllegalArgumentException("Type de référentiel inconnu.");
        }
    }

    private void delete(String type, UUID id) {
        switch (type) {
            case "departements" -> departementService.delete(id);
            case "filieres" -> filiereService.delete(id);
            case "grades" -> gradeService.delete(id);
            case "salles" -> salleService.delete(id);
            default -> throw new IllegalArgumentException("Type de référentiel inconnu.");
        }
    }

    private boolean setHistoryPage(HttpServletRequest request, HttpServletResponse response, String type)
            throws IOException {
        int requestedPage = parsePage(request.getParameter("page"));
        String code = screenCode(type);
        long totalActions = historiqueActionService.countByScreen(code);
        int totalPages = (int) ((totalActions + HISTORY_PAGE_SIZE - 1) / HISTORY_PAGE_SIZE);
        int currentPage = totalPages == 0 ? 1 : Math.min(requestedPage, totalPages);
        long offset = (long) (currentPage - 1) * HISTORY_PAGE_SIZE;
        if (offset > Integer.MAX_VALUE) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Numéro de page trop grand.");
            return false;
        }

        request.setAttribute("historyActions",
                historiqueActionService.findPage(code, (int) offset, HISTORY_PAGE_SIZE).stream()
                        .map(action -> new ActionHistoryEntry(
                                action.getDateAction().format(HISTORY_DATE_FORMAT),
                                action.getDescription(),
                                actionLabel(action.getTypeAction()),
                                action.getUtilisateur().getLogin(),
                                TITLES.get(type)))
                        .toList());
        request.setAttribute("historyPage", currentPage);
        request.setAttribute("historyTotalPages", totalPages);
        request.setAttribute("historyTotalActions", totalActions);
        request.setAttribute("historyFirstPage", Math.max(1, currentPage - 2));
        request.setAttribute("historyLastPage", Math.min(totalPages, currentPage + 2));
        return true;
    }

    private boolean recordAction(HttpServletRequest request, String type, String action, String label) {
        Object user = request.getSession(false).getAttribute(AdminAuthorizationFilter.SESSION_USER_ATTRIBUTE);
        if (!(user instanceof SessionUser sessionUser)) {
            getServletContext().log("Impossible d’enregistrer l’historique : utilisateur de session absent.");
            return false;
        }
        try {
            historiqueActionService.record(screenCode(type), action, label, sessionUser.id());
            return true;
        } catch (EJBException | IllegalArgumentException exception) {
            getServletContext().log("Impossible d’enregistrer l’action " + action + " sur " + type, exception);
            return false;
        }
    }

    private static String screenCode(String type) {
        return switch (type) {
            case "grades" -> "GRADE";
            case "departements" -> "DEPARTEMENT";
            case "filieres" -> "FILIERE";
            case "salles" -> "SALLE";
            default -> throw new IllegalArgumentException("Type de référentiel inconnu.");
        };
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

    private static boolean isCreation(HttpServletRequest request) {
        String id = request.getParameter("id");
        return id == null || id.isBlank();
    }

    private static String requestLabel(String type, HttpServletRequest request) {
        return switch (type) {
            case "grades" -> "Grade : " + clean(request.getParameter("code")) + " — "
                    + clean(request.getParameter("libelle"));
            case "departements" -> "Département : " + clean(request.getParameter("code")) + " — "
                    + clean(request.getParameter("nom"));
            case "filieres" -> "Filière : " + clean(request.getParameter("code")) + " — "
                    + clean(request.getParameter("libelle"));
            case "salles" -> "Salle : " + clean(request.getParameter("nomSalle")) + " — capacité "
                    + clean(request.getParameter("capacite"));
            default -> throw new IllegalArgumentException("Type de référentiel inconnu.");
        };
    }

    private static String entityLabel(String type, BaseEntity element) {
        return switch (type) {
            case "grades" -> {
                Grade grade = (Grade) element;
                yield "Grade : " + grade.getCode() + " — " + grade.getLibelle();
            }
            case "departements" -> {
                Departement departement = (Departement) element;
                yield "Département : " + departement.getCode() + " — " + departement.getNom();
            }
            case "filieres" -> {
                Filiere filiere = (Filiere) element;
                yield "Filière : " + filiere.getCode() + " — " + filiere.getLibelle();
            }
            case "salles" -> {
                Salle salle = (Salle) element;
                yield "Salle : " + salle.getNomSalle() + " — capacité " + salle.getCapacite();
            }
            default -> throw new IllegalArgumentException("Type de référentiel inconnu.");
        };
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static int parsePage(String value) {
        if (value == null || value.isBlank()) {
            return 1;
        }
        try {
            int page = Integer.parseInt(value);
            if (page < 1) {
                throw new IllegalArgumentException("Le numéro de page doit être positif.");
            }
            return page;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Numéro de page invalide.");
        }
    }

    private void showFormError(HttpServletRequest request, HttpServletResponse response,
            String type, String error) throws ServletException, IOException {
        request.setAttribute("type", type);
        request.setAttribute("titre", TITLES.get(type));
        request.setAttribute("erreur", error);
        String id = request.getParameter("id");
        BaseEntity element = newEmptyEntity(type);
        if (id != null && !id.isBlank()) {
            try {
                BaseEntity existing = findById(type, parseId(id));
                if (existing != null) {
                    element = existing;
                }
            } catch (IllegalArgumentException ignored) {
                request.setAttribute("idInvalide", true);
            }
        }
        request.setAttribute("element", element);
        setFormOptions(request, type);
        forwardForm(request, response, type);
    }

    private void setFormOptions(HttpServletRequest request, String type) {
        if ("filieres".equals(type) || "salles".equals(type)) {
            request.setAttribute("departements", departementService.findAll());
            String selectedId = request.getParameter("departementId");
            if (selectedId != null && !selectedId.isBlank()) {
                try {
                    request.setAttribute("selectedDepartementId", parseId(selectedId));
                } catch (IllegalArgumentException ignored) {
                    request.setAttribute("departementInvalide", true);
                }
            } else {
                Object element = request.getAttribute("element");
                if (element instanceof Filiere filiere && filiere.getDepartement() != null) {
                    request.setAttribute("selectedDepartementId", filiere.getDepartement().getId());
                } else if (element instanceof Salle salle && salle.getDepartement() != null) {
                    request.setAttribute("selectedDepartementId", salle.getDepartement().getId());
                }
            }
        }
    }

    private static String typeFromRequest(HttpServletRequest request) {
        String path = request.getServletPath();
        String type = path.substring(path.lastIndexOf('/') + 1);
        return switch (type) {
            case "departements", "filieres", "grades", "salles" -> type;
            default -> throw new IllegalArgumentException("Type de référentiel inconnu.");
        };
    }

    private static String[] pathParts(HttpServletRequest request) {
        String path = request.getPathInfo();
        if (path == null || path.equals("/")) {
            return new String[0];
        }
        return path.substring(1).split("/");
    }

    private static String listView(String type) {
        return "/WEB-INF/views/admin/" + singular(type) + "-list.jsp";
    }

    private static String formView(String type) {
        return "/WEB-INF/views/admin/" + singular(type) + "-form.jsp";
    }

    private static String singular(String type) {
        return switch (type) {
            case "departements" -> "departement";
            case "filieres" -> "filiere";
            case "grades" -> "grade";
            case "salles" -> "salle";
            default -> throw new IllegalArgumentException("Type de référentiel inconnu.");
        };
    }

    private static UUID parseId(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("Identifiant UUID invalide.");
        }
    }

    private static int parseCapacity(String value) {
        try {
            int capacity = Integer.parseInt(value);
            if (capacity <= 0) {
                throw new IllegalArgumentException("La capacité doit être supérieure à zéro.");
            }
            return capacity;
        } catch (NumberFormatException | NullPointerException exception) {
            throw new IllegalArgumentException("La capacité doit être un nombre entier positif.");
        }
    }

    private static void forwardForm(HttpServletRequest request, HttpServletResponse response, String type)
            throws ServletException, IOException {
        request.getRequestDispatcher(formView(type)).forward(request, response);
    }

    private static void redirect(HttpServletRequest request, HttpServletResponse response,
            String type, String parameter, String message) throws IOException {
        response.sendRedirect(request.getContextPath() + "/admin/" + type + "?"
                + parameter + "=" + URLEncoder.encode(message, StandardCharsets.UTF_8));
    }
}
