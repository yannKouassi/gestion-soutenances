# Gestion des soutenances

Application Java EE de gestion des soutenances de mémoires, construite avec
Java 17, Jakarta EE, JPA/Hibernate, MySQL et WildFly.

## Prérequis

- JDK 17
- Eclipse IDE for Enterprise Java and Web avec Maven (m2e) et WildFly Tools
- WildFly 37.x
- MySQL Server 8.0

## Importer dans Eclipse

1. Cloner le dépôt GitHub.
2. Choisir **File > Import > Maven > Existing Maven Projects**, puis sélectionner
   le dossier du projet.
3. Choisir un JDK 17 dans **Project > Properties > Java Build Path**.
4. Faire **clic droit sur le projet > Maven > Update Project**.
5. Ajouter WildFly 37 dans la vue **Servers** et associer le projet au serveur.

## Préparer MySQL et WildFly

1. Exécuter `src/main/resources/db/create-database.sql` dans MySQL pour créer
   la base `gestion_soutenances`.
2. Créer un compte MySQL local réservé à l'application et lui accorder les
   droits sur cette base. Ne pas utiliser le compte `root` pour l'application.
3. Installer le pilote MySQL Connector/J 8.4.0 comme module WildFly `com.mysql`
   (le fichier JAR est téléchargé par Maven dans le dépôt local `.m2`).
4. Dans la console d'administration WildFly (`http://localhost:9990`), déclarer
   le pilote JDBC `mysql` (`com.mysql.cj.jdbc.Driver`), puis créer une datasource :
   - JNDI : `java:jboss/datasources/GestionSoutenancesDS`
   - URL : `jdbc:mysql://localhost:3306/gestion_soutenances`
   - utilisateur et mot de passe : ceux du compte MySQL local créé à l'étape 2.
5. Tester la connexion de la datasource depuis la console WildFly.

La persistance JPA est configurée dans `src/main/resources/META-INF/persistence.xml`.
Hibernate met à jour le schéma (`hibernate.hbm2ddl.auto=update`) au déploiement.

## Lancer le projet

Publier le projet sur WildFly depuis Eclipse, puis ouvrir :

`http://localhost:8080/gestion-soutenances-0.0.1-SNAPSHOT/`

Si le port 8080 est occupé, arrêter le service qui l'utilise avant de lancer
WildFly ; garder le port configuré à 8080.

## Travail en équipe

- Créer une branche par fonctionnalité, par exemple `feature/authentification`.
- Commiter uniquement le code et les fichiers de configuration non secrets.
- Pousser la branche sur GitHub et ouvrir une Pull Request vers `main`.
- Chaque membre configure localement MySQL et WildFly ; les mots de passe,
  `standalone.xml`, les WAR et le dossier `target/` ne doivent pas être partagés.
