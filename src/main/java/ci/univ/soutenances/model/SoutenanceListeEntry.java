package ci.univ.soutenances.model;

public class SoutenanceListeEntry {
    private final Soutenance soutenance;
    private final Etudiant etudiant;
    private final String dateAffichee;
    private final int tailleJury;
    private final String noteAffichee;

    public SoutenanceListeEntry(SoutenanceVue vue, String dateAffichee, int tailleJury, String noteAffichee) {
        this.soutenance = vue.getSoutenance();
        this.etudiant = vue.getEtudiant();
        this.dateAffichee = dateAffichee;
        this.tailleJury = tailleJury;
        this.noteAffichee = noteAffichee;
    }

    public Soutenance getSoutenance() {
        return soutenance;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }

    public String getDateAffichee() {
        return dateAffichee;
    }

    public int getTailleJury() {
        return tailleJury;
    }

    public String getNoteAffichee() {
        return noteAffichee;
    }
}
