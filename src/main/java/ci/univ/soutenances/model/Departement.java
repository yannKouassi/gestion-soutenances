package ci.univ.soutenances.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "Departement", uniqueConstraints = {
        @UniqueConstraint(name = "uk_departement_code", columnNames = "code")
})
@AttributeOverride(name = "id", column = @Column(name = "id_departement", nullable = false, updatable = false))
public class Departement extends BaseEntity {
    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 120)
    private String nom;

    public Departement() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}
