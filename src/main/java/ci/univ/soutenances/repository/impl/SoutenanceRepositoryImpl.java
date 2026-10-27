package ci.univ.soutenances.repository.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Etudiant;
import ci.univ.soutenances.model.Encadreur;
import ci.univ.soutenances.model.Memoire;
import ci.univ.soutenances.model.Salle;
import ci.univ.soutenances.model.Soutenance;
import ci.univ.soutenances.model.SoutenanceVue;
import ci.univ.soutenances.model.StatutSoutenance;
import ci.univ.soutenances.repository.SoutenanceRepository;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.Fetch;
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
    public List<SoutenanceVue> findByFiltres(StatutSoutenance statut, UUID etudiantId, UUID filiereId) {
        return search(statut, etudiantId, filiereId, null);
    }

    @Override
    public Optional<SoutenanceVue> findVueById(UUID id) {
        return search(null, null, null, id).stream().findFirst();
    }

    private List<SoutenanceVue> search(StatutSoutenance statut, UUID etudiantId, UUID filiereId, UUID soutenanceId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = builder.createTupleQuery();
        Root<Soutenance> soutenance = query.from(Soutenance.class);
        Join<Soutenance, Memoire> memoire = (Join<Soutenance, Memoire>) soutenance
                .<Soutenance, Memoire>fetch("memoire", JoinType.INNER);
        Join<Memoire, Encadreur> encadreur = (Join<Memoire, Encadreur>) memoire
                .<Memoire, Encadreur>fetch("encadreur", JoinType.INNER);
        encadreur.fetch("grade", JoinType.INNER);
        encadreur.fetch("departement", JoinType.INNER);
        Join<Soutenance, Salle> salle = (Join<Soutenance, Salle>) soutenance
                .<Soutenance, Salle>fetch("salle", JoinType.INNER);
        salle.fetch("departement", JoinType.INNER);
        Join<Memoire, Etudiant> etudiant = (Join<Memoire, Etudiant>) memoire
                .<Memoire, Etudiant>fetch("etudiant", JoinType.LEFT);
        Fetch<Etudiant, ?> filiere = etudiant.fetch("filiere", JoinType.LEFT);
        filiere.fetch("departement", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();
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
        Join<Soutenance, Memoire> memoire = (Join<Soutenance, Memoire>) soutenance
                .<Soutenance, Memoire>fetch("memoire", JoinType.INNER);
        Join<Memoire, Encadreur> encadreur = (Join<Memoire, Encadreur>) memoire
                .<Memoire, Encadreur>fetch("encadreur", JoinType.INNER);
        encadreur.fetch("grade", JoinType.INNER);
        encadreur.fetch("departement", JoinType.INNER);
        Join<Soutenance, Salle> salle = (Join<Soutenance, Salle>) soutenance
                .<Soutenance, Salle>fetch("salle", JoinType.INNER);
        salle.fetch("departement", JoinType.INNER);
        query.select(soutenance).distinct(true)
                .where(builder.equal(soutenance.get("memoire").get("id"), memoireId));
        return entityManager.createQuery(query).getResultStream().findFirst();
    }

    @Override
    public Optional<Soutenance> findById(UUID id) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Soutenance> query = builder.createQuery(Soutenance.class);
        Root<Soutenance> soutenance = query.from(Soutenance.class);
        Join<Soutenance, Memoire> memoire = (Join<Soutenance, Memoire>) soutenance
                .<Soutenance, Memoire>fetch("memoire", JoinType.INNER);
        Join<Memoire, Encadreur> encadreur = (Join<Memoire, Encadreur>) memoire
                .<Memoire, Encadreur>fetch("encadreur", JoinType.INNER);
        encadreur.fetch("grade", JoinType.INNER);
        encadreur.fetch("departement", JoinType.INNER);
        Join<Memoire, Etudiant> etudiant = (Join<Memoire, Etudiant>) memoire
                .<Memoire, Etudiant>fetch("etudiant", JoinType.LEFT);
        etudiant.fetch("filiere", JoinType.LEFT).fetch("departement", JoinType.LEFT);
        Join<Soutenance, Salle> salle = (Join<Soutenance, Salle>) soutenance
                .<Soutenance, Salle>fetch("salle", JoinType.INNER);
        salle.fetch("departement", JoinType.INNER);
        query.select(soutenance).distinct(true).where(builder.equal(soutenance.get("id"), id));
        return entityManager.createQuery(query).getResultStream().findFirst();
    }

    @Override
    public boolean existsSalleConflit(UUID salleId, LocalDateTime dateHeure, UUID excludedSoutenanceId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Soutenance> soutenance = query.from(Soutenance.class);
        query.select(builder.count(soutenance)).where(
                builder.equal(soutenance.get("salle").get("id"), salleId),
                builder.equal(soutenance.get("dateHeure"), dateHeure),
                excludedSoutenanceId == null ? builder.conjunction()
                        : builder.notEqual(soutenance.get("id"), excludedSoutenanceId));
        return entityManager.createQuery(query).getSingleResult() > 0;
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

    @Override
    public Soutenance save(Soutenance soutenance) {
        if (soutenance.getId() == null) {
            entityManager.persist(soutenance);
            return soutenance;
        }
        return entityManager.merge(soutenance);
    }
}
