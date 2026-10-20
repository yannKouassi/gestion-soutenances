package ci.univ.soutenances.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import ci.univ.soutenances.model.HistoriqueAction;
import ci.univ.soutenances.model.HistoriqueFiltre;

public interface HistoriqueActionService {
    Map<String, String> screens();

    List<HistoriqueAction> findPage(String screenCode, int offset, int pageSize);

    long countByScreen(String screenCode);

    List<HistoriqueAction> search(HistoriqueFiltre filtre, int offset, int pageSize);

    long count(HistoriqueFiltre filtre);

    List<String> findDistinctAuteurs();

    void record(String screenCode, String action, String label, UUID utilisateurId);
}
