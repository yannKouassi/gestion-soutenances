package ci.univ.soutenances.repository.impl;

import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.RoleUtilisateur;
import ci.univ.soutenances.model.Utilisateur;
import ci.univ.soutenances.repository.UtilisateurRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

@Stateless
public class UtilisateurRepositoryImpl implements UtilisateurRepository {
    @PersistenceContext(unitName = "GestionSoutenancesPU")
    private EntityManager entityManager;

    @Override
    public Optional<Utilisateur> findById(UUID id) {
        return Optional.ofNullable(entityManager.find(Utilisateur.class, id));
    }

    @Override
    public Optional<Utilisateur> findByLogin(String login) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Utilisateur> query = builder.createQuery(Utilisateur.class);
        Root<Utilisateur> utilisateur = query.from(Utilisateur.class);
        query.select(utilisateur).where(builder.equal(utilisateur.get("login"), login));
        return entityManager.createQuery(query).getResultStream().findFirst();
    }

    @Override
    public boolean existsAdministrator() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Utilisateur> utilisateur = query.from(Utilisateur.class);
        query.select(builder.count(utilisateur))
                .where(builder.equal(utilisateur.get("role"), RoleUtilisateur.ADMIN));
        return entityManager.createQuery(query).getSingleResult() > 0;
    }

    @Override
    public Utilisateur save(Utilisateur utilisateur) {
        if (utilisateur.getId() == null) {
            entityManager.persist(utilisateur);
            return utilisateur;
        }
        return entityManager.merge(utilisateur);
    }
}
