package ci.univ.soutenances.repository.impl;

import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Soutenance;
import ci.univ.soutenances.repository.SoutenanceRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

@Stateless
public class SoutenanceRepositoryImpl implements SoutenanceRepository {
    @PersistenceContext(unitName = "GestionSoutenancesPU")
    private EntityManager entityManager;

    @Override
    public Optional<Soutenance> findById(UUID id) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Soutenance> query = builder.createQuery(Soutenance.class);
        Root<Soutenance> soutenance = query.from(Soutenance.class);
        soutenance.fetch("memoire", JoinType.INNER);
        soutenance.fetch("salle", JoinType.INNER);
        query.select(soutenance).distinct(true).where(builder.equal(soutenance.get("id"), id));
        return entityManager.createQuery(query).getResultStream().findFirst();
    }

    @Override
    public boolean existsBySalleId(UUID salleId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Soutenance> soutenance = query.from(Soutenance.class);
        query.select(builder.count(soutenance))
                .where(builder.equal(soutenance.get("salle").get("id"), salleId));
        return entityManager.createQuery(query).getSingleResult() > 0;
    }
}
