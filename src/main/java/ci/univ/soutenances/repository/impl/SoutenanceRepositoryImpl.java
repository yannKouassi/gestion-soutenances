package ci.univ.soutenances.repository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Etudiant;
import ci.univ.soutenances.model.Memoire;
import ci.univ.soutenances.model.Soutenance;
import ci.univ.soutenances.model.SoutenanceVue;
import ci.univ.soutenances.model.StatutSoutenance;
import ci.univ.soutenances.repository.SoutenanceRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Stateless
public class SoutenanceRepositoryImpl implements SoutenanceRepository {
    @PersistenceContext(unitName = "GestionSoutenancesPU")
    private EntityManager entityManager;

    @Override
    public List<SoutenanceVue> findAll() {
        return search(null, null, null, null);
    }

    @Override
    public List<SoutenanceVue> findByStatut(StatutSoutenance statut) {
        return search(statut, null, null, null);
    }

    @Override
    public List<SoutenanceVue> findByEtudiantAndFiliere(UUID etudiantId, UUID filiereId) {
        return search(null, etudiantId, filiereId, null);
    }

    @Override
    public Optional<SoutenanceVue> findVueById(UUID id) {
        return search(null, null, null, id).stream().findFirst();
    }

    private List<SoutenanceVue> search(StatutSoutenance statut, UUID etudiantId, UUID filiereId, UUID soutenanceId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = builder.createTupleQuery();
        Root<Soutenance> soutenance = query.from(Soutenance.class);
        Join<Soutenance, Memoire> memoire = (Join<Soutenance, Memoire>) soutenance.<Soutenance, Memoire>fetch(
                "memoire", JoinType.INNER);
        memoire.fetch("encadreur", JoinType.INNER);
        soutenance.fetch("salle", JoinType.INNER);
        Root<Etudiant> etudiant = query.from(Etudiant.class);
        etudiant.fetch("memoire", JoinType.INNER);
        etudiant.fetch("filiere", JoinType.INNER).fetch("departement", JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.equal(etudiant.get("memoire"), memoire));
        if (soutenanceId != null) {
            predicates.add(builder.equal(soutenance.get("id"), soutenanceId));
        }
        if (statut != null) {
            predicates.add(builder.equal(soutenance.get("statut"), statut));
        }
        if (etudiantId != null) {
            predicates.add(builder.equal(etudiant.get("id"), etudiantId));
        }
        if (filiereId != null) {
            predicates.add(builder.equal(etudiant.get("filiere").get("id"), filiereId));
        }

        query.multiselect(soutenance, etudiant).distinct(true)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(builder.desc(soutenance.get("dateHeure")));

        List<Tuple> lignes = entityManager.createQuery(query).getResultList();
        List<SoutenanceVue> soutenances = new ArrayList<>(lignes.size());
        for (Tuple ligne : lignes) {
            soutenances.add(new SoutenanceVue(ligne.get(soutenance), ligne.get(etudiant)));
        }
        return soutenances;
    }

    @Override
    public Optional<Soutenance> findByMemoireId(UUID memoireId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Soutenance> query = builder.createQuery(Soutenance.class);
        Root<Soutenance> soutenance = query.from(Soutenance.class);
        soutenance.fetch("memoire", JoinType.INNER);
        soutenance.fetch("salle", JoinType.INNER);
        query.select(soutenance).distinct(true)
                .where(builder.equal(soutenance.get("memoire").get("id"), memoireId));
        return entityManager.createQuery(query).getResultStream().findFirst();
    }

    @Override
    public Optional<Soutenance> findById(UUID id) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Soutenance> query = builder.createQuery(Soutenance.class);
        Root<Soutenance> soutenance = query.from(Soutenance.class);
        soutenance.fetch("memoire", JoinType.INNER).fetch("encadreur", JoinType.INNER);
        soutenance.fetch("salle", JoinType.INNER);
        query.select(soutenance).distinct(true).where(builder.equal(soutenance.get("id"), id));
        return entityManager.createQuery(query).getResultStream().findFirst();
    }
}
