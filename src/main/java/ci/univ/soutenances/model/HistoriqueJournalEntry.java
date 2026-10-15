package ci.univ.soutenances.model;

public class HistoriqueJournalEntry {
    private final String date;
    private final String label;
    private final String action;
    private final String user;
    private final String userRole;
    private final String screen;

    public HistoriqueJournalEntry(String date, String label, String action,
            String user, String userRole, String screen) {
        this.date = date;
        this.label = label;
        this.action = action;
        this.user = user;
        this.userRole = userRole;
        this.screen = screen;
    }

    public String getDate() {
        return date;
    }

    public String getLabel() {
        return label;
    }

    public String getAction() {
        return action;
    }

    public String getUser() {
        return user;
    }

    public String getUserRole() {
        return userRole;
    }

    public String getScreen() {
        return screen;
    }
}
