package ci.univ.soutenances.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Departement;
import ci.univ.soutenances.model.Encadreur;
import ci.univ.soutenances.model.Evaluation;
import ci.univ.soutenances.model.Etudiant;
import ci.univ.soutenances.model.Grade;
import ci.univ.soutenances.model.RoleUtilisateur;
import ci.univ.soutenances.model.Soutenance;
import ci.univ.soutenances.model.StatutSoutenance;
import ci.univ.soutenances.repository.DepartementRepository;
import ci.univ.soutenances.repository.EncadreurRepository;
import ci.univ.soutenances.repository.EtudiantRepository;
import ci.univ.soutenances.repository.EvaluationRepository;
import ci.univ.soutenances.repository.GradeRepository;
import ci.univ.soutenances.repository.SoutenanceRepository;
import ci.univ.soutenances.service.CompteService;
import ci.univ.soutenances.service.EncadreurService;
import ci.univ.soutenances.service.SoutenanceService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

@Stateless
public class EncadreurServiceImpl implements EncadreurService {
    @EJB
    private EncadreurRepository encadreurRepository;

    @EJB
    private GradeRepository gradeRepository;

    @EJB
    private DepartementRepository departementRepository;

    @EJB
    private CompteService compteService;

    @EJB
    private EvaluationRepository evaluationRepository;

    @EJB
    private EtudiantRepository etudiantRepository;

    @EJB
    private SoutenanceRepository soutenanceRepository;

    @EJB
    private SoutenanceService soutenanceService;

    @Override
    public List<Encadreur> findAll() {
        return encadreurRepository.findAll();
    }

    @Override
    public Optional<Encadreur> findById(UUID id) {
        return encadreurRepository.findById(requireId(id, "encadreur"));
    }

    @Override
    public Encadreur save(Encadreur encadreur, UUID gradeId, UUID departementId) {
        if (encadreur == null) {
            throw new IllegalArgumentException("Les informations de l’encadreur sont obligatoires.");
        }
        encadreur.setNom(required(encadreur.getNom(), "Le nom", 100));
        encadreur.setPrenom(required(encadreur.getPrenom(), "Le prénom", 100));
        encadreur.setEmail(validateEmail(encadreur.getEmail()));
        if (encadreurRepository.emailExists(encadreur.getEmail(), encadreur.getId())) {
            throw new IllegalArgumentException("Cette adresse e-mail est déjà utilisée.");
        }
        Grade grade = gradeRepository.findById(requireId(gradeId, "grade"))
                .orElseThrow(() -> new IllegalArgumentException("Grade introuvable."));
        Departement departement = departementRepository.findById(requireId(departementId, "département"))
                .orElseThrow(() -> new IllegalArgumentException("Département introuvable."));
        encadreur.setGrade(grade);
        encadreur.setDepartement(departement);
        if (encadreur.getId() == null) {
            encadreur.setUtilisateur(compteService.createLinkedAccount(RoleUtilisateur.ENCADREUR,
                    encadreur.getEmail(), encadreur.getPrenom() + " " + encadreur.getNom()));
        } else {
            compteService.updateLinkedAccountLogin(encadreur.getUtilisateur(), encadreur.getEmail());
        }
        return encadreurRepository.save(encadreur);
    }

    @Override
    public Optional<Encadreur> findByUtilisateurId(UUID utilisateurId) {
        return encadreurRepository.findByUtilisateurId(requireId(utilisateurId, "utilisateur"));
    }

    @Override
    public List<Etudiant> findEtudiantsEncadres(UUID encadreurId) {
        return etudiantRepository.findByEncadreurId(requireId(encadreurId, "encadreur"));
    }

    @Override
    public List<Evaluation> findEvaluationsByEncadreur(UUID encadreurId) {
        return evaluationRepository.findByEncadreurId(requireId(encadreurId, "encadreur"));
    }

    @Override
    public List<Evaluation> findEvaluationsBySoutenance(UUID soutenanceId) {
        return evaluationRepository.findBySoutenanceId(requireId(soutenanceId, "soutenance"));
    }

    @Override
    public Optional<Soutenance> findSoutenanceById(UUID soutenanceId) {
        return soutenanceRepository.findById(requireId(soutenanceId, "soutenance"));
    }

    @Override
    public Optional<BigDecimal> calculerMoyenne(UUID soutenanceId) {
        return soutenanceService.calculerMoyenne(requireId(soutenanceId, "soutenance"));
    }

    @Override
    public Evaluation saisirNote(UUID soutenanceId, UUID encadreurId, BigDecimal note, String avis) {
        requireId(soutenanceId, "soutenance");
        requireId(encadreurId, "encadreur");
        if (note == null) {
            throw new IllegalArgumentException("La note est obligatoire.");
        }
        if (note.compareTo(BigDecimal.ZERO) < 0 || note.compareTo(BigDecimal.valueOf(20)) > 0) {
            throw new IllegalArgumentException("La note doit être comprise entre 0 et 20.");
        }
        Evaluation evaluation = evaluationRepository.findBySoutenanceAndEncadreur(soutenanceId, encadreurId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cet encadreur n’est pas membre du jury de cette soutenance."));
        Soutenance soutenance = soutenanceRepository.findById(soutenanceId)
                .orElseThrow(() -> new IllegalArgumentException("Soutenance introuvable."));
        if (soutenance.getStatut() == StatutSoutenance.EVALUEE) {
            throw new IllegalArgumentException("Cette soutenance est déjà évaluée ; les notes sont closes.");
        }
        if (soutenance.getStatut() == StatutSoutenance.ANNULEE) {
            throw new IllegalArgumentException("Cette soutenance est annulée.");
        }
        if (soutenance.getDateHeure() != null && soutenance.getDateHeure().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "La soutenance n’a pas encore eu lieu ; la note ne peut pas être saisie.");
        }
        if (evaluationRepository.findBySoutenanceId(soutenanceId).size() < 2) {
            throw new IllegalArgumentException(
                    "Un jury doit comporter au moins deux membres avant la saisie des notes.");
        }
        if (evaluation.getNote() != null) {
            throw new IllegalArgumentException("Une note a déjà été soumise pour ce membre du jury.");
        }
        evaluation.setNote(note);
        evaluation.setAvis(avis == null || avis.isBlank() ? null : avis.trim());
        Evaluation soumise = evaluationRepository.save(evaluation);
        soutenanceService.mettreAJourStatutApresEvaluation(soutenanceId);
        return soumise;
    }

    private static UUID requireId(UUID id, String label) {
        if (id == null) {
            throw new IllegalArgumentException("Le " + label + " est obligatoire.");
        }
        return id;
    }

    private static String required(String value, String label, int maxLength) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty() || normalized.length() > maxLength) {
            throw new IllegalArgumentException(label + " est obligatoire (maximum " + maxLength + " caractères).");
        }
        return normalized;
    }

    private static String validateEmail(String email) {
        String value = required(email, "L’adresse e-mail", 180).toLowerCase(java.util.Locale.ROOT);
        if (!value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("L’adresse e-mail n’est pas valide.");
        }
        return value;
    }
}
