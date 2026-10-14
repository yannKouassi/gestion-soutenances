package ci.univ.soutenances.repository.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Grade;
import ci.univ.soutenances.repository.GradeRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

@Stateless
public class GradeRepositoryImpl implements GradeRepository {
    @PersistenceContext(unitName = "GestionSoutenancesPU")
    private EntityManager entityManager;

    @Override
    public List<Grade> findAll() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Grade> query = builder.createQuery(Grade.class);
        Root<Grade> grade = query.from(Grade.class);
        query.select(grade).orderBy(builder.asc(grade.get("code")));
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public Optional<Grade> findById(UUID id) {
        return Optional.ofNullable(entityManager.find(Grade.class, id));
    }

    @Override
    public Grade save(Grade grade) {
        if (grade.getId() == null) {
            entityManager.persist(grade);
            return grade;
        }
        return entityManager.merge(grade);
    }

    @Override
    public void delete(Grade grade) {
        entityManager.remove(entityManager.contains(grade)
                ? grade
                : entityManager.merge(grade));
    }
}
