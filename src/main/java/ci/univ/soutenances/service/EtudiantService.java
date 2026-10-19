package ci.univ.soutenances.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Etudiant;
import ci.univ.soutenances.model.Evaluation;
import ci.univ.soutenances.model.Soutenance;

public interface EtudiantService {
    List<Etudiant> findAll();

    Optional<Etudiant> findById(UUID id);

    Optional<Etudiant> findByUtilisateurId(UUID utilisateurId);

    Optional<Soutenance> findSoutenanceByMemoireId(UUID memoireId);

    List<Evaluation> findJuryBySoutenanceId(UUID soutenanceId);

    Optional<BigDecimal> calculerMoyenne(UUID soutenanceId);

    Etudiant save(Etudiant etudiant, UUID filiereId, UUID memoireId);
}
