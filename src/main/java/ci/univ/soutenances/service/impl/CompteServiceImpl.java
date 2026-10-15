package ci.univ.soutenances.service.impl;

import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import ci.univ.soutenances.model.AccountEntry;
import ci.univ.soutenances.model.RoleUtilisateur;
import ci.univ.soutenances.model.Utilisateur;
import ci.univ.soutenances.repository.UtilisateurRepository;
import ci.univ.soutenances.service.CompteService;
import ci.univ.soutenances.service.CourrielService;
import jakarta.ejb.EJB;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;

@Singleton
@Lock(LockType.READ)
public class CompteServiceImpl implements CompteService {
    private static final String PASSWORD_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private final PasswordHasher passwordHasher = new PasswordHasher();

    @EJB
    private UtilisateurRepository utilisateurRepository;

    @EJB
    private CourrielService courrielService;

    @Override
    @Lock(LockType.WRITE)
    public Utilisateur createLinkedAccount(RoleUtilisateur role, String email, String displayName) {
        if (role != RoleUtilisateur.ENCADREUR && role != RoleUtilisateur.ETUDIANT) {
            throw new IllegalArgumentException("Seuls les comptes d’encadreur et d’étudiant sont créés ici.");
        }
        String validEmail = validateEmail(email);
        String name = required(displayName, "Le nom du titulaire", 200);
        String password = temporaryPassword();
        Utilisateur utilisateur = new Utilisateur();
        ensureLoginAvailable(validEmail, null);
        utilisateur.setLogin(validEmail);
        utilisateur.setMotDePasse(passwordHasher.hash(password));
        utilisateur.setRole(role);
        utilisateur.setActif(true);
        utilisateurRepository.save(utilisateur);
        courrielService.sendCredentials(validEmail, name, utilisateur.getLogin(), password, false);
        return utilisateur;
    }

    @Override
    @Lock(LockType.WRITE)
    public void updateLinkedAccountLogin(Utilisateur utilisateur, String email) {
        if (utilisateur == null || utilisateur.getId() == null) {
            throw new IllegalArgumentException("Le compte lié est obligatoire.");
        }
        String validEmail = validateEmail(email);
        ensureLoginAvailable(validEmail, utilisateur.getId());
        utilisateur.setLogin(validEmail);
        utilisateurRepository.save(utilisateur);
    }

    @Override
    public List<AccountEntry> findAll() {
        return utilisateurRepository.findAllAccounts();
    }

    @Override
    @Lock(LockType.WRITE)
    public void setActive(UUID id, boolean active) {
        Utilisateur utilisateur = findAccount(id);
        if (utilisateur.getRole() == RoleUtilisateur.ADMIN && !active
                && utilisateur.isActif() && utilisateurRepository.countActiveAdministrators() <= 1) {
            throw new IllegalArgumentException("Impossible de désactiver le dernier compte administrateur actif.");
        }
        utilisateur.setActif(active);
        utilisateurRepository.save(utilisateur);
    }

    @Override
    @Lock(LockType.WRITE)
    public void resetPassword(UUID id) {
        Utilisateur utilisateur = findAccount(id);
        if (!utilisateur.isActif()) {
            throw new IllegalArgumentException("Réactivez le compte avant de réinitialiser son mot de passe.");
        }
        String email = utilisateurRepository.findEmailById(id);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Aucune adresse e-mail n’est associée à ce compte.");
        }
        String password = temporaryPassword();
        utilisateur.setMotDePasse(passwordHasher.hash(password));
        utilisateurRepository.save(utilisateur);
        courrielService.sendCredentials(email, utilisateur.getLogin(), utilisateur.getLogin(), password, true);
    }

    private Utilisateur findAccount(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("L’identifiant du compte est obligatoire.");
        }
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Compte utilisateur introuvable."));
    }

    private void ensureLoginAvailable(String login, UUID excludedId) {
        Utilisateur existing = utilisateurRepository.findByLogin(login).orElse(null);
        if (existing != null && !existing.getId().equals(excludedId)) {
            throw new IllegalArgumentException("Cette adresse e-mail est déjà utilisée comme identifiant de connexion.");
        }
    }

    private static String temporaryPassword() {
        StringBuilder password = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            password.append(PASSWORD_ALPHABET.charAt(RANDOM.nextInt(PASSWORD_ALPHABET.length())));
        }
        return password.toString();
    }

    private static String validateEmail(String email) {
        String value = required(email, "L’adresse e-mail", 180).toLowerCase(Locale.ROOT);
        if (!value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("L’adresse e-mail n’est pas valide.");
        }
        return value;
    }

    private static String required(String value, String label, int maxLength) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty() || normalized.length() > maxLength) {
            throw new IllegalArgumentException(label + " est obligatoire et ne doit pas dépasser "
                    + maxLength + " caractères.");
        }
        return normalized;
    }
}
