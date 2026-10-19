package ci.univ.soutenances.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ci.univ.soutenances.model.Encadreur;
import ci.univ.soutenances.model.Memoire;
import ci.univ.soutenances.repository.EncadreurRepository;
import ci.univ.soutenances.repository.EtudiantRepository;
import ci.univ.soutenances.repository.MemoireRepository;
import ci.univ.soutenances.repository.SoutenanceRepository;
import ci.univ.soutenances.service.MemoireService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

@Stateless
public class MemoireServiceImpl implements MemoireService {
    @EJB
    private MemoireRepository memoireRepository;

    @EJB
    private EncadreurRepository encadreurRepository;

    @EJB
    private SoutenanceRepository soutenanceRepository;

    @EJB
    private EtudiantRepository etudiantRepository;

    @Override
    public List<Memoire> findAll() {
        return memoireRepository.findAll();
    }

    @Override
    public Optional<Memoire> findById(UUID id) {
        return memoireRepository.findById(requireId(id, "du mémoire"));
    }

    @Override
    public List<Memoire> findNonSoutenus() {
        return memoireRepository.findNonSoutenus();
    }

    @Override
    public Memoire save(Memoire memoire, UUID encadreurId) {
        if (memoire == null) {
            throw new IllegalArgumentException("Le mémoire est obligatoire.");
        }
        memoire.setTitre(ReferentielValidation.required(memoire.getTitre(), "Le titre", 200));
        memoire.setDescription(blankToNull(memoire.getDescription()));
        LocalDate dateDepot = memoire.getDateDepot();
        if (dateDepot != null && dateDepot.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La date de dépôt ne peut pas être dans le futur.");
        }
        Encadreur encadreur = encadreurRepository.findById(requireId(encadreurId, "de l’encadreur"))
                .orElseThrow(() -> new IllegalArgumentException("Encadreur introuvable."));
        memoire.setEncadreur(encadreur);
        return memoireRepository.save(memoire);
    }

    @Override
    public void delete(UUID id) {
        Memoire memoire = memoireRepository.findById(requireId(id, "du mémoire"))
                .orElseThrow(() -> new IllegalArgumentException("Mémoire introuvable."));
        if (soutenanceRepository.findByMemoireId(memoire.getId()).isPresent()) {
            throw new IllegalArgumentException(
                    "Ce mémoire est déjà rattaché à une soutenance et ne peut pas être supprimé.");
        }
        if (etudiantRepository.existsByMemoireId(memoire.getId(), null)) {
            throw new IllegalArgumentException(
                    "Ce mémoire est rattaché à un étudiant et ne peut pas être supprimé.");
        }
        memoireRepository.delete(memoire);
    }

    private static UUID requireId(UUID id, String label) {
        if (id == null) {
            throw new IllegalArgumentException("L’identifiant " + label + " est obligatoire.");
        }
        return id;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
