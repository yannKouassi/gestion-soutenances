package ci.univ.soutenances.service;

import java.util.List;
import java.util.UUID;

import ci.univ.soutenances.model.AccountEntry;
import ci.univ.soutenances.model.RoleUtilisateur;
import ci.univ.soutenances.model.Utilisateur;

public interface CompteService {
    Utilisateur createLinkedAccount(RoleUtilisateur role, String email, String displayName);

    void updateLinkedAccountLogin(Utilisateur utilisateur, String email);

    List<AccountEntry> findAll();

    void setActive(UUID id, boolean active);

    void resetPassword(UUID id);
}
