# Problèmes de cohérence connus

Ce document recense les incohérences identifiées entre le cahier des charges, le MLD
et le modèle JPA du projet, ainsi que leur statut.

## Problème 8 — Mémoire sans étudiant

**Description.** Un mémoire peut exister sans être rattaché à un étudiant :
`Etudiant.memoire` est déclaré `@OneToOne(optional = true)`, donc la clé étrangère
`memoire_id` côté `Etudiant` est nullable. Le cahier des charges impose qu'un mémoire
ait au moins une soutenance et qu'une soutenance porte sur un seul mémoire, mais il ne
dit pas explicitement qu'un mémoire doit avoir un étudiant. Le modèle autorise donc
des mémoires « orphelins » d'étudiant.

**Impact.** Les écrans qui partent d'une soutenance pour retrouver l'étudiant
(`soutenance → memoire → etudiant`) doivent tolérer l'absence d'étudiant, sous peine
de `NullPointerException` ou de ligne manquante dans les listes et les agrégats du
tableau de bord. C'est aussi un point de cohérence métier à clarifier : un mémoire
de PFE est en principe porté par un étudiant.

**Statut.** Corrigé : le modèle reste tel quel (mémoire sans étudiant toujours
possible), mais des garde-fous côté service empêchent les incohérences métier.

**Correction apportée.** Trois vérifications ont été ajoutées :
- `SoutenanceServiceImpl.save()` refuse de planifier une soutenance dont le mémoire
  n'a pas d'étudiant (garde-fou à la planification).
- `EtudiantServiceImpl.save()` refuse de rattacher un mémoire déjà pris par un autre
  étudiant (durcissement étudiant).
- `MemoireServiceImpl.delete()` refuse de supprimer un mémoire rattaché à un étudiant
  (durcissement suppression).

**Piste future.** Trancher avec l'équipe si `Etudiant.memoire` doit devenir obligatoire
(`optional = false`, `nullable = false`) et, le cas échéant, ajouter une contrainte
de validation côté service lors de la création d'un mémoire. À défaut, documenter
explicitement le cas des mémoires sans étudiant dans le cahier des charges.

## Décisions prises

Les problèmes de cohérence suivants ont été corrigés dans le modèle.

- **P1** — `Evaluation.note` était obligatoire, ce qui empêchait de composer un jury avant la saisie des notes : la colonne est désormais nullable.
- **P2** — Le matricule de l'étudiant n'avait pas de contrainte d'unicité : ajout de `uk_etudiant_matricule`.
- **P3** — Le login utilisateur était limité à 100 caractères, insuffisant pour une adresse e-mail : porté à 180.
- **P4** — L'utilisateur ne disposait pas d'un indicateur d'activation : ajout de la colonne `actif`.
- **P5** — L'historique d'action ne portait ni écran ni action ni index : ajout des colonnes `ecran` et `action` et des index associés pour le filtrage du journal.
- **P6** — Les constructeurs sans argument des entités étaient `protected`, bloquant leur instanciation hors du package `model` : rendus `public`.
- **P7** — `Memoire`, `Evaluation` et `Soutenance` ne proposaient pas de fabrique `create()` pour les formulaires : ajout de `create()`.
- **P8** — Mémoire sans étudiant : le modèle reste tel quel, mais des garde-fous côté service (planification, rattachement étudiant, suppression mémoire) empêchent les incohérences métier.
- **P10** — L'absence de chemin direct `Soutenance → Étudiant` empêchait d'afficher l'étudiant d'une soutenance : résolu en utilisant `Etudiant.memoire` comme pont (jointure gauche).