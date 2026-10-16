package ci.univ.soutenances.model;

import java.math.BigDecimal;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "Evaluation", uniqueConstraints = {
        @UniqueConstraint(name = "uk_evaluation_soutenance_encadreur",
                columnNames = { "soutenance_id", "encadreur_id" })
})
@AttributeOverride(name = "id", column = @Column(name = "id_evaluation", nullable = false, updatable = false))
public class Evaluation extends BaseEntity {
    @Column(precision = 4, scale = 2)
    private BigDecimal note;

    @Column(columnDefinition = "TEXT")
    private String avis;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "soutenance_id", nullable = false)
    private Soutenance soutenance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encadreur_id", nullable = false)
    private Encadreur encadreur;

    protected Evaluation() {
    }

    public BigDecimal getNote() {
        return note;
    }

    public void setNote(BigDecimal note) {
        this.note = note;
    }

    public String getAvis() {
        return avis;
    }

    public void setAvis(String avis) {
        this.avis = avis;
    }

    public Soutenance getSoutenance() {
        return soutenance;
    }

    public void setSoutenance(Soutenance soutenance) {
        this.soutenance = soutenance;
    }

    public Encadreur getEncadreur() {
        return encadreur;
    }

    public void setEncadreur(Encadreur encadreur) {
        this.encadreur = encadreur;
    }
}
