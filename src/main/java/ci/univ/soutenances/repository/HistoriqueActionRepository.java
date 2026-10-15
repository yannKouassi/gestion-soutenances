package ci.univ.soutenances.repository;

import java.util.List;

import ci.univ.soutenances.model.HistoriqueAction;
import ci.univ.soutenances.model.HistoriqueFiltre;

public interface HistoriqueActionRepository {
    List<HistoriqueAction> findPage(String screenCode, int offset, int pageSize);

    long countByScreen(String screenCode);

    List<HistoriqueAction> search(HistoriqueFiltre filtre, int offset, int pageSize);

    long count(HistoriqueFiltre filtre);

    List<String> findDistinctAuteurs();

    HistoriqueAction save(HistoriqueAction historiqueAction);
}
