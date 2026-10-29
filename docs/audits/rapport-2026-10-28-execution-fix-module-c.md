# Rapport d'exécution — Audit du module C (Académique & Soutenances)

**Date :** 29 octobre 2026
**Branche :** `fix/module-c-audit-suite` (suite de `fix/module-c-audit-complet`)
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

### 1.6 Interdiction du changement de mémoire sous un jury — commit `642208b`

- **Problème** : un jury pouvait avoir déjà noté (ou noté partiellement) une
  soutenance ; changer le mémoire de cette soutenance remettait en cause le jury
  et la moyenne.
- **Correctif** :
  - `SoutenanceServiceImpl.save` : si la soutenance existe déjà, a des
    évaluations, et qu'une autre `Memoire` est fournie, refuser.
  - `PlanificationServlet.setFormOptions` : passe `memoireVerrouille` ; le
    `soutenance-form.jsp` verrouille le sélecteur de mémoire et conserve l'identifiant
    en champ caché.

### 1.7 Formatage des dates en français — commit `1cf0b60`

- **Problème** : les dates brutes en `LocalDate` ISO (`2026-10-28`) étaient
  affichées telles quelles dans les listes et détails.
- **Correctif** :
  - Nouveau TLD `WEB-INF/tld/format.tld` + utilitaire `ci.univ.soutenances.util.Format`
    avec la fonction EL `fmt2:date(LocalDate)` (format `dd/MM/yyyy`).
  - Les cinq écrans impactés passent par `${fmt2:date(...)}` ; les champs
    `datetime-local` conservent la valeur ISO attendue par le navigateur.

### 1.8 Formatage des notes — commit `9ade38a`

- **Problème** : certaines écrans affichaient `15.50`, d'autres `15,50`.
- **Correctif** :
  - Fonction EL `fmt2:note(BigDecimal)` (format `%.2f` avec locale française).
  - Les écrans jury/admin/encadré passent par `${fmt2:note(...)}`.
  - `SoutenanceServlet` délègue au formatage centralisé au lieu d'une copie.

### 1.9 Messages de feedback en flash de session — commit `64c6a49`

- **Problème** : les messages de succès/erreur étaient transmis via la
  query-string (`?message=…&erreur=…`).
- **Correctif** :
  - Nouvelle classe utilitaire `FlashMessages` : lecture one-shot par requête
    via l'`HttpSession`, stockage des messages avant redirection.
  - Tous les servlets admin/encadré (acteurs, référentiels, mémoires, planification,
    jury, soutenances, encadrés) passent par `FlashMessages`.
  - Les redirections `POST → GET` sont épurées de leurs paramètres.

### 1.10 Pagination de l'historique centralisée — commit `0c0b364`

- **Problème** : cinq implémentations quasi identiques de la pagination
  (`ActeursServlet`, `ReferentielServlet`, `MemoireServlet`, `PlanificationServlet`,
  `JuryServlet`).
- **Correctif** :
  - Nouvelle classe `controller/HistoryPagination` : attributs partagés
    (`historyPage`, `historyTotalPages`, `historyFirstPage`, `historyLastPage`,
    `historyActions`, `historyEditBase`).

### 1.11 Conflits de créneau (salle / encadré) — commit `51452aa`

- **Problème** : contrôle strict d'égalité d'heure pour la salle (aucune
  vérification de chevauchement) et aucune vérification de double occupation
  d'un encadré.
- **Correctif** :
  - Durée de créneau constante `DUREE_SOUTENANCE = 60 min` côté service.
  - `SoutenanceRepository.findCreneauxBySalle(Salle, jour)` : récurrence de
    créneaux sur une journée (hors `ANNULEE`).
  - `SoutenanceRepository.findCreneauxByEncadreur(Encadreur, jour)` et
    `EvaluationRepository.findCreneauxByEncadreur(Encadreur, jour)` : engagements.
  - Chevauchement testé côté service ; refus avec message dédié.

### 1.12 Tableau de bord administrateur — commit `d6296f4`

- **Problème** : l'entrée du menu « Tableau de Bord & Statistiques » était
  désactivée (`nav-item-disabled`) et `/admin/dashboard` n'existait pas.
- **Correctif** :
  - Nouveau `AdminDashboardServlet` (`/admin/dashboard`) : compteurs
    soutenances/états, jurys incomplets, référentiels, cinq prochaines
    soutenances planifiées.
  - Nouvelle vue `admin/dashboard.jsp` + styles `.dashboard-grid/.dashboard-card`.
  - Menu pilotant : lien « Indicateurs & Graphiques » activé.

---

## 2. Bilan des changements livrés

| Commit | Sujet |
|---|---|
| `8ced2da` | fix(jury) : figer la composition du jury hors statut PLANIFIEE |
| `3d18e00` | feat(soutenance) : rendre le statut ANNULEE atteignable |
| `1790899` | fix(admin) : supprimer l'impasse du menu Composition du Jury |
| `635b190` | fix(admin) : remonter le vrai motif des refus metier au POST |
| `522b5ab` | perf(soutenance) : un seul requete pour les resumes de jury |
| `642208b` | fix(soutenance) : interdire le changement de memoire sous un jury existant |
| `1cf0b60` | feat(ui) : formater les dates de depot au format francais |
| `9ade38a` | feat(ui) : uniformiser l'affichage des notes avec deux decimales |
| `64c6a49` | refactor(web) : passer les messages de succes et d'erreur en flash de session |
| `0c0b364` | refactor(admin) : centraliser la pagination de l'historique |
| `51452aa` | fix(planification) : controler les chevauchements de salle et d'encadreur |
| `d6296f4` | feat(admin) : creation du tableau de bord administrateur |

Tous les builds Maven ont été relancés (`mvn -o -q package -DskipTests`) :
**OK**.

---

## 3. Problèmes encore en suspens (issue du rapport d'audit)

Issus de la section 2 du rapport `docs/audits/2026-10-28-audit-module-c-academique-soutenances.md`,
traités ou désormais résolus :

1. **2.1** — **corrigé** (commit `8ced2da`).
2. **2.2** — **corrigé** (commit `3d18e00`).
3. **2.3** — **corrigé** (commit `1790899`).
4. **2.4** — **corrigé** (commit `635b190`).
5. **2.5** — **corrigé** (commit `522b5ab`).
6. **2.6** — **corrigé** (commit `642208b`).
7. **2.7** — **corrigé** (commit `51452aa`).
8. **2.8** — **corrigé** (`fix/mineurs`, commit `914d214`).
9. **2.9** — **corrigé** (commit `64c6a49`).
10. **2.10** — **corrigé** (commit `9ade38a`).
11. **2.11** — **corrigé** (commit `1cf0b60`).
12. **2.12** — **corrigé** (commit `d6296f4`).
13. **2.13** — **corrigé** (commit `0c0b364`).

En suspens, volontairement reportés :

- **2.14** Aucun test automatisé — laissé de côté pour le moment ; à traiter
  dans une phase séparée (ajout de dépendances de test JUnit/EJB en `pom.xml`,
  puis création des tests).

---

## 4. État final

- Branche courante : `fix/module-c-audit-suite`.
- Points 2.1 à **2.13 du rapport d'audit désormais résolus** ;
  2.14 (tests automatisés) reporté.
- Build vérifié après chaque commit de code : **OK**.

## 5. Prochaine étape suggérée

- Traiter **2.14 (tests automatisés)**, sur une branche dédiée.
- Puis clôture du chantier audit module C et mise à jour du changelog.
