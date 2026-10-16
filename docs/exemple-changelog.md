# Exemple de section de changelog

Ce fichier est un gabarit de démonstration : il ne fait pas partie de
`CHANGELOGS.md` et n'est pas destiné à être commité. Il montre la structure
exacte d'une section produite par `scripts/nouvelle-changelog.ps1` puis
complétée par la commande opencode `/nouvelle-changelog`.

La section ci-dessous est la sortie réelle du script pour la branche
`feat/evaluation` (trois commits, deux auteurs), insérée en tête de
`CHANGELOGS.md`.

---

## Structure

```text
---                          ← bloc écrit par le script
feature: <nom de la feature>
branche: feat/<nom-court>
base: main
date: <AAAA-MM-JJ>
auteurs:
  - <git log --format=%an, dédupliqué>
commits: <n>
---                          ← le script refuse d'écrire si la section existe déjà

## <nom de la feature>        ← le titre ne porte plus la date, elle est dans le frontmatter

### Détail                   ← le script pose le titre et un placeholder,
                               l'IA remplace le placeholder par la rédaction
#### <sous-section>            sous-sections `####` et plus profond, écrites par l'IA

### Commits                   ← écrit par le script, jamais réécrit
| Commit | Sujet |           ← une ligne par commit, du plus ancien au plus récent,
|---|---|                     commits de fusion exclus
| `<sha court>` | `<sujet du commit>` |
```

## Sortie exacte du script

Texte produit par `./scripts/nouvelle-changelog.ps1 -Branche feat/evaluation
-Titre "Évaluation des soutenances"`, avant rédaction du détail :

```markdown
---
feature: Évaluation des soutenances
branche: feat/evaluation
base: main
date: 2026-10-15
auteurs:
  - Yannick Kouassi
  - gedeon besse
commits: 3
---

## Évaluation des soutenances

### Détail

_À compléter : intention de la feature, périmètre livré, choix techniques, écarts au plan._

### Commits

| Commit | Sujet |
|---|---|
| `60521b5` | feat(jury) : noter la soutenance depuis le jury de l'encadreur |
| `6990234` | fix(jury) : mettre a jour la moyenne du jury |
| `7166344` | docs(plan) : plan du module evaluation |
```

Le placeholder `_À compléter : ..._` est remplacé par le texte rédigé ; le
`frontmatter` et le bloc `### Commits` restent inchangés.

## Rendu final, après rédaction du détail

```markdown
---
feature: Évaluation des soutenances
branche: feat/evaluation
base: main
date: 2026-10-15
auteurs:
  - Yannick Kouassi
  - gedeon besse
commits: 3
---

## Évaluation des soutenances

### Détail

#### Écran `/encadreur/mes-jurys`

- `EvaluationServlet` sert `/encadreur/mes-jurys` et `POST /encadreur/evaluation`.
  Il s'appuie sur `EncadreurAuthorizationFilter` pour la session, le rôle
  ENCADREUR et le jeton CSRF ; il ne refait aucun contrôle lui-même.
- `evaluation-form.jsp` affiche le jury de la soutenance et la note déjà déposée
  le cas échéant. Un encadreur ne voit que les soutenances auxquelles il est
  convoqué : le filtre passe par `EncadreurRepository`, jamais par une liste
  globale.

#### Notes et moyenne

- `EvaluationDao` porte la relation N-N `Soutenance` × `Encadreur` imposée par le
  cahier des charges : lecture du jury, dépôt de la note, moyenne.
- La moyenne ne retient que les évaluations ayant une note ; la contrainte
  `uk_evaluation_soutenance_encadreur` interdit deux notes pour un même membre
  du jury.
- `EvaluationService` refuse le dépôt si la soutenance est `ANNULEE`.
- La moyenne affichée est un `BigDecimal` à deux décimales, `setScale(2,
  HALF_UP)`, pour que l'affichage soit stable d'un jury à l'autre.

#### Écarts au plan

- Le passage de `PLANIFIEE` à `EVALUEE` n'est pas automatique : il reste une
  action de l'administrateur, alors que le plan prévoyait une bascule dès la
  dernière note. À trancher avec l'équipe.

### Commits

| Commit | Sujet |
|---|---|
| `60521b5` | feat(jury) : noter la soutenance depuis le jury de l'encadreur |
| `6990234` | fix(jury) : mettre a jour la moyenne du jury |
| `7166344` | docs(plan) : plan du module evaluation |
```

## Règles de rédaction

Le script donne la structure, l'IA donne le fond :

- le `frontmatter` vient de git (branche, auteurs, nombre de commits) et n'est
  jamais réécrit à la main ; si un auteur manque, c'est le `git config` de la
  machine qui est en cause, pas le changelog ;
- `### Détail` suit la structure des sections déjà présentes : sous-sections
  `####` par lot ou par écran, classes, servlets, JSP, tables et colonnes
  réellement présents dans la branche ;
- les choix et écarts au plan sont explicités quand ils ne se devinent pas dans
  le diff — c'est là que les corps de commit écrits pendant le développement
  sont utiles ;
- aucune donnée absente du diff, des corps de commit et du contexte fourni à
  `/nouvelle-changelog` ;
- une section déjà rédigée n'est jamais réécrite par une feature suivante.

## Du commit à la section

```text
feat/evaluation                une branche feat/… = une feature
  ├─ feat(jury) : …    ┐
  ├─ fix(jury) : …     ├─ commits propres à la branche
  └─ docs(plan) : …   ┘
                          ↓  scripts/nouvelle-changelog.ps1
                    frontmatter + titre + ### Commits
                          ↓  /nouvelle-changelog (diff + corps de commit + intention)
                    ### Détail rédigé
```

Le corps d'un commit peut décrire un peu plus l'implémentation quand le sujet ne
suffit pas ; c'est cette matière que `/nouvelle-changelog` met en forme. Voir la
section « Commits et changelog » de `AGENTS.md` pour les règles d'écriture des
messages.