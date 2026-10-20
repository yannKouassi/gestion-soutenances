package ci.univ.soutenances.repository.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Memoire;
import ci.univ.soutenances.model.Soutenance;
import ci.univ.soutenances.repository.MemoireRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

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

    @Override
    public Optional<Memoire> findById(UUID id) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Memoire> query = builder.createQuery(Memoire.class);
        Root<Memoire> memoire = query.from(Memoire.class);
        memoire.fetch("encadreur", JoinType.INNER);
        query.select(memoire).distinct(true).where(builder.equal(memoire.get("id"), id));
        return entityManager.createQuery(query).getResultStream().findFirst();
    }

    @Override
    public List<Memoire> findNonSoutenus() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Memoire> query = builder.createQuery(Memoire.class);
        Root<Memoire> memoire = query.from(Memoire.class);
        memoire.fetch("encadreur", JoinType.INNER);
        Subquery<UUID> soutenue = query.subquery(UUID.class);
        Root<Soutenance> soutenance = soutenue.from(Soutenance.class);
        soutenue.select(soutenance.get("id"))
                .where(builder.equal(soutenance.get("memoire"), memoire));
        query.select(memoire).distinct(true)
                .where(builder.not(builder.exists(soutenue)))
                .orderBy(builder.asc(memoire.get("titre")));
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public Memoire save(Memoire memoire) {
        if (memoire.getId() == null) {
            entityManager.persist(memoire);
            return memoire;
        }
        return entityManager.merge(memoire);
    }

    @Override
    public void delete(Memoire memoire) {
        entityManager.remove(entityManager.contains(memoire)
                ? memoire
                : entityManager.merge(memoire));
    }
}
