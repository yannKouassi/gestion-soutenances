package ci.univ.soutenances.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Filiere;

public interface FiliereService {
    List<Filiere> findAll();

    Optional<Filiere> findById(UUID id);

    Filiere save(Filiere filiere, UUID departementId);

    void delete(UUID id);
}
