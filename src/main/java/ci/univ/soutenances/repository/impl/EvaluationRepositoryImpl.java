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
        Join<Evaluation, Encadreur> membre = (Join<Evaluation, Encadreur>) evaluation
                .<Evaluation, Encadreur>fetch("encadreur", JoinType.INNER);
        membre.fetch("grade", JoinType.INNER);
        query.select(evaluation)
                .where(builder.equal(evaluation.get("soutenance").get("id"), soutenanceId))
                .orderBy(builder.asc(membre.get("nom")), builder.asc(membre.get("prenom")));
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<Evaluation> findJuryBySoutenanceId(UUID soutenanceId) {
        return entityManager.createQuery(
                "SELECT e FROM Evaluation e "
                        + "JOIN FETCH e.encadreur "
                        + "WHERE e.soutenance.id = :soutenanceId "
                        + "ORDER BY e.encadreur.nom, e.encadreur.prenom",
                Evaluation.class)
                .setParameter("soutenanceId", soutenanceId)
                .getResultList();
    }

    @Override
    public List<Evaluation> findByEncadreurId(UUID encadreurId) {
        List<Evaluation> evaluations = entityManager.createQuery(
                "SELECT e FROM Evaluation e "
                        + "JOIN FETCH e.soutenance "
                        + "WHERE e.encadreur.id = :encadreurId "
                        + "ORDER BY e.soutenance.dateHeure DESC",
                Evaluation.class)
                .setParameter("encadreurId", encadreurId)
                .getResultList();
        if (!evaluations.isEmpty()) {
            entityManager.createQuery(
                    "SELECT s FROM Soutenance s "
                            + "JOIN FETCH s.memoire "
                            + "JOIN FETCH s.salle "
                            + "WHERE s.id IN :ids",
                    Soutenance.class)
                    .setParameter("ids", evaluations.stream()
                            .map(evaluation -> evaluation.getSoutenance().getId())
                            .toList())
                    .getResultList();
        }
        return evaluations;
    }

    @Override
    public Optional<Evaluation> findBySoutenanceAndEncadreur(UUID soutenanceId, UUID encadreurId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Evaluation> query = builder.createQuery(Evaluation.class);
        Root<Evaluation> evaluation = query.from(Evaluation.class);
        evaluation.fetch("soutenance", JoinType.INNER).fetch("memoire", JoinType.INNER);
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

    @Override
    public void delete(Evaluation evaluation) {
        entityManager.remove(entityManager.contains(evaluation)
                ? evaluation
                : entityManager.merge(evaluation));
    }
}
