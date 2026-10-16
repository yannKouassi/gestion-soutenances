# Journal des modifications

Une section par feature, de la plus récente à la plus ancienne. Une branche
`feat/...` (ou `feature/...`) est une feature : tous ses commits propres à la
branche de base forment la feature.

Chaque section commence par un bloc `frontmatter` — `feature`, `branche`, `base`,
`date`, `auteurs`, `commits` — suivi du titre, de la partie « Détail » rédigée et
de la table des commits. Le `frontmatter` et la table sont posés par
`scripts/nouvelle-changelog.ps1`, qui lit le `git log` de la branche ; la
commande opencode `/nouvelle-changelog` rédige ensuite la partie « Détail » à
partir du diff, des corps de commit et de l'intention de la feature.

---
feature: Espace encadreur
branche: feat/encadreur-espace
base: origin/main
date: 2026-10-16
auteurs:
  - xxvByMe
commits: 7
---

## Espace encadreur

### Détail

Cette section décrit l'espace encadreur intégré dans `feat/encadreur-espace`
depuis `origin/main`. Elle couvre la couche de données, les écrans et leurs
corrections, ainsi que les commits de documentation et de nettoyage présents
sur la branche source.

#### Couche de données

##### Repositories

- Nouveau `EvaluationRepository` : porte la relation N-N `Soutenance` ×
  `Encadreur` portée par l'entité `Evaluation`, point explicitement exigé par
  le cahier des charges. Il couvre la consultation du jury d'une soutenance, des
  convocations d'un encadreur, d'une ligne de jury précise, et
  l'enregistrement.
- Nouveau `SoutenanceRepository`, en lecture seule : recherche par identifiant
  avec chargement du mémoire et de la salle.
- `EncadreurRepository` gagne une résolution par identifiant d'utilisateur,
  nécessaire parce que la session ne porte que l'identifiant du compte.

##### Service et modèle

- `EncadreurService` gagne la consultation des jurys, la consultation d'une
  soutenance, le calcul de la moyenne et la saisie d'une note.
- `Evaluation.note` devient nullable, afin de pouvoir composer un jury avant que
  ses membres n'aient noté.

##### Règles de notation

- La saisie d'une note rejette : identifiants absents, note absente, note hors
  de l'intervalle 0-20, encadreur non convoqué au jury, note déjà soumise par
  ce même membre.
- La soutenance passe au statut `EVALUEE` depuis `saisirNote` lorsque le jury
  compte au moins deux membres et que tous ont noté ; la transition est ignorée
  si la soutenance n'est plus `PLANIFIEE`.
- La moyenne affichée est celle des notes du jury, arrondie à deux décimales, et
  reste absente tant qu'une seule note manque.
- La liste des convocations charge les soutenances dans la première requête,
  puis leurs mémoires et salles dans une seconde requête dont la racine est
  `Soutenance`, compatible avec les règles de fetch de Hibernate 6.

#### Espace web

##### Sécurité et contrôleur

- `EncadreurAuthorizationFilter` sur `/encadreur/*`, symétrique du filtre
  d'administration existant : session requise, rôle ENCADREUR exigé, jeton CSRF
  validé sur les POST en comparaison constante.
- `EncadreurServlet` sert `/encadreur/dashboard`, `/encadreur/mes-jurys` (liste
  des convocations) avec son formulaire de note, `/encadreur/mes-etudiants` et
  `/encadreur/profil`. Un identifiant de soutenance en paramètre ne donne accès
  à rien si l'encadreur n'est pas convoqué.
- Le menu reprend l'arborescence Excel : Tableau de Bord → Résumé / Alertes ;
  Mes Étudiants Encadrés → Liste étudiants encadrés ; Mes Jurys & Évaluations
  → Liste des Jurys et Saisie d'évaluation ; Mon Profil → Édition Profil.
- La liste `/encadreur/mes-etudiants` n'affiche que les étudiants liés aux
  mémoires dont l'encadreur connecté est le responsable.
- `DashboardServlet` renvoie un encadreur connecté vers son tableau de bord au
  lieu de la page d'attente.

##### Modèles de vue

- Les données affichées passent par des modèles de vue dédiés `ConvocationVue`,
  `JuryVue` et `MembreJuryVue`. Les notes et les avis des autres membres du jury
  ne sont jamais exposés aux vues : seuls le nom du membre et le fait qu'il ait
  noté figurent.
- La moyenne du jury n'est publiée que lorsqu'elle est calculable, c'est-à-dire
  lorsque l'encadreur a lui-même noté.

##### Vues et styles

- Les JSP de l'espace encadreur sont en EL et JSTL uniquement, sans scriptlet,
  avec leur propre fragment de navigation ; l'espace d'administration n'est pas
  modifié.
- La feuille de style reçoit les badges de statut de soutenance aux valeurs
  exactes de la charte, les cartes de statistiques et les listes d'information.

### Commits

| Commit | Sujet |
|---|---|
| `a642fd1` | feat(encadreur) : aligner sur le decoupage Excel |
| `be7f0f5` | changelogs |
| `726dd60` | fix(dao) : corriger les jointures de tri des evaluations |
| `c1b16a0` | feat(encadreur) : ajouter l'espace encadreur et ses écrans |
| `ddae22d` | feat(dao) : poser la couche de données de l'espace encadreur |
| `95fb59d` | changelog-theory supprimé |
| `0cba872` | metals files need to be removed |

---
feature: Journal d'audit, Acteurs & comptes et hiérarchie du menu d'administration
branche: feature/admin-historique → feature/admin-menu-audit-hierarchy
base: main
date: 2026-10-15
auteurs:
  - xxvByMe
  - Yannick Kouassi
commits: 10
---

## Journal d'audit, Acteurs & comptes et hiérarchie du menu d'administration

### Détail

Cette section décrit l'état de `main` au commit `c2d8a74` (fusion de la Pull
Request #1, branche `feature/admin-menu-audit-hierarchy`) : les trois lots
livrés — le journal d'audit `/admin/historique`, le module « Acteurs & comptes »
et la hiérarchie du menu d'administration — ainsi que les documents de référence
du dépôt.

#### Documents de référence

- Export au format Markdown des documents de référence du dépôt : charte
  graphique, découpage/MLD et sujet complet (`9f07fa5`).
- Export au format Markdown du cahier des charges (`f5e4312`).
- Ajout des sources PDF/XLSX/DOCX des documents de référence sous
  `documents/` (`faabf54`).
- Ajout de `AGENTS.md` et des plans de conception DAO des modules C et D sous
  `docs/plans/` (`5b96b2b`).

Ces exports complètent les sources PDF/XLSX, qui restent inchangées.

#### Lot « journal d'audit »

##### Modèle et persistance

- `Historique_Action` reçoit deux colonnes nullable, `ecran` et `action`, plus
  trois index (`ecran`, `action`, `date_action`). Le schéma est mis à jour par
  Hibernate (`hibernate.hbm2ddl.auto=update`) ; aucun script DDL ni backfill
  SQL n'est produit (`e62629b`).
- `type_action` continue d'être écrit à chaque enregistrement, en plus des deux
  nouvelles colonnes. Stratégie retenue : « colonne d'abord, `type_action` en
  repli ». Les lignes antérieures à la migration, qui n'ont que `type_action`,
  restent donc lisibles et filtrables sans mise à jour des données existantes.
- Nouveaux types de la consultation : `HistoriqueFiltre` (critères) et
  `HistoriqueJournalEntry` (ligne affichée), avec les méthodes correspondantes
  dans `HistoriqueActionRepository` et `HistoriqueActionService`.

##### Écran `/admin/historique`

- `HistoriqueServlet` répond en GET à `/admin/historique` et forward vers
  `historique-list.jsp`, rédigée en EL et JSTL uniquement. Le servlet ne
  refait aucun contrôle de session ni de rôle : le filtrage `/admin/*` est
  assuré par `AdminAuthorizationFilter`.
- Entrée de navigation « Journal d'audit » ajoutée dans `admin-layout.jspf`,
  sous l'en-tête « Pilotage & audit ».

##### Consultation, filtres et pagination

- Le journal est global : il agrège les actions des quatre écrans référentiels
  (grades, départements, filières, salles) et les présente par date
  décroissante.
- Filtres facultatifs : écran, action, auteur et période (bornes « du » et
  « au »). Le filtre auteur accepte une valeur déjà appliquée même si elle ne
  figure pas dans la liste des auteurs distincts.
- Pagination avec taille de page ajustable (10, 15, 20 ou 50 lignes, 15 par
  défaut) et fenêtre de pages de cinq éléments autour de la page courante.
- Le décompte (`COUNT`) et la page (`SELECT`) partagent le même constructeur de
  prédicats, donc les mêmes critères. Le `JOIN FETCH utilisateur` est conservé
  sur la requête de page pour éviter toute `LazyInitializationException` à
  l'affichage du nom et du rôle de l'auteur.

##### Corrections

`1091367` — cinq défauts relevés en revue :

1. Le repli de filtre sur `type_action` utilisait un motif `LIKE` dont le `_`
   était un générique : remplacé par une égalité exacte sur le suffixe.
2. Une borne de période dépassant les limites de `LocalDate` produisait une
   erreur 500 ; `DateTimeException` est désormais rattrapée et transformée en
   réponse 400.
3. Le filtre auteur appliqué n'apparaissait pas dans le `<select>` : la valeur
   est ajoutée à la liste quand elle y manque.
4. Une règle CSS globale de dernier `th` écrasait la dernière colonne du
   nouveau tableau ; une règle dédiée au tableau du journal a été ajoutée.
5. Les règles de pagination étaient inertes à cause d'un conflit de
   spécificité : classe de pagination du journal distincte de celle des
   formulaires.

`0e417e4` — deux points restés ouverts :

1. Désaccord de casse entre la collation MySQL, insensible à la casse, et la
   comparaison EL, sensible à la casse : le filtre auteur conserve désormais sa
   valeur quelle que soit la casse saisie.
2. Le bandeau affichait « Paramètres généraux » sur un écran de pilotage &
   audit ; il affiche « Pilotage & audit » sur le journal.

##### Non-régression

Les huit JSP des quatre écrans référentiels (listes et formulaires grades,
départements, filières, salles) sont inchangées à l'octet près : leur diff est
vide entre le point de départ de la branche et `0e417e4`. Seuls le fragment de
navigation, le bandeau et la feuille de style partagée ont été modifiés.

#### Lot « Acteurs & Comptes »

##### Modèle et persistance

- `AccountEntry` est une classe porteuse immuable servant de projection JPQL
  pour la liste des comptes ; ce n'est pas une entité JPA.
- `Utilisateur` gagne une colonne `actif` (`BOOLEAN DEFAULT TRUE`) et la
  colonne `login` passe de 100 à 180 caractères. Le hash de mot de passe est
  inchangé : hachage par `PBKDF2WithHmacSHA256` à 310 000 itérations.
- `Encadreur` et `Etudiant` ne gagnent aucune colonne ; seul leur constructeur
  devient public.

##### Repositories

- Nouveaux `EncadreurRepository` et `EtudiantRepository` : lecture, recherche
  par identifiant, contrôle d'unicité de l'e-mail, `matriculeExists` côté
  étudiant, sauvegarde. Ils chargent leurs associations par `JOIN FETCH` pour
  éviter les `LazyInitializationException` au rendu.
- `MemoireRepository` ajouté, en lecture seule (`findAll`).
- `UtilisateurRepository` gagne `findAllAccounts`, `findEmailById` et
  `countActiveAdministrators`.

##### Services

- `CompteService` : création de compte lié à un acteur, mise à jour du login,
  activation/désactivation, réinitialisation de mot de passe. Le mot de passe
  temporaire comporte 6 caractères tirés d'un alphabet sans caractères ambigus,
  générés par `SecureRandom`, haché avant stockage. Les méthodes mutantes sont
  protégées par un verrou pessimiste en écriture.
- `CourrielService` envoie les identifiants par SMTP via `jakarta.mail`, avec
  les paramètres lus dans l'environnement (`SMTP_HOST`, `SMTP_FROM`,
  `SMTP_USERNAME`, `SMTP_PASSWORD`, `SMTP_PORT`, `SMTP_SSL`, `SMTP_STARTTLS`).
  SSL et STARTTLS sont mutuellement exclusifs ; l'authentification est interdite
  sans TLS.
- `EncadreurService` et `EtudiantService` valident les données (nom, prénom,
  e-mail normalisé en minuscules, matricule normalisé en majuscules, filière
  obligatoire, mémoire facultative), créent le compte lié à la création et
  mettent le login à jour quand l'e-mail change.
- `AuthServiceImpl` refuse l'authentification d'un compte inactif.

##### Contrôleur

- `ActeursServlet` sert `/admin/encadreurs`, `/admin/etudiants`,
  `/admin/utilisateurs` et leurs sous-chemins, en s'appuyant sur
  `AdminAuthorizationFilter` pour la session, le rôle ADMIN et le jeton CSRF sur
  POST ; il ne refait aucun contrôle lui-même.
- Il n'existe aucune suppression d'acteur ni de compte. Les écritures sont
  journalisées en `CREATION` et `MODIFICATION`, jamais en `SUPPRESSION`, avec
  les écrans `ENCADREUR`, `ETUDIANT`, `UTILISATEUR`. Les formulaires affichent
  les dernières actions via `admin-history.jspf`.

##### Vues

- Nouvelles `encadreur-list.jsp` (filtres grade et département),
  `encadreur-form.jsp`, `etudiant-list.jsp` (filtre filière),
  `etudiant-form.jsp`, et `utilisateur-list.jsp` refaite en liste de comptes
  avec actions activer / désactiver / réinitialiser le mot de passe en POST.
- `admin-layout.jspf` gagne un groupe de navigation « Acteurs & Comptes » à
  sous-menus repliables.
- `admin-list-search.js` combine désormais recherche plein texte et filtres.
- `admin.css` gagne les styles de filtres de liste, de modale de détail
  d'action et de badge d'état de compte.
- `HistoriqueActionService` accepte les trois nouveaux écrans.

#### Lot « hiérarchie du menu d'administration »

- Le groupe « Pilotage & audit » plat de `admin-layout.jspf` devient deux
  groupes repliables : « Tableau de Bord & Statistiques » avec une entrée
  « Indicateurs & Graphiques » marquée « À venir » et non cliquable, et
  « Historique & Traçabilité » contenant le journal d'audit.
- `admin.css` ajoute le style d'entrée désactivée.
- Les 14 JSP admin passent le cache-buster de la feuille de style à une
  nouvelle valeur.
- `README.md` gagne une section décrivant cette hiérarchie.

### Commits

| Commit | Sujet |
|---|---|
| `6605662` | Aligner les sous-menus audit sur le classeur |
| `3d12054` | Ajouter le module acteurs et détails d'historique |
| `0e417e4` | fix: corriger la casse du filtre auteur et le bandeau de /admin/historique |
| `1091367` | fix: corriger cinq defauts du journal d'audit /admin/historique |
| `778866c` | feat: ajouter le journal d'audit global /admin/historique |
| `e62629b` | feat: indexer l'historique par ecran et action avec recherche globale filtree |
| `f5e4312` | docs: ajouter l'export Markdown du cahier des charges |
| `9f07fa5` | docs: exporter les documents de référence au format Markdown |
| `5b96b2b` | docs: documentation projet, plans DAO modules C et D et config Eclipse |
| `faabf54` | update: ajouts des documents |