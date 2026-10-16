# Commits et changelog — règles et gabarits

Fichier de référence. Il contient les règles d'écriture des commits et le
fonctionnement du changelog, extraites de `AGENTS.md` pour les garder lisibles.
`AGENTS.md` ne fait que pointer ici.

## Branches

Une branche `feat/...` — ou `feature/...` — **est** une feature : tous les
commits propres à cette branche par rapport à `main` forment la feature, et le
changelog de la feature couvre exactement cet ensemble. Nomme la branche
`feat/<nom-court>` : `feat/evaluation`, `feat/journal-audit`,
`feat/planification-soutenances`.

Autres préfixes en usage : `fix/...` pour une correction, `chore/...` et
`docs/...` pour ce qui n'est pas une feature. Le script reconnaît `feat`,
`feature`, `fix`, `bugfix`, `hotfix`, `chore`, `docs`, `refactor`, `test`,
`perf`, et signale tout autre préfixe.

## Messages de commit

Conventional Commits, en français, titre court au plus ~70 caractères :

```text
type(portée) : sujet
```

- `type` : `feat`, `fix`, `refactor`, `docs`, `test`, `chore`, `perf`.
- `portée` : le module ou l'écran touché (`historique`, `acteurs`, `dao`,
  `jpa`).
- `sujet` : à l'infinitif ou au participe passé, sans point final.

### Le gabarit

Un commit qui n'est pas purement cosmétique porte **un corps de 2 à 6 puces**,
séparé du titre par une ligne vide. Le corps dit ce que la feature permet, ce
qu'elle touche, et ce qui ne se devine pas dans le diff.

```text
feat(members): add contribution history export

- Export en CSV et PDF depuis la page membre
- Filtre par période et par type de cotisation
- Limite à 5000 lignes par export
- Ajout du endpoint GET /members/:id/contributions/export
```

Lecture du gabarit, puce par puce :

| Puce | Rôle |
|---|---|
| `Export en CSV et PDF depuis la page membre` | ce que la feature permet — l'impact, en une ligne lisible par quelqu'un qui n'a pas lu le diff |
| `Filtre par période et par type de cotisation` | ce que la feature permet, sous forme de capacité offerte à l'utilisateur |
| `Limite à 5000 lignes par export` | un choix qui n'apparaît nulle part ailleurs : ni dans le sujet, ni dans l'écran |
| `Ajout du endpoint GET /members/:id/contributions/export` | ce qui a été touché, nommé précisément — ici l'URL ; ailleurs fichiers, classes, colonnes |

Ordre : impact, puis périmètre touché, puis choix et écarts. Une puce de
détail d'implémentation n'est écrite que si elle apporte quelque chose : une
contrainte, une valeur retenue, une raison. Jamais de narration ligne à ligne
du diff.

### Variantes

- **Correction** — un `fix:` liste les défauts corrigés, numérotés, avec la
  cause quand elle n'est pas évidente :

  ```text
  fix(historique) : corriger la casse du filtre auteur

  1. La collation MySQL est insensible a la casse, la comparaison EL non
  2. Le filtre conserve desormais la valeur saisie, quelle que soit sa casse
  ```

- **Documentation, chores** — un `docs:` ou un `chore:` qui touche plus d'un
  fichier prend un corps aussi, avec la même exigence.

- **Purement cosmétique** — un commit de reformatage peut n'avoir qu'un titre.

### Pourquoi le corps

Le sujet ne peut pas tout dire en 70 caractères. Le corps est la seule place où
reste le *pourquoi* : c'est lui que `/nouvelle-changelog` relit pour rédiger la
partie « Détail » du changelog. Quand l'intention d'un commit n'est pas
déductible du diff, la dire au moment du commit — le diff seul ne raconte pas le
pourquoi, le changelog non plus.

## Changelog

`CHANGELOGS.md` unique à la racine : une section par feature, de la plus récente
à la plus ancienne.

Chaque section commence par un bloc `frontmatter` — `feature`, `branche`,
`base`, `date`, `auteurs`, `commits` — puis le titre `## <nom de la feature>`
(la date est dans le `frontmatter`), la partie `### Détail` rédigée et la table
`### Commits`.

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
…
### Commits
| Commit | Sujet |
```

### Qui écrit quoi

Le `frontmatter`, le titre et la table des commits sont posés par le script, le
détail est rédigé par l'IA :

```bash
./scripts/nouvelle-changelog.ps1 -Apercu                    # vérifie sans écrire
./scripts/nouvelle-changelog.ps1 -Titre "Journal d'audit"   # écrit la section
```

Le script liste les commits de la branche courante absents de `main`
(`git log` sans les commits de fusion), en déduit les auteurs, le nom de la
feature et la date, insère la section en tête du journal, et refuse d'écrire une
section déjà présente. Options utiles : `-Branche`, `-Base`, `-Titre`, `-Date`,
`-Chemin`.

L'écriture dans `CHANGELOGS.md` se fait à la fin d'une feature, une fois la
branche prête pour la PR — pas commit par commit.

### La commande `/nouvelle-changelog`

Dans l'éditeur opencode, elle enchaîne les deux étapes : elle appelle le script
puis rédige la partie `### Détail` à partir du diff et des corps de commit.

Le détail suit la structure des sections existantes : sous-sections `####` par
lot ou par écran, noms de classes, servlets, JSP et colonnes réellement présents
dans le diff, décisions techniques et écarts au plan. Le `frontmatter` et la
table des commits ne sont jamais réécrits, et une section déjà rédigée n'est
jamais réécrite.

`docs/exemple-changelog.md` montre la structure exacte d'une section, sortie
réelle du script à l'appui.

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