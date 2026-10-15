package ci.univ.soutenances.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Etudiant;

public interface EtudiantService {
    List<Etudiant> findAll();

    Optional<Etudiant> findById(UUID id);

    Etudiant save(Etudiant etudiant, UUID filiereId, UUID memoireId);
}
