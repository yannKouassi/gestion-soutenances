package ci.univ.soutenances.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Encadreur;
import ci.univ.soutenances.model.Evaluation;
import ci.univ.soutenances.model.Etudiant;
import ci.univ.soutenances.model.Soutenance;

public interface EncadreurService {
    List<Encadreur> findAll();

    Optional<Encadreur> findById(UUID id);

    Encadreur save(Encadreur encadreur, UUID gradeId, UUID departementId);

    Optional<Encadreur> findByUtilisateurId(UUID utilisateurId);

    List<Etudiant> findEtudiantsEncadres(UUID encadreurId);

    List<Evaluation> findEvaluationsByEncadreur(UUID encadreurId);

    List<Evaluation> findEvaluationsBySoutenance(UUID soutenanceId);

    Optional<Soutenance> findSoutenanceById(UUID soutenanceId);

    Optional<BigDecimal> calculerMoyenne(UUID soutenanceId);

    Evaluation saisirNote(UUID soutenanceId, UUID encadreurId, BigDecimal note, String avis);
}
