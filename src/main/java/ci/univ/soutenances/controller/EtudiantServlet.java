package ci.univ.soutenances.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.config.EtudiantAuthorizationFilter;
import ci.univ.soutenances.model.Etudiant;
import ci.univ.soutenances.model.Evaluation;
import ci.univ.soutenances.model.JuryVue;
import ci.univ.soutenances.model.MembreJuryVue;
import ci.univ.soutenances.model.Memoire;
import ci.univ.soutenances.model.SessionUser;
import ci.univ.soutenances.model.Soutenance;
import ci.univ.soutenances.model.StatutSoutenance;
import ci.univ.soutenances.service.EtudiantService;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(urlPatterns = {
        "/etudiant/ma-soutenance",
        "/etudiant/menu",
        "/etudiant/profil"
})
public class EtudiantServlet extends HttpServlet {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Map<String, String> TITLES = Map.of(
            "ma-soutenance", "Ma soutenance",
            "profil", "Mon profil");

    @EJB
    private EtudiantService etudiantService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String route = request.getServletPath();
            if ("/etudiant/menu".equals(route)) {
                response.sendRedirect(request.getContextPath() + "/etudiant/ma-soutenance");
                return;
            }
            if ("/etudiant/ma-soutenance".equals(route)) {
                showMaSoutenance(request, response);
                return;
            }
            if ("/etudiant/profil".equals(route)) {
                showProfil(request, response);
                return;
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        } catch (EJBException exception) {
            getServletContext().log("Impossible de charger l’espace étudiant.", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Impossible de charger cette page pour le moment.");
        }
    }

    private void showMaSoutenance(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Etudiant etudiant = resolveEtudiant(request, response);
        if (etudiant == null) {
            return;
        }
        request.setAttribute("type", "ma-soutenance");
        request.setAttribute("titre", TITLES.get("ma-soutenance"));
        request.setAttribute("etudiant", etudiant);
        Memoire memoire = etudiant.getMemoire();
        request.setAttribute("memoirePresente", memoire != null);
        if (memoire != null) {
            request.setAttribute("memoire", memoire);
            request.setAttribute("encadreurNomComplet",
                    nomComplet(memoire.getEncadreur().getPrenom(), memoire.getEncadreur().getNom()));
            request.setAttribute("dateDepotAffichee", formatDay(memoire.getDateDepot()));
            Optional<Soutenance> soutenance = etudiantService.findSoutenanceByMemoireId(memoire.getId());
            request.setAttribute("soutenancePlanifiee", soutenance.isPresent());
            if (soutenance.isPresent()) {
                Soutenance value = soutenance.get();
                List<Evaluation> jury = etudiantService.findJuryBySoutenanceId(value.getId());
                boolean evaluee = value.getStatut() == StatutSoutenance.EVALUEE;
                request.setAttribute("soutenance", value);
                request.setAttribute("dateAffichee", formatDate(value));
                request.setAttribute("jury", buildJury(jury));
                request.setAttribute("juryCompose", jury.size() >= 2);
                request.setAttribute("evaluee", evaluee);
                request.setAttribute("noteFinale",
                        evaluee ? etudiantService.calculerMoyenne(value.getId()).orElse(null) : null);
            }
        }
        setLogoutToken(request);
        request.getRequestDispatcher("/WEB-INF/views/etudiant/ma-soutenance.jsp").forward(request, response);
    }

    private void showProfil(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Etudiant etudiant = resolveEtudiant(request, response);
        if (etudiant == null) {
            return;
        }
        request.setAttribute("type", "profil");
        request.setAttribute("titre", TITLES.get("profil"));
        request.setAttribute("etudiant", etudiant);
        setLogoutToken(request);
        request.getRequestDispatcher("/WEB-INF/views/etudiant/profil.jsp").forward(request, response);
    }

    private Etudiant resolveEtudiant(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        Object user = session == null ? null
                : session.getAttribute(EtudiantAuthorizationFilter.SESSION_USER_ATTRIBUTE);
        if (!(user instanceof SessionUser sessionUser)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Connexion requise.");
            return null;
        }
        Etudiant etudiant = etudiantService.findByUtilisateurId(sessionUser.id()).orElse(null);
        if (etudiant == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Profil étudiant introuvable.");
            return null;
        }
        return etudiant;
    }

    private static JuryVue buildJury(List<Evaluation> jury) {
        List<MembreJuryVue> membres = new ArrayList<>();
        int soumises = 0;
        for (Evaluation membre : jury) {
            boolean note = membre.getNote() != null;
            if (note) {
                soumises++;
            }
            membres.add(new MembreJuryVue(
                    nomComplet(membre.getEncadreur().getPrenom(), membre.getEncadreur().getNom()), false, note));
        }
        return new JuryVue(jury.size(), soumises, null, membres);
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

    private static String formatDate(Soutenance soutenance) {
        if (soutenance == null || soutenance.getDateHeure() == null) {
            return "Date à confirmer";
        }
        return soutenance.getDateHeure().format(DATE_FORMAT);
    }

    private static String formatDay(LocalDate date) {
        return date == null ? "—" : date.format(DAY_FORMAT);
    }

    private static String nomComplet(String prenom, String nom) {
        return (clean(prenom) + " " + clean(nom)).trim();
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
