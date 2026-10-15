package ci.univ.soutenances.model;

import java.time.LocalDateTime;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Historique_Action", indexes = {
        @Index(name = "idx_historique_ecran", columnList = "ecran"),
        @Index(name = "idx_historique_action", columnList = "action"),
        @Index(name = "idx_historique_date", columnList = "date_action")
})
@AttributeOverride(name = "id", column = @Column(name = "id_historique", nullable = false, updatable = false))
public class HistoriqueAction extends BaseEntity {
    @Column(name = "type_action", nullable = false, length = 80)
    private String typeAction;

    @Column(name = "ecran", length = 30)
    private String ecran;

    @Column(name = "action", length = 20)
    private String action;

    @Column(name = "date_action", nullable = false)
    private LocalDateTime dateAction;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    public HistoriqueAction() {
    }

    public String getTypeAction() {
        return typeAction;
    }

    public void setTypeAction(String typeAction) {
        this.typeAction = typeAction;
    }

    public String getEcran() {
        return ecran;
    }

    public void setEcran(String ecran) {
        this.ecran = ecran;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getDateAction() {
        return dateAction;
    }

    public void setDateAction(LocalDateTime dateAction) {
        this.dateAction = dateAction;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }
}
