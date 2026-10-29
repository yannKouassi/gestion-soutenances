package ci.univ.soutenances.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ci.univ.soutenances.model.HistoriqueFiltre;
import ci.univ.soutenances.model.HistoriqueJournalEntry;
import ci.univ.soutenances.model.SoutenanceAgenda;
import ci.univ.soutenances.model.SoutenanceVue;
import ci.univ.soutenances.model.StatutSoutenance;
import ci.univ.soutenances.service.CompteService;
import ci.univ.soutenances.service.DepartementService;
import ci.univ.soutenances.service.EncadreurService;
import ci.univ.soutenances.service.EtudiantService;
import ci.univ.soutenances.service.FiliereService;
import ci.univ.soutenances.service.HistoriqueActionService;
import ci.univ.soutenances.service.MemoireService;
import ci.univ.soutenances.service.SalleService;
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
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter JOUR_FORMAT = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
    private static final Map<String, String> ACTIONS = Map.of(
            "CREATION", "Création",
            "MODIFICATION", "Modification",
            "SUPPRESSION", "Suppression");
    private static final int LIMITE_PROCHAINES = 5;
    private static final int LIMITE_ACTIVITES = 6;

    @EJB
    private DepartementService departementService;
    @EJB
    private FiliereService filiereService;
    @EJB
    private SalleService salleService;
    @EJB
    private EncadreurService encadreurService;
    @EJB
    private EtudiantService etudiantService;
    @EJB
    private MemoireService memoireService;
    @EJB
    private SoutenanceService soutenanceService;
    @EJB
    private CompteService compteService;
    @EJB
    private HistoriqueActionService historiqueActionService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<SoutenanceVue> soutenances = soutenanceService.lister(null, null, null, null);
            long planifiees = compter(soutenances, StatutSoutenance.PLANIFIEE);
            long evaluees = compter(soutenances, StatutSoutenance.EVALUEE);
            long annulees = compter(soutenances, StatutSoutenance.ANNULEE);
            long total = soutenances.size();

            LocalDateTime maintenant = LocalDateTime.now();
            List<SoutenanceAgenda> prochaines = soutenances.stream()
                    .filter(vue -> vue.getSoutenance().getStatut() == StatutSoutenance.PLANIFIEE)
                    .filter(vue -> vue.getSoutenance().getDateHeure() != null)
                    .filter(vue -> !vue.getSoutenance().getDateHeure().isBefore(maintenant))
                    .sorted(Comparator.comparing(vue -> vue.getSoutenance().getDateHeure()))
                    .limit(LIMITE_PROCHAINES)
                    .map(AdminDashboardServlet::versAgenda)
                    .toList();

            Map<String, String> ecrans = historiqueActionService.screens();
            List<HistoriqueJournalEntry> activites = historiqueActionService
                    .search(new HistoriqueFiltre(null, null, null, null, null), 0, LIMITE_ACTIVITES)
                    .stream()
                    .map(action -> new HistoriqueJournalEntry(
                            action.getDateAction().format(DATE_FORMAT),
                            action.getDescription(),
                            ACTIONS.getOrDefault(action.getAction(), action.getAction()),
                            action.getUtilisateur().getLogin(),
                            action.getUtilisateur().getRole().name(),
                            ecrans.getOrDefault(action.getEcran(), action.getEcran())))
                    .toList();

            request.setAttribute("type", "dashboard");
            request.setAttribute("titre", "Tableau de bord");
            request.setAttribute("dateDuJour", jourDuJour());
            request.setAttribute("totalSoutenances", total);
            request.setAttribute("soutenancesPlanifiees", planifiees);
            request.setAttribute("soutenancesEvaluees", evaluees);
            request.setAttribute("soutenancesAnnulees", annulees);
            request.setAttribute("pctPlanifiees", pourcentage(planifiees, total));
            request.setAttribute("pctEvaluees", pourcentage(evaluees, total));
            request.setAttribute("pctAnnulees", pourcentage(annulees, total));
            request.setAttribute("totalEtudiants", etudiantService.findAll().size());
            request.setAttribute("totalEncadreurs", encadreurService.findAll().size());
            request.setAttribute("totalDepartements", departementService.findAll().size());
            request.setAttribute("totalFilieres", filiereService.findAll().size());
            request.setAttribute("totalSalles", salleService.findAll().size());
            request.setAttribute("totalMemoires", memoireService.findAll().size());
            request.setAttribute("totalComptes", compteService.findAll().size());
            request.setAttribute("prochaines", prochaines);
            request.setAttribute("activites", activites);
            request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);
        } catch (EJBException exception) {
            getServletContext().log("Impossible de charger le tableau de bord administrateur.", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Impossible de charger cette page pour le moment.");
        }
    }

    private static SoutenanceAgenda versAgenda(SoutenanceVue vue) {
        String memoire = vue.getSoutenance().getMemoire() == null
                ? "—"
                : vue.getSoutenance().getMemoire().getTitre();
        String etudiant = vue.getEtudiant() == null
                ? "Non rattaché"
                : (vue.getEtudiant().getPrenom() + " " + vue.getEtudiant().getNom()).trim();
        String salle = vue.getSoutenance().getSalle() == null
                ? "—"
                : vue.getSoutenance().getSalle().getNomSalle();
        return new SoutenanceAgenda(
                vue.getSoutenance().getDateHeure().format(DATE_FORMAT),
                memoire, etudiant, salle, vue.getSoutenance().getStatut());
    }

    private static long compter(List<SoutenanceVue> soutenances, StatutSoutenance statut) {
        return soutenances.stream()
                .filter(vue -> vue.getSoutenance().getStatut() == statut)
                .count();
    }

    private static int pourcentage(long valeur, long total) {
        return total == 0 ? 0 : (int) Math.round(valeur * 100.0 / total);
    }

    private static String jourDuJour() {
        String jour = LocalDate.now().format(JOUR_FORMAT);
        return Character.toUpperCase(jour.charAt(0)) + jour.substring(1);
    }

}
