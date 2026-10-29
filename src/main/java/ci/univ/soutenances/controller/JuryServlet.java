package ci.univ.soutenances.controller;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import ci.univ.soutenances.config.AdminAuthorizationFilter;
import ci.univ.soutenances.model.ActionHistoryEntry;
import ci.univ.soutenances.model.Encadreur;
import ci.univ.soutenances.model.Etudiant;
import ci.univ.soutenances.model.Evaluation;
import ci.univ.soutenances.model.SessionUser;
import ci.univ.soutenances.model.Soutenance;
import ci.univ.soutenances.model.SoutenanceVue;
import ci.univ.soutenances.repository.EncadreurRepository;
import ci.univ.soutenances.service.HistoriqueActionService;
import ci.univ.soutenances.service.SoutenanceService;
import ci.univ.soutenances.util.FlashMessages;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = { "/admin/jury/*" })
public class JuryServlet extends HttpServlet {
    private static final String TYPE = "jury";
    private static final String TITLE = "Composition du jury";
    private static final String SCREEN = "JURY";
    private static final String FORM_VIEW = "/WEB-INF/views/admin/jury-form.jsp";
    private static final int HISTORY_PAGE_SIZE = 4;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @EJB
    private SoutenanceService soutenanceService;

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
            if (path.length != 0) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            String rawSoutenance = request.getParameter("soutenance");
            if (rawSoutenance == null || rawSoutenance.isBlank()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Soutenance non précisée.");
                return;
            }
            UUID soutenanceId = parseId(rawSoutenance);
            Soutenance soutenance = soutenanceService.findById(soutenanceId).orElse(null);
            if (soutenance == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            FlashMessages.exposer(request);
            setJuryAttributes(request, soutenance, findEtudiant(soutenanceId));
            if (!setHistoryPage(request, response)) {
                return;
            }
            forwardForm(request, response);
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        } catch (EJBException exception) {
            getServletContext().log("Impossible de charger l’écran de composition du jury.", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Impossible de charger cette page pour le moment.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String[] path = pathParts(request);
        if (path.length != 1 || !("ajouter".equals(path[0]) || "retirer".equals(path[0]))) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            UUID soutenanceId = parseId(request.getParameter("soutenance"));
            UUID encadreurId = parseId(request.getParameter("encadreurId"));
            Evaluation membre;
            String action;
            if ("ajouter".equals(path[0])) {
                membre = soutenanceService.ajouterMembre(soutenanceId, encadreurId);
                action = "CREATION";
            } else {
                membre = soutenanceService.retirerMembre(soutenanceId, encadreurId);
                action = "SUPPRESSION";
            }
            String label = membreLabel(membre, action);
            boolean recorded = recordAction(request, action, label);
            String message = "CREATION".equals(action)
                    ? "Membre ajouté au jury."
                    : "Membre retiré du jury.";
            if (!recorded) {
                message = message + " L’historique de l’action n’a pas pu être enregistré.";
            }
            if (recorded) {
                FlashMessages.succes(request, message);
            } else {
                FlashMessages.erreur(request, message);
            }
            response.sendRedirect(request.getContextPath() + "/admin/jury?soutenance=" + soutenanceId);
        } catch (IllegalArgumentException exception) {
            showJuryError(request, response, request.getParameter("soutenance"), exception.getMessage());
        } catch (EJBException exception) {
            String validationError = ValidationErrors.find(exception);
            if (validationError != null) {
                showJuryError(request, response, request.getParameter("soutenance"), validationError);
                return;
            }
            getServletContext().log("Échec d’une opération sur la composition du jury.", exception);
            showJuryError(request, response, request.getParameter("soutenance"),
                    "Opération impossible. Vérifie la composition du jury.");
        }
    }

    private void setJuryAttributes(HttpServletRequest request, Soutenance soutenance, Etudiant etudiant) {
        List<Evaluation> membres = soutenanceService.findMembres(soutenance.getId());
        Set<UUID> dejaMembres = membres.stream()
                .map(membre -> membre.getEncadreur().getId())
                .collect(Collectors.toSet());
        UUID encadreurMemoireId = encadreurMemoireId(soutenance);
        List<Encadreur> disponibles = encadreurRepository.findAll().stream()
                .filter(encadreur -> !dejaMembres.contains(encadreur.getId()))
                .filter(encadreur -> !encadreur.getId().equals(encadreurMemoireId))
                .toList();
        request.setAttribute("soutenance", soutenance);
        request.setAttribute("etudiant", etudiant);
        request.setAttribute("membres", membres);
        request.setAttribute("encadreurs", disponibles);
        request.setAttribute("selectedEncadreurId", selectedId(request.getParameter("encadreurId")));
        request.setAttribute("dateAffichee", soutenance.getDateHeure() == null
                ? "Date à confirmer"
                : soutenance.getDateHeure().format(DATE_FORMAT));
    }

    private static UUID encadreurMemoireId(Soutenance soutenance) {
        if (soutenance == null || soutenance.getMemoire() == null || soutenance.getMemoire().getEncadreur() == null) {
            return null;
        }
        return soutenance.getMemoire().getEncadreur().getId();
    }

    private Etudiant findEtudiant(UUID soutenanceId) {
        return soutenanceService.findVueById(soutenanceId)
                .map(SoutenanceVue::getEtudiant)
                .orElse(null);
    }

    private void showJuryError(HttpServletRequest request, HttpServletResponse response,
            String rawSoutenance, String error) throws ServletException, IOException {
        request.setAttribute("type", TYPE);
        request.setAttribute("titre", TITLE);
        request.setAttribute("erreur", error);
        try {
            UUID soutenanceId = parseId(rawSoutenance);
            Soutenance soutenance = soutenanceService.findById(soutenanceId).orElse(null);
            if (soutenance == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            setJuryAttributes(request, soutenance, findEtudiant(soutenanceId));
            if (!setHistoryPage(request, response)) {
                return;
            }
            forwardForm(request, response);
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        } catch (EJBException exception) {
            getServletContext().log("Impossible de réafficher la composition du jury.", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Impossible de charger cette page pour le moment.");
        }
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
                                action.getDateAction().format(DATE_FORMAT),
                                action.getDescription(), actionLabel(action.getTypeAction()),
                                action.getUtilisateur().getLogin(), TITLE))
                        .toList());
        request.setAttribute("historyPage", current);
        request.setAttribute("historyTotalPages", pages);
        request.setAttribute("historyTotalActions", total);
        request.setAttribute("historyFirstPage", Math.max(1, current - 2));
        request.setAttribute("historyLastPage", Math.min(pages, current + 2));
        request.setAttribute("historyEditBase",
                request.getContextPath() + "/admin/jury?soutenance=" + request.getParameter("soutenance"));
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

    private static String membreLabel(Evaluation membre, String action) {
        String nomComplet = clean(membre.getEncadreur().getPrenom()) + " " + clean(membre.getEncadreur().getNom());
        String titre = clean(membre.getSoutenance().getMemoire().getTitre());
        String verbe = "CREATION".equals(action) ? "Ajout de " : "Retrait de ";
        return verbe + nomComplet.trim() + " du jury : " + titre;
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

    private static UUID parseId(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("Identifiant invalide.");
        }
    }

    private static UUID selectedId(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            return null;
        }
        try {
            return parseId(rawId);
        } catch (IllegalArgumentException exception) {
            return null;
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

    private static void forwardForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
    }
}
