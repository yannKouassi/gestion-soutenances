package ci.univ.soutenances.repository;

import java.util.List;

import ci.univ.soutenances.model.HistoriqueAction;

public interface HistoriqueActionRepository {
    List<HistoriqueAction> findPage(String screenCode, int offset, int pageSize);

    long countByScreen(String screenCode);

    HistoriqueAction save(HistoriqueAction historiqueAction);
}
