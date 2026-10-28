package ci.univ.soutenances.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Encadreur;
import ci.univ.soutenances.model.Evaluation;
import ci.univ.soutenances.model.Memoire;
import ci.univ.soutenances.model.Salle;
import ci.univ.soutenances.model.Soutenance;
import ci.univ.soutenances.model.SoutenanceVue;
import ci.univ.soutenances.model.StatutSoutenance;
import ci.univ.soutenances.repository.EncadreurRepository;
import ci.univ.soutenances.repository.EvaluationRepository;
import ci.univ.soutenances.repository.MemoireRepository;
import ci.univ.soutenances.repository.SalleRepository;
import ci.univ.soutenances.repository.SoutenanceRepository;
import ci.univ.soutenances.service.SoutenanceService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

@Stateless
public class SoutenanceServiceImpl implements SoutenanceService {
    private static final int JURY_MINIMUM = 2;

    @EJB
    private SoutenanceRepository soutenanceRepository;

    @EJB
    private EvaluationRepository evaluationRepository;

    @EJB
    private MemoireRepository memoireRepository;

    @EJB
    private SalleRepository salleRepository;

    @EJB
    private EncadreurRepository encadreurRepository;

    @Override
    public Optional<Soutenance> findById(UUID id) {
        return soutenanceRepository.findById(requireId(id, "de la soutenance"));
    }

    @Override
    public Optional<SoutenanceVue> findVueById(UUID id) {
        return soutenanceRepository.findVueById(requireId(id, "de la soutenance"));
    }

    @Override
    public List<SoutenanceVue> lister(StatutSoutenance statut, UUID etudiantId, UUID filiereId, LocalDate jour) {
        List<SoutenanceVue> soutenances = soutenanceRepository.findByFiltres(statut, etudiantId, filiereId);
        if (jour == null) {
            return soutenances;
        }
        return soutenances.stream()
                .filter(vue -> vue.getSoutenance().getDateHeure() != null
                        && vue.getSoutenance().getDateHeure().toLocalDate().equals(jour))
                .toList();
    }

    @Override
    public Soutenance save(Soutenance soutenance, UUID memoireId, UUID salleId) {
        if (soutenance == null) {
            throw new IllegalArgumentException("Les informations de la soutenance sont obligatoires.");
        }
        LocalDateTime dateHeure = soutenance.getDateHeure();
        if (dateHeure == null) {
            throw new IllegalArgumentException("La date et l'heure de la soutenance sont obligatoires.");
        }
        if (soutenance.getId() == null) {
            requireFutur(dateHeure);
        } else {
            LocalDateTime dateInitiale = soutenanceRepository.findById(soutenance.getId())
                    .map(Soutenance::getDateHeure)
                    .orElse(null);
            if (!dateHeure.equals(dateInitiale)) {
                requireFutur(dateHeure);
            }
        }
        Memoire memoire = memoireRepository.findById(requireId(memoireId, "du mémoire"))
                .orElseThrow(() -> new IllegalArgumentException("Mémoire introuvable."));
        if (memoire.getDateDepot() != null && dateHeure.toLocalDate().isBefore(memoire.getDateDepot())) {
            throw new IllegalArgumentException(
                    "La date de la soutenance ne peut pas être antérieure à la date de dépôt du mémoire.");
        }
        Salle salle = salleRepository.findById(requireId(salleId, "de la salle"))
                .orElseThrow(() -> new IllegalArgumentException("Salle introuvable."));
        Optional<Soutenance> dejaPlanifiee = soutenanceRepository.findByMemoireId(memoire.getId());
        if (dejaPlanifiee.isPresent() && !dejaPlanifiee.get().getId().equals(soutenance.getId())) {
            throw new IllegalArgumentException("Ce mémoire est déjà rattaché à une soutenance.");
        }
        if (soutenanceRepository.existsSalleConflit(salle.getId(), dateHeure, soutenance.getId())) {
            throw new IllegalArgumentException("Cette salle est déjà occupée sur ce créneau.");
        }
        soutenance.setMemoire(memoire);
        soutenance.setSalle(salle);
        return soutenanceRepository.save(soutenance);
    }

    @Override
    public List<Evaluation> findMembres(UUID soutenanceId) {
        return evaluationRepository.findBySoutenanceId(requireId(soutenanceId, "de la soutenance"));
    }

    @Override
    public Evaluation ajouterMembre(UUID soutenanceId, UUID encadreurId) {
        Soutenance soutenance = soutenanceRepository.findById(requireId(soutenanceId, "de la soutenance"))
                .orElseThrow(() -> new IllegalArgumentException("Soutenance introuvable."));
        requirePlanifiee(soutenance);
        Encadreur encadreur = encadreurRepository.findById(requireId(encadreurId, "de l'encadreur"))
                .orElseThrow(() -> new IllegalArgumentException("Encadreur introuvable."));
        if (evaluationRepository.findBySoutenanceAndEncadreur(soutenance.getId(), encadreur.getId()).isPresent()) {
            throw new IllegalArgumentException("Cet encadreur est déjà membre du jury de cette soutenance.");
        }
        if (soutenance.getMemoire() != null && soutenance.getMemoire().getEncadreur() != null
                && encadreur.getId().equals(soutenance.getMemoire().getEncadreur().getId())) {
            throw new IllegalArgumentException(
                    "Cet encadreur encadre déjà ce mémoire : il ne peut pas être ajouté au jury.");
        }
        Evaluation evaluation = Evaluation.create();
        evaluation.setSoutenance(soutenance);
        evaluation.setEncadreur(encadreur);
        return evaluationRepository.save(evaluation);
    }

    @Override
    public Evaluation retirerMembre(UUID soutenanceId, UUID encadreurId) {
        UUID soutenance = requireId(soutenanceId, "de la soutenance");
        UUID encadreur = requireId(encadreurId, "de l'encadreur");
        Evaluation evaluation = evaluationRepository.findBySoutenanceAndEncadreur(soutenance, encadreur)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cet encadreur n'est pas membre du jury de cette soutenance."));
        requirePlanifiee(evaluation.getSoutenance());
        if (evaluationRepository.findBySoutenanceId(soutenance).size() <= JURY_MINIMUM) {
            throw new IllegalArgumentException("Un jury doit comporter au moins deux membres.");
        }
        evaluationRepository.delete(evaluation);
        return evaluation;
    }

    @Override
    public Optional<BigDecimal> calculerMoyenne(UUID soutenanceId) {
        List<Evaluation> jury = evaluationRepository.findBySoutenanceId(requireId(soutenanceId, "de la soutenance"));
        if (jury.isEmpty()) {
            return Optional.empty();
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Evaluation evaluation : jury) {
            if (evaluation.getNote() == null) {
                return Optional.empty();
            }
            total = total.add(evaluation.getNote());
        }
        return Optional.of(total.divide(BigDecimal.valueOf(jury.size()), 2, RoundingMode.HALF_UP));
    }

    @Override
    public void mettreAJourStatutApresEvaluation(UUID soutenanceId) {
        UUID id = requireId(soutenanceId, "de la soutenance");
        List<Evaluation> jury = evaluationRepository.findBySoutenanceId(id);
        if (jury.size() < JURY_MINIMUM || jury.stream().anyMatch(membre -> membre.getNote() == null)) {
            return;
        }
        soutenanceRepository.findById(id).ifPresent(soutenance -> {
            if (soutenance.getStatut() == StatutSoutenance.PLANIFIEE) {
                soutenance.setStatut(StatutSoutenance.EVALUEE);
            }
        });
    }

    private static void requirePlanifiee(Soutenance soutenance) {
        if (soutenance.getStatut() != StatutSoutenance.PLANIFIEE) {
            throw new IllegalArgumentException(
                    "La composition du jury est figée : la soutenance n'est plus au statut PLANIFIEE.");
        }
    }

    private static void requireFutur(LocalDateTime dateHeure) {
        if (!dateHeure.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "La date et l'heure de la soutenance doivent être dans le futur.");
        }
    }

    private static UUID requireId(UUID id, String label) {
        if (id == null) {
            throw new IllegalArgumentException("L'identifiant " + label + " est obligatoire.");
        }
        return id;
    }
}
