package ci.univ.soutenances.service;

public interface CourrielService {
    void sendCredentials(String recipient, String displayName, String login, String password, boolean reset);
}
