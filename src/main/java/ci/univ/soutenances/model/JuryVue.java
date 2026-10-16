package ci.univ.soutenances.model;

import java.math.BigDecimal;
import java.util.List;

public class JuryVue {
    private final int taille;
    private final int notesSoumises;
    private final BigDecimal moyenne;
    private final List<MembreJuryVue> membres;

    public JuryVue(int taille, int notesSoumises, BigDecimal moyenne, List<MembreJuryVue> membres) {
        this.taille = taille;
        this.notesSoumises = notesSoumises;
        this.moyenne = moyenne;
        this.membres = membres;
    }

    public int getTaille() {
        return taille;
    }

    public int getNotesSoumises() {
        return notesSoumises;
    }

    public BigDecimal getMoyenne() {
        return moyenne;
    }

    public List<MembreJuryVue> getMembres() {
        return membres;
    }
}
