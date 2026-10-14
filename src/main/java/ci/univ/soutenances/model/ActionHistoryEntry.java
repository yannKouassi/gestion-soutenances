package ci.univ.soutenances.model;

public class ActionHistoryEntry {
    private final String date;
    private final String label;
    private final String action;
    private final String user;
    private final String screen;

    public ActionHistoryEntry(String date, String label, String action, String user, String screen) {
        this.date = date;
        this.label = label;
        this.action = action;
        this.user = user;
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

    public String getScreen() {
        return screen;
    }
}
