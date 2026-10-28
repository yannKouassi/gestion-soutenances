package ci.univ.soutenances.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Evaluation;
import ci.univ.soutenances.model.Soutenance;
import ci.univ.soutenances.model.SoutenanceVue;
import ci.univ.soutenances.model.StatutSoutenance;

public interface SoutenanceService {
    Optional<Soutenance> findById(UUID id);

    Optional<SoutenanceVue> findVueById(UUID id);

    List<SoutenanceVue> lister(StatutSoutenance statut, UUID etudiantId, UUID filiereId, LocalDate jour);

    Soutenance save(Soutenance soutenance, UUID memoireId, UUID salleId);

    List<Evaluation> findMembres(UUID soutenanceId);

    Evaluation ajouterMembre(UUID soutenanceId, UUID encadreurId);

    Evaluation retirerMembre(UUID soutenanceId, UUID encadreurId);

    Optional<BigDecimal> calculerMoyenne(UUID soutenanceId);

    void mettreAJourStatutApresEvaluation(UUID soutenanceId);

    Soutenance annuler(UUID soutenanceId);
}
