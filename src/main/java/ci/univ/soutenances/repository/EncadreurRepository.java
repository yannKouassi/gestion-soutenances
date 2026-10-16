package ci.univ.soutenances.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Encadreur;

public interface EncadreurRepository {
    List<Encadreur> findAll();

    Optional<Encadreur> findById(UUID id);

    Optional<Encadreur> findByUtilisateurId(UUID utilisateurId);

    boolean emailExists(String email, UUID excludedId);

    Encadreur save(Encadreur encadreur);
}
