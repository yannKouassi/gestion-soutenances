package ci.univ.soutenances.model;

import java.math.BigDecimal;

public class JuryResume {
    private final int nombreMembres;
    private final BigDecimal moyenne;

    public JuryResume(int nombreMembres, BigDecimal moyenne) {
        this.nombreMembres = nombreMembres;
        this.moyenne = moyenne;
    }

    public int getNombreMembres() {
        return nombreMembres;
    }

    public BigDecimal getMoyenne() {
        return moyenne;
    }
}
