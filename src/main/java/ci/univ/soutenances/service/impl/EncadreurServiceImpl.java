package ci.univ.soutenances.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Departement;
import ci.univ.soutenances.model.Encadreur;
import ci.univ.soutenances.model.Grade;
import ci.univ.soutenances.model.RoleUtilisateur;
import ci.univ.soutenances.repository.DepartementRepository;
import ci.univ.soutenances.repository.EncadreurRepository;
import ci.univ.soutenances.repository.GradeRepository;
import ci.univ.soutenances.service.CompteService;
import ci.univ.soutenances.service.EncadreurService;
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
