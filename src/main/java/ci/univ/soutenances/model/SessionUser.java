package ci.univ.soutenances.model;

import java.io.Serializable;
import java.util.UUID;

public record SessionUser(UUID id, String login, RoleUtilisateur role) implements Serializable {
    private static final long serialVersionUID = 1L;

    public static SessionUser from(Utilisateur utilisateur) {
        return new SessionUser(utilisateur.getId(), utilisateur.getLogin(), utilisateur.getRole());
    }
}
