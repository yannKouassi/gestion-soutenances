package ci.univ.soutenances.model;

import java.time.LocalDateTime;

public class HistoriqueFiltre {
    private final String ecran;
    private final String action;
    private final String auteur;
    private final LocalDateTime debut;
    private final LocalDateTime finExclusive;

    public HistoriqueFiltre(String ecran, String action, String auteur,
            LocalDateTime debut, LocalDateTime finExclusive) {
        this.ecran = ecran;
        this.action = action;
        this.auteur = auteur;
        this.debut = debut;
        this.finExclusive = finExclusive;
    }

    public String getEcran() {
        return ecran;
    }

    public String getAction() {
        return action;
    }

    public String getAuteur() {
        return auteur;
    }

    public LocalDateTime getDebut() {
        return debut;
    }

    public LocalDateTime getFinExclusive() {
        return finExclusive;
    }
}
