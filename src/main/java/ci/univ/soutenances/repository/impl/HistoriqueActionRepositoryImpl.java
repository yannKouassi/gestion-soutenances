package ci.univ.soutenances.repository.impl;

import java.util.ArrayList;
import java.util.List;

import ci.univ.soutenances.model.HistoriqueAction;
import ci.univ.soutenances.model.HistoriqueFiltre;
import ci.univ.soutenances.model.Utilisateur;
import ci.univ.soutenances.repository.HistoriqueActionRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
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
                .where(screenPredicate(builder, action, screenCode))
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
                .where(screenPredicate(builder, action, screenCode));
        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public List<HistoriqueAction> search(HistoriqueFiltre filtre, int offset, int pageSize) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<HistoriqueAction> query = builder.createQuery(HistoriqueAction.class);
        Root<HistoriqueAction> action = query.from(HistoriqueAction.class);
        action.fetch("utilisateur", JoinType.INNER);
        query.select(action)
                .where(filtrePredicates(builder, action, filtre).toArray(new Predicate[0]))
                .orderBy(
                        builder.desc(action.get("dateAction")),
                        builder.desc(action.get("id")));

        return entityManager.createQuery(query)
                .setFirstResult(offset)
                .setMaxResults(pageSize)
                .getResultList();
    }

    @Override
    public long count(HistoriqueFiltre filtre) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<HistoriqueAction> action = query.from(HistoriqueAction.class);
        query.select(builder.count(action))
                .where(filtrePredicates(builder, action, filtre).toArray(new Predicate[0]));
        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public List<String> findDistinctAuteurs() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> query = builder.createQuery(String.class);
        Root<HistoriqueAction> action = query.from(HistoriqueAction.class);
        Join<HistoriqueAction, Utilisateur> utilisateur = action.join("utilisateur", JoinType.INNER);
        query.select(utilisateur.get("login")).distinct(true)
                .orderBy(builder.asc(utilisateur.get("login")));
        return entityManager.createQuery(query).getResultList();
    }

    private static Predicate screenPredicate(CriteriaBuilder builder,
            Root<HistoriqueAction> action, String screenCode) {
        return builder.or(
                builder.equal(action.get("ecran"), screenCode),
                builder.and(
                        builder.isNull(action.get("ecran")),
                        builder.equal(
                                builder.substring(action.get("typeAction"), 1, screenCode.length() + 1),
                                screenCode + "_")));
    }

    private static List<Predicate> filtrePredicates(CriteriaBuilder builder,
            Root<HistoriqueAction> action, HistoriqueFiltre filtre) {
        List<Predicate> predicates = new ArrayList<>();
        if (filtre.getEcran() != null) {
            predicates.add(screenPredicate(builder, action, filtre.getEcran()));
        }
        if (filtre.getAction() != null) {
            String suffixe = "_" + filtre.getAction();
            predicates.add(builder.or(
                    builder.equal(action.get("action"), filtre.getAction()),
                    builder.and(
                            builder.isNull(action.get("action")),
                            builder.greaterThanOrEqualTo(
                                    builder.length(action.get("typeAction")), suffixe.length()),
                            builder.equal(
                                    builder.substring(action.get("typeAction"),
                                            builder.diff(builder.length(action.get("typeAction")),
                                                    suffixe.length() - 1),
                                            builder.literal(suffixe.length())),
                                    suffixe))));
        }
        if (filtre.getAuteur() != null) {
            predicates.add(builder.equal(action.get("utilisateur").get("login"), filtre.getAuteur()));
        }
        if (filtre.getDebut() != null) {
            predicates.add(builder.greaterThanOrEqualTo(action.get("dateAction"), filtre.getDebut()));
        }
        if (filtre.getFinExclusive() != null) {
            predicates.add(builder.lessThan(action.get("dateAction"), filtre.getFinExclusive()));
        }
        return predicates;
    }

    @Override
    public HistoriqueAction save(HistoriqueAction historiqueAction) {
        entityManager.persist(historiqueAction);
        return historiqueAction;
    }
}
