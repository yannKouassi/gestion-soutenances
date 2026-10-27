package ci.univ.soutenances.model;

import java.math.BigDecimal;
import java.util.List;

public class EtudiantSoutenanceVue {
    private final Etudiant etudiant;
    private final Soutenance soutenance;
    private final List<Evaluation> membresJury;
    private final BigDecimal noteFinale;
    private final String dateSoutenanceAffichee;

    public EtudiantSoutenanceVue(Etudiant etudiant, Soutenance soutenance,
            List<Evaluation> membresJury, BigDecimal noteFinale, String dateSoutenanceAffichee) {
        this.etudiant = etudiant;
        this.soutenance = soutenance;
        this.membresJury = List.copyOf(membresJury);
        this.noteFinale = noteFinale;
        this.dateSoutenanceAffichee = dateSoutenanceAffichee;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }

    public Soutenance getSoutenance() {
        return soutenance;
    }

    public List<Evaluation> getMembresJury() {
        return membresJury;
    }

    public BigDecimal getNoteFinale() {
        return noteFinale;
    }

    public String getDateSoutenanceAffichee() {
        return dateSoutenanceAffichee;
    }
}
