package ci.univ.soutenances.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Evaluation;

public interface EvaluationRepository {
    List<Evaluation> findBySoutenanceId(UUID soutenanceId);

    List<Evaluation> findByEncadreurId(UUID encadreurId);

    Optional<Evaluation> findBySoutenanceAndEncadreur(UUID soutenanceId, UUID encadreurId);

    Evaluation save(Evaluation evaluation);
}
