package ci.univ.soutenances.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Departement;

public interface DepartementService {
    List<Departement> findAll();

    Optional<Departement> findById(UUID id);

    Departement save(Departement departement);

    void delete(UUID id);
}
