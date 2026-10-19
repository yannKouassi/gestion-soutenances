package ci.univ.soutenances.repository;

import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Soutenance;

public interface SoutenanceRepository {
    Optional<Soutenance> findById(UUID id);

    Optional<Soutenance> findByMemoireId(UUID memoireId);
}
