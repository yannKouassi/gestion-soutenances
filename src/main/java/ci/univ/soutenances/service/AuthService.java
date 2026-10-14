package ci.univ.soutenances.service;

import java.util.Optional;

import ci.univ.soutenances.model.Utilisateur;

public interface AuthService {
    Optional<Utilisateur> authenticate(String login, String password);

    boolean hasAdministrator();

    void createFirstAdministrator(String login, String password, String confirmation);
}
