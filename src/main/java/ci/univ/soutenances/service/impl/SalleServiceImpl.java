package ci.univ.soutenances.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Departement;
import ci.univ.soutenances.model.Salle;
import ci.univ.soutenances.repository.DepartementRepository;
import ci.univ.soutenances.repository.SalleRepository;
import ci.univ.soutenances.service.SalleService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

@Stateless
public class SalleServiceImpl implements SalleService {
    @EJB
    private SalleRepository salleRepository;

    @EJB
    private DepartementRepository departementRepository;

    @Override
    public List<Salle> findAll() {
        return salleRepository.findAll();
    }

    @Override
    public Optional<Salle> findById(UUID id) {
        return salleRepository.findById(requireId(id, "salle"));
    }

    @Override
    public Salle save(Salle salle, UUID departementId) {
        if (salle == null) {
            throw new IllegalArgumentException("La salle est obligatoire.");
        }
        salle.setNomSalle(ReferentielValidation.required(salle.getNomSalle(), "Le nom de la salle", 100));
        if (salle.getCapacite() <= 0) {
            throw new IllegalArgumentException("La capacité doit être un nombre entier supérieur à zéro.");
        }
        UUID validDepartementId = requireId(departementId, "département");
        Departement departement = departementRepository.findById(validDepartementId)
                .orElseThrow(() -> new IllegalArgumentException("Département introuvable."));
        salle.setDepartement(departement);
        return salleRepository.save(salle);
    }

    @Override
    public void delete(UUID id) {
        Salle salle = salleRepository.findById(requireId(id, "salle"))
                .orElseThrow(() -> new IllegalArgumentException("Salle introuvable."));
        salleRepository.delete(salle);
    }

    private static UUID requireId(UUID id, String label) {
        if (id == null) {
            throw new IllegalArgumentException("L’identifiant de la " + label + " est obligatoire.");
        }
        return id;
    }
}
