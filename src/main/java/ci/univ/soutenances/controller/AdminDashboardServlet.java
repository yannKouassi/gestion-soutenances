package ci.univ.soutenances.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ci.univ.soutenances.model.JuryResume;
import ci.univ.soutenances.model.SoutenanceVue;
import ci.univ.soutenances.model.StatutSoutenance;
import ci.univ.soutenances.repository.EncadreurRepository;
import ci.univ.soutenances.repository.EtudiantRepository;
import ci.univ.soutenances.repository.MemoireRepository;
import ci.univ.soutenances.repository.SalleRepository;
import ci.univ.soutenances.service.SoutenanceService;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {
    private static final String TYPE = "dashboard";
    private static final String TITLE = "Tableau de bord";
    private static final String VIEW = "/WEB-INF/views/admin/dashboard.jsp";
    private static final int PROCHAINES_MAX = 5;

    @EJB
    private SoutenanceService soutenanceService;

    @EJB
    private MemoireRepository memoireRepository;

    @EJB
    private EtudiantRepository etudiantRepository;

    @EJB
    private EncadreurRepository encadreurRepository;

    @EJB
    private SalleRepository salleRepository;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("type", TYPE);
        request.setAttribute("titre", TITLE);

        try {
            List<SoutenanceVue> soutenances = soutenanceService.lister(null, null, null, null);
            List<UUID> ids = soutenances.stream()
                    .map(vue -> vue.getSoutenance().getId())
                    .toList();
            Map<UUID, JuryResume> jurys = soutenanceService.resumerJurys(ids);

            long planifiees = compter(soutenances, StatutSoutenance.PLANIFIEE);
            long evaluees = compter(soutenances, StatutSoutenance.EVALUEE);
            long annulees = compter(soutenances, StatutSoutenance.ANNULEE);
            long jurysIncomplets = soutenances.stream()
                    .filter(vue -> vue.getSoutenance().getStatut() == StatutSoutenance.PLANIFIEE)
                    .filter(vue -> {
                        JuryResume resume = jurys.get(vue.getSoutenance().getId());
                        return resume == null || resume.getNombreMembres() < 2;
                    })
                    .count();

            request.setAttribute("totalSoutenances", soutenances.size());
            request.setAttribute("soutenancesPlanifiees", planifiees);
            request.setAttribute("soutenancesEvaluees", evaluees);
            request.setAttribute("soutenancesAnnulees", annulees);
            request.setAttribute("jurysIncomplets", jurysIncomplets);
            request.setAttribute("totalMemoires", memoireRepository.findAll().size());
            request.setAttribute("totalEtudiants", etudiantRepository.findAll().size());
            request.setAttribute("totalEncadreurs", encadreurRepository.findAll().size());
            request.setAttribute("totalSalles", salleRepository.findAll().size());
            request.setAttribute("prochaines", prochaines(soutenances));
            request.getRequestDispatcher(VIEW).forward(request, response);
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        } catch (EJBException exception) {
            getServletContext().log("Impossible de charger le tableau de bord administrateur.", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Impossible de charger cette page pour le moment.");
        }
    }

    private static long compter(List<SoutenanceVue> soutenances, StatutSoutenance statut) {
        return soutenances.stream()
                .filter(vue -> vue.getSoutenance().getStatut() == statut)
                .count();
    }

    private static List<SoutenanceVue> prochaines(List<SoutenanceVue> soutenances) {
        LocalDateTime maintenant = LocalDateTime.now();
        return soutenances.stream()
                .filter(vue -> vue.getSoutenance().getStatut() == StatutSoutenance.PLANIFIEE)
                .filter(vue -> vue.getSoutenance().getDateHeure() != null
                        && vue.getSoutenance().getDateHeure().isAfter(maintenant))
                .sorted(Comparator.comparing(vue -> vue.getSoutenance().getDateHeure()))
                .limit(PROCHAINES_MAX)
                .toList();
    }
}
