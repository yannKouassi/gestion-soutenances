package ci.univ.soutenances.repository;

import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Utilisateur;

public interface UtilisateurRepository {
    Optional<Utilisateur> findById(UUID id);

    Optional<Utilisateur> findByLogin(String login);

    boolean existsAdministrator();

    Utilisateur save(Utilisateur utilisateur);
}
