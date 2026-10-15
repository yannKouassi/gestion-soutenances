package ci.univ.soutenances.repository.impl;

import java.util.List;

import ci.univ.soutenances.model.Memoire;
import ci.univ.soutenances.repository.MemoireRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

@Stateless
public class MemoireRepositoryImpl implements MemoireRepository {
    @PersistenceContext(unitName = "GestionSoutenancesPU")
    private EntityManager entityManager;

    @Override
    public List<Memoire> findAll() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Memoire> query = builder.createQuery(Memoire.class);
        Root<Memoire> memoire = query.from(Memoire.class);
        memoire.fetch("encadreur", JoinType.INNER);
        query.select(memoire).orderBy(builder.asc(memoire.get("titre")));
        return entityManager.createQuery(query).getResultList();
    }
}
