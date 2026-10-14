package ci.univ.soutenances.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Departement;
import ci.univ.soutenances.repository.DepartementRepository;
import ci.univ.soutenances.service.DepartementService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

@Stateless
public class DepartementServiceImpl implements DepartementService {
    @EJB
    private DepartementRepository departementRepository;

    @Override
    public List<Departement> findAll() {
        return departementRepository.findAll();
    }

    @Override
    public Optional<Departement> findById(UUID id) {
        return departementRepository.findById(requireId(id));
    }

    @Override
    public Departement save(Departement departement) {
        if (departement == null) {
            throw new IllegalArgumentException("Le département est obligatoire.");
        }
        departement.setCode(ReferentielValidation.code(departement.getCode()));
        departement.setNom(ReferentielValidation.required(departement.getNom(), "Le nom", 120));
        return departementRepository.save(departement);
    }

    @Override
    public void delete(UUID id) {
        Departement departement = departementRepository.findById(requireId(id))
                .orElseThrow(() -> new IllegalArgumentException("Département introuvable."));
        departementRepository.delete(departement);
    }

    private static UUID requireId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("L’identifiant du département est obligatoire.");
        }
        return id;
    }
}
