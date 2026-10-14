package ci.univ.soutenances.repository.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Filiere;
import ci.univ.soutenances.repository.FiliereRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

@Stateless
public class FiliereRepositoryImpl implements FiliereRepository {
    @PersistenceContext(unitName = "GestionSoutenancesPU")
    private EntityManager entityManager;

    @Override
    public List<Filiere> findAll() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Filiere> query = builder.createQuery(Filiere.class);
        Root<Filiere> filiere = query.from(Filiere.class);
        filiere.fetch("departement", JoinType.INNER);
        query.select(filiere).distinct(true).orderBy(builder.asc(filiere.get("code")));
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public Optional<Filiere> findById(UUID id) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Filiere> query = builder.createQuery(Filiere.class);
        Root<Filiere> filiere = query.from(Filiere.class);
        filiere.fetch("departement", JoinType.INNER);
        query.select(filiere).distinct(true).where(builder.equal(filiere.get("id"), id));
        return entityManager.createQuery(query).getResultStream().findFirst();
    }

    @Override
    public Filiere save(Filiere filiere) {
        if (filiere.getId() == null) {
            entityManager.persist(filiere);
            return filiere;
        }
        return entityManager.merge(filiere);
    }

    @Override
    public void delete(Filiere filiere) {
        entityManager.remove(entityManager.contains(filiere)
                ? filiere
                : entityManager.merge(filiere));
    }
}
