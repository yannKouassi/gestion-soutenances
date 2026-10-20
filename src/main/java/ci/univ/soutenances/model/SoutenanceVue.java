package ci.univ.soutenances.model;

public class SoutenanceVue {
    private final Soutenance soutenance;
    private final Etudiant etudiant;

    public SoutenanceVue(Soutenance soutenance, Etudiant etudiant) {
        this.soutenance = soutenance;
        this.etudiant = etudiant;
    }

    public Soutenance getSoutenance() {
        return soutenance;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }
}
