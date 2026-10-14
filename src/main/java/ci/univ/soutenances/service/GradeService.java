package ci.univ.soutenances.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Grade;

public interface GradeService {
    List<Grade> findAll();

    Optional<Grade> findById(UUID id);

    Grade save(Grade grade);

    void delete(UUID id);
}
