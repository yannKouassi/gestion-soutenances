package ci.univ.soutenances.model;

public class MembreJuryVue {
    private final String nomComplet;
    private final boolean actuel;
    private final boolean note;

    public MembreJuryVue(String nomComplet, boolean actuel, boolean note) {
        this.nomComplet = nomComplet;
        this.actuel = actuel;
        this.note = note;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public boolean isActuel() {
        return actuel;
    }

    public boolean isNote() {
        return note;
    }
}
