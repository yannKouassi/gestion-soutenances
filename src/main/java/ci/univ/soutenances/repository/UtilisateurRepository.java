package ci.univ.soutenances.repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import ci.univ.soutenances.model.AccountEntry;
import ci.univ.soutenances.model.Utilisateur;

public interface UtilisateurRepository {
    Optional<Utilisateur> findById(UUID id);

    Optional<Utilisateur> findByLogin(String login);

    List<AccountEntry> findAllAccounts();

    String findEmailById(UUID id);

    long countActiveAdministrators();

    boolean existsAdministrator();

    Utilisateur save(Utilisateur utilisateur);
}
