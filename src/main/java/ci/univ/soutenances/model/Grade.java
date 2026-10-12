package ci.univ.soutenances.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "Grade", uniqueConstraints = {
        @UniqueConstraint(name = "uk_grade_code", columnNames = "code")
})
@AttributeOverride(name = "id", column = @Column(name = "id_grade", nullable = false, updatable = false))
public class Grade extends BaseEntity {
    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 120)
    private String libelle;

    protected Grade() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
}
