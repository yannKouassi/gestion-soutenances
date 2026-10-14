package ci.univ.soutenances.service;

import java.util.List;
import java.util.UUID;

import ci.univ.soutenances.model.HistoriqueAction;

public interface HistoriqueActionService {
    List<HistoriqueAction> findPage(String screenCode, int offset, int pageSize);

    long countByScreen(String screenCode);

    void record(String screenCode, String action, String label, UUID utilisateurId);
}
