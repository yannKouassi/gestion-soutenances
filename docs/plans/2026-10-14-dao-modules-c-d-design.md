# Couche DAO — modules C et D (Administrateur) : note de conception

**Statut : note de conception du 14 octobre 2026. Aucune ligne de code DAO n'existe à ce jour.**
Ce document ne contient aucune implémentation : il enregistre le périmètre arrêté pendant la
discussion de design, le contenu attendu de chaque DAO, les tests associés, les risques
techniques connus et la seule question encore ouverte.

C'est un document étroit posé **au-dessus** de `DAO.md` (conception générale de la couche
`dao/` : forme du CRUD générique, règles JPQL, `JOIN FETCH`, frontière DAO/service) et de
`suggestion.md` (architecture globale, table des écrans/URL, pièges techniques). Il ne les
remplace pas : en cas de doute sur une signature générique ou une règle de rédaction de
requête, `DAO.md` fait foi. Ici, on ne tranche que ce qui concerne les modules **C
(Académique & Soutenances)** et **D (Pilotage & Audit)** de l'écran administrateur.

---

## 1. État du dépôt au moment de cette note

- **Existe** : la couche `model/` seule — `BaseEntity` (id `UUID`), 11 entités, 2 enums, dans
  `src/main/java/ci/univ/soutenances/model/`. Le package `ci.univ.soutenances` ne contient
  que ce dossier `model/`.
- **N'existe pas** : le dossier `dao/`, les servlets, les JSP métier, `src/main/webapp/css/`,
  les tests (`src/test/` entier). `src/main/webapp/index.jsp` est un placeholder.
- **`pom.xml`** : exactement trois dépendances, toutes `provided` — `jakarta.servlet-api`
  6.0.0, `jakarta.persistence-api` 3.1.0, `mysql-connector-j` 8.4.0. Ni JUnit, ni Hibernate
  (le provider JPA est fourni par WildFly à l'exécution, il est absent du projet), ni H2.
- **`persistence.xml`** (`src/main/resources/META-INF/`) : unité `GestionSoutenancesPU`,
  `transaction-type="JTA"`, datasource JNDI, `hibernate.hbm2ddl.auto=update`.

### Attributs du modèle utilisés par la conception qui suit

Vérifiés dans les sources de `model/`, pas repris de mémoire — le JPQL de §5 en dépend :

| Entité | Attributs et relations mobilisés par C/D |
|---|---|
| `Memoire` | `titre`, `description`, `dateDepot` (`LocalDate`, **nullable**), `encadreur` |
| `Soutenance` | `dateHeure` (`LocalDateTime`), `statut`, `memoire` (`@OneToOne`, unique : `uk_soutenance_memoire`), `salle` |
| `Evaluation` | `note` (`BigDecimal` precision 4, scale 2, non null), `avis`, `soutenance`, `encadreur` — contrainte `uk_evaluation_soutenance_encadreur` |
| `HistoriqueAction` | `typeAction`, `dateAction` (`LocalDateTime`), `description`, `utilisateur` — table SQL `Historique_Action` |
| `Etudiant` | `matricule`, `nom`, `prenom`, `email`, `filiere`, `memoire` (`@OneToOne`, nullable), `utilisateur` |
| `Encadreur` | `nom`, `prenom`, `email`, `grade`, `departement`, `utilisateur` |
| `Filiere` | `code`, `libelle`, `departement` |
| `Salle` | `nomSalle`, `capacite` (primitif `int`), `departement` |
| `Departement` | `code`, `nom` |

Points d'attention vérifiés dans le code :

- Toutes les associations sont `FetchType.LAZY` ; toutes les entités étendent `BaseEntity`
  (id `UUID` généré).
- `Encadreur` n'a **pas** d'attribut `specialite`, alors que le cahier des charges en
  parle : ne pas écrire de requête dessus.
- `Etudiant` n'a **pas** de `departement` : le département de l'étudiant se rejoint par
  `e.filiere.departement`.
- En JPQL on écrit les noms d'entités Java (`FROM HistoriqueAction h`), jamais les noms de
  tables SQL (`Historique_Action`) — `DAO.md` §7.

---

## 2. Fichiers à créer

Inventaire complet de la passe : **huit fichiers** dans `ci.univ.soutenances.dao` (le dossier
`dao/` n'existe pas encore), dont cinq porteurs de logique et trois limités à une méthode de
lecture :

```
src/main/java/ci/univ/soutenances/dao/
├── BaseDao.java              CRUD générique (DAO.md §3), EntityManager injecté par constructeur
├── MemoireDao.java           module C
├── SoutenanceDao.java        modules C + D (listes, filtres, agrégats du dashboard)
├── EvaluationDao.java        module C — DAO exigé pour la relation N-N Soutenance × Encadreur
├── HistoriqueActionDao.java  module D
├── SalleDao.java             C — uniquement une méthode de lecture (select de planification)
├── EncadreurDao.java         C — uniquement une méthode de lecture (select du jury)
└── FiliereDao.java           C — uniquement une méthode de lecture (filtre filière de soutenance-list)

pom.xml                       4 ajouts réservés aux tests (§6.1)
src/test/resources/META-INF/persistence.xml   unité de persistance de test (§6.2)
src/test/java/ci/univ/soutenances/dao/<Nom>DaoTest.java   un test par DAO (§6.3)
```

Ordre de travail : les 4 ajouts `pom.xml` + l'unité de test d'abord (sinon rien ne tourne),
puis `BaseDao`, puis chaque DAO **avec son test dans le même passage** (décision 4), puis les
trois méthodes de lookup.

---

## 3. Décisions prises

Cinq décisions **arrêtées** pendant la discussion. Elles ne sont pas des questions : ne pas
les rouvrir en revue de PR.

### 1. Périmètre (SCOPE) — arrêté

Livrables de cette passe, au détail près (inventaire de §2) — huit fichiers dans `dao/` :

- `BaseDao` + les quatre DAOs complets des modules C et D : `MemoireDao`, `SoutenanceDao`,
  `EvaluationDao`, `HistoriqueActionDao` ;
- plus **exactement trois** méthodes de lecture, ajoutées à `SalleDao`, `EncadreurDao` et
  `FiliereDao` (fichiers aujourd'hui inexistants, créés pour l'occasion) : une seule méthode
  chacun, celle qui alimente une liste déroulante de C (select de planification, sélection du
  jury, filtre filière de `soutenance-list.jsp`) afin d'afficher le libellé d'une clé
  étrangère.

Ces trois derniers DAOs sont des DAOs de lecture seule : ils n'ajoutent ni écriture, ni
validation, ni autre méthode. Rien au-delà : **pas de servlet, pas de JSP, pas de CSS** (§8).

### 2. Stratégie de transactions JTA — laissée en attente (voir §4)

`persistence.xml` déclare `transaction-type="JTA"` : `em.getTransaction()` est donc
indisponible en production. `BaseDao` est donc écrit avec un champ
`protected EntityManager em` rempli par le constructeur. Justification consignée : `DAO.md` §2
établit que les signatures des DAOs, les requêtes JPQL, les `JOIN FETCH` et les appels
intra-DAO sont **identiques** sous les
deux stratégies (A : `UserTransaction` depuis la servlet ; B : CDI + `@Transactional`) — seul
le remplissage du champ change. Contrainte dure qui en découle : **aucune servlet ne doit
appeler `new XxxDao(em)` tant que l'équipe n'a pas tranché**. Rien d'autre dans ce périmètre
ne dépend de la décision — les tests tournent sur une unité `RESOURCE_LOCAL` (§6).

### 3. Agrégats du dashboard dans le DAO de l'entité — arrêté

`countTotal()` et `countByStatut(...)` vivent dans `SoutenanceDao`, **pas** dans un
`DashboardDao` dédié : la règle « un DAO par entité » de `DAO.md` §4 prime sur la commodité
d'un fichier unique. Même logique pour la répartition par filière/département du dashboard
(risque 1, §7) : elle reste dans `SoutenanceDao`, une éventuelle agrégation inter-entités
allant dans un futur `service/`.

### 4. Tests — dans le périmètre, dès cette passe — arrêté

Chaque méthode de DAO livrée part avec son test JUnit 5 dans le **même** passage. Cela
impose quatre ajouts au `pom.xml` (§6.1) et une seconde unité de persistance pour les tests
(§6.2). Limite assumée et non négociée : H2 n'est pas MySQL (§6.4).

### 5. Hors périmètre par conception — arrêté

- Les validations métier (jury d'au moins deux membres, salle disponible, mémoire pas déjà
  soutenu) appartiennent à un futur `service/` selon `DAO.md` §9. Les DAOs de §5 leur
  fourniront les requêtes, ils ne contiendront aucune de ces règles.
- Les lignes `HistoriqueAction` ne sont **jamais** écrites depuis l'intérieur d'un DAO.
- Aucune servlet, aucune JSP, aucun CSS dans cette passe.

Détail complet en §8.

---

## 4. Question ouverte (la seule) : stratégie de transactions JTA

**Seule question en attente, et seul point qui bloque la couche servlet.** Deux options,
détaillées dans `DAO.md` §2 :

- **Stratégie A** — `UserTransaction` explicite (`ut.begin()` / `commit()` / `rollback()`)
  pilotée depuis chaque servlet d'écriture.
- **Stratégie B** — CDI + `@Transactional` sur les DAOs (`beans.xml` +
  `jakarta.transaction-api` ajoutés au projet).

Pourquoi elle peut rester en attente pendant toute cette passe :

- Tout ce qui est conçu ici est identique dans les deux cas : signatures des DAOs, requêtes
  JPQL, `JOIN FETCH`, découpe par entité (`DAO.md` §2, « Ce qui ne change pas »).
- `BaseDao` prend déjà son `EntityManager` par constructeur : seule la façon de le fournir
  changera (`new XxxDao(em)` avec `UserTransaction` côté servlet, ou `@PersistenceContext` +
  injection CDI côté DAO).
- Les tests de §6 utilisent une unité `RESOURCE_LOCAL` : ils ne sentent pas le choix.

**Contrainte à retenir tant que la question est ouverte : aucune servlet ne doit appeler
`new XxxDao(em)`.** La décision ne bloque que cela — pas l'écriture d'un seul DAO, pas ses
tests. Après la décision : adapter uniquement le remplissage du champ `em` ; en stratégie B,
ajouter `WEB-INF/beans.xml` et `jakarta.transaction-api` comme prévu par `DAO.md` §2.

---

## 5. Contenu attendu de chaque DAO

Rappels communs, valables pour toutes les méthodes ci-dessous (`DAO.md` §7, §10) : requêtes
toujours typées, paramètres nommés (`:param`), aucune concaténation de valeur, aucune règle
métier (`if`) dans un DAO, aucune méthode n'accepte de `String` venu d'une URL (risque 4),
aucun `em.getTransaction()`. Les méthodes héritées de `BaseDao` (`findById`, `findAll`, `create`,
`update`, `delete`, `count`) sont décrites dans `DAO.md` §3 et ne sont pas répétées ici.

### `BaseDao` — le CRUD générique

Conforme à `DAO.md` §3 : champ `protected EntityManager em` rempli par le constructeur,
`abstract Class<T> getEntityClass()` (et pas de réflexion), les cinq opérations CRUD +
`count()` protégé. Pas d'autre responsabilité.

### `MemoireDao` — écrans `/admin/memoires` (liste + formulaire) et select de `/admin/planification`

- `findSansSoutenance()` — select de planification : les mémoires qui n'ont pas encore de
  soutenance (`NOT EXISTS` sur `Soutenance.memoire`), requête déjà conçue dans `DAO.md` §5.
  Justifiée par la contrainte `uk_soutenance_memoire` : un mémoire soutenu ne doit plus
  apparaître dans le select (`suggestion.md` §8.4).
- Méthode de liste pour `memoire-list.jsp` avec `JOIN FETCH m.encadreur` : la colonne
  « directeur de mémoire » s'affiche hors transaction (risque 2).
- À tolérer côté test et écran : `dateDepot` est `nullable`. Aucune contrainte unique sur
  `titre` → pas de méthode de contrôle d'unicité à écrire (contrairement à `findByCode` de
  `GradeDao`).

### `SoutenanceDao` — écrans `/admin/soutenances` (liste filtrée) et `/admin/dashboard`

- `findByStatut(StatutSoutenance)` — filtrage de liste, requête de `DAO.md` §5.
- Méthode de liste filtrée pour `soutenance-list.jsp` : filtres statut / date / filière, avec
  `JOIN FETCH` de tout ce que la JSP affiche. Voir les risques 2 et 3 : la chaîne de fetch et
  la pagination posent problème sur cet écran précis.
- `countTotal()` et `countByStatut(StatutSoutenance)` — agrégats du dashboard (décision 3).
- Répartition du dashboard : décompte par filière dans ce DAO ; l'agrégation par département
  suit le risque 1 (§7), avec un repli convenu.
- La moyenne du dashboard, si le dashboard l'affiche, vient d'`EvaluationDao`
  (`moyenneBySoutenance`), pas d'une nouvelle requête ici — `DAO.md` §1 (une information,
  trois écrans, une seule méthode).

### `EvaluationDao` — le DAO exigé par le cahier des charges (relation N-N `Soutenance` × `Encadreur`)

Entité porteuse : `Evaluation`, avec contrainte `uk_evaluation_soutenance_encadreur`.
Écrans : `jury-form` (C), moyenne affichée sur `/admin/soutenances`, dashboards. Quatre
requêtes, déjà conçues dans `DAO.md` §6 :

- `findEncadreursBySoutenance(UUID)` — membres du jury d'une soutenance.
- `countBySoutenance(UUID)` — combien de membres ont déjà soumis une note (condition de la
  bascule en `EVALUEE`).
- `moyenneBySoutenance(UUID)` — moyenne du jury ; peut renvoyer `null` sans note. Le DAO rend
  le `BigDecimal` brut de `AVG(...)` ; l'arrondi `setScale(2, HALF_UP)` et le test de `null`
  se font à l'affichage (`DAO.md` §6).
- `exists(UUID, UUID)` — contrôle du doublon avant insertion, pour afficher un message métier
  au lieu d'une `PersistenceException`.

Aucune logique de bascule de statut ici : elle relèvera d'`EvaluationService` (§8).

### `HistoriqueActionDao` — écrans `/admin/historique` et dashboard (dernières actions)

- `findDernieres(int)` — avec `JOIN FETCH h.utilisateur` : la liste affiche qui a fait quoi
  (risque 2). Le tri par `dateAction` décroissant et la limite `int` font partie de la
  méthode.
- `findByUtilisateur(UUID)` — historique d'un compte (`DAO.md` §5).
- Lecture seule dans les faits : les écritures sont portées par les futurs services, jamais
  depuis l'intérieur d'un DAO (décision 5). Rappel pour l'auteur futur de ces services :
  `typeAction` et `dateAction` sont `nullable = false`.

### `SalleDao` — créé pour C : une seule méthode de lecture

Liste des salles pour le select de `/admin/planification`, renvoyant au minimum le libellé de
l'option (`nomSalle`). Si `soutenance-form.jsp` affiche aussi le département, la méthode
charge la relation par `JOIN FETCH s.departement` (forme `findAllAvecDepartement()` de
`DAO.md` §8). Rien d'autre n'est ajouté à ce DAO dans cette passe.

### `EncadreurDao` — créé pour C : une seule méthode de lecture

Liste des encadreurs pour composer le jury dans `jury-form.jsp`, renvoyant au minimum le
libellé de l'option (`nom` + `prenom`) et chargeant par `JOIN FETCH` les relations affichées
(grade, département — `DAO.md` §8). `findByEmail` et `findByUtilisateurId` de `DAO.md`
§5 servent l'authentification et les formulaires de la partie B : ils viendront avec leur
écran, pas ici.

### `FiliereDao` — créé pour C : une seule méthode de lecture

Liste des filières pour le menu déroulant du filtre de `soutenance-list.jsp`, renvoyant au
minimum le libellé affiché par l'option (`libelle`, le `code` servant de valeur). `Filiere`
porte `code`, `libelle` et un `departement` en `LAZY` : comme pour `SalleDao` et
`EncadreurDao`, la relation se charge par `LEFT JOIN FETCH` si la JSP l'affiche (`DAO.md` §8).
Rien d'autre n'est ajouté à ce DAO dans cette passe — `findByCode` et `findByDepartement` de
`DAO.md` §5 viendront avec les formulaires du bloc A, pas ici.

---

## 6. Tests — dans le périmètre (décision 4)

### 6.1 Les quatre ajouts au `pom.xml`

Tous réservés aux tests ; les trois dépendances actuelles restent inchangées :

| # | Ajout | Où dans le `pom.xml` | Version proposée |
|---|---|---|---|
| 1 | `org.junit.jupiter:junit-jupiter` | `<dependencies>`, `<scope>test</scope>` | 5.x |
| 2 | `org.hibernate.orm:hibernate-core` | `<dependencies>`, `<scope>test</scope>` | 6.x |
| 3 | `com.h2database:h2` | `<dependencies>`, `<scope>test</scope>` | 2.x |
| 4 | `maven-surefire-plugin` | `<build><plugins>` | 3.x |

Précisions :

- **(2) est indispensable** : le provider JPA est aujourd'hui totalement absent du projet —
  WildFly le fournit à l'exécution, Maven non. En `scope test`, le WAR n'est pas impacté.
  Choisir une version 6.x proche de celle embarquée par WildFly 37 (à vérifier dans les
  modules du serveur) pour que les tests parlent à peu près la même version de Hibernate que
  la production.
- **(4) est un plugin, pas une dépendance** : il n'a pas de `<scope>`, il se pose dans
  `<build><plugins>`. Le surefire embarqué par défaut de Maven n'exécute pas JUnit 5, d'où
  l'obligation de fixer sa version en 3.x.
- Versions à confirmer au premier `mvn test`, sur le modèle des JSTL annoncés « à confirmer »
  dans `suggestion.md` §4.

### 6.2 Unité de persistance de test

Fichier : `src/test/resources/META-INF/persistence.xml` (à côté, pas à la place, de
l'unité de production `src/main/resources/META-INF/persistence.xml`) :

- Nom distinct (par ex. `GestionSoutenancesTestPU`), `transaction-type="RESOURCE_LOCAL"` :
  dans les tests seulement, `em.getTransaction().begin()` est donc disponible — jamais dans
  le code produit (`DAO.md` §10).
- Pas de `jta-data-source` : URL JDBC H2 en mémoire.
- `hibernate.hbm2ddl.auto=create-drop` : Hibernate génère le schéma **à partir des vraies
  entités** du projet. Aucun script DDL à écrire (le projet n'en a pas —
  `src/main/resources/db/create-database.sql` ne crée que le schéma MySQL).
- Avantage : `mvn test` tourne sans WildFly, sans MySQL, sans datasource.
- Point de vigilance, à vérifier au premier test avant d'en conclure à un bug de requête : en
  mode standalone, Hibernate découvre les entités depuis l'archive qui contient le
  `persistence.xml` (`target/test-classes`) ; s'il ne voit pas les entités de
  `target/classes`, lister explicitement les 11 éléments `<class>` (ou activer la détection
  d'archives Hibernate).

### 6.3 Couverture attendue

Un test par DAO, nommé `<Nom>DaoTest` sous `src/test/java/ci/univ/soutenances/dao/`. Pour
chaque méthode : au minimum le cas nominal **et** le cas vide / `null`. Le CRUD hérité de
`BaseDao` est couvert à travers un DAO concret (`create` → `findById` → `findAll` → `update`
→ `delete` → `count`), pas via une fausse entité de test. Détail par DAO :

- **`MemoireDaoTest`** : `findSansSoutenance` avec un mémoire sans soutenance **et** un
  mémoire ayant déjà sa `Soutenance` (jeu semé) — seul le premier doit revenir ; création
  sans `dateDepot` acceptée (nullable) ; liste : fermer l'`EntityManager` puis accéder
  `m.getEncadreur().getNom()` — si le `JOIN FETCH` manque, le test échoue par
  `LazyInitializationException`, c'est exactement ce qu'on veut vérifier.
- **`SoutenanceDaoTest`** : `countTotal` et `countByStatut` sur un jeu semé couvrant les trois
  statuts (`PLANIFIEE`, `EVALUEE`, `ANNULEE`) ; répartition du dashboard par
  filière/département — **c'est ce test qui valide ou invalide la syntaxe d'entité-join
  Hibernate 6 du risque 1**, avant même l'écriture du dashboard ; liste filtrée par
  statut/date/filière, puis accès aux relations hors transaction.
- **`EvaluationDaoTest`** : `exists` vrai et faux ; `moyenneBySoutenance` renvoyant `null`
  sans aucune note, puis une valeur avec plusieurs notes ; `countBySoutenance` ;
  `findEncadreursBySoutenance` ; au choix, une insertion en doublon du couple
  (soutenance, encadreur) qui doit échouer — preuve que `uk_evaluation_soutenance_encadreur`
  existe aussi sous H2.
- **`HistoriqueActionDaoTest`** : `findDernieres(n)` ordonnées par `dateAction`
  décroissante et limitées à `n` ; `findByUtilisateur` renvoyant une liste vide pour un
  utilisateur sans action ; accès à `h.getUtilisateur()` hors transaction.
- **`SalleDaoTest` / `EncadreurDaoTest` / `FiliereDaoTest`** : la méthode de lookup renvoie le
  jeu semé, avec le libellé de l'option accessible une fois la transaction fermée (pour
  `FiliereDao`, `libelle` et le `departement` fetché).

Infrastructure : un point d'entrée commun pour ouvrir l'`EntityManager` de l'unité de test et
délivrer une transaction par test (à écrire une fois, sous les tests), sans que `BaseDao` ni
les DAOs de production n'en sachent rien.

### 6.4 Limite connue et acceptée : H2 n'est pas MySQL

C'est le prix à payer pour avoir des tests sans serveur, et le projet l'assume : le schéma de
test est généré par Hibernate sur H2 (`create-drop`), celui de production monte sur MySQL via
`update` ; les types, le dialecte et certaines fonctions diffèrent, et les
`columnDefinition = "TEXT"` du modèle sont recopiés tels quels dans le DDL généré. **Une
requête validée sous H2 peut donc encore avoir besoin d'être confirmée au déploiement sur
MySQL.** Le test sert à valider la logique JPQL, la forme des DAOs et les cas métier vides /
null — pas la parfaite équivalence des deux bases. Chaque écran sera de toute façon testé
manuellement sur WildFly + MySQL avant la PR finale.

---

## 7. Risques techniques connus

Consignés pendant la discussion, à ne pas redécouvrir en implémentation.

### Risque 1 — Répartition du dashboard par département : aucune clé directe `Soutenance` → `Etudiant`

Le seul chemin passe par `Soutenance` → `memoire`, puis `Etudiant.memoire` — une association
`@OneToOne` déclarée **côté `Etudiant`**, sans contrepartie côté `Memoire`. Il n'existe donc
aucune jointure JPA « naturelle » entre une soutenance et l'étudiant qui la porte. La solution
conçue est une jointure d'entité ad hoc en JPQL :

```
... FROM Soutenance s JOIN Memoire m ON s.memoire = m JOIN Etudiant e ON e.memoire = m ...
```

C'est de la **syntaxe Hibernate 6**. Si cette syntaxe n'est pas acceptée (elle dépend de la
version d'Hibernate, celle des tests et celle de WildFly pouvant différer), le repli
**convenu à l'avance** n'est **pas** de déplacer la logique dans une servlet : on compte par
filière dans `SoutenanceDao` et on agrège par département dans un futur `service/` (décision
3). Le test de §6.3 tranche à l'écriture.

### Risque 2 — `LAZY` partout + transaction fermée avant le rendu de la JSP

Toutes les associations du modèle sont `LAZY`, et la transaction JTA est close quand la JSP
se rend. Toute méthode de DAO qui alimente un écran doit `JOIN FETCH` **exactement** ce que
cet écran affiche, sinon `LazyInitializationException` au premier `${...}` qui traverse une
relation (`DAO.md` §8). Corollaire : les méthodes « nues » héritées de `BaseDao` conviennent
aux formulaires et aux contrôles d'existence, pas aux écrans de liste — chaque écran de C/D
reçoit sa méthode dédiée avec fetch (§5).

### Risque 3 — Filtre filière sur `soutenance-list.jsp` : chaîne longue + pagination

Le filtre filière ouvre la chaîne `soutenance -> memoire -> etudiant -> filiere ->
departement`. Un `JOIN FETCH` sur toute cette chaîne, combiné à une pagination, produit des
lignes dupliquées : le `LIMIT` SQL s'applique aux lignes jointes, pas aux entités distinctes.
Le problème doit être traité **dans** la méthode de liste de `SoutenanceDao`. Tension à
garder en tête : charger toute la chaîne d'un coup contredit les filtres statut / date /
filière que le cahier des charges demande sur cet écran. Les trois exigences — relations
chargées, filtres, pagination — tirent la requête dans des directions opposées ; le choix se
fait à l'écriture de cette méthode et n'affecte aucun autre DAO du périmètre.

### Risque 4 — `UUID` reçu depuis une URL

`req.getParameter("id")` renvoie une `String` ; `UUID.fromString(...)` doit être entouré d'un
`try/catch` (page d'erreur propre si le format est invalide). Cela appartient à la couche
**servlet** future, **pas** au DAO : toutes les méthodes de §5 reçoivent déjà des `UUID`
typés. Aucune d'elles ne doit accepter de `String` venant d'un paramètre d'URL — si une telle
méthode apparaît, c'est une erreur de conception.

---

## 8. Hors périmètre

Ce qui est **délibérément absent** de cette passe, pour qu'aucun relecteur ne le prenne pour
un oubli (décision 5) :

- **Validations métier** : jury d'au moins deux membres, salle disponible, mémoire pas déjà
  soutenu — à porter dans un futur `service/` selon `DAO.md` §9 (`SoutenanceService`,
  `EvaluationService`). Les DAOs de §5 fourniront les requêtes nécessaires, mais aucune règle
  conditionnelle ne vit dans un DAO.
- **Écriture d'`HistoriqueAction`** : jamais depuis l'intérieur d'un DAO. Les futurs services
  écriront l'audit de leurs propres écritures.
- **Servlets, JSP métier, CSS** : rien dans cette passe. `src/main/webapp/` ne contient que
  le `index.jsp` placeholder ; `src/main/webapp/css/` n'existe pas et n'est pas créé ici.
- **`SoutenanceDao.findBySalleAndDateBetween`** (détection de conflit de salle,
  `suggestion.md` §8.8) : sa seule appelante sera `SoutenanceService`, qui est hors périmètre —
  la requête viendra avec lui.
- **Les autres méthodes de `DAO.md` §5** qui servent d'autres écrans que C/D (authentification
  et `UtilisateurDao`, référentiels du bloc A, acteurs du bloc B, écrans encadreur/étudiant)
  sont ajoutées en même temps que leur écran, pas dans cette passe.
- **AuthService, `PasswordHasher`, JSTL, amorçage des données, arborescence des JSP** : sujets
  de `suggestion.md`, non traités ici.
- **Stratégie JTA** : ce n'est pas du hors périmètre, c'est la question en attente — §4.

---

## 9. Avant de proposer une PR touchant `dao/`

La checklist de `DAO.md` §10 s'applique telle quelle (aucun `em.createQuery` hors de `dao`,
paramètres nommés uniquement, pas de `em.getTransaction()`, `JOIN FETCH` pour chaque écran,
contrôles d'unicité sur les champs sous contrainte `unique`, `EvaluationDao` avec ses quatre
requêtes, `AVG` protégé contre `null`, ni HTML ni `HttpServletRequest` dans un DAO). S'y
ajoutent, propres à cette passe :

- [ ] Chaque méthode de §5 a son test vert sous `mvn test` (unité de test, §6.2).
- [ ] Aucun `new XxxDao(em)` n'apparaît dans une servlet (question JTA ouverte, §4).
- [ ] Aucun fichier situé en dehors de `dao/`, de `pom.xml` et de `src/test/` n'a été touché.
- [ ] Les trois méthodes de lookup de `SalleDao`, `EncadreurDao` et `FiliereDao` sont les
      seules ajoutées à ces DAOs (périmètre, décision 1).
