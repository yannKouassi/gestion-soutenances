package ci.univ.soutenances.repository.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Etudiant;
import ci.univ.soutenances.repository.EtudiantRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

@Stateless
public class EtudiantRepositoryImpl implements EtudiantRepository {
    @PersistenceContext(unitName = "GestionSoutenancesPU")
    private EntityManager entityManager;

    @Override
    public List<Etudiant> findAll() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Etudiant> query = builder.createQuery(Etudiant.class);
        Root<Etudiant> etudiant = query.from(Etudiant.class);
        etudiant.fetch("filiere", JoinType.INNER).fetch("departement", JoinType.INNER);
        etudiant.fetch("memoire", JoinType.LEFT);
        etudiant.fetch("utilisateur", JoinType.INNER);
        query.select(etudiant).distinct(true)
                .orderBy(builder.asc(etudiant.get("nom")), builder.asc(etudiant.get("prenom")));
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public Optional<Etudiant> findById(UUID id) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Etudiant> query = builder.createQuery(Etudiant.class);
        Root<Etudiant> etudiant = query.from(Etudiant.class);
        etudiant.fetch("filiere", JoinType.INNER).fetch("departement", JoinType.INNER);
        etudiant.fetch("memoire", JoinType.LEFT);
        etudiant.fetch("utilisateur", JoinType.INNER);
        query.select(etudiant).distinct(true).where(builder.equal(etudiant.get("id"), id));
        return entityManager.createQuery(query).getResultStream().findFirst();
    }

    @Override
    public boolean emailExists(String email, UUID excludedId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Etudiant> etudiant = query.from(Etudiant.class);
        query.select(builder.count(etudiant)).where(builder.equal(etudiant.get("email"), email),
                excludedId == null ? builder.conjunction() : builder.notEqual(etudiant.get("id"), excludedId));
        return entityManager.createQuery(query).getSingleResult() > 0;
    }

    @Override
    public boolean matriculeExists(String matricule, UUID excludedId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Etudiant> etudiant = query.from(Etudiant.class);
        query.select(builder.count(etudiant)).where(builder.equal(etudiant.get("matricule"), matricule),
                excludedId == null ? builder.conjunction() : builder.notEqual(etudiant.get("id"), excludedId));
        return entityManager.createQuery(query).getSingleResult() > 0;
    }

    @Override
    public Etudiant save(Etudiant etudiant) {
        if (etudiant.getId() == null) {
            entityManager.persist(etudiant);
            return etudiant;
        }
        return entityManager.merge(etudiant);
    }
}
