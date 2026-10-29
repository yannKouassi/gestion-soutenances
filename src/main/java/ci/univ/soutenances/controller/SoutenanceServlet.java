package ci.univ.soutenances.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import ci.univ.soutenances.config.AdminAuthorizationFilter;
import ci.univ.soutenances.model.JuryResume;
import ci.univ.soutenances.model.SessionUser;
import ci.univ.soutenances.model.Soutenance;
import ci.univ.soutenances.model.SoutenanceListeEntry;
import ci.univ.soutenances.model.SoutenanceVue;
import ci.univ.soutenances.model.StatutSoutenance;
import ci.univ.soutenances.repository.EtudiantRepository;
import ci.univ.soutenances.repository.FiliereRepository;
import ci.univ.soutenances.service.HistoriqueActionService;
import ci.univ.soutenances.service.SoutenanceService;
import ci.univ.soutenances.util.Format;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = { "/admin/soutenances", "/admin/soutenances/*" })
public class SoutenanceServlet extends HttpServlet {
    private static final String TYPE = "soutenances";
    private static final String TITLE = "Gestion Globale des Soutenances";
    private static final String SCREEN = "SOUTENANCE";
    private static final String LIST_VIEW = "/WEB-INF/views/admin/soutenance-list.jsp";
    private static final String DETAIL_VIEW = "/WEB-INF/views/admin/soutenance-detail.jsp";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @EJB
    private SoutenanceService soutenanceService;

    @EJB
    private HistoriqueActionService historiqueActionService;

    @EJB
    private EtudiantRepository etudiantRepository;

    @EJB
    private FiliereRepository filiereRepository;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("type", TYPE);
        request.setAttribute("titre", TITLE);

        try {
            if ("detail".equals(request.getParameter("vue"))) {
                afficherDetail(request, response);
                return;
            }

            StatutSoutenance statut = parseStatut(request.getParameter("statut"));
            LocalDate date = parseDate(request.getParameter("date"));
            UUID etudiantId = parseOptionalId(request.getParameter("etudiant"));
            UUID filiereId = parseOptionalId(request.getParameter("filiere"));

            List<SoutenanceListeEntry> entries = assembler(
                    soutenanceService.lister(statut, etudiantId, filiereId, date));
            request.setAttribute("entries", entries);
            request.setAttribute("etudiants", etudiantRepository.findAll());
            request.setAttribute("filieres", filiereRepository.findAll());
            request.setAttribute("statuts", List.of(StatutSoutenance.values()));
            request.setAttribute("filtreStatut", statut == null ? "" : statut.name());
            request.setAttribute("filtreDate", date == null ? "" : date.toString());
            request.setAttribute("filtreEtudiant", etudiantId == null ? "" : etudiantId.toString());
            request.setAttribute("filtreFiliere", filiereId == null ? "" : filiereId.toString());
            request.setAttribute("message", request.getParameter("message"));
            request.setAttribute("erreur", request.getParameter("erreur"));
            request.getRequestDispatcher(LIST_VIEW).forward(request, response);
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        } catch (EJBException exception) {
            getServletContext().log("Impossible de charger l’écran des soutenances " + TYPE, exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Impossible de charger cette page pour le moment.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String[] path = pathParts(request);
        if (path.length != 1 || !"annuler".equals(path[0])) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        UUID soutenanceId;
        try {
            soutenanceId = parseOptionalId(request.getParameter("id"));
            if (soutenanceId == null) {
                throw new IllegalArgumentException("Soutenance non précisée.");
            }
            Soutenance soutenance = soutenanceService.annuler(soutenanceId);
            String label = "Annulation de la soutenance : " + clean(soutenance.getMemoire().getTitre());
            boolean recorded = recordAction(request, "MODIFICATION", label);
            String message = "Soutenance annulée.";
            if (!recorded) {
                message = message + " L’historique de l’action n’a pas pu être enregistré.";
            }
            redirectDetail(request, response, soutenanceId, recorded ? "message" : "erreur", message);
        } catch (IllegalArgumentException exception) {
            UUID id = parseOptionalId(request.getParameter("id"));
            if (id == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
                return;
            }
            redirectDetail(request, response, id, "erreur", exception.getMessage());
        } catch (EJBException exception) {
            getServletContext().log("Échec de l’annulation d’une soutenance.", exception);
            UUID id = parseOptionalId(request.getParameter("id"));
            if (id == null) {
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "Annulation impossible pour le moment.");
                return;
            }
            redirectDetail(request, response, id, "erreur",
                    "Annulation impossible pour le moment.");
        }
    }

    private void afficherDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UUID soutenanceId = parseOptionalId(request.getParameter("id"));
        if (soutenanceId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Soutenance non précisée.");
            return;
        }
        SoutenanceVue vue = soutenanceService.findVueById(soutenanceId).orElse(null);
        if (vue == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("vueSoutenance", vue);
        request.setAttribute("message", request.getParameter("message"));
        request.setAttribute("erreur", request.getParameter("erreur"));
        request.setAttribute("membres", soutenanceService.findMembres(soutenanceId));
        request.setAttribute("noteFinale", soutenanceService.calculerMoyenne(soutenanceId)
                .map(Format::note)
                .orElse(null));
        request.setAttribute("dateAffichee", vue.getSoutenance().getDateHeure() == null
                ? null
                : vue.getSoutenance().getDateHeure().format(DATE_FORMAT));
        request.getRequestDispatcher(DETAIL_VIEW).forward(request, response);
    }

    private List<SoutenanceListeEntry> assembler(List<SoutenanceVue> soutenances) {
        List<UUID> ids = soutenances.stream()
                .map(vue -> vue.getSoutenance().getId())
                .toList();
        Map<UUID, JuryResume> jurys = soutenanceService.resumerJurys(ids);
        List<SoutenanceListeEntry> entries = new ArrayList<>(soutenances.size());
        for (SoutenanceVue vue : soutenances) {
            JuryResume resume = jurys.get(vue.getSoutenance().getId());
            int tailleJury = resume == null ? 0 : resume.getNombreMembres();
            String noteAffichee = resume == null || resume.getMoyenne() == null
                    ? null
                    : Format.note(resume.getMoyenne());
            entries.add(new SoutenanceListeEntry(vue, formatDate(vue), tailleJury, noteAffichee));
        }
        return entries;
    }

    private static String formatDate(SoutenanceVue vue) {
        return vue.getSoutenance().getDateHeure() == null
                ? null
                : vue.getSoutenance().getDateHeure().format(DATE_FORMAT);
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

    private static void redirectDetail(HttpServletRequest request, HttpServletResponse response,
            UUID soutenanceId, String parameter, String message) throws IOException {
        response.sendRedirect(request.getContextPath() + "/admin/soutenances?vue=detail&id=" + soutenanceId
                + "&" + parameter + "=" + encode(message));
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String[] pathParts(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null || path.equals("/") ? new String[0] : path.substring(1).split("/");
    }

    private static StatutSoutenance parseStatut(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return StatutSoutenance.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Statut de soutenance invalide.");
        }
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Date de soutenance invalide.");
        }
    }

    private static UUID parseOptionalId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Identifiant de filtre invalide.");
        }
    }
}
