package ci.univ.soutenances.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Grade;

public interface GradeRepository {
    List<Grade> findAll();

    Optional<Grade> findById(UUID id);

    Grade save(Grade grade);

    void delete(Grade grade);
}
