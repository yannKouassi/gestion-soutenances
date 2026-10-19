package ci.univ.soutenances.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Etudiant;
import ci.univ.soutenances.model.Evaluation;
import ci.univ.soutenances.model.Filiere;
import ci.univ.soutenances.model.Memoire;
import ci.univ.soutenances.model.RoleUtilisateur;
import ci.univ.soutenances.model.Soutenance;
import ci.univ.soutenances.repository.EtudiantRepository;
import ci.univ.soutenances.repository.EvaluationRepository;
import ci.univ.soutenances.repository.FiliereRepository;
import ci.univ.soutenances.repository.MemoireRepository;
import ci.univ.soutenances.repository.SoutenanceRepository;
import ci.univ.soutenances.service.CompteService;
import ci.univ.soutenances.service.EtudiantService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

@Stateless
public class EtudiantServiceImpl implements EtudiantService {
    @EJB
    private EtudiantRepository etudiantRepository;

    @EJB
    private FiliereRepository filiereRepository;

    @EJB
    private MemoireRepository memoireRepository;

    @EJB
    private SoutenanceRepository soutenanceRepository;

    @EJB
    private EvaluationRepository evaluationRepository;

    @EJB
    private CompteService compteService;

    @Override
    public List<Etudiant> findAll() {
        return etudiantRepository.findAll();
    }

    @Override
    public Optional<Etudiant> findById(UUID id) {
        return etudiantRepository.findById(requireId(id, "étudiant"));
    }

    @Override
    public Optional<Etudiant> findByUtilisateurId(UUID utilisateurId) {
        return etudiantRepository.findByUtilisateurId(requireId(utilisateurId, "utilisateur"));
    }

    @Override
    public Optional<Soutenance> findSoutenanceByMemoireId(UUID memoireId) {
        return soutenanceRepository.findByMemoireId(requireId(memoireId, "mémoire"));
    }

    @Override
    public List<Evaluation> findJuryBySoutenanceId(UUID soutenanceId) {
        return evaluationRepository.findJuryBySoutenanceId(requireId(soutenanceId, "soutenance"));
    }

    @Override
    public Optional<BigDecimal> calculerMoyenne(UUID soutenanceId) {
        List<Evaluation> jury = evaluationRepository.findJuryBySoutenanceId(requireId(soutenanceId, "soutenance"));
        if (jury.isEmpty()) {
            return Optional.empty();
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Evaluation evaluation : jury) {
            if (evaluation.getNote() == null) {
                return Optional.empty();
            }
            total = total.add(evaluation.getNote());
        }
        return Optional.of(total.divide(BigDecimal.valueOf(jury.size()), 2, RoundingMode.HALF_UP));
    }

    @Override
    public Etudiant save(Etudiant etudiant, UUID filiereId, UUID memoireId) {
        if (etudiant == null) {
            throw new IllegalArgumentException("Les informations de l’étudiant sont obligatoires.");
        }
        etudiant.setMatricule(required(etudiant.getMatricule(), "Le matricule", 40).toUpperCase());
        etudiant.setNom(required(etudiant.getNom(), "Le nom", 100));
        etudiant.setPrenom(required(etudiant.getPrenom(), "Le prénom", 100));
        etudiant.setEmail(validateEmail(etudiant.getEmail()));
        if (etudiantRepository.matriculeExists(etudiant.getMatricule(), etudiant.getId())) {
            throw new IllegalArgumentException("Ce matricule est déjà utilisé.");
        }
        if (etudiantRepository.emailExists(etudiant.getEmail(), etudiant.getId())) {
            throw new IllegalArgumentException("Cette adresse e-mail est déjà utilisée.");
        }
        Filiere filiere = filiereRepository.findById(requireId(filiereId, "filière"))
                .orElseThrow(() -> new IllegalArgumentException("Filière introuvable."));
        etudiant.setFiliere(filiere);
        if (memoireId == null) {
            etudiant.setMemoire(null);
        } else {
            Memoire memoire = memoireRepository.findAll().stream()
                    .filter(item -> item.getId().equals(memoireId))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Mémoire introuvable."));
            etudiant.setMemoire(memoire);
        }
        if (etudiant.getId() == null) {
            etudiant.setUtilisateur(compteService.createLinkedAccount(RoleUtilisateur.ETUDIANT,
                    etudiant.getEmail(), etudiant.getPrenom() + " " + etudiant.getNom()));
        } else {
            compteService.updateLinkedAccountLogin(etudiant.getUtilisateur(), etudiant.getEmail());
        }
        return etudiantRepository.save(etudiant);
    }

    private static UUID requireId(UUID id, String label) {
        if (id == null) {
            throw new IllegalArgumentException("La " + label + " est obligatoire.");
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
