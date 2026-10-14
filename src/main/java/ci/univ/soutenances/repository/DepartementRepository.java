package ci.univ.soutenances.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Departement;

public interface DepartementRepository {
    List<Departement> findAll();

    Optional<Departement> findById(UUID id);

    Departement save(Departement departement);

    void delete(Departement departement);
}
