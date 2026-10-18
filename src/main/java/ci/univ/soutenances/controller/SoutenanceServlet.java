package ci.univ.soutenances.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import ci.univ.soutenances.model.SoutenanceListeEntry;
import ci.univ.soutenances.model.SoutenanceVue;
import ci.univ.soutenances.model.StatutSoutenance;
import ci.univ.soutenances.repository.EtudiantRepository;
import ci.univ.soutenances.repository.FiliereRepository;
import ci.univ.soutenances.service.SoutenanceService;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/soutenances")
public class SoutenanceServlet extends HttpServlet {
    private static final String TYPE = "soutenances";
    private static final String TITLE = "Soutenances";
    private static final String LIST_VIEW = "/WEB-INF/views/admin/soutenance-list.jsp";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @EJB
    private SoutenanceService soutenanceService;

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

    private List<SoutenanceListeEntry> assembler(List<SoutenanceVue> soutenances) {
        List<SoutenanceListeEntry> entries = new ArrayList<>(soutenances.size());
        for (SoutenanceVue vue : soutenances) {
            UUID id = vue.getSoutenance().getId();
            int tailleJury = soutenanceService.findMembres(id).size();
            String noteAffichee = soutenanceService.calculerMoyenne(id)
                    .map(SoutenanceServlet::formatNote)
                    .orElse(null);
            entries.add(new SoutenanceListeEntry(vue, formatDate(vue), tailleJury, noteAffichee));
        }
        return entries;
    }

    private static String formatDate(SoutenanceVue vue) {
        return vue.getSoutenance().getDateHeure() == null
                ? null
                : vue.getSoutenance().getDateHeure().format(DATE_FORMAT);
    }

    private static String formatNote(BigDecimal note) {
        return String.format(Locale.FRANCE, "%.2f", note);
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
