package ci.univ.soutenances.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Salle;

public interface SalleRepository {
    List<Salle> findAll();

    Optional<Salle> findById(UUID id);

    Salle save(Salle salle);

    void delete(Salle salle);
}
