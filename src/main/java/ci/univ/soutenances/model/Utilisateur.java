package ci.univ.soutenances.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "Utilisateur", uniqueConstraints = {
        @UniqueConstraint(name = "uk_utilisateur_login", columnNames = "login")
})
@AttributeOverride(name = "id", column = @Column(name = "id_utilisateur", nullable = false, updatable = false))
public class Utilisateur extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String login;

    @Column(name = "mot_de_passe", nullable = false, length = 255)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleUtilisateur role;

    public Utilisateur() {
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public RoleUtilisateur getRole() {
        return role;
    }

    public void setRole(RoleUtilisateur role) {
        this.role = role;
    }
}
