---
utilisateur: xxvByMe
branche: feature/admin-historique
description: 'Lot « journal d''audit » — écran /admin/historique, colonnes ecran et action indexées sur Historique_Action, filtres et pagination, corrections de revue, exports Markdown des documents de référence'
---

# Changelog — branche `feature/admin-historique`

Récapitulatif des 6 commits de la branche, tous réalisés le 15 octobre 2026 par
`xxvByMe` à partir de la fusion `8510509` sur `main`. Branche en cours de
livraison, HEAD `0e417e4`.

## Documents de référence en Markdown

- Export au format Markdown des documents de référence du dépôt : charte
  graphique, découpage/MLD et sujet complet (`9f07fa5`).
- Export au format Markdown du cahier des charges (`f5e4312`).

Ces exports complètent les sources PDF/XLSX, qui restent inchangées.

## Modèle et persistance

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

## Écran `/admin/historique`

- `HistoriqueServlet` répond en GET à `/admin/historique` et forward vers
  `historique-list.jsp`, rédigée en EL et JSTL uniquement. Le servlet ne
  redoit aucun contrôle de session ni de rôle : le filtrage `/admin/*` est
  assuré par `AdminAuthorizationFilter`.
- Entrée de navigation « Journal d'audit » ajoutée dans `admin-layout.jspf`,
  sous l'en-tête « Pilotage & audit ».

## Consultation, filtres et pagination

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

## Corrections

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

## Non-régression

Les huit JSP des quatre écrans référentiels (listes et formulaires grades,
départements, filières, salles) sont inchangées à l'octet près : leur diff est
vide entre le point de départ de la branche et `0e417e4`. Seuls le fragment de
navigation, le bandeau et la feuille de style partagée ont été modifiés.

## Hors périmètre et points en attente

- `/admin/dashboard` reste hors périmètre. Les indicateurs documentés dans le
  découpage (« nb soutenances », « taux validation », « répartition par
  département ») ne sont pas implémentés : `Soutenance` et `Departement` n'ont
  pas de repository, et aucun seuil de validation n'est documenté.
- La colonne `entite` sur `Historique_Action` est décidée en stand-by : elle
  n'existe pas dans le modèle.
- Le décompte et la sélection de la page restent exécutés dans deux
  transactions distinctes.

## Commits

| Commit | Sujet |
|---|---|
| `0e417e4` | fix: corriger la casse du filtre auteur et le bandeau de /admin/historique |
| `1091367` | fix: corriger cinq defauts du journal d'audit /admin/historique |
| `778866c` | feat: ajouter le journal d'audit global /admin/historique |
| `e62629b` | feat: indexer l'historier par ecran et action avec recherche globale filtree |
| `f5e4312` | docs: ajouter l'export Markdown du cahier des charges |
| `9f07fa5` | docs: exporter les documents de référence au format Markdown |
