package ci.univ.soutenances.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Filiere;

public interface FiliereRepository {
    List<Filiere> findAll();

    Optional<Filiere> findById(UUID id);

    Filiere save(Filiere filiere);

    void delete(Filiere filiere);
}
