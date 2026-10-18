package ci.univ.soutenances.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Soutenance;
import ci.univ.soutenances.model.SoutenanceVue;
import ci.univ.soutenances.model.StatutSoutenance;

public interface SoutenanceRepository {
    List<SoutenanceVue> findAll();

    List<SoutenanceVue> findByStatut(StatutSoutenance statut);

    List<SoutenanceVue> findByEtudiantAndFiliere(UUID etudiantId, UUID filiereId);

    Optional<SoutenanceVue> findVueById(UUID id);

    Optional<Soutenance> findByMemoireId(UUID memoireId);

    Optional<Soutenance> findById(UUID id);

    boolean existsSalleConflit(UUID salleId, LocalDateTime dateHeure, UUID excludedSoutenanceId);

    Soutenance save(Soutenance soutenance);
}
