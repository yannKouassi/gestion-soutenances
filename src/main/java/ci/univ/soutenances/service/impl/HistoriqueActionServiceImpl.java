package ci.univ.soutenances.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import ci.univ.soutenances.model.HistoriqueAction;
import ci.univ.soutenances.model.HistoriqueFiltre;
import ci.univ.soutenances.model.Utilisateur;
import ci.univ.soutenances.repository.HistoriqueActionRepository;
import ci.univ.soutenances.repository.UtilisateurRepository;
import ci.univ.soutenances.service.HistoriqueActionService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

@Stateless
public class HistoriqueActionServiceImpl implements HistoriqueActionService {
    private static final Set<String> SCREENS = Set.of(
            "GRADE", "DEPARTEMENT", "FILIERE", "SALLE", "ENCADREUR", "ETUDIANT", "UTILISATEUR");
    private static final Set<String> ACTIONS = Set.of("CREATION", "MODIFICATION", "SUPPRESSION");

    @EJB
    private HistoriqueActionRepository historiqueActionRepository;

    @EJB
    private UtilisateurRepository utilisateurRepository;

    @Override
    public List<HistoriqueAction> findPage(String screenCode, int offset, int pageSize) {
        requireScreen(screenCode);
        if (offset < 0 || pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("Paramètres de pagination invalides.");
        }
        return historiqueActionRepository.findPage(screenCode, offset, pageSize);
    }

    @Override
    public long countByScreen(String screenCode) {
        requireScreen(screenCode);
        return historiqueActionRepository.countByScreen(screenCode);
    }

    @Override
    public List<HistoriqueAction> search(HistoriqueFiltre filtre, int offset, int pageSize) {
        HistoriqueFiltre verified = requireFiltre(filtre);
        requirePagination(offset, pageSize);
        return historiqueActionRepository.search(verified, offset, pageSize);
    }

    @Override
    public long count(HistoriqueFiltre filtre) {
        return historiqueActionRepository.count(requireFiltre(filtre));
    }

    @Override
    public List<String> findDistinctAuteurs() {
        return historiqueActionRepository.findDistinctAuteurs();
    }

    @Override
    public void record(String screenCode, String action, String label, UUID utilisateurId) {
        requireScreen(screenCode);
        if (!ACTIONS.contains(action)) {
            throw new IllegalArgumentException("Action d’historique invalide.");
        }
        if (label == null || label.isBlank() || label.length() > 500) {
            throw new IllegalArgumentException("Le libellé de l’action est invalide.");
        }
        if (utilisateurId == null) {
            throw new IllegalArgumentException("L’utilisateur de l’action est obligatoire.");
        }

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur de l’action introuvable."));
        HistoriqueAction historiqueAction = new HistoriqueAction();
        historiqueAction.setTypeAction(screenCode + "_" + action);
        historiqueAction.setEcran(screenCode);
        historiqueAction.setAction(action);
        historiqueAction.setDateAction(LocalDateTime.now());
        historiqueAction.setDescription(label);
        historiqueAction.setUtilisateur(utilisateur);
        historiqueActionRepository.save(historiqueAction);
    }

    private static void requireScreen(String screenCode) {
        if (!SCREENS.contains(screenCode)) {
            throw new IllegalArgumentException("Écran d’historique inconnu.");
        }
    }

    private static void requirePagination(int offset, int pageSize) {
        if (offset < 0 || pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("Paramètres de pagination invalides.");
        }
    }

    private static HistoriqueFiltre requireFiltre(HistoriqueFiltre filtre) {
        if (filtre == null) {
            throw new IllegalArgumentException("Filtre d’historique obligatoire.");
        }
        String ecran = blankToNull(filtre.getEcran());
        if (ecran != null && !SCREENS.contains(ecran)) {
            throw new IllegalArgumentException("Écran d’historique inconnu.");
        }
        String action = blankToNull(filtre.getAction());
        if (action != null && !ACTIONS.contains(action)) {
            throw new IllegalArgumentException("Action d’historique invalide.");
        }
        if (filtre.getDebut() != null && filtre.getFinExclusive() != null
                && filtre.getDebut().isAfter(filtre.getFinExclusive())) {
            throw new IllegalArgumentException("La période de l’historique est invalide.");
        }
        return new HistoriqueFiltre(ecran, action, blankToNull(filtre.getAuteur()),
                filtre.getDebut(), filtre.getFinExclusive());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
