package ci.univ.soutenances.repository.impl;

import java.util.List;

import ci.univ.soutenances.model.HistoriqueAction;
import ci.univ.soutenances.repository.HistoriqueActionRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

@Stateless
public class HistoriqueActionRepositoryImpl implements HistoriqueActionRepository {
    @PersistenceContext(unitName = "GestionSoutenancesPU")
    private EntityManager entityManager;

    @Override
    public List<HistoriqueAction> findPage(String screenCode, int offset, int pageSize) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<HistoriqueAction> query = builder.createQuery(HistoriqueAction.class);
        Root<HistoriqueAction> action = query.from(HistoriqueAction.class);
        action.fetch("utilisateur", JoinType.INNER);
        query.select(action)
                .where(builder.equal(
                        builder.substring(action.get("typeAction"), 1, screenCode.length() + 1),
                        screenCode + "_"))
                .orderBy(
                        builder.desc(action.get("dateAction")),
                        builder.desc(action.get("id")));

        return entityManager.createQuery(query)
                .setFirstResult(offset)
                .setMaxResults(pageSize)
                .getResultList();
    }

    @Override
    public long countByScreen(String screenCode) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<HistoriqueAction> action = query.from(HistoriqueAction.class);
        query.select(builder.count(action))
                .where(builder.equal(
                        builder.substring(action.get("typeAction"), 1, screenCode.length() + 1),
                        screenCode + "_"));
        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public HistoriqueAction save(HistoriqueAction historiqueAction) {
        entityManager.persist(historiqueAction);
        return historiqueAction;
    }
}
