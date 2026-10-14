package ci.univ.soutenances.repository.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Salle;
import ci.univ.soutenances.repository.SalleRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

@Stateless
public class SalleRepositoryImpl implements SalleRepository {
    @PersistenceContext(unitName = "GestionSoutenancesPU")
    private EntityManager entityManager;

    @Override
    public List<Salle> findAll() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Salle> query = builder.createQuery(Salle.class);
        Root<Salle> salle = query.from(Salle.class);
        salle.fetch("departement", JoinType.INNER);
        query.select(salle).distinct(true).orderBy(builder.asc(salle.get("nomSalle")));
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public Optional<Salle> findById(UUID id) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Salle> query = builder.createQuery(Salle.class);
        Root<Salle> salle = query.from(Salle.class);
        salle.fetch("departement", JoinType.INNER);
        query.select(salle).distinct(true).where(builder.equal(salle.get("id"), id));
        return entityManager.createQuery(query).getResultStream().findFirst();
    }

    @Override
    public Salle save(Salle salle) {
        if (salle.getId() == null) {
            entityManager.persist(salle);
            return salle;
        }
        return entityManager.merge(salle);
    }

    @Override
    public void delete(Salle salle) {
        entityManager.remove(entityManager.contains(salle)
                ? salle
                : entityManager.merge(salle));
    }
}
