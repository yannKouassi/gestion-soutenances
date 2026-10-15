package ci.univ.soutenances.service.impl;

import java.util.Optional;
import java.util.regex.Pattern;

import ci.univ.soutenances.model.RoleUtilisateur;
import ci.univ.soutenances.model.Utilisateur;
import ci.univ.soutenances.repository.UtilisateurRepository;
import ci.univ.soutenances.service.AuthService;
import jakarta.ejb.EJB;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;

@Singleton
@Lock(LockType.READ)
public class AuthServiceImpl implements AuthService {
    private static final Pattern LOGIN_PATTERN = Pattern.compile("[A-Za-z0-9._-]{3,100}");
    private static final int MINIMUM_PASSWORD_LENGTH = 8;

    @EJB
    private UtilisateurRepository utilisateurRepository;

    private final PasswordHasher passwordHasher = new PasswordHasher();

    @Override
    public Optional<Utilisateur> authenticate(String login, String password) {
        if (login == null || password == null || login.isBlank()) {
            return Optional.empty();
        }
        return utilisateurRepository.findByLogin(login.trim())
                .filter(utilisateur -> utilisateur.getRole() != null)
                .filter(Utilisateur::isActif)
                .filter(utilisateur -> passwordHasher.matches(password, utilisateur.getMotDePasse()));
    }

    @Override
    public boolean hasAdministrator() {
        return utilisateurRepository.existsAdministrator();
    }

    @Override
    @Lock(LockType.WRITE)
    public void createFirstAdministrator(String login, String password, String confirmation) {
        String normalizedLogin = normalizeLogin(login);
        validatePassword(password, confirmation);
        if (utilisateurRepository.existsAdministrator()) {
            throw new IllegalStateException("Un administrateur existe déjà. La création initiale est désactivée.");
        }
        if (utilisateurRepository.findByLogin(normalizedLogin).isPresent()) {
            throw new IllegalArgumentException("Ce login est déjà utilisé.");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setLogin(normalizedLogin);
        utilisateur.setMotDePasse(passwordHasher.hash(password));
        utilisateur.setRole(RoleUtilisateur.ADMIN);
        utilisateurRepository.save(utilisateur);
    }

    private static String normalizeLogin(String login) {
        if (login == null) {
            throw new IllegalArgumentException("Le login est obligatoire.");
        }
        String normalized = login.trim();
        if (!LOGIN_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "Le login doit contenir de 3 à 100 caractères (lettres, chiffres, point, tiret ou souligné).");
        }
        return normalized;
    }

    private static void validatePassword(String password, String confirmation) {
        if (password == null || password.length() < MINIMUM_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères.");
        }
        if (password.length() > 128) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas dépasser 128 caractères.");
        }
        if (!password.equals(confirmation)) {
            throw new IllegalArgumentException("Les deux mots de passe ne correspondent pas.");
        }
    }
}
