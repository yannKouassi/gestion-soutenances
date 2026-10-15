package ci.univ.soutenances.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Encadreur;

public interface EncadreurService {
    List<Encadreur> findAll();

    Optional<Encadreur> findById(UUID id);

    Encadreur save(Encadreur encadreur, UUID gradeId, UUID departementId);
}
