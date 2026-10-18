package ci.univ.soutenances.model;

import java.time.LocalDate;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Memoire")
@AttributeOverride(name = "id", column = @Column(name = "id_memoire", nullable = false, updatable = false))
public class Memoire extends BaseEntity {
    @Column(nullable = false, length = 200)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_depot")
    private LocalDate dateDepot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encadreur_id", nullable = false)
    private Encadreur encadreur;

    protected Memoire() {
    }

    public static Memoire create() {
        return new Memoire();
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDateDepot() {
        return dateDepot;
    }

    public void setDateDepot(LocalDate dateDepot) {
        this.dateDepot = dateDepot;
    }

    public Encadreur getEncadreur() {
        return encadreur;
    }

    public void setEncadreur(Encadreur encadreur) {
        this.encadreur = encadreur;
    }
}
