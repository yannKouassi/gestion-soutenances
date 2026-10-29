# Audit du module C — Académique & Soutenances (Administrateur)

**Statut : audit du 28 octobre 2026, branche `fix/module-c-date-depot-et-filtre-jury`
(commit `f195aa6`, dérivée de `feature/ameliorations`).**

Ce document a deux parties : les deux correctifs livrés pendant l'audit (§1), puis les
problèmes constatés et **non corrigés** (§2), classés par gravité, avec les références
exactes dans les sources. Le §3 liste ce qui a été vérifié conforme pour éviter de
« corriger » du code sain lors d'une prochaine passe.

Toutes les références `fichier:ligne` renvoient à l'état du dépôt au commit `f195aa6`.
Les documents de référence (`documents/`) restent la source de vérité pour les règles
métier : le présent rapport ne tranche que des points d'implémentation.

---

## 1. Correctifs livrés

### 1.1 Soutenance possible le jour même du dépôt du mémoire

La règle imposait une date **strictement postérieure** au dépôt, à trois endroits
(service, borne HTML `min`, script client). Elle est remplacée par « pas de date
**antérieure** au dépôt » : le jour du dépôt est accepté.

| Fichier | Ligne | Changement |
|---|---|---|
| `service/impl/SoutenanceServiceImpl.java` | 89-92 | `!isAfter(dateDepot)` → `isBefore(dateDepot)` ; message « ne peut pas être antérieure à la date de dépôt ». |
| `controller/PlanificationServlet.java` | 161-174 | Borne `min` : `depot.atStartOfDay()` (au lieu de `depot.plusDays(1)`). |
| `controller/PlanificationServlet.java` | 162 | `LocalDateTime.now()` → `now().plusMinutes(1)` : la borne ne propose plus un créneau déjà passé à la seconde près. |
| `WEB-INF/views/admin/soutenance-form.jsp` | 46 | Aide sous le champ date : « Le jour même du dépôt est accepté ». |
| `WEB-INF/views/admin/soutenance-form.jsp` | 76-97 | Le script client recalcule la même borne (suppression du `+1 jour`, arrondi à la minute suivante). |

La règle « la soutenance doit être dans le futur » (`SoutenanceServiceImpl.requireFutur`,
`176-181`) reste active et s'applique en plus. Pour une démonstration :

1. créer un mémoire avec **date de dépôt = aujourd'hui** (`/admin/memoires`, la date
   future est refusée par `MemoireServiceImpl.java:55`) ;
2. planifier sa soutenance **le jour même**, à une heure encore à venir.

### 1.2 L'encadreur du mémoire n'est plus ajoutable au jury

L'encadreur rattaché au mémoire est déjà lié à la soutenance par
`soutenance → memoire → encadreur`. Il ne doit donc pas pouvoir être ajouté une seconde
fois comme membre du jury.

| Fichier | Ligne | Changement |
|---|---|---|
| `controller/JuryServlet.java` | 134-137 | La liste `encadreurs` envoyée au `<select>` exclut désormais l'encadreur du mémoire, en plus des membres déjà présents. |
| `controller/JuryServlet.java` | 149-154 | Helper `encadreurMemoireId(Soutenance)` avec garde sur `memoire` et `encadreur` nuls. |
| `service/impl/SoutenanceServiceImpl.java` | 121-125 | Garde-fou dans `ajouterMembre` : refus même si le POST est forgé (le contrôleur n'est pas une frontière de sécurité). |
| `WEB-INF/views/admin/jury-form.jsp` | 21, 65, 68 | Textes d'aide mis à jour pour expliquer l'absence de l'encadreur dans la liste. |

Rétro-compatible : les jurys existants qui contiennent déjà le directeur (données de
démo `sou1`, `sou2`…) s'affichent et se composent normalement ; seul l'**ajout** est
refusé, avec un message explicite.

### 1.3 Question ouverte laissée par ce correctif

L'encadreur du mémoire est désormais traité comme « déjà rattaché », mais il **ne compte
pas** dans le minimum de deux membres : ce minimum est porté par les `Evaluation`
(`SoutenanceServiceImpl.JURY_MINIMUM`, `:29`, utilisé `:139` et `:166`). Un jury
« conforme » est donc aujourd'hui **directeur + 2 autres encadreurs évaluateurs**.

Aligner sur « directeur + 1 = 2 membres » suppose de modifier aussi
`mettreAJourStatutApresEvaluation` (`:163`) et la garde de saisie de note
(`EncadreurServiceImpl.java:141-143`). Décision d'équipe à prendre, non tranchée ici.

---

## 2. Problèmes constatés, non corrigés

### 🔴 2.1 Un jury reste modifiable après passage en `EVALUEE` — la moyenne disparaît

`SoutenanceServiceImpl.ajouterMembre` (`:113`) et `retirerMembre` (`:133`) ne testent
jamais le statut de la soutenance. Scénario reproductible :

1. soutenance `EVALUEE`, 3 membres, tous notés, moyenne affichée ;
2. l'admin ajoute un 4ᵉ membre → accepté ;
3. `calculerMoyenne` (`:147`) renvoie `Optional.empty()` car une note est nulle → **la
   note finale disparaît** de la liste et du détail, alors que le statut reste `EVALUEE` ;
4. le nouveau membre ne peut jamais noter : `EncadreurServiceImpl.java:141-143` refuse
   toute saisie sur une soutenance `EVALUEE`. La soutenance est définitivement bloquée.

Le retrait d'un membre **déjà noté** est également accepté : la moyenne est recalculée
silencieusement sur les membres restants, sans trace ni changement de statut.

*Correctif attendu* : refuser ajout et retrait dès que `statut != PLANIFIEE`, et masquer
les formulaires correspondants dans `jury-form.jsp`.

### 🔴 2.2 Le statut `ANNULEE` n'est jamais atteignable

`grep "ANNULEE"` ne trouve que des **lectures** : la déclaration de l'enum
(`model/StatutSoutenance.java`), les gardes de saisie de note
(`EncadreurServiceImpl.java:144`, `EncadreurServlet.java:472`). Aucun `setStatut(...)`,
aucun bouton « Annuler la soutenance » dans `soutenance-form.jsp`,
`soutenance-list.jsp` ou `soutenance-detail.jsp`. `SoutenanceServlet` n'implémente même
pas `doPost` (161 lignes, `doGet` seul → HTTP 405).

Or le cahier des charges liste `PLANIFIEE / EVALUEE / ANNULEE`, et le jeu de démo
contient une soutenance annulée (`donnees-demo.sql`, `@sou5`). Fonctionnalité manquante,
visible à l'oral.

### 🟠 2.3 Le menu « Composition du Jury » mène à une impasse

`WEB-INF/views/admin/admin-layout.jspf:78` pointe sur `/admin/soutenances?vue=jurys`,
mais `SoutenanceServlet` ne traite que `vue=detail` (`:51`). Le paramètre ne sert qu'à
surligner le menu : on arrive sur la liste, sans lien direct vers
`/admin/jury?soutenance=…`. Il faut passer par « Détails » puis « Gérer le jury »
(`soutenance-detail.jsp:29`).

### 🟠 2.4 Les erreurs métier sont perdues au POST (planification et jury)

`MemoireServlet` possède `findValidationError(Throwable)` (`:286-298`) qui déplie la
chaîne de causes d'un `EJBException` pour retrouver l'`IllegalArgumentException` du
service. `PlanificationServlet` (`:118-124`) et `JuryServlet` (`:120-126`) ne l'ont pas :
si WildFly enveloppe l'exception dans une `EJBTransactionRolledbackException`, l'admin
lit « Enregistrement impossible. Vérifie la mémoire, la salle et le créneau choisis. » au
lieu du motif réel (« Cette salle est déjà occupée sur ce créneau. », « Ce mémoire est
déjà rattaché à une soutenance. »…).

*Correctif attendu* : extraire l'utilitaire dans une classe commune aux servlets.

### 🟠 2.5 N+1 inutile sur la liste des soutenances

`SoutenanceServlet.assembler` (`:106-117`) calcule `tailleJury` (`findMembres`, 1 requête)
et `noteAffichee` (`calculerMoyenne`, 2 requêtes) **pour chaque ligne**. Or
`soutenance-list.jsp` n'affiche ni l'un ni l'autre (colonnes : Mémoire, Étudiant, Date,
Statut, Actions). 50 soutenances = 100 requêtes dont le résultat est jeté.

*Correctif attendu* : afficher ces deux colonnes (elles existent dans
`SoutenanceListeEntry`, `:30-36`) ou supprimer le calcul.

### 🟠 2.6 Le mémoire d'une soutenance déjà dotée d'un jury peut être changé

`SoutenanceServiceImpl.save` (`:102`) fait `setMemoire(memoire)` sans condition. Le
contrôle d'unicité (`:95-98`) empêche seulement de rattacher un mémoire déjà soutenu
*ailleurs*. Donc : planifier sur le mémoire A, composer le jury, puis éditer vers le
mémoire B → **le jury est conservé** (dont l'encadreur de A), le statut aussi, et
l'historique mentionne le nouveau titre. Aucune purge, aucune trace de la substitution.

*Correctif attendu* : interdire le changement de mémoire dès qu'au moins une
`Evaluation` existe pour la soutenance.

### 🟠 2.7 Aucun contrôle de disponibilité des encadreurs, aucune durée de soutenance

Les salles sont contrôlées (`SoutenanceRepositoryImpl.existsSalleConflit`, `:143-152`)
mais par **égalité exacte du `LocalDateTime`**. Conséquences :

- deux soutenances dans la même salle à 09:00 et 09:01 sont acceptées ;
- un encadreur peut être convoqué sur deux soutenances à la même heure : rien ne le
  vérifie dans `ajouterMembre` ;
- aucune durée de soutenance n'existe dans le modèle, donc aucune notion de créneau.

### 🟡 2.8 Session expirée → page d'erreur technique

`config/AdminAuthorizationFilter.java:32` et `:38` renvoient `401 "Connexion requise."`
(page d'erreur WildFly). AGENTS.md exige une vérification de session côté contrôleur avec
**redirection** selon le rôle ; seul `DashboardServlet.java:24` redirige vers `/login`.
Un clic dans le menu après expiration de session affiche une page d'erreur.

Les filtres `EncadreurAuthorizationFilter` et `EtudiantAuthorizationFilter` suivent le
même modèle : à traiter ensemble.

### 🟡 2.9 Les messages de succès/erreur passent par la query-string

Motif `?message=` / `?erreur=` (URL-encodé) relu au GET :
`PlanificationServlet.java:75-76`, `JuryServlet.java:72-73`, `MemoireServlet.java:76`,
`SoutenanceServlet.java:71-72`, `EncadreurServlet.java:188-189`. Les messages sont
visibles dans l'URL, rejouables, sans expiration et partageables à tort. Une *flash* en
session serait plus propre.

### 🟡 2.10 Deux conventions d'affichage des notes dans la même application

- `15,50` dans la liste : `SoutenanceServlet.formatNote` (`:125`, `Locale.FRANCE`) ;
- `15.50` dans `jury-form.jsp:50` et `soutenance-detail.jsp:111`, qui affichent
  `${membre.note}` brut.

### 🟡 2.11 Dates affichées en ISO, non localisées

`${memoire.dateDepot}` affiché brut (`2026-06-15`) dans `memoire-list.jsp:54`,
`memoire-detail.jsp:43`, `soutenance-detail.jsp:45`, `jury-detail.jsp:57`,
`encadreur/etudiant-detail.jsp:58`, alors que les soutenances sont formatées en
`dd/MM/yyyy HH:mm`. Incohérence visible sur une même page
(`soutenance-detail.jsp:84` vs `:45`).

### 🟡 2.12 `/admin/dashboard` n'existe pas

Le mapping est demandé par AGENTS.md (écran D) ; aucune servlet ne le sert, le menu
affiche « À venir » (`admin-layout.jspf:93`). Écran encore vide.

### 🟡 2.13 Pagination d'historique copiée-collée cinq fois

Même bloc `setHistoryPage` avec `HISTORY_PAGE_SIZE = 4` dans
`PlanificationServlet.java:201-229`, `JuryServlet.java:179-205`,
`MemoireServlet.java:228-252`, `ActeursServlet` et `ReferentielServlet`. Toute évolution
(page size, tri, filtre) doit être répétée cinq fois.

### 🟡 2.14 Aucun test automatisé

`src/test/` n'existe pas. Les règles métier les plus sensibles — date vs dépôt, jury
minimum de deux, unicité mémoire/soutenance, moyenne du jury — ne sont vérifiées qu'à la
main. Un test JUnit sur `SoutenanceServiceImpl` (avec repositories mockés) couvrirait les
deux tiers du risque avant la soutenance.

---

## 3. Vérifié conforme — à ne pas « corriger »

- **Mots de passe** : `service/impl/PasswordHasher.java`, PBKDF2-HMAC-SHA256, 310 000
  itérations, sel aléatoire de 16 octets — au-delà de l'exigence « au minimum SHA-256 ».
- **Requêtes** : JPA/Criteria et JPQL nommé (`JOIN FETCH`, paramètres liés) ; aucune
  concaténation de chaîne dans une requête.
- **JSP** : uniquement EL et JSTL, aucun scriptlet, échappement systématique par
  `<c:out>` pour les données saisies.
- **CSRF** : jeton de session généré et vérifié par `AdminAuthorizationFilter`
  (`:47-57`), présent dans tous les formulaires POST du module.
- **Relations** : `FetchType.LAZY` partout ; `Evaluation` porte bien la N-N
  `Soutenance × Encadreur` avec `uk_evaluation_soutenance_encadreur` ; le repository dédié
  à cette relation existe (`repository/EvaluationRepository.java`).
- **Entités** : conventions respectées (extension de `BaseEntity`, `@AttributeOverride`
  sur l'identifiant, constructeur `protected`, colonnes `snake_case`).

---

## 4. Ordre de traitement proposé

| Priorité | Point | Effort |
|---|---|---|
| 1 | 2.1 verrouiller le jury quand la soutenance n'est plus `PLANIFIEE` | faible |
| 2 | 2.2 permettre l'annulation d'une soutenance (`ANNULEE`) | moyen |
| 3 | 2.3 lien direct vers la composition du jury depuis la liste | faible |
| 4 | 2.4 remonter les messages métier de planification et de jury | faible |
| 5 | 2.5 supprimer le N+1 de la liste des soutenances | faible |
| 6 | 2.11 / 2.10 uniformiser dates et notes | faible |
| 7 | 2.6 / 2.7 cohérence mémoire et disponibilités | moyen |
| 8 | 2.8 / 2.9 session et messages *flash* | moyen |
| 9 | 2.12 tableau de bord, 2.14 tests | élevé |

Les points 1 à 4 sont ceux qui se voient le plus à la démonstration et à l'oral.
