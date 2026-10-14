package ci.univ.soutenances.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Salle;

public interface SalleService {
    List<Salle> findAll();

    Optional<Salle> findById(UUID id);

    Salle save(Salle salle, UUID departementId);

    void delete(UUID id);
}
