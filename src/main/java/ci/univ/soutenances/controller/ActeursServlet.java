package ci.univ.soutenances.controller;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

import ci.univ.soutenances.config.AdminAuthorizationFilter;
import ci.univ.soutenances.model.ActionHistoryEntry;
import ci.univ.soutenances.model.Encadreur;
import ci.univ.soutenances.model.Etudiant;
import ci.univ.soutenances.model.SessionUser;
import ci.univ.soutenances.service.CompteService;
import ci.univ.soutenances.util.FlashMessages;
import ci.univ.soutenances.service.DepartementService;
import ci.univ.soutenances.service.EncadreurService;
import ci.univ.soutenances.service.EtudiantService;
import ci.univ.soutenances.service.FiliereService;
import ci.univ.soutenances.service.GradeService;
import ci.univ.soutenances.service.HistoriqueActionService;
import ci.univ.soutenances.repository.MemoireRepository;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {
        "/admin/encadreurs/*",
        "/admin/etudiants/*",
        "/admin/utilisateurs/*"
})
public class ActeursServlet extends HttpServlet {
    private static final int HISTORY_PAGE_SIZE = 4;
    private static final DateTimeFormatter HISTORY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Map<String, String> TITLES = Map.of(
            "encadreurs", "Encadreurs",
            "etudiants", "Étudiants",
            "utilisateurs", "Comptes utilisateurs");

    @EJB
    private EncadreurService encadreurService;
    @EJB
    private EtudiantService etudiantService;
    @EJB
    private CompteService compteService;
    @EJB
    private GradeService gradeService;
    @EJB
    private DepartementService departementService;
    @EJB
    private FiliereService filiereService;
    @EJB
    private MemoireRepository memoireRepository;
    @EJB
    private HistoriqueActionService historiqueActionService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String type = type(request);
        request.setAttribute("type", type);
        request.setAttribute("titre", TITLES.get(type));
        try {
            String[] parts = pathParts(request);
            if ("utilisateurs".equals(type)) {
                if (parts.length != 0) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                request.setAttribute("accounts", compteService.findAll());
                FlashMessages.exposer(request);
                request.getRequestDispatcher("/WEB-INF/views/admin/utilisateur-list.jsp").forward(request, response);
                return;
            }
            if (parts.length == 0 && "form".equals(request.getParameter("vue"))) {
                String rawId = request.getParameter("id");
                Object element = rawId == null || rawId.isBlank() ? newActor(type) : findActor(type, parseId(rawId));
                if (element == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                request.setAttribute("element", element);
                setFormOptions(request, type);
                FlashMessages.exposer(request);
                if (!setHistoryPage(request, response, type)) {
                    return;
                }
                forwardForm(request, response, type);
                return;
            }
            if (parts.length == 0) {
                request.setAttribute("elements", findAll(type));
                if ("encadreurs".equals(type)) {
                    request.setAttribute("grades", gradeService.findAll());
                    request.setAttribute("departements", departementService.findAll());
                } else {
                    request.setAttribute("filieres", filiereService.findAll());
                }
                FlashMessages.exposer(request);
                request.getRequestDispatcher(listView(type)).forward(request, response);
                return;
            }
            if (parts.length == 2 && "modifier".equals(parts[1])) {
                Object element = findActor(type, parseId(parts[0]));
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
            getServletContext().log("Impossible de charger l’écran administrateur " + type, exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Impossible de charger cette page pour le moment.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String type = type(request);
        String[] parts = pathParts(request);
        try {
            if ("utilisateurs".equals(type)) {
                handleAccountAction(request, response, parts);
                return;
            }
            if (parts.length != 1 || !"enregistrer".equals(parts[0])) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            boolean creation = request.getParameter("id") == null || request.getParameter("id").isBlank();
            String label;
            if ("encadreurs".equals(type)) {
                Encadreur encadreur = creation ? new Encadreur()
                        : encadreurService.findById(parseId(request.getParameter("id")))
                                .orElseThrow(() -> new IllegalArgumentException("Encadreur introuvable."));
                encadreur.setNom(request.getParameter("nom"));
                encadreur.setPrenom(request.getParameter("prenom"));
                encadreur.setEmail(request.getParameter("email"));
                label = "Encadreur : " + clean(encadreur.getPrenom()) + " " + clean(encadreur.getNom());
                encadreurService.save(encadreur, parseId(request.getParameter("gradeId")),
                        parseId(request.getParameter("departementId")));
            } else {
                Etudiant etudiant = creation ? new Etudiant()
                        : etudiantService.findById(parseId(request.getParameter("id")))
                                .orElseThrow(() -> new IllegalArgumentException("Étudiant introuvable."));
                etudiant.setMatricule(request.getParameter("matricule"));
                etudiant.setNom(request.getParameter("nom"));
                etudiant.setPrenom(request.getParameter("prenom"));
                etudiant.setEmail(request.getParameter("email"));
                String memoryId = request.getParameter("memoireId");
                label = "Étudiant : " + clean(etudiant.getMatricule()) + " — "
                        + clean(etudiant.getPrenom()) + " " + clean(etudiant.getNom());
                etudiantService.save(etudiant, parseId(request.getParameter("filiereId")),
                        memoryId == null || memoryId.isBlank() ? null : parseId(memoryId));
            }
            String screen = screenCode(type);
            if (!recordAction(request, screen, creation ? "CREATION" : "MODIFICATION", label)) {
                redirect(request, response, type, "erreur",
                        "Enregistrement effectué, mais l’historique n’a pas pu être enregistré.");
            } else {
                if (creation) {
                    FlashMessages.succes(request, "Compte créé et identifiants envoyés par e-mail.");
                    response.sendRedirect(request.getContextPath() + "/admin/" + type + "?vue=form");
                } else {
                    redirect(request, response, type, "message", "Modifications enregistrées.");
                }
            }
        } catch (IllegalArgumentException exception) {
            if ("utilisateurs".equals(type)) {
                redirect(request, response, type, "erreur", exception.getMessage());
            } else {
                showActorFormError(request, response, type, exception.getMessage());
            }
        } catch (EJBException exception) {
            getServletContext().log("Échec d’une opération du module Acteurs & Comptes.", exception);
            String error = "L’opération a échoué. Vérifie les champs et la configuration SMTP.";
            if ("utilisateurs".equals(type)) {
                redirect(request, response, type, "erreur", error);
            } else {
                showActorFormError(request, response, type, error);
            }
        }
    }

    private void handleAccountAction(HttpServletRequest request, HttpServletResponse response, String[] parts)
            throws IOException {
        if (parts.length != 1) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        UUID id = parseId(request.getParameter("id"));
        switch (parts[0]) {
            case "activer" -> {
                compteService.setActive(id, true);
                auditAccountAction(request, "MODIFICATION", "Activation du compte " + id);
                redirect(request, response, "utilisateurs", "message", "Compte activé.");
            }
            case "desactiver" -> {
                compteService.setActive(id, false);
                auditAccountAction(request, "MODIFICATION", "Désactivation du compte " + id);
                redirect(request, response, "utilisateurs", "message", "Compte désactivé.");
            }
            case "reinitialiser-mot-de-passe" -> {
                compteService.resetPassword(id);
                auditAccountAction(request, "MODIFICATION", "Réinitialisation du mot de passe du compte " + id);
                redirect(request, response, "utilisateurs", "message",
                        "Nouveau mot de passe temporaire envoyé par e-mail.");
            }
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void auditAccountAction(HttpServletRequest request, String action, String label) {
        Object user = request.getSession(false).getAttribute(AdminAuthorizationFilter.SESSION_USER_ATTRIBUTE);
        if (user instanceof SessionUser sessionUser) {
            try {
                historiqueActionService.record("UTILISATEUR", action, label, sessionUser.id());
            } catch (EJBException | IllegalArgumentException exception) {
                getServletContext().log("Impossible d’enregistrer l’historique du compte utilisateur.", exception);
            }
        }
    }

    private Object findAll(String type) {
        return switch (type) {
            case "encadreurs" -> encadreurService.findAll();
            case "etudiants" -> etudiantService.findAll();
            default -> throw new IllegalArgumentException("Écran inconnu.");
        };
    }

    private Object findActor(String type, UUID id) {
        return switch (type) {
            case "encadreurs" -> encadreurService.findById(id).orElse(null);
            case "etudiants" -> etudiantService.findById(id).orElse(null);
            default -> throw new IllegalArgumentException("Écran inconnu.");
        };
    }

    private static Object newActor(String type) {
        return switch (type) {
            case "encadreurs" -> new Encadreur();
            case "etudiants" -> new Etudiant();
            default -> throw new IllegalArgumentException("Écran inconnu.");
        };
    }

    private void setFormOptions(HttpServletRequest request, String type) {
        if ("encadreurs".equals(type)) {
            request.setAttribute("grades", gradeService.findAll());
            request.setAttribute("departements", departementService.findAll());
            Encadreur encadreur = (Encadreur) request.getAttribute("element");
            request.setAttribute("selectedGradeId", selectedId(request.getParameter("gradeId"),
                    encadreur == null || encadreur.getGrade() == null ? null : encadreur.getGrade().getId()));
            request.setAttribute("selectedDepartementId", selectedId(request.getParameter("departementId"),
                    encadreur == null || encadreur.getDepartement() == null
                            ? null : encadreur.getDepartement().getId()));
        } else {
            request.setAttribute("filieres", filiereService.findAll());
            request.setAttribute("memoires", memoireRepository.findAll());
            Etudiant etudiant = (Etudiant) request.getAttribute("element");
            request.setAttribute("selectedFiliereId", selectedId(request.getParameter("filiereId"),
                    etudiant == null || etudiant.getFiliere() == null ? null : etudiant.getFiliere().getId()));
            request.setAttribute("selectedMemoireId", selectedId(request.getParameter("memoireId"),
                    etudiant == null || etudiant.getMemoire() == null ? null : etudiant.getMemoire().getId()));
        }
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

    private boolean setHistoryPage(HttpServletRequest request, HttpServletResponse response, String type)
            throws IOException {
        int requestedPage = parsePage(request.getParameter("page"));
        String screen = screenCode(type);
        long total = historiqueActionService.countByScreen(screen);
        int pages = (int) ((total + HISTORY_PAGE_SIZE - 1) / HISTORY_PAGE_SIZE);
        int current = pages == 0 ? 1 : Math.min(requestedPage, pages);
        long offset = (long) (current - 1) * HISTORY_PAGE_SIZE;
        if (offset > Integer.MAX_VALUE) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Numéro de page trop grand.");
            return false;
        }
        request.setAttribute("historyActions",
                historiqueActionService.findPage(screen, (int) offset, HISTORY_PAGE_SIZE).stream()
                        .map(action -> new ActionHistoryEntry(
                                action.getDateAction().format(HISTORY_DATE_FORMAT),
                                action.getDescription(), actionLabel(action.getTypeAction()),
                                action.getUtilisateur().getLogin(), TITLES.get(type)))
                        .toList());
        request.setAttribute("historyPage", current);
        request.setAttribute("historyTotalPages", pages);
        request.setAttribute("historyTotalActions", total);
        request.setAttribute("historyFirstPage", Math.max(1, current - 2));
        request.setAttribute("historyLastPage", Math.min(pages, current + 2));
        return true;
    }

    private void showActorFormError(HttpServletRequest request, HttpServletResponse response,
            String type, String error) throws ServletException, IOException {
        request.setAttribute("type", type);
        request.setAttribute("titre", TITLES.get(type));
        request.setAttribute("erreur", error);
        Object element = newActor(type);
        String rawId = request.getParameter("id");
        if (rawId != null && !rawId.isBlank()) {
            try {
                Object existing = findActor(type, parseId(rawId));
                if (existing != null) {
                    element = existing;
                }
            } catch (IllegalArgumentException ignored) {
                request.setAttribute("idInvalide", true);
            }
        }
        request.setAttribute("element", element);
        setFormOptions(request, type);
        try {
            setHistoryPage(request, response, type);
        } catch (IOException exception) {
            throw exception;
        }
        forwardForm(request, response, type);
    }

    private boolean recordAction(HttpServletRequest request, String screen, String action, String label) {
        Object user = request.getSession(false).getAttribute(AdminAuthorizationFilter.SESSION_USER_ATTRIBUTE);
        if (!(user instanceof SessionUser sessionUser)) {
            getServletContext().log("Impossible d’enregistrer l’historique : utilisateur de session absent.");
            return false;
        }
        try {
            historiqueActionService.record(screen, action, label, sessionUser.id());
            return true;
        } catch (EJBException | IllegalArgumentException exception) {
            getServletContext().log("Impossible d’enregistrer l’action " + action + " sur " + screen, exception);
            return false;
        }
    }

    private static String type(HttpServletRequest request) {
        String value = request.getServletPath();
        String type = value.substring(value.lastIndexOf('/') + 1);
        if (!TITLES.containsKey(type)) {
            throw new IllegalArgumentException("Écran inconnu.");
        }
        return type;
    }

    private static String[] pathParts(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null || path.equals("/") ? new String[0] : path.substring(1).split("/");
    }

    private static String screenCode(String type) {
        return switch (type) {
            case "encadreurs" -> "ENCADREUR";
            case "etudiants" -> "ETUDIANT";
            case "utilisateurs" -> "UTILISATEUR";
            default -> throw new IllegalArgumentException("Écran d’historique inconnu.");
        };
    }

    private static String listView(String type) {
        return "/WEB-INF/views/admin/" + (type.equals("encadreurs") ? "encadreur" : "etudiant") + "-list.jsp";
    }

    private static String formView(String type) {
        return "/WEB-INF/views/admin/" + (type.equals("encadreurs") ? "encadreur" : "etudiant") + "-form.jsp";
    }

    private static UUID parseId(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("Identifiant invalide.");
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

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static String actionLabel(String typeAction) {
        if (typeAction.endsWith("_CREATION")) return "Création";
        if (typeAction.endsWith("_MODIFICATION")) return "Modification";
        if (typeAction.endsWith("_SUPPRESSION")) return "Suppression";
        return typeAction;
    }

    private static void forwardForm(HttpServletRequest request, HttpServletResponse response, String type)
            throws ServletException, IOException {
        request.getRequestDispatcher(formView(type)).forward(request, response);
    }

    private static void redirect(HttpServletRequest request, HttpServletResponse response,
            String type, String parameter, String message) throws IOException {
        if ("erreur".equals(parameter)) {
            FlashMessages.erreur(request, message);
        } else {
            FlashMessages.succes(request, message);
        }
        response.sendRedirect(request.getContextPath() + "/admin/" + type);
    }
}
