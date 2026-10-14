package ci.univ.soutenances.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Departement;
import ci.univ.soutenances.model.Filiere;
import ci.univ.soutenances.repository.DepartementRepository;
import ci.univ.soutenances.repository.FiliereRepository;
import ci.univ.soutenances.service.FiliereService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

@Stateless
public class FiliereServiceImpl implements FiliereService {
    @EJB
    private FiliereRepository filiereRepository;

    @EJB
    private DepartementRepository departementRepository;

    @Override
    public List<Filiere> findAll() {
        return filiereRepository.findAll();
    }

    @Override
    public Optional<Filiere> findById(UUID id) {
        return filiereRepository.findById(requireId(id, "filière"));
    }

    @Override
    public Filiere save(Filiere filiere, UUID departementId) {
        if (filiere == null) {
            throw new IllegalArgumentException("La filière est obligatoire.");
        }
        filiere.setCode(ReferentielValidation.code(filiere.getCode()));
        filiere.setLibelle(ReferentielValidation.required(filiere.getLibelle(), "Le libellé", 120));
        UUID validDepartementId = requireId(departementId, "département");
        Departement departement = departementRepository.findById(validDepartementId)
                .orElseThrow(() -> new IllegalArgumentException("Département introuvable."));
        filiere.setDepartement(departement);
        return filiereRepository.save(filiere);
    }

    @Override
    public void delete(UUID id) {
        Filiere filiere = filiereRepository.findById(requireId(id, "filière"))
                .orElseThrow(() -> new IllegalArgumentException("Filière introuvable."));
        filiereRepository.delete(filiere);
    }

    private static UUID requireId(UUID id, String label) {
        if (id == null) {
            throw new IllegalArgumentException("L’identifiant du " + label + " est obligatoire.");
        }
        return id;
    }
}
