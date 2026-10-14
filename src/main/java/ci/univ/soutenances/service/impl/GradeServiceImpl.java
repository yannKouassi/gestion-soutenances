package ci.univ.soutenances.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Grade;
import ci.univ.soutenances.repository.GradeRepository;
import ci.univ.soutenances.service.GradeService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

@Stateless
public class GradeServiceImpl implements GradeService {
    @EJB
    private GradeRepository gradeRepository;

    @Override
    public List<Grade> findAll() {
        return gradeRepository.findAll();
    }

    @Override
    public Optional<Grade> findById(UUID id) {
        return gradeRepository.findById(requireId(id));
    }

    @Override
    public Grade save(Grade grade) {
        if (grade == null) {
            throw new IllegalArgumentException("Le grade est obligatoire.");
        }
        grade.setCode(ReferentielValidation.code(grade.getCode()));
        grade.setLibelle(ReferentielValidation.required(grade.getLibelle(), "Le libellé", 120));
        return gradeRepository.save(grade);
    }

    @Override
    public void delete(UUID id) {
        Grade grade = gradeRepository.findById(requireId(id))
                .orElseThrow(() -> new IllegalArgumentException("Grade introuvable."));
        gradeRepository.delete(grade);
    }

    private static UUID requireId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("L’identifiant du grade est obligatoire.");
        }
        return id;
    }
}
