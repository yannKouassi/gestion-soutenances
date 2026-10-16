package ci.univ.soutenances.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ci.univ.soutenances.config.EncadreurAuthorizationFilter;
import ci.univ.soutenances.model.ConvocationVue;
import ci.univ.soutenances.model.Encadreur;
import ci.univ.soutenances.model.Evaluation;
import ci.univ.soutenances.model.JuryVue;
import ci.univ.soutenances.model.MembreJuryVue;
import ci.univ.soutenances.model.SessionUser;
import ci.univ.soutenances.model.Soutenance;
import ci.univ.soutenances.model.StatutSoutenance;
import ci.univ.soutenances.service.EncadreurService;
import ci.univ.soutenances.service.HistoriqueActionService;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(urlPatterns = {
        "/encadreur/dashboard",
        "/encadreur/mes-jurys/*",
        "/encadreur/profil"
})
public class EncadreurServlet extends HttpServlet {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Map<String, String> TITLES = Map.of(
            "dashboard", "Tableau de bord",
            "mes-jurys", "Mes jurys",
            "profil", "Mon profil");

    @EJB
    private EncadreurService encadreurService;
    @EJB
    private HistoriqueActionService historiqueActionService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String route = request.getServletPath();
            if ("/encadreur/dashboard".equals(route)) {
                showDashboard(request, response);
                return;
            }
            if ("/encadreur/mes-jurys".equals(route)) {
                if ("note".equals(request.getParameter("vue"))) {
                    showNoteForm(request, response);
                } else {
                    showJurys(request, response);
                }
                return;
            }
            if ("/encadreur/profil".equals(route)) {
                showProfil(request, response);
                return;
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        } catch (EJBException exception) {
            getServletContext().log("Impossible de charger l’espace encadreur.", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Impossible de charger cette page pour le moment.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            if (!"/encadreur/mes-jurys".equals(request.getServletPath())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            String[] parts = pathParts(request);
            if (parts.length != 1 || !"noter".equals(parts[0])) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            handleNoter(request, response);
        } catch (IllegalArgumentException exception) {
            showNoteFormError(request, response, request.getParameter("soutenanceId"), exception.getMessage());
        } catch (EJBException exception) {
            getServletContext().log("Échec de la saisie d’une note par un encadreur.", exception);
            showNoteFormError(request, response, request.getParameter("soutenanceId"),
                    "L’opération a échoué. Vérifie la note saisie et réessaie.");
        }
    }

    private void showDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Encadreur encadreur = resolveEncadreur(request, response);
        if (encadreur == null) {
            return;
        }
        List<Evaluation> convocations = encadreurService.findEvaluationsByEncadreur(encadreur.getId());
        List<ConvocationVue> lignes = buildLignes(convocations, encadreur.getId());
        long soumises = convocations.stream().filter(convocation -> convocation.getNote() != null).count();
        long evaluees = convocations.stream()
                .filter(convocation -> convocation.getSoutenance().getStatut() == StatutSoutenance.EVALUEE)
                .count();
        request.setAttribute("type", "dashboard");
        request.setAttribute("titre", TITLES.get("dashboard"));
        request.setAttribute("encadreur", encadreur);
        request.setAttribute("totalConvocations", convocations.size());
        request.setAttribute("notesSoumises", soumises);
        request.setAttribute("notesEnAttente", convocations.size() - soumises);
        request.setAttribute("soutenancesEvaluees", evaluees);
        request.setAttribute("lignes", lignes);
        request.setAttribute("aNoter",
                lignes.stream().filter(ligne -> ligne.getConvocation().getNote() == null).toList());
        setLogoutToken(request);
        request.getRequestDispatcher("/WEB-INF/views/encadreur/dashboard.jsp").forward(request, response);
    }

    private void showJurys(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Encadreur encadreur = resolveEncadreur(request, response);
        if (encadreur == null) {
            return;
        }
        List<Evaluation> convocations = encadreurService.findEvaluationsByEncadreur(encadreur.getId());
        request.setAttribute("type", "mes-jurys");
        request.setAttribute("titre", TITLES.get("mes-jurys"));
        request.setAttribute("encadreur", encadreur);
        request.setAttribute("lignes", buildLignes(convocations, encadreur.getId()));
        request.setAttribute("message", request.getParameter("message"));
        request.setAttribute("erreur", request.getParameter("erreur"));
        setLogoutToken(request);
        request.getRequestDispatcher("/WEB-INF/views/encadreur/mes-jurys.jsp").forward(request, response);
    }

    private void showNoteForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Encadreur encadreur = resolveEncadreur(request, response);
        if (encadreur == null) {
            return;
        }
        UUID soutenanceId = parseId(request.getParameter("id"));
        Soutenance soutenance = encadreurService.findSoutenanceById(soutenanceId).orElse(null);
        if (soutenance == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        List<Evaluation> jury = encadreurService.findEvaluationsBySoutenance(soutenanceId);
        Evaluation maConvocation = findMine(jury, encadreur.getId());
        if (maConvocation == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Vous n’êtes pas membre du jury de cette soutenance.");
            return;
        }
        request.setAttribute("type", "mes-jurys");
        request.setAttribute("titre", TITLES.get("mes-jurys"));
        request.setAttribute("encadreur", encadreur);
        request.setAttribute("soutenance", soutenance);
        request.setAttribute("maConvocation", maConvocation);
        request.setAttribute("jury", buildJury(jury, encadreur.getId(), soutenanceId));
        request.setAttribute("moyenne", encadreurService.calculerMoyenne(soutenanceId).orElse(null));
        request.setAttribute("dejaNote", maConvocation.getNote() != null);
        request.setAttribute("dateAffichee", formatDate(soutenance));
        request.setAttribute("message", request.getParameter("message"));
        request.setAttribute("erreur", request.getParameter("erreur"));
        setLogoutToken(request);
        request.getRequestDispatcher("/WEB-INF/views/encadreur/evaluation-form.jsp").forward(request, response);
    }

    private void showNoteFormError(HttpServletRequest request, HttpServletResponse response,
            String rawSoutenanceId, String error) throws ServletException, IOException {
        try {
            Encadreur encadreur = resolveEncadreur(request, response);
            if (encadreur == null) {
                return;
            }
            UUID soutenanceId = parseId(rawSoutenanceId);
            Soutenance soutenance = encadreurService.findSoutenanceById(soutenanceId).orElse(null);
            if (soutenance == null) {
                redirectJurys(request, response, "erreur", "Soutenance introuvable.");
                return;
            }
            List<Evaluation> jury = encadreurService.findEvaluationsBySoutenance(soutenanceId);
            Evaluation maConvocation = findMine(jury, encadreur.getId());
            if (maConvocation == null) {
                redirectJurys(request, response, "erreur",
                        "Vous n’êtes pas membre du jury de cette soutenance.");
                return;
            }
            request.setAttribute("type", "mes-jurys");
            request.setAttribute("titre", TITLES.get("mes-jurys"));
            request.setAttribute("encadreur", encadreur);
            request.setAttribute("soutenance", soutenance);
            request.setAttribute("maConvocation", maConvocation);
            request.setAttribute("jury", buildJury(jury, encadreur.getId(), soutenanceId));
            request.setAttribute("moyenne", encadreurService.calculerMoyenne(soutenanceId).orElse(null));
            request.setAttribute("dejaNote", maConvocation.getNote() != null);
            request.setAttribute("dateAffichee", formatDate(soutenance));
            request.setAttribute("erreur", error);
            setLogoutToken(request);
            request.getRequestDispatcher("/WEB-INF/views/encadreur/evaluation-form.jsp").forward(request, response);
        } catch (IllegalArgumentException | EJBException exception) {
            getServletContext().log("Impossible de réafficher le formulaire de saisie de note.", exception);
            redirectJurys(request, response, "erreur", error);
        }
    }

    private void handleNoter(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Encadreur encadreur = resolveEncadreur(request, response);
        if (encadreur == null) {
            return;
        }
        UUID soutenanceId = parseId(request.getParameter("soutenanceId"));
        BigDecimal note = parseNote(request.getParameter("note"));
        String avis = request.getParameter("avis");
        encadreurService.saisirNote(soutenanceId, encadreur.getId(), note, avis);
        String label = "Saisie de note pour la soutenance " + soutenanceId + " : " + note + "/20";
        if (!recordAction(request, "ENCADREUR", "MODIFICATION", label)) {
            redirectJurys(request, response, "erreur",
                    "Note enregistrée, mais l’historique n’a pas pu être enregistré.");
        } else {
            redirectJurys(request, response, "message", "Note enregistrée.");
        }
    }

    private void showProfil(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Encadreur encadreur = resolveEncadreur(request, response);
        if (encadreur == null) {
            return;
        }
        request.setAttribute("type", "profil");
        request.setAttribute("titre", TITLES.get("profil"));
        request.setAttribute("encadreur", encadreur);
        setLogoutToken(request);
        request.getRequestDispatcher("/WEB-INF/views/encadreur/profil.jsp").forward(request, response);
    }

    private Encadreur resolveEncadreur(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Object user = request.getSession(false) == null ? null
                : request.getSession(false).getAttribute(EncadreurAuthorizationFilter.SESSION_USER_ATTRIBUTE);
        if (!(user instanceof SessionUser sessionUser)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Connexion requise.");
            return null;
        }
        Encadreur encadreur = encadreurService.findByUtilisateurId(sessionUser.id()).orElse(null);
        if (encadreur == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Profil encadreur introuvable.");
            return null;
        }
        return encadreur;
    }

    private List<ConvocationVue> buildLignes(List<Evaluation> convocations, UUID encadreurId) {
        List<ConvocationVue> lignes = new ArrayList<>();
        for (Evaluation convocation : convocations) {
            UUID soutenanceId = convocation.getSoutenance().getId();
            List<Evaluation> jury = encadreurService.findEvaluationsBySoutenance(soutenanceId);
            lignes.add(new ConvocationVue(convocation, buildJury(jury, encadreurId, soutenanceId),
                    formatDate(convocation.getSoutenance())));
        }
        return lignes;
    }

    private JuryVue buildJury(List<Evaluation> jury, UUID encadreurId, UUID soutenanceId) {
        List<MembreJuryVue> membres = new ArrayList<>();
        int soumises = 0;
        for (Evaluation membre : jury) {
            boolean actuel = membre.getEncadreur().getId().equals(encadreurId);
            boolean note = membre.getNote() != null;
            if (note) {
                soumises++;
            }
            String nomComplet = clean(membre.getEncadreur().getPrenom()) + " "
                    + clean(membre.getEncadreur().getNom());
            membres.add(new MembreJuryVue(nomComplet.trim(), actuel, note));
        }
        BigDecimal moyenne = encadreurService.calculerMoyenne(soutenanceId).orElse(null);
        return new JuryVue(jury.size(), soumises, moyenne, membres);
    }

    private static Evaluation findMine(List<Evaluation> jury, UUID encadreurId) {
        for (Evaluation membre : jury) {
            if (membre.getEncadreur().getId().equals(encadreurId)) {
                return membre;
            }
        }
        return null;
    }

    private boolean recordAction(HttpServletRequest request, String screen, String action, String label) {
        Object user = request.getSession(false).getAttribute(EncadreurAuthorizationFilter.SESSION_USER_ATTRIBUTE);
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

    private static String[] pathParts(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null || path.equals("/") ? new String[0] : path.substring(1).split("/");
    }

    private static void setLogoutToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        String logoutToken = (String) session.getAttribute("logoutCsrfToken");
        if (logoutToken == null) {
            logoutToken = UUID.randomUUID().toString();
            session.setAttribute("logoutCsrfToken", logoutToken);
        }
        request.setAttribute("logoutCsrfToken", logoutToken);
    }

    private static UUID parseId(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("Identifiant invalide.");
        }
    }

    private static BigDecimal parseNote(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("La note est obligatoire.");
        }
        try {
            return new BigDecimal(raw.trim().replace(',', '.'));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("La note doit être un nombre entre 0 et 20.");
        }
    }

    private static String formatDate(Soutenance soutenance) {
        if (soutenance == null || soutenance.getDateHeure() == null) {
            return "Date à confirmer";
        }
        return soutenance.getDateHeure().format(DATE_FORMAT);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static void redirectJurys(HttpServletRequest request, HttpServletResponse response,
            String parameter, String message) throws IOException {
        response.sendRedirect(request.getContextPath() + "/encadreur/mes-jurys?"
                + parameter + "=" + URLEncoder.encode(message, StandardCharsets.UTF_8));
    }
}
