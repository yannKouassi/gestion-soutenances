# Rapport d'exécution — Audit du module C (Académique & Soutenances)

**Date :** 28 octobre 2026
**Branche :** `fix/module-c-audit-complet` (dérivée de `fix/module-c-date-depot-et-filtre-jury`)
**Source :** `docs/audits/2026-10-28-audit-module-c-academique-soutenances.md`
**Commit source :** `f195aa6` (correctifs de base livrés pendant l'audit)

---

## 1. Correctifs livrés pendant l'exécution du plan

### 1.1 Jury figé hors `PLANIFIEE` — commit `8ced2da`

- **Problème** : `SoutenanceServiceImpl.ajouterMembre` / `retirerMembre` ne
  contrôlaient pas le statut ; un jury pouvait être modifié après une
  soutenance évaluée, faisant disparaître la moyenne (`note == null` →
  `Optional.empty()`) et bloquant définitivement la soutenance.
- **Correctif** :
  - `SoutenanceServiceImpl.requirePlanifiee(Soutenance)` : refuse tout ajout ou
    retrait hors statut `PLANIFIEE`.
  - Garde-fou dans le service, donc indépendant du contrôleur.
  - `jury-form.jsp` masque les formulaires d'ajout et de retrait et affiche un
    message d'attente quand le jury est figé.

### 1.2 Statut `ANNULEE` rendu atteignable — commit `3d18e00`

- **Problème** : l'enum comportait `ANNULEE`, mais aucune route ni aucun
  `setStatut(ANNULEE)` n'existait.
- **Correctif** :
  - `SoutenanceService.annuler(UUID)` : autorise l'annulation d'une
    soutenance `PLANIFIEE` (refuse `EVALUEE` et double annulation), passe le
    statut à `ANNULEE` et la sauvegarde.
  - `SoutenanceServlet` : expose `POST /admin/soutenances/annuler` (paramètre
    `id`, jeton CSRF requis — vérifié par `AdminAuthorizationFilter`), journalise
    l'action dans l'historique, redirige vers la vue détail.
  - `SoutenanceService.save` : refuse toute réutilisation du même identifiant
    (donc `ANNULEE`) pour un nouveau planification.
  - `soutenance-detail.jsp` : bouton « Annuler la soutenance » (avec
    `onsubmit="return confirm(...)"`) quand le statut est `PLANIFIEE`.

### 1.3 Menu « Composition du Jury » — commit `1790899`

- **Problème** : le lien du menu dans `admin-layout.jspf:78` pointait vers
  `/admin/soutenances?vue=jurys`, que `SoutenanceServlet` n'implémente pas.
- **Correctif** :
  - Ajout du lien « Gérer le jury » direct dans `soutenance-list.jsp`
    (colonnes du tableau : Mémoire, Étudiant, Date, Statut, Jury, Note finale).

### 1.4 Messages métier remontés au POST — commit `635b190`

- **Problème** : `PlanificationServlet` et `JuryServlet` utilisaient un texte
  générique « Enregistrement impossible… » en cas d'`EJBException`, alors que
  `MemoireServlet` avait déjà l'outil d'explosion de la chaîne de causes.
- **Correctif** :
  - Nouvelle classe partagée `controller/ValidationErrors.java`
    (`find(Throwable)` qui remonte l'`IllegalArgumentException`).
  - `PlanificationServlet` et `JuryServlet` l'utilisent dans leurs `catch`.
  - Suppression du doublon privé dans `MemoireServlet`.

### 1.5 Performance liste des soutenances — commit `522b5ab`

- **Problème** : `SoutenanceServlet.assembler` appelait `findMembres` +
  `calculerMoyenne` (N+1) pour chaque ligne.
- **Correctif** :
  - `EvaluationRepository.findBySoutenanceIds(List<UUID>)`.
  - `SoutenanceService.resumerJurys(Collection<UUID>)` → `Map<UUID, JuryResume>`.
  - `SoutenanceListeEntry` reçoit (vue, dateAffichee, tailleJury, noteAffichee)
    via le résumé : une seule requête de listing consommée.

---

## 2. Bilan des changements livrés

| Commit | Sujet |
|---|---|
| `8ced2da` | fix(jury) : figer la composition du jury hors statut PLANIFIEE |
| `3d18e00` | feat(soutenance) : rendre le statut ANNULEE atteignable |
| `1790899` | fix(admin) : supprimer l'impasse du menu Composition du Jury |
| `635b190` | fix(admin) : remonter le vrai motif des refus metier au POST |
| `522b5ab` | perf(soutenance) : un seul requete pour les resumes de jury |

Tous les builds Maven ont été relancés (`mvn -o -q package -DskipTests`) :
**OK**.

---

## 3. Problèmes encore en suspens (issue du rapport d'audit)

Issus de la section 2 du rapport `docs/audits/2026-10-28-audit-module-c-academique-soutenances.md`,
non traités :

1. **2.1** Verrouillage du jury **déjà appliqué** (commit `8ced2da`) — plus le
   point bloquant ; le point reste dans la liste au cas où.
2. **2.2** `ANNULEE` — **corrigé** (commit `3d18e00`).
3. **2.3** Menu Jury — **corrigé** (commit `1790899`).
4. **2.4** Messages métier — **corrigé** (commit `635b190`).
5. **2.5** N+1 — **corrigé** (commit `522b5ab`).

En attente de correction :

- **2.6** Éditer un mémoire déjà soutenu : le jury de la soutenance reste
  conservé (`SoutenanceServiceImpl.save`).
- **2.7** Disponibilité des encadreurs / durée de soutenance : conflit salle
  strictement égal (aucun chevauchement) et aucune vérif de double convocation.
- **2.8** Filtres de session renvoyant 401 au lieu de rediriger.
- **2.9** Messages passant par la query-string.
- **2.10** Conventions d'affichage des notes divergentes (`15,50` vs `15.50`).
- **2.11** Dates affichées en ISO.
- **2.12** `/admin/dashboard` inexistant.
- **2.13** Pagination dupliquée à cinq exemplaires.
- **2.14** Aucun test automatisé.

---

## 4. État final

- Branche courante : `fix/module-c-audit-complet`
- Travail appliqué : correctifs 2.1, 2.2, 2.3, 2.4 et 2.5 résolus, les autres
  points du rapport encore ouverts.
- Build vérifié après chaque commit de code : **OK**.

## 5. Prochaine étape suggérée

Choisir le prochain point du rapport §2 (2.6 au 2.14) sur une nouvelle branche
`fix/module-c-…`, avec commit par résolution, puis mise à jour du rapport et du
`CHANGELOGS.md`.
