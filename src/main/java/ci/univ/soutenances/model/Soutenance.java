package ci.univ.soutenances.model;

import java.time.LocalDateTime;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "Soutenance", uniqueConstraints = {
        @UniqueConstraint(name = "uk_soutenance_memoire", columnNames = "memoire_id")
})
@AttributeOverride(name = "id", column = @Column(name = "id_soutenance", nullable = false, updatable = false))
public class Soutenance extends BaseEntity {
    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutSoutenance statut = StatutSoutenance.PLANIFIEE;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "memoire_id", nullable = false, unique = true)
    private Memoire memoire;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    protected Soutenance() {
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }

    public StatutSoutenance getStatut() {
        return statut;
    }

    public void setStatut(StatutSoutenance statut) {
        this.statut = statut;
    }

    public Memoire getMemoire() {
        return memoire;
    }

    public void setMemoire(Memoire memoire) {
        this.memoire = memoire;
    }

    public Salle getSalle() {
        return salle;
    }

    public void setSalle(Salle salle) {
        this.salle = salle;
    }
}
