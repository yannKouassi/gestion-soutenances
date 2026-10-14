# DAO — conception de la couche d'accès aux données

Document de travail pour la couche `ci.univ.soutenances.dao` du projet **gestion-soutenances**.
Il décrit ce qu'est un DAO, la forme exacte de ceux de ce projet, et les règles à respecter.

---

## 1. Principe : un seul endroit parle à la base

Un **DAO (Data Access Object)** est une classe dont le seul travail est de traduire une
intention métier en requête JPA et de rendre des entités.

```
Servlet (contrôleur)  →  DAO  →  EntityManager  →  MySQL via la datasource JNDI
```

Conséquences directes :

- Une servlet ne contient **jamais** `em.createQuery(...)`. C'est du SQL dans un contrôleur,
  donc une violation du MVC strict — refusé en revue de code.
- Aucun DAO ne connaît `HttpServletRequest`, `HttpServletResponse` ou une JSP.
- Si une information est nécessaire à trois écrans (ex. la moyenne du jury), la requête
  vit dans un DAO et les trois écrans l'appellent. Pas de duplication.

> Exemple concret : la moyenne des notes d'une soutenance est affichée sur
> `/encadreur/mes-jurys`, `/admin/soutenances` et `/etudiant/ma-soutenance`.
> Une seule méthode `moyenneBySoutenance(UUID)` dans `EvaluationDao` sert les trois.

---

## 2. Le problème technique à résoudre d'abord : transactions JTA

`persistence.xml` déclare `transaction-type="JTA"`. Deux conséquences, souvent découvertes
au premier `em.persist()` :

```java
em.persist(grade);          // → TransactionRequiredException (aucune transaction active)
em.getTransaction().begin() // → IllegalStateException (réservé au mode RESOURCE_LOCAL)
```

En JTA c'est **le conteneur** (WildFly) qui délimite les transactions. Il faut donc choisir
comment. Deux stratégies, à trancher avant d'écrire le premier DAO.

### Stratégie A — `UserTransaction` pilotée depuis la servlet

```java
@WebServlet("/admin/grades")
public class GradeServlet extends HttpServlet {

    @PersistenceContext(unitName = "GestionSoutenancesPU")
    private EntityManager em;

    @Resource
    private UserTransaction ut;

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) ... {
        ut.begin();
        try {
            new GradeDao(em).create(grade);
            ut.commit();
        } catch (Exception e) {
            ut.rollback();
            throw e;
        }
    }
}
```

- **Pour** : explicite, très facile à redessiner au tableau et à défendre à l'oral.
- **Contre** : le bloc `begin/commit/rollback` se répète dans chaque servlet d'écriture.
- **Attention** : en JTA les lectures doivent aussi être transactionnelles. Les `findAll()`
  doivent donc eux aussi être entourés, sinon comportement dépendant de la version de WildFly.

### Stratégie B — CDI + `@Transactional`

Nécessite un fichier `src/main/webapp/WEB-INF/beans.xml` vide (activation de CDI) et la
dépendance `jakarta.transaction:jakarta.transaction-api` (scope `provided`) dans le `pom.xml`.

```java
@ApplicationScoped                 // un DAO = un bean CDI réutilisable
public class GradeDao extends BaseDao<Grade> {

    @PersistenceContext(unitName = "GestionSoutenancesPU")
    private EntityManager em;      // injecté ici, plus dans la servlet

    @Transactional                 // transaction ouverte/fermée par le conteneur
    public void create(Grade g) { em.persist(g); }
}
```

Puis dans la servlet : `@Inject private GradeDao gradeDao;` — plus aucune ligne de transaction.

- **Pour** : le code le plus court, transactions couvrant uniformément lectures et écritures,
  DAOs injectables donc testables.
- **Contre** : il faut savoir expliquer ce que fait CDI et pourquoi `beans.xml` est nécessaire.

### Ce qui ne change pas dans les deux cas

La signature des DAOs, les requêtes JPQL, les `JOIN FETCH`, la découpe par entité : identiques.
Seule la façon d'obtenir l'`EntityManager` change. Le présent document est donc valable
quelle que soit la stratégie retenue.

**Recommandation** : stratégie B si l'équipe est à l'aise avec l'injection, stratégie A si
l'objectif prioritaire est de pouvoir tout expliquer ligne par ligne à l'oral.

---

## 3. `BaseDao` : le CRUD générique, écrit une seule fois

Toutes les entités du projet étendent `BaseEntity` (`src/main/java/ci/univ/soutenances/model/BaseEntity.java`),
donc elles ont toutes un `UUID` en clé primaire. Une classe générique factorise les cinq
opérations communes.

```java
public abstract class BaseDao<T extends BaseEntity> {

    protected EntityManager em;

    protected BaseDao(EntityManager em) {
        this.em = em;
    }

    protected abstract Class<T> getEntityClass();

    public T findById(UUID id) {
        return em.find(getEntityClass(), id);
    }

    public List<T> findAll() {
        String jpql = "SELECT x FROM " + getEntityClass().getSimpleName() + " x ORDER BY x.id";
        return em.createQuery(jpql, getEntityClass()).getResultList();
    }

    public void create(T entite) {
        em.persist(entite);
    }

    public T update(T entite) {
        return em.merge(entite);
    }

    public void delete(T entite) {
        em.remove(em.contains(entite) ? entite : em.merge(entite));
    }

    protected long count() {
        return em.createQuery("SELECT COUNT(x) FROM " + getEntityClass().getSimpleName() + " x",
                Long.class).getSingleResult();
    }
}
```

Points à savoir expliquer :

- `ORDER BY x.id` fonctionne parce que l'identifiant est déclaré dans `BaseEntity` :
  tous les DAOs héritent du même champ.
- `getEntityClass()` est une méthode abstraite et **pas** de la réflexion : chaque sous-classe
  écrit `return Grade.class;`. Plus lisible, et ça s'explique en une phrase à l'oral.
- `findAll()` construit une chaîne JPQL par concaténation du **nom de la classe**, ce qui est
  sans risque (aucune donnée utilisateur). Aucune valeur saisie par l'utilisateur ne doit
  jamais entrer dans une chaîne de requête — voir §7.

---

## 4. Arborescence cible

```
src/main/java/ci/univ/soutenances/dao/
├── BaseDao.java              CRUD générique
├── UtilisateurDao.java       findByLogin, findByRole
├── GradeDao.java             findByCode
├── DepartementDao.java       findByCode
├── FiliereDao.java           findByCode, findByDepartement
├── SalleDao.java             findByDepartement
├── EncadreurDao.java         findByEmail, findByUtilisateurId, findByDepartement
├── EtudiantDao.java          findByMatricule, findByFiliere, findByUtilisateurId, findByEncadreur
├── MemoireDao.java           findByEncadreur, findByTitre, findSansSoutenance
├── SoutenanceDao.java        findByStatut, findBySalle, findByEncadreur, findAVenir
├── EvaluationDao.java        ★ DAO de la relation N-N Soutenance × Encadreur
└── HistoriqueActionDao.java  findDernieres, findByUtilisateur
```

Règle : **un DAO par entité**. Les 11 entités → 11 DAOs maximum, `BaseDao` mis à part.
Les classes qui orchestrent plusieurs DAOs vont dans `service/`, pas ici.

---

## 5. Ce que contient chaque DAO

Pour chaque DAO : les méthodes héritées de `BaseDao` + les requêtes spécifiques imposées par
les écrans et les règles métier.

| DAO | Méthodes spécifiques | Écran appelant |
|---|---|---|
| `UtilisateurDao` | `findByLogin(String)` | `/login` (authentification) |
| | `findByRole(RoleUtilisateur)` | `/admin/utilisateurs` |
| `GradeDao` | `findByCode(String)` | formulaire grade (contrôle d'unicité) |
| `DepartementDao` | `findByCode(String)` | formulaire département |
| `FiliereDao` | `findByCode(String)`, `findByDepartement(UUID)` | formulaires filière / étudiant |
| `SalleDao` | `findByDepartement(UUID)` | planification de soutenance |
| `EncadreurDao` | `findByEmail(String)`, `findByUtilisateurId(UUID)` | formulaire encadreur, session |
| `EtudiantDao` | `findByMatricule(String)`, `findByFiliere(UUID)`, `findByUtilisateurId(UUID)`, `findByEncadreur(UUID)` | listes admin, `/etudiant/*`, `/encadreur/mes-etudiants` |
| `MemoireDao` | `findByEncadreur(UUID)`, `findSansSoutenance()` | `/admin/memoires`, planification |
| `SoutenanceDao` | `findByStatut(StatutSoutenance)`, `findBySalle(...)`, `findByEncadreur(UUID)`, `findByDateBetween(...)` | listes, jurys, conflits de salle |
| `EvaluationDao` | voir §6 | évaluation, dashboards, étudiant |
| `HistoriqueActionDao` | `findDernieres(int)`, `findByUtilisateur(UUID)` | `/admin/historique`, dashboards |

### Exemple complet : `GradeDao`

```java
public class GradeDao extends BaseDao<Grade> {

    public GradeDao(EntityManager em) {
        super(em);
    }

    @Override
    protected Class<Grade> getEntityClass() {
        return Grade.class;
    }

    // uk_grade_code existe en base (Grade.java:11) : on vérifie avant d'insérer
    public Grade findByCode(String code) {
        return em.createQuery("SELECT g FROM Grade g WHERE g.code = :code", Grade.class)
                 .setParameter("code", code)
                 .getResultStream()
                 .findFirst()
                 .orElse(null);
    }
}
```

### Exemple : `UtilisateurDao` (base de l'authentification)

```java
public Utilisateur findByLogin(String login) {
    return em.createQuery("SELECT u FROM Utilisateur u WHERE u.login = :login", Utilisateur.class)
             .setParameter("login", login)
             .getResultStream()
             .findFirst()
             .orElse(null);
}
```

### Exemple : `SoutenanceDao` (règle « un membre du jury ne voit que ses soutenances »)

Le jury n'est pas une table : c'est l'ensemble des `Evaluation` rattachées à la soutenance.

```java
public List<Soutenance> findByEncadreur(UUID encadreurId) {
    return em.createQuery(
            "SELECT DISTINCT s FROM Soutenance s WHERE EXISTS " +
            "(SELECT ev FROM Evaluation ev WHERE ev.soutenance = s AND ev.encadreur.id = :eid)",
            Soutenance.class)
            .setParameter("eid", encadreurId)
            .getResultList();
}
```

### Exemple : `EtudiantDao` (règle « un étudiant ne voit que ses données »)

```java
public Optional<Etudiant> findByUtilisateurId(UUID utilisateurId) { ... :uid ... }

// Etudiant.memoire est nullable (Etudiant.java:38) → LEFT JOIN obligatoire
public List<Etudiant> findByEncadreur(UUID encadreurId) {
    return em.createQuery(
            "SELECT e FROM Etudiant e LEFT JOIN e.memoire m WHERE m.encadreur.id = :eid",
            Etudiant.class)
            .setParameter("eid", encadreurId)
            .getResultList();
}
```

### Exemple : `MemoireDao` (alimenter le select de planification)

```java
public List<Memoire> findSansSoutenance() {
    return em.createQuery(
            "SELECT m FROM Memoire m WHERE NOT EXISTS " +
            "(SELECT s FROM Soutenance s WHERE s.memoire = m)",
            Memoire.class)
            .getResultList();
}
```

---

## 6. `EvaluationDao` — le DAO le plus important du projet

`Soutenance` × `Encadreur` est une relation N-N **portée par l'entité `Evaluation`**
(`src/main/java/ci/univ/soutenances/model/Evaluation.java`), avec contrainte unique
`uk_evaluation_soutenance_encadreur`. Le cahier des charges exige un DAO dédié pour cette
relation : c'est celui-ci.

Quatre requêtes suffisent à faire fonctionner la règle métier
« `EVALUEE` seulement quand tout le jury a noté, note finale = moyenne » :

```java
public class EvaluationDao extends BaseDao<Evaluation> {

    // 1. Membres du jury d'une soutenance
    public List<Encadreur> findEncadreursBySoutenance(UUID soutenanceId) {
        return em.createQuery(
                "SELECT ev.encadreur FROM Evaluation ev WHERE ev.soutenance.id = :sid",
                Encadreur.class)
                .setParameter("sid", soutenanceId)
                .getResultList();
    }

    // 2. Combien de membres ont déjà soumis une note
    public long countBySoutenance(UUID soutenanceId) {
        return em.createQuery(
                "SELECT COUNT(ev) FROM Evaluation ev WHERE ev.soutenance.id = :sid",
                Long.class)
                .setParameter("sid", soutenanceId)
                .getSingleResult();
    }

    // 3. Moyenne du jury — peut renvoyer null si aucune note
    public BigDecimal moyenneBySoutenance(UUID soutenanceId) {
        return em.createQuery(
                "SELECT AVG(ev.note) FROM Evaluation ev WHERE ev.soutenance.id = :sid",
                BigDecimal.class)
                .setParameter("sid", soutenanceId)
                .getSingleResult();
    }

    // 4. Contrôle avant insertion (uk_evaluation_soutenance_encadreur)
    public boolean exists(UUID soutenanceId, UUID encadreurId) {
        Long n = em.createQuery(
                "SELECT COUNT(ev) FROM Evaluation ev " +
                "WHERE ev.soutenance.id = :sid AND ev.encadreur.id = :eid",
                Long.class)
                .setParameter("sid", soutenanceId)
                .setParameter("eid", encadreurId)
                .getSingleResult();
        return n != null && n > 0;
    }
}
```

À savoir pour l'oral :

- `AVG()` sur un `BigDecimal precision(4) scale(2)` rend un `BigDecimal` sans arrondi défini
  (`14.3333333...`). L'affichage doit faire un `setScale(2, RoundingMode.HALF_UP)`, et le
  résultat peut être `null` si la soutenance n'a aucune note — à tester avant d'appeler
  `setScale`, sinon `NullPointerException`.
- La contrainte unique signifie qu'un encadreur ne peut pas noter deux fois la même
  soutenance. `exists(...)` sert à afficher un message métier propre au lieu de laisser
  remonter une `PersistenceException` illisible.

---

## 7. Requêtes JPQL : règles de rédaction

**Autorisé** — paramètres nommés :

```java
"SELECT g FROM Grade g WHERE g.code = :code"    .setParameter("code", code)
"SELECT m FROM Memoire m WHERE m.titre LIKE :mot"  .setParameter("mot", "%" + mot + "%")
```

**Interdit** — concaténation d'une valeur saisie :

```java
"SELECT g FROM Grade g WHERE g.code = '" + code + "'"   // injection JPQL → refusé en PR
String.format("... WHERE g.code = '%s'", code)          // idem
```

La seule concaténation tolérée est celle du **nom d'entité** dans `BaseDao` (valeur connue du
développeur, pas donnée utilisateur).

Détails à respecter :

- Le JPQL utilise les **noms d'entités et d'attributs Java**, pas les noms de tables SQL.
  On écrit `FROM Evaluation ev`, pas `FROM Evaluation` pensé comme la table — et surtout pas
  `FROM evaluation`. Les colonnes `snake_case` et les tables PascalCase sont gérées par
  Hibernate via les annotations du modèle, jamais dans la requête.
- Les attributs des relations se parcourent par point : `ev.soutenance.id`, `m.encadreur.id`.
- Toujours typer la requête : `createQuery(jpql, Grade.class)`.

---

## 8. Piège `LAZY` + JSP : les `JOIN FETCH`

Toutes les associations du modèle sont en `FetchType.LAZY` (règle du projet). Conséquence :
`${salle.departement.nom}` dans une JSP déclenche un chargement différé. Si la transaction JTA
est fermée à ce moment-là, on obtient un `LazyInitializationException`.

Deux règles :

1. Toute méthode de DAO qui alimente un **écran de liste** charge les relations nécessaires
   à la JSP :

```java
public List<Salle> findAllAvecDepartement() {
    return em.createQuery(
            "SELECT s FROM Salle s LEFT JOIN FETCH s.departement ORDER BY s.nomSalle",
            Salle.class)
            .getResultList();
}
```

2. Les méthodes héritées de `BaseDao` restent « nues » : elles servent aux cas simples
   (formulaire d'édition, contrôle d'existence). Pour un écran, on ajoute une méthode dédiée
   avec `FETCH` dans le DAO concerné.

---

## 9. Frontière entre DAO et `service/`

- **DAO** : une méthode = un écran a besoin d'une information = une requête. Le DAO ne décide
  rien, il ne fait aucun `if` métier.
- **Service** : orchestre plusieurs DAOs et porte les règles. Dans ce projet, trois cas
  justifient un service :

| Service | Rôle | Pourquoi pas dans un DAO |
|---|---|---|
| `AuthService` | vérifier login + comparaison du hash SHA-256, ouvrir la session | combine `UtilisateurDao`, `EncadreurDao`/`EtudiantDao` et le hashage |
| `SoutenanceService` | planifier : vérifier salle libre, mémoire non déjà soutenue, jury ≥ 2 membres, écrire l'`HistoriqueAction` | enchaîne 4 DAOs et des validations |
| `EvaluationService` | refuser un doublon, insérer l'`Evaluation`, recalculer la moyenne, basculer le statut en `EVALUEE` | logique de la règle métier centrale |

- Le hashage SHA-256 (`PasswordHasher`) est une classe utilitaire pure, appelée par les
  services. Jamais depuis une JSP, jamais depuis une servlet directement.

---

## 10. Avant de proposer une PR touchant `dao/`

Cocher mentalement :

- [ ] Aucun `em.createQuery` hors du package `dao`
- [ ] Aucune concaténation de valeur utilisateur dans une chaîne JPQL (`:param` uniquement)
- [ ] Pas de `em.getTransaction()` (interdit en JTA)
- [ ] Les relations nécessaires à la JSP sont chargées par `JOIN FETCH`
- [ ] Les champs sous contrainte `UNIQUE` ont une méthode de contrôle (`findByCode`, `findByEmail`, `findByMatricule`, `exists`)
- [ ] `EvaluationDao` porte les 4 requêtes du §6
- [ ] `AVG(...)` protégé contre le `null` et arrondi à l'affichage
- [ ] Le DAO ne contient ni HTML, ni `HttpServletRequest`, ni règle métier conditionnelle
- [ ] Le nom des méthodes est en français métier (`findByCode`, `moyenneBySoutenance`), pas en anglais technique

---

## 11. Ordre d'écriture proposé

1. `BaseDao.java`
2. `UtilisateurDao` + `AuthService` (bloque tout le reste : session et rôles)
3. `GradeDao` puis l'écran `/admin/grades` complet → valide la chaîne Servlet → DAO → JSP de bout en bout
4. `DepartementDao`, `FiliereDao`, `SalleDao`
5. `EncadreurDao`, `EtudiantDao`
6. `MemoireDao`, `SoutenanceDao`, `EvaluationDao` + `SoutenanceService` / `EvaluationService`
7. `HistoriqueActionDao` et branchement des dashboards
