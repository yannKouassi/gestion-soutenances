package ci.univ.soutenances.model;

import java.util.UUID;

public class AccountEntry {
    private final UUID id;
    private final String login;
    private final RoleUtilisateur role;
    private final boolean actif;
    private final String email;

    public AccountEntry(UUID id, String login, RoleUtilisateur role, boolean actif, String email) {
        this.id = id;
        this.login = login;
        this.role = role;
        this.actif = actif;
        this.email = email;
    }

    public UUID getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public RoleUtilisateur getRole() {
        return role;
    }

    public boolean isActif() {
        return actif;
    }

    public String getEmail() {
        return email;
    }
}
