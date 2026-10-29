package ci.univ.soutenances.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Memoire;

public interface MemoireService {
    List<Memoire> findAll();

    Optional<Memoire> findById(UUID id);

    List<Memoire> findNonSoutenus();

    Memoire save(Memoire memoire, UUID encadreurId);

    void delete(UUID id);
}
