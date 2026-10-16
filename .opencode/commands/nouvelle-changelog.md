---
description: Rédige la section datée de la feature courante dans CHANGELOGS.md
agent: build
---

Objectif : créer ou compléter la section de changelog de la feature courante dans
`CHANGELOGS.md`, en conservant les sections déjà rédigées.

Contexte fourni par l'utilisateur (intention de la feature, périmètre attendu) :
$ARGUMENTS

## Entrées

Branche courante :

!`git rev-parse --abbrev-ref HEAD`

Commits de la branche absents de `main` :

!`git log --reverse --no-merges --format="%h %s" "$(git merge-base main HEAD)..HEAD"`

Diff complet de la feature (stat) :

!`git diff --stat "$(git merge-base main HEAD)..HEAD"`

## Étapes

1. Si une section dont le `frontmatter` porte `feature: <titre>` existe déjà pour
   cette branche, ne la recrée pas : passe à l'étape 3.
2. Lance le script qui pose le `frontmatter`, le titre et la table des commits
   (il déduit la date et les auteurs du `git log`) :
   `./scripts/nouvelle-changelog.ps1 -Apercu` pour vérifier, puis sans `-Apercu`
   pour écrire. Si le titre déduit du nom de branche ne correspond pas à
   l'intention, passe `-Titre "..."`.
3. Rédige la partie `### Détail` de cette section, en français, en partant du
   diff et des corps de commit — pas seulement des sujets :
   - `git log --format="%h%n%s%n%b" <merge-base>..HEAD` pour les corps ;
   - `git diff <merge-base>..HEAD` et `git show <sha>` pour le détail.
4. Respecte la structure et le ton des sections déjà présentes dans
   `CHANGELOGS.md` : sous-sections `####` par lot ou par écran, noms de classes,
   de servlets, de JSP et de colonnes réellement présents dans le diff,
   décisions techniques et écarts au plan explicités quand ils existent.
   N'invente rien qui ne soit pas dans le diff, dans les corps de commit ou dans
   le contexte fourni.
5. Remplace le placeholder `_À compléter : ..._` par le texte rédigé.
6. Ne touche à rien d'autre : ni au `frontmatter`, ni à la table `### Commits`,
   ni aux autres sections du journal.

À la fin, affiche dans ta réponse : le chemin, la date, le titre de la section
écrite et le nombre de commits couverts.