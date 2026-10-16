package ci.univ.soutenances.repository.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Encadreur;
import ci.univ.soutenances.repository.EncadreurRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

@Stateless
public class EncadreurRepositoryImpl implements EncadreurRepository {
    @PersistenceContext(unitName = "GestionSoutenancesPU")
    private EntityManager entityManager;

    @Override
    public List<Encadreur> findAll() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Encadreur> query = builder.createQuery(Encadreur.class);
        Root<Encadreur> encadreur = query.from(Encadreur.class);
        encadreur.fetch("grade", JoinType.INNER);
        encadreur.fetch("departement", JoinType.INNER);
        encadreur.fetch("utilisateur", JoinType.INNER);
        query.select(encadreur).distinct(true)
                .orderBy(builder.asc(encadreur.get("nom")), builder.asc(encadreur.get("prenom")));
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public Optional<Encadreur> findById(UUID id) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Encadreur> query = builder.createQuery(Encadreur.class);
        Root<Encadreur> encadreur = query.from(Encadreur.class);
        encadreur.fetch("grade", JoinType.INNER);
        encadreur.fetch("departement", JoinType.INNER);
        encadreur.fetch("utilisateur", JoinType.INNER);
        query.select(encadreur).distinct(true).where(builder.equal(encadreur.get("id"), id));
        return entityManager.createQuery(query).getResultStream().findFirst();
    }

    @Override
    public Optional<Encadreur> findByUtilisateurId(UUID utilisateurId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Encadreur> query = builder.createQuery(Encadreur.class);
        Root<Encadreur> encadreur = query.from(Encadreur.class);
        encadreur.fetch("grade", JoinType.INNER);
        encadreur.fetch("departement", JoinType.INNER);
        encadreur.fetch("utilisateur", JoinType.INNER);
        query.select(encadreur).distinct(true)
                .where(builder.equal(encadreur.get("utilisateur").get("id"), utilisateurId));
        return entityManager.createQuery(query).getResultStream().findFirst();
    }

    @Override
    public boolean emailExists(String email, UUID excludedId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Encadreur> encadreur = query.from(Encadreur.class);
        query.select(builder.count(encadreur)).where(builder.equal(encadreur.get("email"), email),
                excludedId == null ? builder.conjunction() : builder.notEqual(encadreur.get("id"), excludedId));
        return entityManager.createQuery(query).getSingleResult() > 0;
    }

    @Override
    public Encadreur save(Encadreur encadreur) {
        if (encadreur.getId() == null) {
            entityManager.persist(encadreur);
            return encadreur;
        }
        return entityManager.merge(encadreur);
    }
}
