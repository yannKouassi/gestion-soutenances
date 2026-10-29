package ci.univ.soutenances.model;

public class SoutenanceAgenda {
    private final String date;
    private final String memoire;
    private final String etudiant;
    private final String salle;
    private final StatutSoutenance statut;

    public SoutenanceAgenda(String date, String memoire, String etudiant, String salle,
            StatutSoutenance statut) {
        this.date = date;
        this.memoire = memoire;
        this.etudiant = etudiant;
        this.salle = salle;
        this.statut = statut;
    }

    public String getDate() {
        return date;
    }

    public String getMemoire() {
        return memoire;
    }

    public String getEtudiant() {
        return etudiant;
    }

    public String getSalle() {
        return salle;
    }

    public StatutSoutenance getStatut() {
        return statut;
    }
}
