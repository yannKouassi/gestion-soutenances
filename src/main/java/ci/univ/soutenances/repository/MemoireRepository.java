package ci.univ.soutenances.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Memoire;

public interface MemoireRepository {
    List<Memoire> findAll();

    Optional<Memoire> findById(UUID id);

    List<Memoire> findNonSoutenus();

    Memoire save(Memoire memoire);

    void delete(Memoire memoire);
}
