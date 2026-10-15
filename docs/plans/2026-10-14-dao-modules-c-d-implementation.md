# Couche DAO — modules C et D (Administrateur) : plan d'implémentation TDD (partie 1)

> **Mode d'emploi :** ce plan s'exécute tâche par tâche, dans l'ordre. Chaque étape est une
> case à cocher (`- [ ]`) pour suivre l'avancement ; on ne passe à l'étape suivante que sur une
> étape validée. Ce fichier contient la **partie 1 sur 3** (tâches 1 à 3) : le socle de tests,
> `BaseDao` et `SalleDao`. Les tâches 4 et suivantes seront appendues à la suite du même fichier
> par les parties 2 et 3.

**Objectif :** mettre en place l'infrastructure de tests du projet et livrer les deux premiers
DAO de `ci.univ.soutenances.dao` — `BaseDao` (CRUD générique) et `SalleDao` (lookup du select de
planification) — en TDD, jusqu'à un `mvn test` vert, sans toucher au code produit en dehors de
`dao/`.

**Architecture :** l'unité de production `GestionSoutenancesPU` (JTA, datasource MySQL) reste
intacte ; les tests s'appuient sur une seconde unité de persistance `GestionSoutenancesTestPU`,
`transaction-type="RESOURCE_LOCAL"`, sur H2 en mémoire, dont Hibernate génère le schéma à partir
des 11 entités réelles (`create-drop`, aucun script DDL). Une classe de base `DaoTestBase` boote
l'`EntityManagerFactory` une fois par classe de test, fournit un `EntityManager` par test et
ouvre/valide/annule les transactions de test — les DAOs de production n'en savent rien. Chaque
DAO s'écrit en TDD : son test est écrit et vu échouer avant l'implémentation.

**Stack technique :** Java 17 · Maven WAR · Jakarta EE 10 / JPA 3.1 · WildFly 37 + MySQL
(production) · JUnit Jupiter (tests) · Hibernate ORM (provider JPA, **scope `test` uniquement** —
WildFly le fournit en production) · H2 (base de test en mémoire, scope `test`).

---

## Vérifications et limites communes

- **Commande de vérification :** `mvn test`, lancée depuis la racine du dépôt. Ce dépôt n'a pas
  de wrapper Maven — il n'existe pas de `mvnw`, seul `mvn` fait foi. **Chaque tâche se termine
  sur un `mvn test` vert** (`BUILD SUCCESS`, `Failures: 0`, `Errors: 0`). En cas d'échec, on
  répare avant de continuer.
- **Limite connue et acceptée** (design §6.4) : H2 n'est pas MySQL. Une requête validée sous H2
  peut encore avoir besoin d'être confirmée au déploiement sur WildFly + MySQL. Le test valide la
  logique JPQL, la forme du DAO et les cas vides / null — pas la parfaite équivalence des deux
  bases.
- La checklist `DAO.md` §10 s'applique à toute PR touchant `dao/`. Rappel des points qui
  concernent déjà cette partie : aucun `em.getTransaction()` dans le code produit (les tests,
  eux, en ont le droit via l'unité `RESOURCE_LOCAL`), requêtes toujours typées, `JOIN FETCH`
  pour tout ce qu'un écran affiche.
- **Périmètre strict de cette partie :** `pom.xml`, `src/test/` et `src/main/java/ci/univ/soutenances/dao/`.
  Ni servlet, ni JSP, ni CSS, ni validation métier, ni écriture d'`HistoriqueAction` (design §3,
  décision 5).

---

## Tâche 1 — Infrastructure de test : pom.xml, unité de persistance de test, classe de base

**Files:**
- Modify : `pom.xml`
- Create : `src/test/resources/META-INF/persistence.xml`
- Create : `src/test/java/ci/univ/soutenances/dao/DaoTestBase.java`

Rappel de l'état de départ vérifié dans le dépôt : le `pom.xml` contient exactement trois
dépendances, toutes `provided` — `jakarta.servlet-api` 6.0.0, `jakarta.persistence-api` 3.1.0,
`mysql-connector-j` 8.4.0. Aucun dossier `src/test/` n'existe. Le provider JPA est totalement
absent du projet : c'est WildFly qui le fournit à l'exécution, donc il doit être ajouté en
`scope test` pour que les tests aient un provider.

- [ ] **Étape 1.1 — Modifier `pom.xml`**

Ajouter exactement quatre choses, rien d'autre : trois dépendances en `<scope>test</scope>`
(`org.junit.jupiter:junit-jupiter`, `org.hibernate.orm:hibernate-core`, `com.h2database:h2`) et
le plugin `maven-surefire-plugin` en 3.x (le surefire embarqué par défaut de Maven n'exécute pas
JUnit 5). Les trois dépendances `provided` existantes restent inchangées.

Remplacer le contenu de `pom.xml` par le fichier complet suivant. **Quatre balises `<version>`
portent encore les tranches proposées par le design §6.1 (`5.x`, `6.x`, `2.x`, `3.x`) avec un
commentaire `À CONFIRMER` : les numéros exacts se figurent à l'étape 1.2. Tout le reste se copie
tel quel.**

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>ci.univ.soutenances</groupId>
  <artifactId>gestion-soutenances</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <packaging>war</packaging>
  <name>Gestion Soutenances</name>
  <description>Système de gestion des soutenances et des mémoires - M1 MIAGE</description>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.release>17</maven.compiler.release>
  </properties>

  <dependencies>
    <!-- existant, inchangé -->
    <dependency>
      <groupId>jakarta.servlet</groupId>
      <artifactId>jakarta.servlet-api</artifactId>
      <version>6.0.0</version>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>jakarta.persistence</groupId>
      <artifactId>jakarta.persistence-api</artifactId>
      <version>3.1.0</version>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <version>8.4.0</version>
      <scope>provided</scope>
    </dependency>

    <!-- ajout 1 : JUnit 5, moteur d'exécution des tests -->
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>5.x</version> <!-- À CONFIRMER : numéro exact, étape 1.2 -->
      <scope>test</scope>
    </dependency>
    <!-- ajout 2 : provider JPA pour les tests (WildFly le fournit en production,
         il est donc totalement absent du projet aujourd'hui) -->
    <dependency>
      <groupId>org.hibernate.orm</groupId>
      <artifactId>hibernate-core</artifactId>
      <version>6.x</version> <!-- À CONFIRMER : numéro exact, étape 1.2 -->
      <scope>test</scope>
    </dependency>
    <!-- ajout 3 : base de test en mémoire, remplacée par MySQL en production -->
    <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId>
      <version>2.x</version> <!-- À CONFIRMER : numéro exact, étape 1.2 -->
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.13.0</version>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-war-plugin</artifactId>
        <version>3.4.0</version>
        <configuration>
          <failOnMissingWebXml>false</failOnMissingWebXml>
        </configuration>
      </plugin>
      <!-- ajout 4 : surefire 3.x, indispensable pour exécuter JUnit 5 -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.x</version> <!-- À CONFIRMER : numéro exact, étape 1.2 -->
      </plugin>
    </plugins>
  </build>
</project>
```

- [ ] **Étape 1.2 — Fixer les quatre versions de test (seul endroit du plan où elles se choisissent)**

Les numéros exacts n'ont **pas** été figés à la conception : le design §6.1 ne donne que des
tranches — `junit-jupiter` 5.x, `hibernate-core` 6.x, `h2` 2.x, `maven-surefire-plugin` 3.x.
Cette étape est le seul endroit du plan où ils se choisissent ; aucun autre endroit n'en
mentionne.

- [ ] Sur `https://mvnrepository.com` (ou `https://search.maven.org`), relever pour chaque
      artefact le numéro exact le plus récent **dans sa tranche** :
      `org.junit.jupiter:junit-jupiter` (5.x), `org.hibernate.orm:hibernate-core` (6.x),
      `com.h2database:h2` (2.x), `org.apache.maven.plugins:maven-surefire-plugin` (3.x).
- [ ] Pour `hibernate-core`, préférer une version 6.x proche de celle embarquée par WildFly 37
      (design §6.1 : à vérifier dans les modules du serveur si elle est connue) afin que les
      tests parlent à peu près la même version de Hibernate que la production.
- [ ] Remplacer `5.x`, `6.x`, `2.x`, `3.x` dans le `pom.xml` par ces quatre numéros exacts, et
      effacer les quatre commentaires `<!-- À CONFIRMER ... -->`.
- [ ] La confirmation se fait au premier `mvn test` (étape 1.5) : `BUILD SUCCESS` signale que
      les quatre versions existent et fonctionnent ensemble ; un message
      `Could not find artifact` signale un numéro inexistant → en reprendre un autre dans la
      même tranche et relancer.

- [ ] **Étape 1.3 — Créer `src/test/resources/META-INF/persistence.xml`**

Fichier complet, prêt à l'emploi, à côté (jamais à la place) de l'unité de production
`src/main/resources/META-INF/persistence.xml`. Nom distinct `GestionSoutenancesTestPU`,
`transaction-type="RESOURCE_LOCAL"` (dans les tests seulement, `em.getTransaction()` est donc
disponible — jamais dans le code produit). Les 11 entités sont listées explicitement pour ne pas
dépendre de la détection par scan du classpath. `create-drop` : Hibernate génère le schéma à
partir des **vraies** entités du projet, aucun script DDL à écrire.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence https://jakarta.ee/xml/ns/persistence/persistence_3_1.xsd"
             version="3.1">
    <persistence-unit name="GestionSoutenancesTestPU" transaction-type="RESOURCE_LOCAL">
        <exclude-unlisted-classes/>
        <class>ci.univ.soutenances.model.Utilisateur</class>
        <class>ci.univ.soutenances.model.Departement</class>
        <class>ci.univ.soutenances.model.Grade</class>
        <class>ci.univ.soutenances.model.Filiere</class>
        <class>ci.univ.soutenances.model.Salle</class>
        <class>ci.univ.soutenances.model.Encadreur</class>
        <class>ci.univ.soutenances.model.Memoire</class>
        <class>ci.univ.soutenances.model.Etudiant</class>
        <class>ci.univ.soutenances.model.Soutenance</class>
        <class>ci.univ.soutenances.model.Evaluation</class>
        <class>ci.univ.soutenances.model.HistoriqueAction</class>
        <properties>
            <property name="jakarta.persistence.jdbc.driver" value="org.h2.Driver"/>
            <!-- DB_CLOSE_DELAY=-1 : la base vit aussi longtemps que le JVM de surefire ;
                 create-drop recrée un schéma vierge à chaque EntityManagerFactory -->
            <property name="jakarta.persistence.jdbc.url" value="jdbc:h2:mem:soutenances;DB_CLOSE_DELAY=-1"/>
            <property name="jakarta.persistence.jdbc.user" value="sa"/>
            <property name="jakarta.persistence.jdbc.password" value=""/>
            <property name="hibernate.hbm2ddl.auto" value="create-drop"/>
            <property name="hibernate.show_sql" value="false"/>
            <property name="hibernate.format_sql" value="true"/>
        </properties>
    </persistence-unit>
</persistence>
```

Remarque : l'unité de production `GestionSoutenancesPU` reste également visible sur le classpath
de test (deux `persistence.xml`, deux noms d'unités) — elle ne sera jamais démarrée, les tests
ne demandent que `GestionSoutenancesTestPU`.

- [ ] **Étape 1.4 — Créer `src/test/java/ci/univ/soutenances/dao/DaoTestBase.java`**

Classe de base que tous les tests de DAO étendront : une `EntityManagerFactory` par classe de
test, un `EntityManager` par test, transactions ouvertes/validées/annulées par des helpers. Le
champ `emf` est statique : partagé par toutes les classes de test du même JVM, on ne le recrée
que s'il est fermé (après le `@AfterAll` de la classe de test précédente).

```java
package ci.univ.soutenances.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public abstract class DaoTestBase {

    private static EntityManagerFactory emf;

    protected EntityManager em;

    @BeforeAll
    static void creerEntityManagerFactory() {
        if (emf == null || !emf.isOpen()) {
            emf = Persistence.createEntityManagerFactory("GestionSoutenancesTestPU");
        }
    }

    @AfterAll
    static void fermerEntityManagerFactory() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @BeforeEach
    void ouvrirEntityManager() {
        em = emf.createEntityManager();
    }

    @AfterEach
    void fermerEntityManager() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

    // Les trois helpers ci-dessous ne fonctionnent que sous l'unité RESOURCE_LOCAL
    // des tests : em.getTransaction() est interdit dans le code produit (DAO.md §10).
    protected void ouvrirTransaction() {
        em.getTransaction().begin();
    }

    protected void validerTransaction() {
        em.getTransaction().commit();
    }

    protected void annulerTransaction() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }
}
```

- [ ] **Étape 1.5 — Vérifier : `mvn test` réussit avec zéro test exécuté**

Run : `mvn test`
Expected : `BUILD SUCCESS`. Surefire ne trouve aucune classe de test encore
(`DaoTestBase` est abstraite et ne porte aucun `@Test`) : le journal affiche `No tests to run`
(ou `Tests run: 0`). Cela prouve que le socle est en place **avant qu'un seul DAO n'existe** :
les quatre versions résolvent, le classpath de test embarque JUnit, Hibernate et H2,
`DaoTestBase` compile, et surefire 3.x s'exécute.
Dépannage : `Could not find artifact` → un des quatre numéros de l'étape 1.2 n'existe pas :
en reprendre un existant dans la même tranche, puis relancer.
Si la résolution de la version `hibernate-core` échoue côté classes manquantes du modèle de
persistance, revenir à l'étape 1.3 et vérifier que les 11 éléments `<class>` sont bien présents.

- [ ] **Étape 1.6 — Commit**

```bash
git add pom.xml src/test/
git commit -m "test: infrastructure de tests (JUnit 5, Hibernate, H2) et unité de persistance de test"
```

---

## Tâche 2 — BaseDao : le CRUD générique (TDD : test d'abord)

**Files:**
- Test : `src/test/java/ci/univ/soutenances/dao/BaseDaoTest.java`
- Create : `src/test/java/ci/univ/soutenances/model/FabriqueEntites.java`
- Create : `src/main/java/ci/univ/soutenances/dao/BaseDao.java`

La forme de `BaseDao` est fixée par `DAO.md` §3 : générique sur `T extends BaseEntity`,
champ `protected EntityManager em` rempli par le constructeur, méthode abstraite
`getEntityClass()` (pas de réflexion), `findById` / `findAll` / `create` / `update` / `delete` /
`count()` protégé. On écrit le test **avant** l'implémentation.

- [ ] **Étape 2.1 — Créer `src/test/java/ci/univ/soutenances/model/FabriqueEntites.java`**

Cette fabrique existe parce que le constructeur sans argument de chaque entité est
`protected` (convention JPA du projet) : un test du package `ci.univ.soutenances.dao` ne peut
pas appeler `new Grade()` directement. Placée dans le même package `model` que les entités,
c'est la seule porte d'entrée légale. Elle grandira avec les besoins de jeu de test des tâches
suivantes. Pour cette tâche, elle n'a besoin que de `Grade`.

```java
package ci.univ.soutenances.model;

public final class FabriqueEntites {

    private FabriqueEntites() {
    }

    public static Grade nouveauGrade(String code, String libelle) {
        Grade grade = new Grade();
        grade.setCode(code);
        grade.setLibelle(libelle);
        return grade;
    }
}
```

- [ ] **Étape 2.2 — Écrire le test échouant : `src/test/java/ci/univ/soutenances/dao/BaseDaoTest.java`**

Le test exerce le CRUD générique sur une sous-classe jetable de `BaseDao` portant la **vraie**
entité `Grade`. Choix justifié en une ligne : aucun DAO concret de production n'existe à ce
stade (`SalleDao` n'arrive qu'à la tâche 3) et `DAO.md` §6.3 interdit les fausses entités de
test — `Grade` est une entité plate, sans relation, idéale pour le CRUD. `count()` est
`protected` mais accessible ici : le test est dans le même package `ci.univ.soutenances.dao`
que `BaseDao`. Le nettoyage en masse est du code de test, pas de la production.

```java
package ci.univ.soutenances.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;

import ci.univ.soutenances.model.FabriqueEntites;
import ci.univ.soutenances.model.Grade;

class BaseDaoTest extends DaoTestBase {

    // Sous-classe jetable : à la tâche 2 aucun DAO concret de production n'existe encore.
    private static class GradeDaoDeTest extends BaseDao<Grade> {

        GradeDaoDeTest(EntityManager em) {
            super(em);
        }

        @Override
        protected Class<Grade> getEntityClass() {
            return Grade.class;
        }
    }

    @AfterEach
    void nettoyerLesGrades() {
        annulerTransaction();
        ouvrirTransaction();
        em.createQuery("DELETE FROM Grade").executeUpdate();
        validerTransaction();
    }

    @Test
    void createPuisFindByIdRenvoieLeMemeGrade() {
        GradeDaoDeTest dao = new GradeDaoDeTest(em);

        ouvrirTransaction();
        Grade grade = FabriqueEntites.nouveauGrade("M2", "Master 2");
        dao.create(grade);
        validerTransaction();

        assertNotNull(grade.getId());
        Grade relu = dao.findById(grade.getId());
        assertNotNull(relu);
        assertEquals("M2", relu.getCode());
        assertEquals("Master 2", relu.getLibelle());
    }

    @Test
    void findAllRenvoieLesGradesCrees() {
        GradeDaoDeTest dao = new GradeDaoDeTest(em);

        ouvrirTransaction();
        dao.create(FabriqueEntites.nouveauGrade("M1", "Master 1"));
        dao.create(FabriqueEntites.nouveauGrade("M2", "Master 2"));
        validerTransaction();

        List<Grade> grades = dao.findAll();
        assertEquals(2, grades.size());
    }

    @Test
    void updateModifieLeLibelle() {
        GradeDaoDeTest dao = new GradeDaoDeTest(em);

        ouvrirTransaction();
        Grade grade = FabriqueEntites.nouveauGrade("M1", "Master 1");
        dao.create(grade);
        validerTransaction();

        ouvrirTransaction();
        grade.setLibelle("Master 1 modifié");
        dao.update(grade);
        validerTransaction();

        Grade relu = dao.findById(grade.getId());
        assertEquals("Master 1 modifié", relu.getLibelle());
    }

    @Test
    void deleteRetireLeGrade() {
        GradeDaoDeTest dao = new GradeDaoDeTest(em);

        ouvrirTransaction();
        Grade grade = FabriqueEntites.nouveauGrade("M1", "Master 1");
        dao.create(grade);
        validerTransaction();

        ouvrirTransaction();
        dao.delete(grade);
        validerTransaction();

        assertNull(dao.findById(grade.getId()));
    }

    @Test
    void countCompteLesGrades() {
        GradeDaoDeTest dao = new GradeDaoDeTest(em);

        assertEquals(0, dao.count());

        ouvrirTransaction();
        dao.create(FabriqueEntites.nouveauGrade("M1", "Master 1"));
        validerTransaction();

        assertEquals(1, dao.count());
    }
}
```

- [ ] **Étape 2.3 — Lancer le test pour le voir échouer (rouge)**

Run : `mvn test`
Expected : `BUILD FAILURE` — échec de compilation du module de test avec
`cannot find symbol` sur `BaseDao` dans `BaseDaoTest.java` (le compilateur ne trouve ni la
classe `BaseDao`, qui n'existe pas encore). C'est exactement l'échec attendu : le test est
écrit, l'implémentation manque.

- [ ] **Étape 2.4 — Créer `src/main/java/ci/univ/soutenances/dao/BaseDao.java`**

Implémentation **exacte** de `DAO.md` §3, avec imports complets. La seule concaténation de
chaîne est le nom de la classe d'entité (`getEntityClass().getSimpleName()`), valeur connue du
développeur — autorisée par `DAO.md` §7 ; aucune valeur utilisateur n'entre jamais dans une
requête.

```java
package ci.univ.soutenances.dao;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import ci.univ.soutenances.model.BaseEntity;

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

Points à pouvoir expliquer à l'oral (DAO.md §3) : `ORDER BY x.id` fonctionne parce que
l'identifiant est déclaré dans `BaseEntity`, hérité par toutes les entités ; `getEntityClass()`
est une méthode abstraite et pas de la réflexion, chaque sous-classe écrit simplement
`return Grade.class;`.

- [ ] **Étape 2.5 — Lancer le test pour le voir passer (vert)**

Run : `mvn test`
Expected : `BUILD SUCCESS` avec toute la suite verte :
`Tests run: 5, Failures: 0, Errors: 0, Skipped: 0` — les 5 tests de `BaseDaoTest`, seule classe
de test du projet à ce stade. Si un test échoue, relire l'implémentation ligne à ligne par
rapport au bloc ci-dessus avant tout autre changement.

- [ ] **Étape 2.6 — Commit**

```bash
git add src/main/java/ci/univ/soutenances/dao/BaseDao.java src/test/java/ci/univ/soutenances/dao/BaseDaoTest.java src/test/java/ci/univ/soutenances/model/FabriqueEntites.java
git commit -m "feat: BaseDao (CRUD générique) et BaseDaoTest"
```

> **Contrainte dure à retenir (design §4 et §9) :** tant que l'équipe n'a pas tranché la
> stratégie de transactions JTA (`UserTransaction` en A, CDI + `@Transactional` en B),
> **aucune servlet ne doit appeler `new XxxDao(em)`**. Rien dans cette partie n'en dépend :
> les tests tournent sur l'unité `RESOURCE_LOCAL`, seuls le remplissage du champ `em` et la
> couche servlet changeront après la décision.

---

## Tâche 3 — SalleDao : le lookup du select de planification (TDD : test d'abord)

**Files:**
- Test : `src/test/java/ci/univ/soutenances/dao/SalleDaoTest.java`
- Modify : `src/test/java/ci/univ/soutenances/model/FabriqueEntites.java`
- Create : `src/main/java/ci/univ/soutenances/dao/SalleDao.java`

`SalleDao` est un DAO de lecture seule (design §5) : une seule méthode,
`findAllAvecDepartement()`, qui alimente le select de salles du formulaire
`/admin/planification` (`soutenance-form.jsp`). Elle charge le département par `JOIN FETCH`
**exactement comme dans `DAO.md` §8** — sans cela, `${salle.departement.nom}` dans la JSP
lèverait une `LazyInitializationException` (risque 2 du design §7). On garde la forme
`LEFT JOIN FETCH` du document de référence ; le `departement` étant obligatoire côté entité
(`optional = false`), une forme `JOIN FETCH` intérieur reviendrait au même. Attributs lus dans
les sources : `Salle.nomSalle`, `Salle.capacite` (primitif `int`, **jamais `null`**),
`Salle.departement` ; `Departement.code`, `Departement.nom`.

- [ ] **Étape 3.1 — Étendre `src/test/java/ci/univ/soutenances/model/FabriqueEntites.java`**

Ajouter la création de `Departement` et `Salle` (toujours sans import : mêmes packages). Fichier
complet après modification :

```java
package ci.univ.soutenances.model;

public final class FabriqueEntites {

    private FabriqueEntites() {
    }

    public static Grade nouveauGrade(String code, String libelle) {
        Grade grade = new Grade();
        grade.setCode(code);
        grade.setLibelle(libelle);
        return grade;
    }

    public static Departement nouveauDepartement(String code, String nom) {
        Departement departement = new Departement();
        departement.setCode(code);
        departement.setNom(nom);
        return departement;
    }

    public static Salle nouvelleSalle(String nom, int capacite, Departement departement) {
        Salle salle = new Salle();
        salle.setNomSalle(nom);
        salle.setCapacite(capacite);
        salle.setDepartement(departement);
        return salle;
    }
}
```

`capacite` est un `int` primitif (lu dans `Salle.java`) : la fabrique le prend en `int`, sans
possible `null`.

- [ ] **Étape 3.2 — Écrire le test échouant : `src/test/java/ci/univ/soutenances/dao/SalleDaoTest.java`**

Trois tests : le jeu semé renvoyé trié par `nomSalle` ; le cas vide ; et le test qui prouve le
`JOIN FETCH` — après fermeture de la transaction, `em.clear()` détache **tout** : sans
`JOIN FETCH`, `getDepartement()` renverrait un proxy mort et lèverait une
`LazyInitializationException` ; c'est exactement ce que le test doit vérifier (risque 2). Le
nettoyage supprime d'abord les salles puis les départements (clé étrangère `departement_id`).

```java
package ci.univ.soutenances.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ci.univ.soutenances.model.Departement;
import ci.univ.soutenances.model.FabriqueEntites;
import ci.univ.soutenances.model.Salle;

class SalleDaoTest extends DaoTestBase {

    private SalleDao dao;

    @BeforeEach
    void creerDao() {
        dao = new SalleDao(em);
    }

    @AfterEach
    void nettoyerLesSallesEtDepartements() {
        annulerTransaction();
        ouvrirTransaction();
        em.createQuery("DELETE FROM Salle").executeUpdate();
        em.createQuery("DELETE FROM Departement").executeUpdate();
        validerTransaction();
    }

    @Test
    void findAllAvecDepartementRenvoieLesSallesTrieesParNom() {
        ouvrirTransaction();
        Departement info = FabriqueEntites.nouveauDepartement("INFO", "Informatique");
        em.persist(info);
        em.persist(FabriqueEntites.nouvelleSalle("Amphi B", 120, info));
        em.persist(FabriqueEntites.nouvelleSalle("Amphi A", 80, info));
        em.persist(FabriqueEntites.nouvelleSalle("Salle C01", 30, info));
        validerTransaction();

        ouvrirTransaction();
        List<Salle> salles = dao.findAllAvecDepartement();
        validerTransaction();

        assertEquals(3, salles.size());
        assertEquals("Amphi A", salles.get(0).getNomSalle());
        assertEquals("Amphi B", salles.get(1).getNomSalle());
        assertEquals("Salle C01", salles.get(2).getNomSalle());
    }

    @Test
    void findAllAvecDepartementLisLeDepartementApresFermetureDeLaTransaction() {
        ouvrirTransaction();
        Departement info = FabriqueEntites.nouveauDepartement("INFO", "Informatique");
        em.persist(info);
        em.persist(FabriqueEntites.nouvelleSalle("Salle C01", 30, info));
        validerTransaction();

        ouvrirTransaction();
        List<Salle> salles = dao.findAllAvecDepartement();
        validerTransaction();

        // Détache tout : sans JOIN FETCH dans la requête, getDepartement() ci-dessous
        // renverrait un proxy mort et lèverait LazyInitializationException.
        em.clear();

        assertEquals(1, salles.size());
        assertEquals("Salle C01", salles.get(0).getNomSalle());
        assertEquals(30, salles.get(0).getCapacite());
        assertEquals("Informatique", salles.get(0).getDepartement().getNom());
        assertEquals("INFO", salles.get(0).getDepartement().getCode());
    }

    @Test
    void findAllAvecDepartementRenvoieUneListeVide() {
        ouvrirTransaction();
        List<Salle> salles = dao.findAllAvecDepartement();
        validerTransaction();

        assertTrue(salles.isEmpty());
    }
}
```

Chaque test démarre sur des tables vides : le `create-drop` crée un schéma vierge au démarrage
de l'`EntityManagerFactory` de la classe, et le `@AfterEach` remet les tables à zéro après
chaque test.

- [ ] **Étape 3.3 — Lancer le test pour le voir échouer (rouge)**

Run : `mvn test`
Expected : `BUILD FAILURE` — échec de compilation du module de test avec
`cannot find symbol` sur `SalleDao` dans `SalleDaoTest.java`. `FabriqueEntites` compile déjà
(seule la classe `SalleDao` manque) : c'est l'échec attendu.

- [ ] **Étape 3.4 — Créer `src/main/java/ci/univ/soutenances/dao/SalleDao.java`**

```java
package ci.univ.soutenances.dao;

import java.util.List;

import jakarta.persistence.EntityManager;

import ci.univ.soutenances.model.Salle;

public class SalleDao extends BaseDao<Salle> {

    public SalleDao(EntityManager em) {
        super(em);
    }

    @Override
    protected Class<Salle> getEntityClass() {
        return Salle.class;
    }

    public List<Salle> findAllAvecDepartement() {
        return em.createQuery(
                "SELECT s FROM Salle s LEFT JOIN FETCH s.departement ORDER BY s.nomSalle",
                Salle.class)
                .getResultList();
    }
}
```

Le tri `ORDER BY s.nomSalle` donne le libellé des options dans l'ordre alphabétique attendu du
select. Aucune autre méthode n'est ajoutée à `SalleDao` dans cette passe (design §5, décision 1).

- [ ] **Étape 3.5 — Lancer le test pour le voir passer (vert)**

Run : `mvn test`
Expected : `BUILD SUCCESS` avec toute la suite verte :
`Tests run: 8, Failures: 0, Errors: 0, Skipped: 0` — les 5 tests de `BaseDaoTest` plus les 3
tests de `SalleDaoTest`. Le test
`findAllAvecDepartementLisLeDepartementApresFermetureDeLaTransaction` passe uniquement grâce au
`JOIN FETCH` : c'est la preuve exécutable du risque 2 (design §7), défendable à l'oral.

- [ ] **Étape 3.6 — Commit**

```bash
git add src/main/java/ci/univ/soutenances/dao/SalleDao.java src/test/java/ci/univ/soutenances/dao/SalleDaoTest.java src/test/java/ci/univ/soutenances/model/FabriqueEntites.java
git commit -m "feat: SalleDao.findAllAvecDepartement pour le select de planification"
```

---

## Tâche 4 — EncadreurDao : le lookup du select de jury (TDD : test d'abord)

**Files:**
- Test : `src/test/java/ci/univ/soutenances/dao/EncadreurDaoTest.java`
- Modify : `src/test/java/ci/univ/soutenances/model/FabriqueEntites.java`
- Create : `src/main/java/ci/univ/soutenances/dao/EncadreurDao.java`

`EncadreurDao` est le second DAO de lecture seule de la passe (design §5, décision 1) : une
seule méthode, `findAllPourJury()`, qui alimente le select de composition du jury dans
`jury-form.jsp` (`/admin/planification`). La JSP affiche le nom et le prénom de l'option, et
éventuellement le grade et le département de l'encadreur : les deux relations se chargent par
`LEFT JOIN FETCH`, sur le modèle exact de `SalleDao` (design §7, risque 2). Le tri attendu du
select est `nom` puis `prenom`. Attributs lus dans les sources : `Encadreur.nom`,
`Encadreur.prenom`, `Encadreur.email`, `Encadreur.grade`, `Encadreur.departement`,
`Encadreur.utilisateur` ; `Grade.libelle` ; `Departement.nom`, `Departement.code`.

Deux contraintes uniques pèsent sur les jeux de test : `uk_encadreur_email` sur `email` et
`uk_encadreur_utilisateur` sur `utilisateur_id` — chaque encadreur semé doit donc avoir un
email **et** un `Utilisateur` distincts. `Encadreur.utilisateur` est `optional = false` : la
fabrique prend le compte en paramètre, aucun encadreur n'est persisté sans compte. C'est aussi
l'occasion d'ajouter `Utilisateur` à `FabriqueEntites`, dont le constructeur sans argument est
lui aussi `protected`.

- [ ] **Étape 4.1 — Étendre `src/test/java/ci/univ/soutenances/model/FabriqueEntites.java`**

Ajouter la création de `Utilisateur` et `Encadreur` (toujours sans import : mêmes packages).
`RoleUtilisateur` est l'enum du package `model` (`ADMIN`, `ENCADREUR`, `ETUDIANT`). La valeur
`motDePasse` est un simple texte de test : le hash SHA-256 viendra du futur `PasswordHasher`,
jamais d'un DAO ni d'une fabrique de test. Fichier complet après modification :

```java
package ci.univ.soutenances.model;

public final class FabriqueEntites {

    private FabriqueEntites() {
    }

    public static Grade nouveauGrade(String code, String libelle) {
        Grade grade = new Grade();
        grade.setCode(code);
        grade.setLibelle(libelle);
        return grade;
    }

    public static Departement nouveauDepartement(String code, String nom) {
        Departement departement = new Departement();
        departement.setCode(code);
        departement.setNom(nom);
        return departement;
    }

    public static Salle nouvelleSalle(String nom, int capacite, Departement departement) {
        Salle salle = new Salle();
        salle.setNomSalle(nom);
        salle.setCapacite(capacite);
        salle.setDepartement(departement);
        return salle;
    }

    public static Utilisateur nouvelUtilisateur(String login, String motDePasse, RoleUtilisateur role) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setLogin(login);
        utilisateur.setMotDePasse(motDePasse);
        utilisateur.setRole(role);
        return utilisateur;
    }

    public static Encadreur nouvelEncadreur(String nom, String prenom, String email, Grade grade,
            Departement departement, Utilisateur utilisateur) {
        Encadreur encadreur = new Encadreur();
        encadreur.setNom(nom);
        encadreur.setPrenom(prenom);
        encadreur.setEmail(email);
        encadreur.setGrade(grade);
        encadreur.setDepartement(departement);
        encadreur.setUtilisateur(utilisateur);
        return encadreur;
    }
}
```

- [ ] **Étape 4.2 — Écrire le test échouant : `src/test/java/ci/univ/soutenances/dao/EncadreurDaoTest.java`**

Trois tests, sur le modèle de `SalleDaoTest` : le jeu semé renvoyé trié `nom` puis `prenom`
(les trois encadreurs sont créés dans le désordre volontairement) ; le test qui prouve le
double `JOIN FETCH` — après fermeture de la transaction, `em.clear()` détache **tout** : sans
`JOIN FETCH`, `getGrade()` et `getDepartement()` renverraient des proxies morts et lèveraient
une `LazyInitializationException` ; et le cas vide. Chaque encadreur porte son propre
`Utilisateur` (logins distincts) et son propre email, pour satisfaire les deux contraintes
uniques. Le nettoyage supprime dans l'ordre des clés étrangères : encadreurs, utilisateurs,
grades, départements.

```java
package ci.univ.soutenances.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ci.univ.soutenances.model.Departement;
import ci.univ.soutenances.model.Encadreur;
import ci.univ.soutenances.model.FabriqueEntites;
import ci.univ.soutenances.model.Grade;
import ci.univ.soutenances.model.RoleUtilisateur;

class EncadreurDaoTest extends DaoTestBase {

    private EncadreurDao dao;

    @BeforeEach
    void creerDao() {
        dao = new EncadreurDao(em);
    }

    @AfterEach
    void nettoyerLesEncadreursUtilisateursGradesEtDepartements() {
        annulerTransaction();
        ouvrirTransaction();
        em.createQuery("DELETE FROM Encadreur").executeUpdate();
        em.createQuery("DELETE FROM Utilisateur").executeUpdate();
        em.createQuery("DELETE FROM Grade").executeUpdate();
        em.createQuery("DELETE FROM Departement").executeUpdate();
        validerTransaction();
    }

    // Trois encadreurs, trois emails et trois Utilisateurs distincts
    // (uk_encadreur_email et uk_encadreur_utilisateur), créés dans le désordre.
    private void semerTroisEncadreurs() {
        Departement info = FabriqueEntites.nouveauDepartement("INFO", "Informatique");
        Grade m1 = FabriqueEntites.nouveauGrade("M1", "Master 1");
        em.persist(info);
        em.persist(m1);
        em.persist(FabriqueEntites.nouvelEncadreur("Zoko", "Anne", "anne.zoko@univ.ci", m1, info,
                FabriqueEntites.nouvelUtilisateur("a.zoko", "hash-de-test", RoleUtilisateur.ENCADREUR)));
        em.persist(FabriqueEntites.nouvelEncadreur("Konan", "Bruno", "bruno.konan@univ.ci", m1, info,
                FabriqueEntites.nouvelUtilisateur("b.konan", "hash-de-test", RoleUtilisateur.ENCADREUR)));
        em.persist(FabriqueEntites.nouvelEncadreur("Konan", "Ali", "ali.konan@univ.ci", m1, info,
                FabriqueEntites.nouvelUtilisateur("a.konan", "hash-de-test", RoleUtilisateur.ENCADREUR)));
    }

    @Test
    void findAllPourJuryRenvoieLesEncadreursTriesParNomPrenom() {
        ouvrirTransaction();
        semerTroisEncadreurs();
        validerTransaction();

        ouvrirTransaction();
        List<Encadreur> encadreurs = dao.findAllPourJury();
        validerTransaction();

        assertEquals(3, encadreurs.size());
        assertEquals("Konan", encadreurs.get(0).getNom());
        assertEquals("Ali", encadreurs.get(0).getPrenom());
        assertEquals("Konan", encadreurs.get(1).getNom());
        assertEquals("Bruno", encadreurs.get(1).getPrenom());
        assertEquals("Zoko", encadreurs.get(2).getNom());
        assertEquals("Anne", encadreurs.get(2).getPrenom());
    }

    @Test
    void findAllPourJuryLisGradeEtDepartementApresFermetureDeLaTransaction() {
        ouvrirTransaction();
        Departement info = FabriqueEntites.nouveauDepartement("INFO", "Informatique");
        Grade m1 = FabriqueEntites.nouveauGrade("M1", "Master 1");
        em.persist(info);
        em.persist(m1);
        em.persist(FabriqueEntites.nouvelEncadreur("Konan", "Ali", "ali.konan@univ.ci", m1, info,
                FabriqueEntites.nouvelUtilisateur("a.konan", "hash-de-test", RoleUtilisateur.ENCADREUR)));
        validerTransaction();

        ouvrirTransaction();
        List<Encadreur> encadreurs = dao.findAllPourJury();
        validerTransaction();

        // Détache tout : sans JOIN FETCH dans la requête, getGrade() et getDepartement()
        // ci-dessous renverraient des proxies morts et lèveraient LazyInitializationException.
        em.clear();

        assertEquals(1, encadreurs.size());
        assertEquals("Konan", encadreurs.get(0).getNom());
        assertEquals("Ali", encadreurs.get(0).getPrenom());
        assertEquals("Master 1", encadreurs.get(0).getGrade().getLibelle());
        assertEquals("M1", encadreurs.get(0).getGrade().getCode());
        assertEquals("Informatique", encadreurs.get(0).getDepartement().getNom());
        assertEquals("INFO", encadreurs.get(0).getDepartement().getCode());
    }

    @Test
    void findAllPourJuryRenvoieUneListeVide() {
        ouvrirTransaction();
        List<Encadreur> encadreurs = dao.findAllPourJury();
        validerTransaction();

        assertTrue(encadreurs.isEmpty());
    }
}
```

- [ ] **Étape 4.3 — Lancer le test pour le voir échouer (rouge)**

Run : `mvn test`
Expected : `BUILD FAILURE` — échec de compilation du module de test avec
`cannot find symbol` sur `EncadreurDao` dans `EncadreurDaoTest.java`. `FabriqueEntites`
compile déjà (seule la classe `EncadreurDao` manque) : c'est l'échec attendu.

- [ ] **Étape 4.4 — Créer `src/main/java/ci/univ/soutenances/dao/EncadreurDao.java`**

```java
package ci.univ.soutenances.dao;

import java.util.List;

import jakarta.persistence.EntityManager;

import ci.univ.soutenances.model.Encadreur;

public class EncadreurDao extends BaseDao<Encadreur> {

    public EncadreurDao(EntityManager em) {
        super(em);
    }

    @Override
    protected Class<Encadreur> getEntityClass() {
        return Encadreur.class;
    }

    public List<Encadreur> findAllPourJury() {
        return em.createQuery(
                "SELECT e FROM Encadreur e LEFT JOIN FETCH e.grade LEFT JOIN FETCH e.departement "
                        + "ORDER BY e.nom, e.prenom",
                Encadreur.class)
                .getResultList();
    }
}
```

Le tri `ORDER BY e.nom, e.prenom` donne les options du select dans l'ordre alphabétique
attendu. La forme `LEFT JOIN FETCH` reprend `SalleDao` et `DAO.md` §8 ; `grade` et
`departement` étant `optional = false` côté entité, une forme intérieure reviendrait au même.
Aucune autre méthode n'est ajoutée à `EncadreurDao` dans cette passe : `findByEmail` et
`findByUtilisateurId` de `DAO.md` §5 viendront avec les écrans du bloc B (design §5).

- [ ] **Étape 4.5 — Lancer le test pour le voir passer (vert)**

Run : `mvn test`
Expected : `BUILD SUCCESS` avec toute la suite verte :
`Tests run: 11, Failures: 0, Errors: 0, Skipped: 0` — les 5 tests de `BaseDaoTest`, les 3
tests de `SalleDaoTest` et les 3 tests de `EncadreurDaoTest`. Le test
`findAllPourJuryLisGradeEtDepartementApresFermetureDeLaTransaction` passe uniquement grâce aux
deux `JOIN FETCH` : c'est la preuve exécutable du risque 2 (design §7) sur ce DAO.

- [ ] **Étape 4.6 — Commit**

```bash
git add src/main/java/ci/univ/soutenances/dao/EncadreurDao.java src/test/java/ci/univ/soutenances/dao/EncadreurDaoTest.java src/test/java/ci/univ/soutenances/model/FabriqueEntites.java
git commit -m "feat: EncadreurDao.findAllPourJury pour la composition du jury"
```

---

## Tâche 5 — FiliereDao : le filtre filière de `/admin/soutenances` (TDD : test d'abord)

**Files:**
- Test : `src/test/java/ci/univ/soutenances/dao/FiliereDaoTest.java`
- Modify : `src/test/java/ci/univ/soutenances/model/FabriqueEntites.java`
- Create : `src/main/java/ci/univ/soutenances/dao/FiliereDao.java`

`FiliereDao` est le troisième et dernier DAO de lecture seule de la passe (design §5,
décision 1) : une seule méthode, `findAllAvecDepartement()`, qui alimente le menu déroulant
du filtre filière de `soutenance-list.jsp` (`/admin/soutenances`). L'option affiche le
`libelle` (le `code` servant de valeur) et la JSP peut afficher le département de la filière :
la relation se charge donc par `LEFT JOIN FETCH f.departement`, comme dans `SalleDao` et
`EncadreurDao` (design §7, risque 2). Le tri attendu du filtre est `libelle`. Attributs lus
dans les sources : `Filiere.code`, `Filiere.libelle`, `Filiere.departement` ;
`Departement.code`, `Departement.nom`.

`Filiere.code` est unique (`uk_filiere_code`) : les jeux de test créent des codes distincts.
Rien d'autre n'est ajouté à ce DAO dans cette passe — `findByCode` et `findByDepartement` de
`DAO.md` §5 viendront avec les formulaires du bloc A (design §5).

- [ ] **Étape 5.1 — Étendre `src/test/java/ci/univ/soutenances/model/FabriqueEntites.java`**

Ajouter la création de `Filiere`. Fichier complet après modification :

```java
package ci.univ.soutenances.model;

public final class FabriqueEntites {

    private FabriqueEntites() {
    }

    public static Grade nouveauGrade(String code, String libelle) {
        Grade grade = new Grade();
        grade.setCode(code);
        grade.setLibelle(libelle);
        return grade;
    }

    public static Departement nouveauDepartement(String code, String nom) {
        Departement departement = new Departement();
        departement.setCode(code);
        departement.setNom(nom);
        return departement;
    }

    public static Salle nouvelleSalle(String nom, int capacite, Departement departement) {
        Salle salle = new Salle();
        salle.setNomSalle(nom);
        salle.setCapacite(capacite);
        salle.setDepartement(departement);
        return salle;
    }

    public static Utilisateur nouvelUtilisateur(String login, String motDePasse, RoleUtilisateur role) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setLogin(login);
        utilisateur.setMotDePasse(motDePasse);
        utilisateur.setRole(role);
        return utilisateur;
    }

    public static Encadreur nouvelEncadreur(String nom, String prenom, String email, Grade grade,
            Departement departement, Utilisateur utilisateur) {
        Encadreur encadreur = new Encadreur();
        encadreur.setNom(nom);
        encadreur.setPrenom(prenom);
        encadreur.setEmail(email);
        encadreur.setGrade(grade);
        encadreur.setDepartement(departement);
        encadreur.setUtilisateur(utilisateur);
        return encadreur;
    }

    public static Filiere nouvelleFiliere(String code, String libelle, Departement departement) {
        Filiere filiere = new Filiere();
        filiere.setCode(code);
        filiere.setLibelle(libelle);
        filiere.setDepartement(departement);
        return filiere;
    }
}
```

- [ ] **Étape 5.2 — Écrire le test échouant : `src/test/java/ci/univ/soutenances/dao/FiliereDaoTest.java`**

Trois tests, sur le modèle de `SalleDaoTest` : le jeu semé renvoyé trié par `libelle` (trois
codes uniques, créés dans le désordre) ; le test qui prouve le `JOIN FETCH` du département
après `em.clear()` ; et le cas vide. Le nettoyage supprime d'abord les filières puis les
départements (clé étrangère `departement_id`).

```java
package ci.univ.soutenances.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ci.univ.soutenances.model.Departement;
import ci.univ.soutenances.model.FabriqueEntites;
import ci.univ.soutenances.model.Filiere;

class FiliereDaoTest extends DaoTestBase {

    private FiliereDao dao;

    @BeforeEach
    void creerDao() {
        dao = new FiliereDao(em);
    }

    @AfterEach
    void nettoyerLesFilieresEtDepartements() {
        annulerTransaction();
        ouvrirTransaction();
        em.createQuery("DELETE FROM Filiere").executeUpdate();
        em.createQuery("DELETE FROM Departement").executeUpdate();
        validerTransaction();
    }

    @Test
    void findAllAvecDepartementRenvoieLesFilieresTrieesParLibelle() {
        ouvrirTransaction();
        Departement info = FabriqueEntites.nouveauDepartement("INFO", "Informatique");
        em.persist(info);
        em.persist(FabriqueEntites.nouvelleFiliere("M2IL", "Master 2 Info", info));
        em.persist(FabriqueEntites.nouvelleFiliere("L3IL", "Licence 3 Info", info));
        em.persist(FabriqueEntites.nouvelleFiliere("M1MI", "Master 1 MIAGE", info));
        validerTransaction();

        ouvrirTransaction();
        List<Filiere> filieres = dao.findAllAvecDepartement();
        validerTransaction();

        assertEquals(3, filieres.size());
        assertEquals("Licence 3 Info", filieres.get(0).getLibelle());
        assertEquals("Master 1 MIAGE", filieres.get(1).getLibelle());
        assertEquals("Master 2 Info", filieres.get(2).getLibelle());
    }

    @Test
    void findAllAvecDepartementLisLeDepartementApresFermetureDeLaTransaction() {
        ouvrirTransaction();
        Departement info = FabriqueEntites.nouveauDepartement("INFO", "Informatique");
        em.persist(info);
        em.persist(FabriqueEntites.nouvelleFiliere("M1MI", "Master 1 MIAGE", info));
        validerTransaction();

        ouvrirTransaction();
        List<Filiere> filieres = dao.findAllAvecDepartement();
        validerTransaction();

        // Détache tout : sans JOIN FETCH dans la requête, getDepartement() ci-dessous
        // renverrait un proxy mort et lèverait LazyInitializationException.
        em.clear();

        assertEquals(1, filieres.size());
        assertEquals("M1MI", filieres.get(0).getCode());
        assertEquals("Master 1 MIAGE", filieres.get(0).getLibelle());
        assertEquals("Informatique", filieres.get(0).getDepartement().getNom());
        assertEquals("INFO", filieres.get(0).getDepartement().getCode());
    }

    @Test
    void findAllAvecDepartementRenvoieUneListeVide() {
        ouvrirTransaction();
        List<Filiere> filieres = dao.findAllAvecDepartement();
        validerTransaction();

        assertTrue(filieres.isEmpty());
    }
}
```

- [ ] **Étape 5.3 — Lancer le test pour le voir échouer (rouge)**

Run : `mvn test`
Expected : `BUILD FAILURE` — échec de compilation du module de test avec
`cannot find symbol` sur `FiliereDao` dans `FiliereDaoTest.java`. `FabriqueEntites` compile
déjà (seule la classe `FiliereDao` manque) : c'est l'échec attendu.

- [ ] **Étape 5.4 — Créer `src/main/java/ci/univ/soutenances/dao/FiliereDao.java`**

```java
package ci.univ.soutenances.dao;

import java.util.List;

import jakarta.persistence.EntityManager;

import ci.univ.soutenances.model.Filiere;

public class FiliereDao extends BaseDao<Filiere> {

    public FiliereDao(EntityManager em) {
        super(em);
    }

    @Override
    protected Class<Filiere> getEntityClass() {
        return Filiere.class;
    }

    public List<Filiere> findAllAvecDepartement() {
        return em.createQuery(
                "SELECT f FROM Filiere f LEFT JOIN FETCH f.departement ORDER BY f.libelle",
                Filiere.class)
                .getResultList();
    }
}
```

Le tri `ORDER BY f.libelle` donne les options du filtre dans l'ordre alphabétique attendu ;
la forme du `LEFT JOIN FETCH` reprend à l'identique `SalleDao.findAllAvecDepartement()` et
`DAO.md` §8.

- [ ] **Étape 5.5 — Lancer le test pour le voir passer (vert)**

Run : `mvn test`
Expected : `BUILD SUCCESS` avec toute la suite verte :
`Tests run: 14, Failures: 0, Errors: 0, Skipped: 0` — les 5 tests de `BaseDaoTest`, les 3
tests de `SalleDaoTest`, les 3 tests de `EncadreurDaoTest` et les 3 tests de
`FiliereDaoTest`. Les trois DAOs de lookup sont désormais livrés (périmètre, design §3
décision 1) ; les deux tâches suivantes portent les DAOs complets `MemoireDao` puis
`HistoriqueActionDao`.

- [ ] **Étape 5.6 — Commit**

```bash
git add src/main/java/ci/univ/soutenances/dao/FiliereDao.java src/test/java/ci/univ/soutenances/dao/FiliereDaoTest.java src/test/java/ci/univ/soutenances/model/FabriqueEntites.java
git commit -m "feat: FiliereDao.findAllAvecDepartement pour le filtre filière"
```

---

## Tâche 6 — MemoireDao : écrans `/admin/memoires` et select de planification (TDD : test d'abord)

**Files:**
- Test : `src/test/java/ci/univ/soutenances/dao/MemoireDaoTest.java`
- Modify : `src/test/java/ci/univ/soutenances/model/FabriqueEntites.java`
- Create : `src/main/java/ci/univ/soutenances/dao/MemoireDao.java`

`MemoireDao` couvre les écrans mémoire du module C (design §5) : la liste
`memoire-list.jsp` (`/admin/memoires`), le select mémoire du formulaire de planification
(`/admin/planification`) et un lookup par encadreur (`DAO.md` §5). Trois méthodes :

- `findAllAvecEncadreur()` — la liste de `memoire-list.jsp`, avec
  `LEFT JOIN FETCH m.encadreur` : la colonne « directeur de mémoire » s'affiche hors
  transaction (design §7, risque 2), tri `m.titre`.
- `findSansSoutenance()` — le select de planification : seuls les mémoires **libres** doivent
  y figurer, c'est-à-dire ni rattachés à une `Soutenance` (contrainte
  `uk_soutenance_memoire` : un mémoire soutenu ne doit plus être planifiable) ni rattachés à
  un `Etudiant` (`Etudiant.memoire` est lui aussi un `@OneToOne` unique) — les deux exclusions
  se font par `NOT EXISTS`, aucune des deux relations n'étant navigable depuis `Memoire`
  (pas d'attribut contrepartie côté `Memoire`).
- `findByEncadreur(UUID)` — les mémoires d'un encadreur donné, tri `m.titre`.

Points d'attention vérifiés dans les sources : `Memoire.titre` est `nullable = false`,
`Memoire.dateDepot` est un `LocalDate` **nullable** (aucune contrainte unique sur `titre` →
pas de contrôle d'unicité, design §5), `Memoire.encadreur` est `optional = false` — chaque
mémoire semé porte un encadreur persisté au préalable. Attributs lus : `Memoire.titre`,
`Memoire.description`, `Memoire.dateDepot`, `Memoire.encadreur` ; `Encadreur.nom` ;
`Soutenance.memoire`, `Soutenance.dateHeure`, `Soutenance.salle` ; `Etudiant.memoire`,
`Etudiant.matricule`, `Etudiant.nom`, `Etudiant.prenom`, `Etudiant.email`,
`Etudiant.filiere` ; `Utilisateur.login`, `Utilisateur.motDePasse`,
`Utilisateur.role` (`RoleUtilisateur.ETUDIANT`).

- [ ] **Étape 6.1 — Étendre `src/test/java/ci/univ/soutenances/model/FabriqueEntites.java`**

Ajouter la création de `Memoire`, `Soutenance` et `Etudiant`. `Memoire.dateDepot` est nullable
: la fabrique le prend en `LocalDate` **nullable** (passer `null` crée un mémoire sans date de
dépôt — c'est le cas que le test de l'étape 6.2 doit couvrir explicitement). `Soutenance.statut`
a sa valeur par défaut dans l'entité (`PLANIFIEE`) : la fabrique ne le prend pas.
`Etudiant.memoire` est nullable côté entité, mais le test de cette tâche ne crée que des
étudiants **avec** mémoire : la fabrique le prend donc en paramètre. Fichier complet après
modification :

```java
package ci.univ.soutenances.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class FabriqueEntites {

    private FabriqueEntites() {
    }

    public static Grade nouveauGrade(String code, String libelle) {
        Grade grade = new Grade();
        grade.setCode(code);
        grade.setLibelle(libelle);
        return grade;
    }

    public static Departement nouveauDepartement(String code, String nom) {
        Departement departement = new Departement();
        departement.setCode(code);
        departement.setNom(nom);
        return departement;
    }

    public static Salle nouvelleSalle(String nom, int capacite, Departement departement) {
        Salle salle = new Salle();
        salle.setNomSalle(nom);
        salle.setCapacite(capacite);
        salle.setDepartement(departement);
        return salle;
    }

    public static Utilisateur nouvelUtilisateur(String login, String motDePasse, RoleUtilisateur role) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setLogin(login);
        utilisateur.setMotDePasse(motDePasse);
        utilisateur.setRole(role);
        return utilisateur;
    }

    public static Encadreur nouvelEncadreur(String nom, String prenom, String email, Grade grade,
            Departement departement, Utilisateur utilisateur) {
        Encadreur encadreur = new Encadreur();
        encadreur.setNom(nom);
        encadreur.setPrenom(prenom);
        encadreur.setEmail(email);
        encadreur.setGrade(grade);
        encadreur.setDepartement(departement);
        encadreur.setUtilisateur(utilisateur);
        return encadreur;
    }

    public static Filiere nouvelleFiliere(String code, String libelle, Departement departement) {
        Filiere filiere = new Filiere();
        filiere.setCode(code);
        filiere.setLibelle(libelle);
        filiere.setDepartement(departement);
        return filiere;
    }

    public static Memoire nouveauMemoire(String titre, String description, LocalDate dateDepot,
            Encadreur encadreur) {
        Memoire memoire = new Memoire();
        memoire.setTitre(titre);
        memoire.setDescription(description);
        memoire.setDateDepot(dateDepot); // nullable : null = mémoire sans date de dépôt
        memoire.setEncadreur(encadreur);
        return memoire;
    }

    public static Soutenance nouvelleSoutenance(LocalDateTime dateHeure, Memoire memoire, Salle salle) {
        Soutenance soutenance = new Soutenance();
        soutenance.setDateHeure(dateHeure);
        soutenance.setMemoire(memoire);
        soutenance.setSalle(salle);
        return soutenance;
    }

    public static Etudiant nouvelEtudiant(String matricule, String nom, String prenom, String email,
            Filiere filiere, Memoire memoire, Utilisateur utilisateur) {
        Etudiant etudiant = new Etudiant();
        etudiant.setMatricule(matricule);
        etudiant.setNom(nom);
        etudiant.setPrenom(prenom);
        etudiant.setEmail(email);
        etudiant.setFiliere(filiere);
        etudiant.setMemoire(memoire);
        etudiant.setUtilisateur(utilisateur);
        return etudiant;
    }
}
```

- [ ] **Étape 6.2 — Écrire le test échouant : `src/test/java/ci/univ/soutenances/dao/MemoireDaoTest.java`**

Six tests : `findSansSoutenance` sur un jeu semé couvrant les trois cas (mémoire libre,
mémoire avec `Soutenance`, mémoire avec `Etudiant`) — seul le premier doit revenir ; le cas
vide de `findSansSoutenance` ; la création **sans** `dateDepot` (nullable : le mémoire est
accepté et `getDateDepot()` renvoie `null`) ; le `JOIN FETCH` de l'encadreur prouvé après
`em.clear()` (le test vérifie aussi le tri par `titre`) ; `findByEncadreur` avec un jeu semé
de deux encadreurs ; et le cas vide de `findByEncadreur`. Le nettoyage suit l'ordre des clés
étrangères : soutenance, étudiant, mémoire, encadreur, utilisateur, filière, grade, salle,
département.

```java
package ci.univ.soutenances.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ci.univ.soutenances.model.Departement;
import ci.univ.soutenances.model.Encadreur;
import ci.univ.soutenances.model.FabriqueEntites;
import ci.univ.soutenances.model.Filiere;
import ci.univ.soutenances.model.Grade;
import ci.univ.soutenances.model.Memoire;
import ci.univ.soutenances.model.RoleUtilisateur;
import ci.univ.soutenances.model.Salle;

class MemoireDaoTest extends DaoTestBase {

    private MemoireDao dao;

    @BeforeEach
    void creerDao() {
        dao = new MemoireDao(em);
    }

    @AfterEach
    void nettoyerLesTables() {
        annulerTransaction();
        ouvrirTransaction();
        em.createQuery("DELETE FROM Soutenance").executeUpdate();
        em.createQuery("DELETE FROM Etudiant").executeUpdate();
        em.createQuery("DELETE FROM Memoire").executeUpdate();
        em.createQuery("DELETE FROM Encadreur").executeUpdate();
        em.createQuery("DELETE FROM Utilisateur").executeUpdate();
        em.createQuery("DELETE FROM Filiere").executeUpdate();
        em.createQuery("DELETE FROM Grade").executeUpdate();
        em.createQuery("DELETE FROM Salle").executeUpdate();
        em.createQuery("DELETE FROM Departement").executeUpdate();
        validerTransaction();
    }

    // Département, grade et un encadreur persistés ; à appeler dans une transaction ouverte.
    private Encadreur semerReferentielsEtEncadreur() {
        Departement info = FabriqueEntites.nouveauDepartement("INFO", "Informatique");
        Grade m1 = FabriqueEntites.nouveauGrade("M1", "Master 1");
        em.persist(info);
        em.persist(m1);
        Encadreur encadreur = FabriqueEntites.nouvelEncadreur("Konan", "Ali", "ali.konan@univ.ci",
                m1, info, FabriqueEntites.nouvelUtilisateur("a.konan", "hash-de-test",
                        RoleUtilisateur.ENCADREUR));
        em.persist(encadreur);
        return encadreur;
    }

    @Test
    void findSansSoutenanceExclutLesMemoiresAvecSoutenanceOuEtudiant() {
        ouvrirTransaction();
        Encadreur encadreur = semerReferentielsEtEncadreur();
        Departement info = encadreur.getDepartement();
        Filiere miage = FabriqueEntites.nouvelleFiliere("M1MI", "Master 1 MIAGE", info);
        em.persist(miage);
        Salle c01 = FabriqueEntites.nouvelleSalle("Salle C01", 30, info);
        em.persist(c01);
        Memoire libre = FabriqueEntites.nouveauMemoire("Memoire libre", "description",
                LocalDate.of(2026, 1, 10), encadreur);
        Memoire soutenu = FabriqueEntites.nouveauMemoire("Memoire soutenu", "description",
                LocalDate.of(2026, 1, 11), encadreur);
        Memoire attribue = FabriqueEntites.nouveauMemoire("Memoire attribue", "description",
                LocalDate.of(2026, 1, 12), encadreur);
        em.persist(libre);
        em.persist(soutenu);
        em.persist(attribue);
        em.persist(FabriqueEntites.nouvelleSoutenance(LocalDateTime.of(2026, 6, 1, 9, 0), soutenu,
                c01));
        em.persist(FabriqueEntites.nouvelEtudiant("ETU001", "Diabate", "Mamadou",
                "m.diabate@univ.ci", miage, attribue,
                FabriqueEntites.nouvelUtilisateur("m.diabate", "hash-de-test",
                        RoleUtilisateur.ETUDIANT)));
        validerTransaction();

        ouvrirTransaction();
        List<Memoire> memoires = dao.findSansSoutenance();
        validerTransaction();

        assertEquals(1, memoires.size());
        assertEquals("Memoire libre", memoires.get(0).getTitre());
    }

    @Test
    void findSansSoutenanceRenvoieUneListeVide() {
        ouvrirTransaction();
        List<Memoire> memoires = dao.findSansSoutenance();
        validerTransaction();

        assertTrue(memoires.isEmpty());
    }

    @Test
    void createSansDateDepotEstAccepteeEtRenvoieNull() {
        ouvrirTransaction();
        Encadreur encadreur = semerReferentielsEtEncadreur();
        Memoire memoire = FabriqueEntites.nouveauMemoire("Memoire sans date", "description", null,
                encadreur);
        dao.create(memoire);
        validerTransaction();

        Memoire relu = dao.findById(memoire.getId());
        assertNotNull(relu);
        assertEquals("Memoire sans date", relu.getTitre());
        assertNull(relu.getDateDepot());
    }

    @Test
    void findAllAvecEncadreurLisEncadreurEtTrieParTitreApresFermetureDeLaTransaction() {
        ouvrirTransaction();
        Encadreur encadreur = semerReferentielsEtEncadreur();
        em.persist(FabriqueEntites.nouveauMemoire("Alpha", "description",
                LocalDate.of(2026, 1, 10), encadreur));
        em.persist(FabriqueEntites.nouveauMemoire("Beta", "description",
                LocalDate.of(2026, 1, 11), encadreur));
        validerTransaction();

        ouvrirTransaction();
        List<Memoire> memoires = dao.findAllAvecEncadreur();
        validerTransaction();

        // Détache tout : sans JOIN FETCH dans la requête, getEncadreur() ci-dessous
        // renverrait un proxy mort et lèverait LazyInitializationException.
        em.clear();

        assertEquals(2, memoires.size());
        assertEquals("Alpha", memoires.get(0).getTitre());
        assertEquals("Beta", memoires.get(1).getTitre());
        assertEquals("Konan", memoires.get(0).getEncadreur().getNom());
        assertEquals("Ali", memoires.get(0).getEncadreur().getPrenom());
    }

    @Test
    void findByEncadreurNeRenvoieQueLesMemoiresDeEncadreur() {
        ouvrirTransaction();
        Encadreur encadreur = semerReferentielsEtEncadreur();
        Departement info = encadreur.getDepartement();
        Grade m2 = FabriqueEntites.nouveauGrade("M2", "Master 2");
        em.persist(m2);
        Encadreur autre = FabriqueEntites.nouvelEncadreur("Zoko", "Anne", "anne.zoko@univ.ci", m2,
                info, FabriqueEntites.nouvelUtilisateur("a.zoko", "hash-de-test",
                        RoleUtilisateur.ENCADREUR));
        em.persist(autre);
        em.persist(FabriqueEntites.nouveauMemoire("Memoire A1", "description",
                LocalDate.of(2026, 1, 10), encadreur));
        em.persist(FabriqueEntites.nouveauMemoire("Memoire A2", "description",
                LocalDate.of(2026, 1, 11), encadreur));
        em.persist(FabriqueEntites.nouveauMemoire("Memoire B1", "description",
                LocalDate.of(2026, 1, 12), autre));
        validerTransaction();

        ouvrirTransaction();
        List<Memoire> memoires = dao.findByEncadreur(encadreur.getId());
        validerTransaction();

        assertEquals(2, memoires.size());
        assertEquals("Memoire A1", memoires.get(0).getTitre());
        assertEquals("Memoire A2", memoires.get(1).getTitre());
    }

    @Test
    void findByEncadreurRenvoieUneListeVide() {
        ouvrirTransaction();
        List<Memoire> memoires = dao.findByEncadreur(UUID.randomUUID());
        validerTransaction();

        assertTrue(memoires.isEmpty());
    }
}
```

- [ ] **Étape 6.3 — Lancer le test pour le voir échouer (rouge)**

Run : `mvn test`
Expected : `BUILD FAILURE` — échec de compilation du module de test avec
`cannot find symbol` sur `MemoireDao` dans `MemoireDaoTest.java`. `FabriqueEntites` compile
déjà (seule la classe `MemoireDao` manque) : c'est l'échec attendu.

- [ ] **Étape 6.4 — Créer `src/main/java/ci/univ/soutenances/dao/MemoireDao.java`**

```java
package ci.univ.soutenances.dao;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import ci.univ.soutenances.model.Memoire;

public class MemoireDao extends BaseDao<Memoire> {

    public MemoireDao(EntityManager em) {
        super(em);
    }

    @Override
    protected Class<Memoire> getEntityClass() {
        return Memoire.class;
    }

    public List<Memoire> findAllAvecEncadreur() {
        return em.createQuery(
                "SELECT m FROM Memoire m LEFT JOIN FETCH m.encadreur ORDER BY m.titre",
                Memoire.class)
                .getResultList();
    }

    // Libres = ni rattachés à une Soutenance (uk_soutenance_memoire), ni rattachés à un
    // Etudiant (Etudiant.memoire, @OneToOne unique) : les deux associations sont déclarées
    // côté Soutenance et côté Etudiant, jamais côté Memoire, d'où deux NOT EXISTS.
    public List<Memoire> findSansSoutenance() {
        return em.createQuery(
                "SELECT m FROM Memoire m WHERE NOT EXISTS "
                        + "(SELECT s FROM Soutenance s WHERE s.memoire = m) AND NOT EXISTS "
                        + "(SELECT e FROM Etudiant e WHERE e.memoire = m) ORDER BY m.titre",
                Memoire.class)
                .getResultList();
    }

    public List<Memoire> findByEncadreur(UUID encadreurId) {
        return em.createQuery(
                "SELECT m FROM Memoire m WHERE m.encadreur.id = :eid ORDER BY m.titre",
                Memoire.class)
                .setParameter("eid", encadreurId)
                .getResultList();
    }
}
```

Les trois requêtes sont typées, n'utilisent que des paramètres nommés (`:eid`) et portent sur
le nom d'entité JPQL `Memoire` (jamais le nom de table SQL). `findByEncadreur` reste sans
`JOIN FETCH` : aucune JSP de cette passe n'en dépend ; si un futur écran en affiche les
encadreurs, la méthode dédiée avec fetch viendra alors (design §7, risque 2). Aucune écriture
d'`HistoriqueAction` ici (design §3, décision 5).

- [ ] **Étape 6.5 — Lancer le test pour le voir passer (vert)**

Run : `mvn test`
Expected : `BUILD SUCCESS` avec toute la suite verte :
`Tests run: 20, Failures: 0, Errors: 0, Skipped: 0` — les 5 tests de `BaseDaoTest`, les 3
tests de `SalleDaoTest`, les 3 tests de `EncadreurDaoTest`, les 3 tests de `FiliereDaoTest`
et les 6 tests de `MemoireDaoTest`. Le test
`createSansDateDepotEstAccepteeEtRenvoieNull` est la preuve exécutable du caractère nullable
de `dateDepot` ; le test
`findSansSoutenanceExclutLesMemoiresAvecSoutenanceOuEtudiant` valide les deux exclusions du
select de planification.

- [ ] **Étape 6.6 — Commit**

```bash
git add src/main/java/ci/univ/soutenances/dao/MemoireDao.java src/test/java/ci/univ/soutenances/dao/MemoireDaoTest.java src/test/java/ci/univ/soutenances/model/FabriqueEntites.java
git commit -m "feat: MemoireDao (liste avec encadreur, sans soutenance, par encadreur)"
```

---

## Tâche 7 — HistoriqueActionDao : l'écran d'audit `/admin/historique` (TDD : test d'abord)

**Files:**
- Test : `src/test/java/ci/univ/soutenances/dao/HistoriqueActionDaoTest.java`
- Modify : `src/test/java/ci/univ/soutenances/model/FabriqueEntites.java`
- Create : `src/main/java/ci/univ/soutenances/dao/HistoriqueActionDao.java`

`HistoriqueActionDao` couvre l'écran d'audit du module D (design §5) avec trois méthodes de
lecture :

- `findDernieres(int limite)` — les actions les plus récentes pour `historique-list.jsp` et le
  dashboard : tri `h.dateAction` **décroissant** + `setMaxResults(limite)` — la requête est
  bornée par la méthode elle-même, on ne charge jamais toute la table (l'appelant passe par
  exemple 20).
- `findByUtilisateur(UUID)` — l'historique d'un seul compte, même tri décroissant
  (`DAO.md` §5).
- `findByDateActionBetween(LocalDateTime debut, LocalDateTime fin)` — le filtre par période de
  `historique-list.jsp`. `dateAction` est un `LocalDateTime` ; les deux bornes sont
  **ouvertes** : une borne passée à `null` signifie « pas de limite de ce côté ». La méthode
  assemble alors des fragments JPQL **fixes** (aucune valeur utilisateur dans la chaîne —
  `DAO.md` §7 interdit uniquement la concaténation de valeurs, les paramètres restent nommés).

Les trois méthodes portent `JOIN FETCH h.utilisateur` : cet écran affiche l'auteur de chaque
action, et la transaction est close avant le rendu de la JSP (design §7, risque 2). Piège de
nommage à connaître pour l'oral : la table SQL s'appelle `Historique_Action`, mais le JPQL
parle en nom d'entité Java — `FROM HistoriqueAction h` (`DAO.md` §7, design §1). Attributs lus
dans les sources : `HistoriqueAction.typeAction`, `HistoriqueAction.dateAction`,
`HistoriqueAction.description`, `HistoriqueAction.utilisateur` ; `Utilisateur.login`.
Rappel (design §3, décision 5) : ce DAO **n'écrit jamais** une ligne `HistoriqueAction` — la
création des lignes d'audit appartiendra aux futurs services.

- [ ] **Étape 7.1 — Étendre `src/test/java/ci/univ/soutenances/model/FabriqueEntites.java`**

Ajouter la création de `HistoriqueAction`. Fichier complet après modification :

```java
package ci.univ.soutenances.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class FabriqueEntites {

    private FabriqueEntites() {
    }

    public static Grade nouveauGrade(String code, String libelle) {
        Grade grade = new Grade();
        grade.setCode(code);
        grade.setLibelle(libelle);
        return grade;
    }

    public static Departement nouveauDepartement(String code, String nom) {
        Departement departement = new Departement();
        departement.setCode(code);
        departement.setNom(nom);
        return departement;
    }

    public static Salle nouvelleSalle(String nom, int capacite, Departement departement) {
        Salle salle = new Salle();
        salle.setNomSalle(nom);
        salle.setCapacite(capacite);
        salle.setDepartement(departement);
        return salle;
    }

    public static Utilisateur nouvelUtilisateur(String login, String motDePasse, RoleUtilisateur role) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setLogin(login);
        utilisateur.setMotDePasse(motDePasse);
        utilisateur.setRole(role);
        return utilisateur;
    }

    public static Encadreur nouvelEncadreur(String nom, String prenom, String email, Grade grade,
            Departement departement, Utilisateur utilisateur) {
        Encadreur encadreur = new Encadreur();
        encadreur.setNom(nom);
        encadreur.setPrenom(prenom);
        encadreur.setEmail(email);
        encadreur.setGrade(grade);
        encadreur.setDepartement(departement);
        encadreur.setUtilisateur(utilisateur);
        return encadreur;
    }

    public static Filiere nouvelleFiliere(String code, String libelle, Departement departement) {
        Filiere filiere = new Filiere();
        filiere.setCode(code);
        filiere.setLibelle(libelle);
        filiere.setDepartement(departement);
        return filiere;
    }

    public static Memoire nouveauMemoire(String titre, String description, LocalDate dateDepot,
            Encadreur encadreur) {
        Memoire memoire = new Memoire();
        memoire.setTitre(titre);
        memoire.setDescription(description);
        memoire.setDateDepot(dateDepot); // nullable : null = mémoire sans date de dépôt
        memoire.setEncadreur(encadreur);
        return memoire;
    }

    public static Soutenance nouvelleSoutenance(LocalDateTime dateHeure, Memoire memoire, Salle salle) {
        Soutenance soutenance = new Soutenance();
        soutenance.setDateHeure(dateHeure);
        soutenance.setMemoire(memoire);
        soutenance.setSalle(salle);
        return soutenance;
    }

    public static Etudiant nouvelEtudiant(String matricule, String nom, String prenom, String email,
            Filiere filiere, Memoire memoire, Utilisateur utilisateur) {
        Etudiant etudiant = new Etudiant();
        etudiant.setMatricule(matricule);
        etudiant.setNom(nom);
        etudiant.setPrenom(prenom);
        etudiant.setEmail(email);
        etudiant.setFiliere(filiere);
        etudiant.setMemoire(memoire);
        etudiant.setUtilisateur(utilisateur);
        return etudiant;
    }

    public static HistoriqueAction nouvelleHistoriqueAction(String typeAction, LocalDateTime dateAction,
            String description, Utilisateur utilisateur) {
        HistoriqueAction action = new HistoriqueAction();
        action.setTypeAction(typeAction);
        action.setDateAction(dateAction);
        action.setDescription(description);
        action.setUtilisateur(utilisateur);
        return action;
    }
}
```

- [ ] **Étape 7.2 — Écrire le test échouant : `src/test/java/ci/univ/soutenances/dao/HistoriqueActionDaoTest.java`**

Neuf tests : `findDernieres(n)` limitée à `n` et ordonnée par `dateAction` décroissante sur
un jeu semé de 5 actions ; `findDernieres` lisant `utilisateur` après `em.clear()` (preuve du
`JOIN FETCH`) ; le cas vide de `findDernieres` ; `findByUtilisateur` ne renvoyant que les
actions d'un compte sur deux ; le cas vide de `findByUtilisateur` ; et les quatre cas du
filtre par période — bornes des deux côtés, sans borne de début, sans borne de fin, et sans
aucune borne (tout est renvoyé). `typeAction` et `dateAction` étant `nullable = false` côté
entité, toutes les actions semées portent un type et une date. Le nettoyage supprime
d'abord les actions puis les utilisateurs (clé étrangère `utilisateur_id`).

```java
package ci.univ.soutenances.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ci.univ.soutenances.model.FabriqueEntites;
import ci.univ.soutenances.model.HistoriqueAction;
import ci.univ.soutenances.model.RoleUtilisateur;
import ci.univ.soutenances.model.Utilisateur;

class HistoriqueActionDaoTest extends DaoTestBase {

    private static final LocalDateTime JOUR_10 = LocalDateTime.of(2026, 1, 10, 8, 0);
    private static final LocalDateTime JOUR_12 = LocalDateTime.of(2026, 1, 12, 9, 30);
    private static final LocalDateTime JOUR_15 = LocalDateTime.of(2026, 1, 15, 14, 15);

    private HistoriqueActionDao dao;

    @BeforeEach
    void creerDao() {
        dao = new HistoriqueActionDao(em);
    }

    @AfterEach
    void nettoyerLHistoriqueEtLesUtilisateurs() {
        annulerTransaction();
        ouvrirTransaction();
        em.createQuery("DELETE FROM HistoriqueAction").executeUpdate();
        em.createQuery("DELETE FROM Utilisateur").executeUpdate();
        validerTransaction();
    }

    // Un Utilisateur persisté ; à appeler dans une transaction ouverte.
    private Utilisateur semerUtilisateur(String login) {
        Utilisateur utilisateur = FabriqueEntites.nouvelUtilisateur(login, "hash-de-test",
                RoleUtilisateur.ADMIN);
        em.persist(utilisateur);
        return utilisateur;
    }

    private void semerAction(Utilisateur utilisateur, String typeAction, LocalDateTime dateAction) {
        em.persist(FabriqueEntites.nouvelleHistoriqueAction(typeAction, dateAction,
                "description de " + typeAction, utilisateur));
    }

    @Test
    void findDernieresLimiteLesResultatsParDateDecroissante() {
        ouvrirTransaction();
        Utilisateur admin = semerUtilisateur("admin1");
        semerAction(admin, "ACTION_1", JOUR_10);
        semerAction(admin, "ACTION_2", LocalDateTime.of(2026, 1, 11, 8, 0));
        semerAction(admin, "ACTION_3", JOUR_12);
        semerAction(admin, "ACTION_4", LocalDateTime.of(2026, 1, 14, 8, 0));
        semerAction(admin, "ACTION_5", JOUR_15);
        validerTransaction();

        ouvrirTransaction();
        List<HistoriqueAction> actions = dao.findDernieres(3);
        validerTransaction();

        assertEquals(3, actions.size());
        assertEquals("ACTION_5", actions.get(0).getTypeAction());
        assertEquals(JOUR_15, actions.get(0).getDateAction());
        assertEquals("ACTION_4", actions.get(1).getTypeAction());
        assertEquals("ACTION_3", actions.get(2).getTypeAction());
        assertEquals(JOUR_12, actions.get(2).getDateAction());
    }

    @Test
    void findDernieresLisUtilisateurApresFermetureDeLaTransaction() {
        ouvrirTransaction();
        Utilisateur admin = semerUtilisateur("admin1");
        semerAction(admin, "CREATION_MEMOIRE", JOUR_10);
        validerTransaction();

        ouvrirTransaction();
        List<HistoriqueAction> actions = dao.findDernieres(10);
        validerTransaction();

        // Détache tout : sans JOIN FETCH dans la requête, getUtilisateur() ci-dessous
        // renverrait un proxy mort et lèverait LazyInitializationException.
        em.clear();

        assertEquals(1, actions.size());
        assertEquals("CREATION_MEMOIRE", actions.get(0).getTypeAction());
        assertEquals("admin1", actions.get(0).getUtilisateur().getLogin());
    }

    @Test
    void findDernieresRenvoieUneListeVide() {
        ouvrirTransaction();
        List<HistoriqueAction> actions = dao.findDernieres(10);
        validerTransaction();

        assertTrue(actions.isEmpty());
    }

    @Test
    void findByUtilisateurNeRenvoieQueLesActionsDuCompte() {
        ouvrirTransaction();
        Utilisateur admin1 = semerUtilisateur("admin1");
        Utilisateur admin2 = semerUtilisateur("admin2");
        semerAction(admin1, "ACTION_A", JOUR_10);
        semerAction(admin2, "ACTION_X", JOUR_10);
        semerAction(admin1, "ACTION_B", JOUR_15);
        validerTransaction();

        ouvrirTransaction();
        List<HistoriqueAction> actions = dao.findByUtilisateur(admin1.getId());
        validerTransaction();

        assertEquals(2, actions.size());
        assertEquals("ACTION_B", actions.get(0).getTypeAction());
        assertEquals("ACTION_A", actions.get(1).getTypeAction());
    }

    @Test
    void findByUtilisateurRenvoieUneListeVide() {
        ouvrirTransaction();
        Utilisateur admin = semerUtilisateur("admin1");
        validerTransaction();

        ouvrirTransaction();
        List<HistoriqueAction> actions = dao.findByUtilisateur(admin.getId());
        validerTransaction();

        assertTrue(actions.isEmpty());
    }

    @Test
    void findByDateActionBetweenAvecLesDeuxBornes() {
        ouvrirTransaction();
        Utilisateur admin = semerUtilisateur("admin1");
        semerAction(admin, "AVANT", JOUR_10);
        semerAction(admin, "DANS_LA_PERIODE", JOUR_12);
        semerAction(admin, "APRES", JOUR_15);
        validerTransaction();

        ouvrirTransaction();
        List<HistoriqueAction> actions = dao.findByDateActionBetween(
                LocalDateTime.of(2026, 1, 11, 0, 0), LocalDateTime.of(2026, 1, 13, 0, 0));
        validerTransaction();

        assertEquals(1, actions.size());
        assertEquals("DANS_LA_PERIODE", actions.get(0).getTypeAction());
    }

    @Test
    void findByDateActionBetweenSansDebutFiltreJusquALaFin() {
        ouvrirTransaction();
        Utilisateur admin = semerUtilisateur("admin1");
        semerAction(admin, "AVANT", JOUR_10);
        semerAction(admin, "DANS_LA_PERIODE", JOUR_12);
        semerAction(admin, "APRES", JOUR_15);
        validerTransaction();

        ouvrirTransaction();
        List<HistoriqueAction> actions = dao.findByDateActionBetween(
                null, LocalDateTime.of(2026, 1, 13, 0, 0));
        validerTransaction();

        assertEquals(2, actions.size());
        assertEquals("DANS_LA_PERIODE", actions.get(0).getTypeAction());
        assertEquals("AVANT", actions.get(1).getTypeAction());
    }

    @Test
    void findByDateActionBetweenSansFinFiltreDepuisLeDebut() {
        ouvrirTransaction();
        Utilisateur admin = semerUtilisateur("admin1");
        semerAction(admin, "AVANT", JOUR_10);
        semerAction(admin, "DANS_LA_PERIODE", JOUR_12);
        semerAction(admin, "APRES", JOUR_15);
        validerTransaction();

        ouvrirTransaction();
        List<HistoriqueAction> actions = dao.findByDateActionBetween(
                LocalDateTime.of(2026, 1, 11, 0, 0), null);
        validerTransaction();

        assertEquals(2, actions.size());
        assertEquals("APRES", actions.get(0).getTypeAction());
        assertEquals("DANS_LA_PERIODE", actions.get(1).getTypeAction());
    }

    @Test
    void findByDateActionBetweenSansBornesRenvoieTout() {
        ouvrirTransaction();
        Utilisateur admin = semerUtilisateur("admin1");
        semerAction(admin, "AVANT", JOUR_10);
        semerAction(admin, "DANS_LA_PERIODE", JOUR_12);
        semerAction(admin, "APRES", JOUR_15);
        validerTransaction();

        ouvrirTransaction();
        List<HistoriqueAction> actions = dao.findByDateActionBetween(null, null);
        validerTransaction();

        assertEquals(3, actions.size());
        assertEquals("APRES", actions.get(0).getTypeAction());
        assertEquals("DANS_LA_PERIODE", actions.get(1).getTypeAction());
        assertEquals("AVANT", actions.get(2).getTypeAction());
    }
}
```

- [ ] **Étape 7.3 — Lancer le test pour le voir échouer (rouge)**

Run : `mvn test`
Expected : `BUILD FAILURE` — échec de compilation du module de test avec
`cannot find symbol` sur `HistoriqueActionDao` dans `HistoriqueActionDaoTest.java`.
`FabriqueEntites` compile déjà (seule la classe `HistoriqueActionDao` manque) : c'est
l'échec attendu.

- [ ] **Étape 7.4 — Créer `src/main/java/ci/univ/soutenances/dao/HistoriqueActionDao.java`**

```java
package ci.univ.soutenances.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import ci.univ.soutenances.model.HistoriqueAction;

public class HistoriqueActionDao extends BaseDao<HistoriqueAction> {

    public HistoriqueActionDao(EntityManager em) {
        super(em);
    }

    @Override
    protected Class<HistoriqueAction> getEntityClass() {
        return HistoriqueAction.class;
    }

    // Le JPQL parle en nom d'entité Java (HistoriqueAction), jamais en nom de table SQL
    // (Historique_Action) : DAO.md §7.
    public List<HistoriqueAction> findDernieres(int limite) {
        return em.createQuery(
                "SELECT h FROM HistoriqueAction h JOIN FETCH h.utilisateur ORDER BY h.dateAction DESC",
                HistoriqueAction.class)
                .setMaxResults(limite)
                .getResultList();
    }

    public List<HistoriqueAction> findByUtilisateur(UUID utilisateurId) {
        return em.createQuery(
                "SELECT h FROM HistoriqueAction h JOIN FETCH h.utilisateur "
                        + "WHERE h.utilisateur.id = :uid ORDER BY h.dateAction DESC",
                HistoriqueAction.class)
                .setParameter("uid", utilisateurId)
                .getResultList();
    }

    // Bornes ouvertes : une borne null = « pas de limite de ce côté ». On assemble des
    // fragments JPQL fixes — aucune valeur utilisateur n'entre dans la chaîne (DAO.md §7) —
    // et on pose chaque paramètre nommé seulement s'il est utilisé.
    public List<HistoriqueAction> findByDateActionBetween(LocalDateTime debut, LocalDateTime fin) {
        StringBuilder jpql = new StringBuilder(
                "SELECT h FROM HistoriqueAction h JOIN FETCH h.utilisateur");
        if (debut != null && fin != null) {
            jpql.append(" WHERE h.dateAction BETWEEN :debut AND :fin");
        } else if (debut != null) {
            jpql.append(" WHERE h.dateAction >= :debut");
        } else if (fin != null) {
            jpql.append(" WHERE h.dateAction <= :fin");
        }
        jpql.append(" ORDER BY h.dateAction DESC");
        TypedQuery<HistoriqueAction> query = em.createQuery(jpql.toString(), HistoriqueAction.class);
        if (debut != null) {
            query.setParameter("debut", debut);
        }
        if (fin != null) {
            query.setParameter("fin", fin);
        }
        return query.getResultList();
    }
}
```

Points à pouvoir expliquer à l'oral : `setMaxResults(limite)` est le plafond SQL de
`findDernieres` — l'écran des dernières actions ne charge jamais toute la table ; les trois
requêtes `JOIN FETCH` l'utilisateur car l'écran d'audit affiche l'auteur hors transaction
(risque 2) ; `findByDateActionBetween` ne contient aucun `if` métier, seulement l'assemblage
technique de fragments JPQL fixes pour gérer les bornes ouvertes, avec `:debut` et `:fin` en
paramètres nommés. Aucune écriture d'`HistoriqueAction` n'est livrée ici : les futurs
services écriront l'audit de leurs propres écritures (design §3, décision 5).

- [ ] **Étape 7.5 — Lancer le test pour le voir passer (vert)**

Run : `mvn test`
Expected : `BUILD SUCCESS` avec toute la suite verte :
`Tests run: 29, Failures: 0, Errors: 0, Skipped: 0` — les 5 tests de `BaseDaoTest`, les 3
tests de `SalleDaoTest`, les 3 tests de `EncadreurDaoTest`, les 3 tests de `FiliereDaoTest`,
les 6 tests de `MemoireDaoTest` et les 9 tests de `HistoriqueActionDaoTest`. Si un test
échoue, relire l'implémentation ligne à ligne par rapport au bloc ci-dessus avant tout autre
changement.

- [ ] **Étape 7.6 — Commit**

```bash
git add src/main/java/ci/univ/soutenances/dao/HistoriqueActionDao.java src/test/java/ci/univ/soutenances/dao/HistoriqueActionDaoTest.java src/test/java/ci/univ/soutenances/model/FabriqueEntites.java
git commit -m "feat: HistoriqueActionDao (dernières actions, par compte, par période)"
```

---

**Fin de la partie 2 (tâches 4 à 7).** L'état attendu à ce point : `mvn test` vert (29 tests),
les quatre DAOs de cette partie — `EncadreurDao`, `FiliereDao`, `MemoireDao`,
`HistoriqueActionDao` — livrés avec leurs tests, `FabriqueEntites` enrichie des fabriques
`Utilisateur`, `Encadreur`, `Filiere`, `Memoire`, `Soutenance`, `Etudiant` et
`HistoriqueAction`. Les livrables de la partie 1 (`BaseDao`, `SalleDao`, le socle de tests)
sont inchangés.

---

## Tâche 8 — SoutenanceDao : liste filtrée `/admin/soutenances` et agrégats du dashboard (TDD : test d'abord)

**Files:**
- Test : `src/test/java/ci/univ/soutenances/dao/SoutenanceDaoTest.java`
- Create : `src/main/java/ci/univ/soutenances/dao/SoutenanceDao.java`

Aucun `Modify:` cette fois : `FabriqueEntites`, `DaoTestBase` et `BaseDao` sont réutilisés
tels qu'ils sont définis aux tâches 1 à 7 (vérification à l'étape 8.1). Rien n'est touché en
dehors de `dao/` et `src/test/`.

`SoutenanceDao` est le seul DAO de la passe qui sert **deux modules** : la liste filtrée
`/admin/soutenances` (module C) et les compteurs du dashboard `/admin/dashboard` (module D).
Décision arrêtée à la conception, **réaffirmée ici pour ne pas la rouvrir** : les agrégats du
dashboard vivent dans ce DAO, et non dans un `DashboardDao` dédié — la règle « un DAO par
entité » de `DAO.md` §4 prime sur la commodité d'un fichier unique (design §3, décision 3).

Trois méthodes, pas une de plus :

- `findAllFiltrees(StatutSoutenance statut, LocalDateTime debut, LocalDateTime fin, UUID filiereId)`
  — la méthode de liste de `soutenance-list.jsp`, avec `JOIN FETCH` de `memoire`, `salle` et
  `salle.departement` (`DAO.md` §8, design §7 risque 2) et les trois filtres que cet écran
  spécifie : statut, période de date et filière. Les quatre paramètres sont ouverts — `null`
  signifie « pas de filtre de ce côté ». Le filtre statut annoncé par `DAO.md` §5
  (`findByStatut(StatutSoutenance)`) est couvert par ce paramètre : un écran, une méthode —
  une seconde méthode ne ferait que dupliquer la même requête (`DAO.md` §1).
- `countTotal()` — le total des soutenances, pour la carte du dashboard.
- `countByStatut(StatutSoutenance)` — le décompte par statut, appelé une fois par carte
  (`PLANIFIEE`, `EVALUEE`, `ANNULEE`).

Attributs lus dans les sources — **vérifiés, pas repris de mémoire** :
`Soutenance.dateHeure` (`LocalDateTime`, `nullable = false`) ; `Soutenance.statut`
(`StatutSoutenance`, `@Enumerated(EnumType.STRING)`, valeur par défaut `PLANIFIEE`,
`nullable = false`, `length = 20`) ; `Soutenance.memoire` (`@OneToOne` LAZY, `optional = false`,
`nullable = false`, contrainte `uk_soutenance_memoire` sur `memoire_id`) ; `Soutenance.salle`
(`@ManyToOne` LAZY, `optional = false`) ; `Salle.nomSalle`, `Salle.capacite` (primitif `int`),
`Salle.departement` ; `Departement.code`, `Departement.nom` ; `Memoire.titre` ;
`Etudiant.memoire` (`@OneToOne` LAZY, **nullable**, unique `uk_etudiant_memoire`, déclaré
**côté `Etudiant`**) ; `Etudiant.filiere` ; `Filiere.code`, `Filiere.libelle`.

Hors périmètre de cette tâche (design §8, et rappel du cadre général) : aucune validation
métier — salle libre, jury ≥ 2 membres, mémoire pas déjà soutenu — qui relèvera du futur
`service/` ; aucune écriture d'`HistoriqueAction` (design §3, décision 5) ; aucune servlet, ni
JSP, ni CSS. `SoutenanceDao.findBySalleAndDateBetween` (conflit de salle, `suggestion.md` §8.8)
et `findByEncadreur` (restreindre une liste aux jurys) ne sont **pas** livrés ici : leur seule
appelante sera un futur `service/` ou un écran encadreur.

### Le point technique de la tâche — à lire avant d'écrire une ligne

**1. Le filtre filière n'a aucune jointure naturelle (design §7, risque 1).** Il n'existe
aucune clé étrangère `Soutenance` → `Etudiant` : le seul pont est `Etudiant.memoire`, une
association `@OneToOne` déclarée côté `Etudiant`, **sans contrepartie côté `Memoire`**. Atteindre
la filière demande donc une jointure d'entité ad hoc en syntaxe Hibernate 6 :

```jpql
... JOIN Memoire m ON s.memoire = m JOIN Etudiant e ON e.memoire = m ... WHERE e.filiere.id = :filiereId
```

C'est la **première chose que cette tâche vérifie** : le test
`findAllFiltreesParFiliereJointEtudiantParSonPontMemoire` est le premier écrit dans la classe de
test, et son jeu de test attache réellement un `Etudiant` au mémoire — sans étudiant rattaché,
ce test ne prouve absolument rien.

> **Repli convenu d'avance, à respecter à la lettre :** si cette syntaxe d'entité-join est
> refusée par la version d'Hibernate en service (celle des tests, ou celle de WildFly 37 en
> production), on ne déplace **jamais** la logique dans une servlet : on compte par filière dans
> `SoutenanceDao` et on agrège par département dans un futur `service/` (design §3 décision 3,
> §7 risque 1). Ce repli n'est pas une question ouverte — il est écrit ici pour qu'aucun
> contributeur ne « résolve » le problème en poussant du JPQL ou un `if` dans un contrôleur.

**2. `/admin/soutenances` ne pagine pas — choix assumé, et voici pourquoi.** Le design (§7,
risque 3) met en garde : un `JOIN FETCH` sur une chaîne longue combiné à une limite de lignes
produit des entités parentes dupliquées, le `LIMIT` SQL s'appliquant aux lignes jointes et non
aux entités distinctes. Le choix fait ici est le plus simple des deux : `findAllFiltrees`
**n'appelle jamais `setMaxResults`** et ne tronque rien. Trois raisons, dans l'ordre :

- le volume est celui d'une promotion — quelques dizaines à quelques centaines de soutenances
  par année universitaire : la liste filtrée entière tient en mémoire sans peine ;
- toutes les associations traversées sont **to-one** sous contrainte unique
  (`uk_soutenance_memoire`, `uk_etudiant_memoire`) : aucune jointure ne multiplie les lignes —
  pas même celle de l'étudiant, un mémoire n'ayant au plus un étudiant — donc, sans limite,
  aucune ligne dupliquée n'est possible ;
- une pagination honnête sur cette requête demanderait **deux requêtes** (les identifiants
  d'abord, avec `DISTINCT` + `setMaxResults`, puis le `JOIN FETCH` de ces identifiants-là) — un
  surcoût que ce volume ne justifie pas aujourd'hui.

Si le volume croît un jour, ce sera ce motif en deux requêtes qui viendra, **jamais** un simple
`setMaxResults` posé sur cette requête à fetch (design §7, risque 3). Ce choix n'affecte aucun
autre DAO du périmètre : `findDernieres(int)` d'`HistoriqueActionDao` garde sa limite, elle est
sans `JOIN FETCH` sur une chaîne et donc sans risque de doublon.

**3. `statut` est un enum, pas une chaîne.** `Soutenance.statut` est `@Enumerated(EnumType.STRING)` :
les filtres passent l'objet `StatutSoutenance` en paramètre nommé —
`query.setParameter("statut", statut)` — jamais `"... = 'PLANIFIEE'"` concaténé, ni un `String`
brut (`DAO.md` §7).

- [ ] **Étape 8.1 — Vérifier que `FabriqueEntites` couvre déjà tous les jeux de test**

Contrairement aux tâches 3 à 7, **`FabriqueEntites` n'a pas besoin d'être étendue** : toutes
les fabriques dont `SoutenanceDaoTest` a besoin existent déjà à la fin de la tâche 7 —
`nouveauGrade`, `nouveauDepartement`, `nouvelleSalle`, `nouvelUtilisateur`, `nouvelEncadreur`,
`nouvelleFiliere`, `nouveauMemoire`, `nouvelleSoutenance(dateHeure, memoire, salle)` et
`nouvelEtudiant(matricule, nom, prenom, email, filiere, memoire, utilisateur)`. C'est pourquoi
le bloc `Files:` ci-dessus ne contient ni `Modify:`, ni aucun fichier hors `dao/` et `src/test/`.

Vérifier simplement les deux points suivants dans
`src/test/java/ci/univ/soutenances/model/FabriqueEntites.java` avant d'écrire le test :

- `nouvelleSoutenance` ne prend pas le `statut` : elle s'appuie sur la valeur par défaut de
  l'entité (`PLANIFIEE`, lue dans `Soutenance.java`). Les tests qui ont besoin d'`EVALUEE` ou
  d'`ANNULEE` appellent ensuite `soutenance.setStatut(...)` — setter `public`, utilisable
  depuis le package de test.
- `nouvelEtudiant` prend bien le `memoire` en paramètre : c'est le pont vers la filière, celui
  que le filtre traverse.

`DaoTestBase` non plus n'est pas modifié : les trois helpers de transaction
(`ouvrirTransaction`, `validerTransaction`, `annulerTransaction`) et le nettoyage en masse du
`@AfterEach` suffisent, sur le modèle exact de `MemoireDaoTest`.

- [ ] **Étape 8.2 — Écrire le test échouant : `src/test/java/ci/univ/soutenances/dao/SoutenanceDaoTest.java`**

Dix tests, **le premier étant celui de la jointure ad hoc** (point 1 de la section « Le point
technique de la tâche » ci-dessus) — si un seul doit être lu avant les autres, c'est lui :

1. `findAllFiltreesParFiliereJointEtudiantParSonPontMemoire` — **le test critique** : deux
   filières, trois soutenances, dont deux mémoires portés par un étudiant (l'un dans chaque
   filière) et un mémoire **sans** étudiant ; filtrer sur la première filière ne doit renvoyer
   que la soutenance de son étudiant. C'est ce test qui valide ou invalide la syntaxe
   d'entité-join Hibernate 6 du risque 1, avant même l'écran (design §6.3).
2. `findAllFiltreesSansCritereRenvoieToutesLesSoutenancesTrieesParDateHeure` — les quatre
   paramètres à `null` renvoient tout, trié par `dateHeure` croissante (semé dans le désordre).
3. `findAllFiltreesParStatutCompareLEnumStatutSoutenance` — jeu semé couvrant les **trois**
   statuts (`PLANIFIEE` par défaut, `EVALUEE` et `ANNULEE` posés par le test) ; le filtre en
   renvoie exactement un par statut.
4. `findAllFiltreesParPeriodeDeDateAvecLesDeuxBornes` — bornes des deux côtés.
5. `findAllFiltreesParPeriodeDeDateSansDebutFiltreJusquALaFin` — borne de début `null`
   (« pas de limite de ce côté », même convention que `findByDateActionBetween` à la tâche 7).
6. `findAllFiltreesLisMemoireSalleEtDepartementApresFermetureDeLaTransaction` — **preuve du
   `JOIN FETCH`** : après `em.clear()`, `getMemoire()`, `getSalle()` et `getSalle().getDepartement()`
   restent lisibles ; sans fetch, `LazyInitializationException`.
7. `findAllFiltreesRenvoieUneListeVide` — cas vide.
8. `countTotalCompteToutesLesSoutenances` — `0` avant semis, `3` après.
9. `countByStatutCompteChaqueStatutSeparement` — `1` / `1` / `1` sur le jeu des trois statuts.
10. `createDeuxSoutenancesSurLeMemeMemoireEstRefuseeParLaContrainteUnique` — deux soutenances
    sur le **même** mémoire : la seconde est refusée par `uk_soutenance_memoire`, et il ne
    reste qu'une ligne (la contrainte existe aussi sous H2, sur le schéma généré depuis les
    vraies entités).

Le nettoyage du `@AfterEach` suit l'ordre des clés étrangères, identique à `MemoireDaoTest` :
soutenance, étudiant, mémoire, encadreur, utilisateur, filière, grade, salle, département.
Hors test 10, aucune soutenance ne partage son mémoire avec une autre (`uk_soutenance_memoire`) :
trois mémoires sont créés à chaque semis, et la même salle sert de soutenance en soutenance —
ce que le modèle autorise sans contrainte.

```java
package ci.univ.soutenances.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ci.univ.soutenances.model.Departement;
import ci.univ.soutenances.model.Encadreur;
import ci.univ.soutenances.model.FabriqueEntites;
import ci.univ.soutenances.model.Filiere;
import ci.univ.soutenances.model.Grade;
import ci.univ.soutenances.model.Memoire;
import ci.univ.soutenances.model.RoleUtilisateur;
import ci.univ.soutenances.model.Salle;
import ci.univ.soutenances.model.StatutSoutenance;
import ci.univ.soutenances.model.Soutenance;

class SoutenanceDaoTest extends DaoTestBase {

    private static final LocalDateTime JOUR_10 = LocalDateTime.of(2026, 6, 10, 9, 0);
    private static final LocalDateTime JOUR_12 = LocalDateTime.of(2026, 6, 12, 14, 0);
    private static final LocalDateTime JOUR_15 = LocalDateTime.of(2026, 6, 15, 10, 30);

    private SoutenanceDao dao;

    @BeforeEach
    void creerDao() {
        dao = new SoutenanceDao(em);
    }

    @AfterEach
    void nettoyerLesTables() {
        annulerTransaction();
        ouvrirTransaction();
        em.createQuery("DELETE FROM Soutenance").executeUpdate();
        em.createQuery("DELETE FROM Etudiant").executeUpdate();
        em.createQuery("DELETE FROM Memoire").executeUpdate();
        em.createQuery("DELETE FROM Encadreur").executeUpdate();
        em.createQuery("DELETE FROM Utilisateur").executeUpdate();
        em.createQuery("DELETE FROM Filiere").executeUpdate();
        em.createQuery("DELETE FROM Grade").executeUpdate();
        em.createQuery("DELETE FROM Salle").executeUpdate();
        em.createQuery("DELETE FROM Departement").executeUpdate();
        validerTransaction();
    }

    // Département, grade, compte et encadreur persistés ; à appeler dans une transaction ouverte.
    private Encadreur semerReferentielsEtEncadreur() {
        Departement info = FabriqueEntites.nouveauDepartement("INFO", "Informatique");
        Grade m1 = FabriqueEntites.nouveauGrade("M1", "Master 1");
        em.persist(info);
        em.persist(m1);
        Encadreur encadreur = FabriqueEntites.nouvelEncadreur("Konan", "Ali", "ali.konan@univ.ci",
                m1, info, FabriqueEntites.nouvelUtilisateur("a.konan", "hash-de-test",
                        RoleUtilisateur.ENCADREUR));
        em.persist(encadreur);
        return encadreur;
    }

    // Salle persistée dans le département de l'encadreur ; à appeler dans une transaction ouverte.
    private Salle semerSalle(Encadreur encadreur) {
        Salle salle = FabriqueEntites.nouvelleSalle("Salle C01", 30, encadreur.getDepartement());
        em.persist(salle);
        return salle;
    }

    // Trois mémoires distincts persistés ; à appeler dans une transaction ouverte.
    private List<Memoire> semerTroisMemoires(Encadreur encadreur) {
        List<Memoire> memoires = List.of(
                FabriqueEntites.nouveauMemoire("Memoire 1", "description",
                        LocalDate.of(2026, 1, 10), encadreur),
                FabriqueEntites.nouveauMemoire("Memoire 2", "description",
                        LocalDate.of(2026, 1, 11), encadreur),
                FabriqueEntites.nouveauMemoire("Memoire 3", "description",
                        LocalDate.of(2026, 1, 12), encadreur));
        for (Memoire memoire : memoires) {
            em.persist(memoire);
        }
        return memoires;
    }

    // Une soutenance par statut, sur trois mémoires distincts (uk_soutenance_memoire) ;
    // à appeler dans une transaction ouverte.
    private void semerTroisSoutenancesDeStatutsDifferents() {
        Encadreur encadreur = semerReferentielsEtEncadreur();
        Salle c01 = semerSalle(encadreur);
        Memoire memoirePlanifiee = FabriqueEntites.nouveauMemoire("Memoire planifie", "description",
                LocalDate.of(2026, 1, 10), encadreur);
        Memoire memoireEvaluee = FabriqueEntites.nouveauMemoire("Memoire evalue", "description",
                LocalDate.of(2026, 1, 11), encadreur);
        Memoire memoireAnnulee = FabriqueEntites.nouveauMemoire("Memoire annule", "description",
                LocalDate.of(2026, 1, 12), encadreur);
        em.persist(memoirePlanifiee);
        em.persist(memoireEvaluee);
        em.persist(memoireAnnulee);

        // PLANIFIEE est la valeur par défaut de l'entité : la fabrique ne fixe pas le statut.
        em.persist(FabriqueEntites.nouvelleSoutenance(JOUR_10, memoirePlanifiee, c01));

        Soutenance soutenanceEvaluee = FabriqueEntites.nouvelleSoutenance(JOUR_12, memoireEvaluee, c01);
        soutenanceEvaluee.setStatut(StatutSoutenance.EVALUEE);
        em.persist(soutenanceEvaluee);

        Soutenance soutenanceAnnulee = FabriqueEntites.nouvelleSoutenance(JOUR_15, memoireAnnulee, c01);
        soutenanceAnnulee.setStatut(StatutSoutenance.ANNULEE);
        em.persist(soutenanceAnnulee);
    }

    // ══ Test critique de la tâche : syntaxe d'entité-join Hibernate 6 (design §7, risque 1) ══
    // Sans Etudiant rattaché au mémoire, ce test ne prouve rien : c'est l'étudiant qui est le
    // seul pont entre la soutenance et la filière.
    @Test
    void findAllFiltreesParFiliereJointEtudiantParSonPontMemoire() {
        ouvrirTransaction();
        Encadreur encadreur = semerReferentielsEtEncadreur();
        Departement info = encadreur.getDepartement();
        Filiere miage = FabriqueEntites.nouvelleFiliere("M1MI", "Master 1 MIAGE", info);
        Filiere autreFiliere = FabriqueEntites.nouvelleFiliere("M2IL", "Master 2 Info", info);
        em.persist(miage);
        em.persist(autreFiliere);
        Salle c01 = semerSalle(encadreur);

        Memoire memoireMiage = FabriqueEntites.nouveauMemoire("Memoire MIAGE", "description",
                LocalDate.of(2026, 1, 10), encadreur);
        Memoire memoireAutre = FabriqueEntites.nouveauMemoire("Memoire Info", "description",
                LocalDate.of(2026, 1, 11), encadreur);
        Memoire memoireSansEtudiant = FabriqueEntites.nouveauMemoire("Memoire sans etudiant",
                "description", LocalDate.of(2026, 1, 12), encadreur);
        em.persist(memoireMiage);
        em.persist(memoireAutre);
        em.persist(memoireSansEtudiant);

        em.persist(FabriqueEntites.nouvelleSoutenance(JOUR_10, memoireMiage, c01));
        em.persist(FabriqueEntites.nouvelleSoutenance(JOUR_12, memoireAutre, c01));
        em.persist(FabriqueEntites.nouvelleSoutenance(JOUR_15, memoireSansEtudiant, c01));

        em.persist(FabriqueEntites.nouvelEtudiant("ETU001", "Diabate", "Mamadou",
                "m.diabate@univ.ci", miage, memoireMiage,
                FabriqueEntites.nouvelUtilisateur("m.diabate", "hash-de-test",
                        RoleUtilisateur.ETUDIANT)));
        em.persist(FabriqueEntites.nouvelEtudiant("ETU002", "Yao", "Kouassi",
                "k.yao@univ.ci", autreFiliere, memoireAutre,
                FabriqueEntites.nouvelUtilisateur("k.yao", "hash-de-test",
                        RoleUtilisateur.ETUDIANT)));
        validerTransaction();

        ouvrirTransaction();
        List<Soutenance> soutenances = dao.findAllFiltrees(null, null, null, miage.getId());
        validerTransaction();

        // Seule la soutenance de l'étudiant M1MI revient : l'autre filière est exclue, et le
        // mémoire sans étudiant aussi — le pont Etudiant.memoire est indispensable.
        assertEquals(1, soutenances.size());
        assertEquals("Memoire MIAGE", soutenances.get(0).getMemoire().getTitre());
        assertEquals(JOUR_10, soutenances.get(0).getDateHeure());
    }

    @Test
    void findAllFiltreesSansCritereRenvoieToutesLesSoutenancesTrieesParDateHeure() {
        ouvrirTransaction();
        Encadreur encadreur = semerReferentielsEtEncadreur();
        Salle c01 = semerSalle(encadreur);
        List<Memoire> memoires = semerTroisMemoires(encadreur);
        em.persist(FabriqueEntites.nouvelleSoutenance(JOUR_15, memoires.get(0), c01));
        em.persist(FabriqueEntites.nouvelleSoutenance(JOUR_10, memoires.get(1), c01));
        em.persist(FabriqueEntites.nouvelleSoutenance(JOUR_12, memoires.get(2), c01));
        validerTransaction();

        ouvrirTransaction();
        List<Soutenance> soutenances = dao.findAllFiltrees(null, null, null, null);
        validerTransaction();

        assertEquals(3, soutenances.size());
        assertEquals(JOUR_10, soutenances.get(0).getDateHeure());
        assertEquals(JOUR_12, soutenances.get(1).getDateHeure());
        assertEquals(JOUR_15, soutenances.get(2).getDateHeure());
    }

    @Test
    void findAllFiltreesParStatutCompareLEnumStatutSoutenance() {
        ouvrirTransaction();
        semerTroisSoutenancesDeStatutsDifferents();
        validerTransaction();

        ouvrirTransaction();
        List<Soutenance> annulees = dao.findAllFiltrees(StatutSoutenance.ANNULEE, null, null, null);
        List<Soutenance> planifiees = dao.findAllFiltrees(StatutSoutenance.PLANIFIEE, null, null, null);
        validerTransaction();

        assertEquals(1, annulees.size());
        assertEquals(JOUR_15, annulees.get(0).getDateHeure());
        assertEquals(1, planifiees.size());
        assertEquals(JOUR_10, planifiees.get(0).getDateHeure());
    }

    @Test
    void findAllFiltreesParPeriodeDeDateAvecLesDeuxBornes() {
        ouvrirTransaction();
        Encadreur encadreur = semerReferentielsEtEncadreur();
        Salle c01 = semerSalle(encadreur);
        List<Memoire> memoires = semerTroisMemoires(encadreur);
        em.persist(FabriqueEntites.nouvelleSoutenance(JOUR_10, memoires.get(0), c01));
        em.persist(FabriqueEntites.nouvelleSoutenance(JOUR_12, memoires.get(1), c01));
        em.persist(FabriqueEntites.nouvelleSoutenance(JOUR_15, memoires.get(2), c01));
        validerTransaction();

        ouvrirTransaction();
        List<Soutenance> soutenances = dao.findAllFiltrees(null,
                LocalDateTime.of(2026, 6, 11, 0, 0), LocalDateTime.of(2026, 6, 13, 0, 0), null);
        validerTransaction();

        assertEquals(1, soutenances.size());
        assertEquals(JOUR_12, soutenances.get(0).getDateHeure());
    }

    @Test
    void findAllFiltreesParPeriodeDeDateSansDebutFiltreJusquALaFin() {
        ouvrirTransaction();
        Encadreur encadreur = semerReferentielsEtEncadreur();
        Salle c01 = semerSalle(encadreur);
        List<Memoire> memoires = semerTroisMemoires(encadreur);
        em.persist(FabriqueEntites.nouvelleSoutenance(JOUR_10, memoires.get(0), c01));
        em.persist(FabriqueEntites.nouvelleSoutenance(JOUR_12, memoires.get(1), c01));
        em.persist(FabriqueEntites.nouvelleSoutenance(JOUR_15, memoires.get(2), c01));
        validerTransaction();

        ouvrirTransaction();
        List<Soutenance> soutenances = dao.findAllFiltrees(null, null,
                LocalDateTime.of(2026, 6, 13, 0, 0), null);
        validerTransaction();

        assertEquals(2, soutenances.size());
        assertEquals(JOUR_10, soutenances.get(0).getDateHeure());
        assertEquals(JOUR_12, soutenances.get(1).getDateHeure());
    }

    @Test
    void findAllFiltreesLisMemoireSalleEtDepartementApresFermetureDeLaTransaction() {
        ouvrirTransaction();
        Encadreur encadreur = semerReferentielsEtEncadreur();
        Salle c01 = semerSalle(encadreur);
        Memoire memoire = FabriqueEntites.nouveauMemoire("Memoire A", "description",
                LocalDate.of(2026, 1, 10), encadreur);
        em.persist(memoire);
        em.persist(FabriqueEntites.nouvelleSoutenance(JOUR_10, memoire, c01));
        validerTransaction();

        ouvrirTransaction();
        List<Soutenance> soutenances = dao.findAllFiltrees(null, null, null, null);
        validerTransaction();

        // Détache tout : sans JOIN FETCH, getMemoire(), getSalle() et getDepartement()
        // ci-dessous renverraient des proxies morts et lèveraient LazyInitializationException.
        em.clear();

        assertEquals(1, soutenances.size());
        assertEquals("Memoire A", soutenances.get(0).getMemoire().getTitre());
        assertEquals("Salle C01", soutenances.get(0).getSalle().getNomSalle());
        assertEquals(30, soutenances.get(0).getSalle().getCapacite());
        assertEquals("Informatique", soutenances.get(0).getSalle().getDepartement().getNom());
        assertEquals("INFO", soutenances.get(0).getSalle().getDepartement().getCode());
        // En revanche memoire.encadreur n'est PAS fetchée et soutenance-list.jsp ne l'affiche
        // pas : on n'y accède pas ici, ce serait une LazyInitializationException attendue.
    }

    @Test
    void findAllFiltreesRenvoieUneListeVide() {
        ouvrirTransaction();
        List<Soutenance> soutenances = dao.findAllFiltrees(null, null, null, null);
        validerTransaction();

        assertTrue(soutenances.isEmpty());
    }

    @Test
    void countTotalCompteToutesLesSoutenances() {
        ouvrirTransaction();
        assertEquals(0, dao.countTotal());
        validerTransaction();

        ouvrirTransaction();
        semerTroisSoutenancesDeStatutsDifferents();
        validerTransaction();

        ouvrirTransaction();
        assertEquals(3, dao.countTotal());
        validerTransaction();
    }

    @Test
    void countByStatutCompteChaqueStatutSeparement() {
        ouvrirTransaction();
        semerTroisSoutenancesDeStatutsDifferents();
        validerTransaction();

        ouvrirTransaction();
        long planifiees = dao.countByStatut(StatutSoutenance.PLANIFIEE);
        long evaluees = dao.countByStatut(StatutSoutenance.EVALUEE);
        long annulees = dao.countByStatut(StatutSoutenance.ANNULEE);
        validerTransaction();

        assertEquals(1, planifiees);
        assertEquals(1, evaluees);
        assertEquals(1, annulees);
    }

    @Test
    void createDeuxSoutenancesSurLeMemeMemoireEstRefuseeParLaContrainteUnique() {
        ouvrirTransaction();
        Encadreur encadreur = semerReferentielsEtEncadreur();
        Salle c01 = semerSalle(encadreur);
        Memoire memoire = FabriqueEntites.nouveauMemoire("Memoire unique", "description",
                LocalDate.of(2026, 1, 10), encadreur);
        em.persist(memoire);
        dao.create(FabriqueEntites.nouvelleSoutenance(JOUR_10, memoire, c01));
        validerTransaction();

        // uk_soutenance_memoire : un mémoire ne porte qu'une seule soutenance. L'exception
        // lève au flush du commit — son type exact dépend de la version d'Hibernate
        // (PersistenceException enrobant une ConstraintViolationException, ou cette dernière
        // directement) : on asserte donc « une exception », pas un type précis.
        ouvrirTransaction();
        assertThrows(Exception.class, () -> {
            dao.create(FabriqueEntites.nouvelleSoutenance(JOUR_12, memoire, c01));
            validerTransaction();
        });
        annulerTransaction();
        // Oublie l'entité refusée : sans cela, le prochain flush pourrait tenter de la réinsérer.
        em.clear();

        ouvrirTransaction();
        assertEquals(1, dao.countTotal());
        validerTransaction();
    }
}
```

Chaque test démarre sur des tables vides : le `create-drop` crée un schéma vierge au démarrage
de l'`EntityManagerFactory` de la classe, et le `@AfterEach` remet les tables à zéro après
chaque test.

- [ ] **Étape 8.3 — Lancer le test pour le voir échouer (rouge)**

Run : `mvn test`
Expected : `BUILD FAILURE` — échec de compilation du module de test avec
`cannot find symbol` sur `SoutenanceDao` dans `SoutenanceDaoTest.java`. `FabriqueEntites`
compile déjà (seule la classe `SoutenanceDao` manque) : c'est l'échec attendu. Si la compilation
échoue ailleurs, c'est que l'étape 8.1 a été survolée — recontrôler les signatures des fabriques
utilisées.

- [ ] **Étape 8.4 — Créer `src/main/java/ci/univ/soutenances/dao/SoutenanceDao.java`**

```java
package ci.univ.soutenances.dao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import ci.univ.soutenances.model.StatutSoutenance;
import ci.univ.soutenances.model.Soutenance;

public class SoutenanceDao extends BaseDao<Soutenance> {

    public SoutenanceDao(EntityManager em) {
        super(em);
    }

    @Override
    protected Class<Soutenance> getEntityClass() {
        return Soutenance.class;
    }

    // Écran /admin/soutenances (soutenance-list.jsp). Les quatre critères sont ouverts :
    // null = « pas de filtre de ce côté ». Seuls des fragments JPQL fixés ici et des
    // paramètres nommés entrent dans la requête — jamais de valeur utilisateur (DAO.md §7).
    public List<Soutenance> findAllFiltrees(StatutSoutenance statut, LocalDateTime debut,
            LocalDateTime fin, UUID filiereId) {
        StringBuilder jpql = new StringBuilder(
                "SELECT s FROM Soutenance s "
                        + "LEFT JOIN FETCH s.memoire "
                        + "LEFT JOIN FETCH s.salle salle "
                        + "LEFT JOIN FETCH salle.departement");
        if (filiereId != null) {
            // Aucune clé étrangère Soutenance → Etudiant : le seul pont est Etudiant.memoire,
            // association déclarée côté Etudiant, sans contrepartie côté Memoire (design §7,
            // risque 1). Jointure d'entité ad hoc en syntaxe Hibernate 6, validée par
            // SoutenanceDaoTest.findAllFiltreesParFiliereJointEtudiantParSonPontMemoire.
            jpql.append(" JOIN Memoire m ON s.memoire = m JOIN Etudiant e ON e.memoire = m");
        }

        List<String> criteres = new ArrayList<>();
        if (statut != null) {
            criteres.add("s.statut = :statut");
        }
        if (debut != null) {
            criteres.add("s.dateHeure >= :debut");
        }
        if (fin != null) {
            criteres.add("s.dateHeure <= :fin");
        }
        if (filiereId != null) {
            criteres.add("e.filiere.id = :filiereId");
        }
        if (!criteres.isEmpty()) {
            jpql.append(" WHERE ").append(String.join(" AND ", criteres));
        }
        // Tri chronologique attendu par l'écran. Aucun setMaxResults ici : /admin/soutenances
        // ne pagine pas (choix assumé, justifié en tête de tâche — design §7, risque 3).
        jpql.append(" ORDER BY s.dateHeure");

        TypedQuery<Soutenance> query = em.createQuery(jpql.toString(), Soutenance.class);
        if (statut != null) {
            // Enum passé tel quel : @Enumerated(EnumType.STRING) sur Soutenance.statut.
            // Jamais de chaîne littérale dans le JPQL (DAO.md §7).
            query.setParameter("statut", statut);
        }
        if (debut != null) {
            query.setParameter("debut", debut);
        }
        if (fin != null) {
            query.setParameter("fin", fin);
        }
        if (filiereId != null) {
            query.setParameter("filiereId", filiereId);
        }
        return query.getResultList();
    }

    // Agrégats du dashboard /admin/dashboard : ils vivent dans le DAO de l'entité, pas dans un
    // DashboardDao dédié (design §3, décision 3). countTotal() expose publiquement, sous le
    // nom attendu par le dashboard, le count() protégé hérité de BaseDao — pas de requête
    // dupliquée.
    public long countTotal() {
        return count();
    }

    public long countByStatut(StatutSoutenance statut) {
        return em.createQuery(
                "SELECT COUNT(s) FROM Soutenance s WHERE s.statut = :statut",
                Long.class)
                .setParameter("statut", statut)
                .getSingleResult();
    }
}
```

Points à pouvoir expliquer à l'oral :

- la jointure `JOIN Etudiant e ON e.memoire = m` et **pourquoi** elle est nécessaire (aucune
  clé `Soutenance` → `Etudiant`, `Etudiant.memoire` est le seul pont, déclaré côté `Etudiant`) ;
  le test du filtre en est la preuve exécutable ;
- `findAllFiltrees` n'assemble que des **fragments fixes** et ne pose que des **paramètres
  nommés** (`:statut`, `:debut`, `:fin`, `:filiereId`) : aucun `if` métier, aucune concaténation
  de valeur — la technique est celle, déjà validée, de `findByDateActionBetween` (tâche 7) ;
- les trois `LEFT JOIN FETCH` correspondent exactement à ce que `soutenance-list.jsp` affiche
  hors transaction (`DAO.md` §8, design §7 risque 2) ;
- pourquoi l'écran ne pagine pas, et pourquoi les jointures to-one sous contrainte unique
  empêchent tout doublon (tête de tâche, point 2) ;
- `countTotal()` délègue au `count()` de `BaseDao` ; `countByStatut` compare l'enum, jamais une
  chaîne. Aucune écriture d'`HistoriqueAction` ici (design §3, décision 5).

- [ ] **Étape 8.5 — Lancer le test pour le voir passer (vert)**

Run : `mvn test`
Expected : `BUILD SUCCESS` avec toute la suite verte :
`Tests run: 39, Failures: 0, Errors: 0, Skipped: 0` — les 5 tests de `BaseDaoTest`, les 3
tests de `SalleDaoTest`, les 3 tests de `EncadreurDaoTest`, les 3 tests de `FiliereDaoTest`,
les 6 tests de `MemoireDaoTest`, les 9 tests de `HistoriqueActionDaoTest` (29 à la fin de la
tâche 7) et les **10** tests de `SoutenanceDaoTest`. Deux tests portent la charge de la tâche :
`findAllFiltreesParFiliereJointEtudiantParSonPontMemoire` valide la syntaxe d'entité-join du
risque 1, et
`findAllFiltreesLisMemoireSalleEtDepartementApresFermetureDeLaTransaction` prouve les
`JOIN FETCH` du risque 2. Si un test échoue, relire l'implémentation ligne à ligne par rapport
au bloc ci-dessus avant tout autre changement — en particulier les alias `salle` et `m`, et
l'ordre `fetch` / jointure d'entité / `WHERE` / `ORDER BY`.

- [ ] **Étape 8.6 — Commit**

```bash
git add src/main/java/ci/univ/soutenances/dao/SoutenanceDao.java src/test/java/ci/univ/soutenances/dao/SoutenanceDaoTest.java
git commit -m "feat: SoutenanceDao (liste filtrée, compte total et par statut)"
```
