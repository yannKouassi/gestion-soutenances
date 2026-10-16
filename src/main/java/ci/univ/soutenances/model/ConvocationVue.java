package ci.univ.soutenances.model;

public class ConvocationVue {
    private final Evaluation convocation;
    private final JuryVue jury;
    private final String dateAffichee;

    public ConvocationVue(Evaluation convocation, JuryVue jury, String dateAffichee) {
        this.convocation = convocation;
        this.jury = jury;
        this.dateAffichee = dateAffichee;
    }

    public Evaluation getConvocation() {
        return convocation;
    }

    public JuryVue getJury() {
        return jury;
    }

    public String getDateAffichee() {
        return dateAffichee;
    }
}
