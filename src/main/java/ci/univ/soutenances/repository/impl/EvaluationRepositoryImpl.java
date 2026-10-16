package ci.univ.soutenances.repository.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Encadreur;
import ci.univ.soutenances.model.Evaluation;
import ci.univ.soutenances.model.Soutenance;
import ci.univ.soutenances.repository.EvaluationRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

@Stateless
public class EvaluationRepositoryImpl implements EvaluationRepository {
    @PersistenceContext(unitName = "GestionSoutenancesPU")
    private EntityManager entityManager;

    @Override
    public List<Evaluation> findBySoutenanceId(UUID soutenanceId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Evaluation> query = builder.createQuery(Evaluation.class);
        Root<Evaluation> evaluation = query.from(Evaluation.class);
        Join<Evaluation, Encadreur> membre = evaluation.join("encadreur", JoinType.INNER);
        query.select(evaluation)
                .where(builder.equal(evaluation.get("soutenance").get("id"), soutenanceId))
                .orderBy(builder.asc(membre.get("nom")), builder.asc(membre.get("prenom")));
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<Evaluation> findByEncadreurId(UUID encadreurId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Evaluation> query = builder.createQuery(Evaluation.class);
        Root<Evaluation> evaluation = query.from(Evaluation.class);
        Join<Evaluation, Soutenance> convocation = evaluation.join("soutenance", JoinType.INNER);
        convocation.fetch("memoire", JoinType.INNER);
        convocation.fetch("salle", JoinType.INNER);
        query.select(evaluation)
                .where(builder.equal(evaluation.get("encadreur").get("id"), encadreurId))
                .orderBy(builder.desc(convocation.get("dateHeure")));
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public Optional<Evaluation> findBySoutenanceAndEncadreur(UUID soutenanceId, UUID encadreurId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Evaluation> query = builder.createQuery(Evaluation.class);
        Root<Evaluation> evaluation = query.from(Evaluation.class);
        evaluation.fetch("soutenance", JoinType.INNER);
        evaluation.fetch("encadreur", JoinType.INNER);
        query.select(evaluation).distinct(true).where(
                builder.equal(evaluation.get("soutenance").get("id"), soutenanceId),
                builder.equal(evaluation.get("encadreur").get("id"), encadreurId));
        return entityManager.createQuery(query).getResultStream().findFirst();
    }

    @Override
    public Evaluation save(Evaluation evaluation) {
        if (evaluation.getId() == null) {
            entityManager.persist(evaluation);
            return evaluation;
        }
        return entityManager.merge(evaluation);
    }
}
