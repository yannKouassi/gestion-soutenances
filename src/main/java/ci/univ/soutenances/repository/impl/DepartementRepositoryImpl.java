package ci.univ.soutenances.repository.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Departement;
import ci.univ.soutenances.repository.DepartementRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

@Stateless
public class DepartementRepositoryImpl implements DepartementRepository {
    @PersistenceContext(unitName = "GestionSoutenancesPU")
    private EntityManager entityManager;

    @Override
    public List<Departement> findAll() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Departement> query = builder.createQuery(Departement.class);
        Root<Departement> departement = query.from(Departement.class);
        query.select(departement).orderBy(builder.asc(departement.get("code")));
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public Optional<Departement> findById(UUID id) {
        return Optional.ofNullable(entityManager.find(Departement.class, id));
    }

    @Override
    public Departement save(Departement departement) {
        if (departement.getId() == null) {
            entityManager.persist(departement);
            return departement;
        }
        return entityManager.merge(departement);
    }

    @Override
    public void delete(Departement departement) {
        entityManager.remove(entityManager.contains(departement)
                ? departement
                : entityManager.merge(departement));
    }
}
