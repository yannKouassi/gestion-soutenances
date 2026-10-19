package ci.univ.soutenances.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Etudiant;

public interface EtudiantRepository {
    List<Etudiant> findAll();

    List<Etudiant> findByEncadreurId(UUID encadreurId);

    Optional<Etudiant> findById(UUID id);

    Optional<Etudiant> findByUtilisateurId(UUID utilisateurId);

    boolean emailExists(String email, UUID excludedId);

    boolean matriculeExists(String matricule, UUID excludedId);

    Etudiant save(Etudiant etudiant);
}
