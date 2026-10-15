## CAHIER DE CHARGE

## 1. Comprendre le projet

L'objectif est de développer une application web qui permet de gérer l'organisation des soutenances de mémoire.

<!-- image -->

## 1. Administrateur

-  Gérer les étudiants et les encadreurs.
-  Planifier les soutenances.
-  Composer les jurys.
-  Gérer les comptes utilisateurs.
2. Encadreur ou membre du jury
-  Consulter ses soutenances.
-  Attribuer une note sur 20.
-  Rédiger un avis.

<!-- image -->

<!-- image -->

## 3. Étudiant

-  Consulter la date et l'heure de sa soutenance.
-  Connaître la salle et les membres du jury.
-  Consulter sa note finale lorsque les évaluations sont terminées.

## 2. Les technologies à utiliser

| Technologie          | Rôle dans le projet          |
|----------------------|------------------------------|
| Java EE / Jakarta EE | Développer l'application web |
| Eclipse              | Écrire et organiser le code  |
| WildFly              | Exécuter l'application       |

MySQL

Servlet Stocker les données Recevoir et traiter les requêtes

JSP, EL et JSTL

Afficher les pages

JDBC et PreparedStatement Communiquer avec la base de données

MVC

Séparer le contrôleur, le modèle et la vue

HttpSession

JPE

Gérer la connexion des utilisateurs

Front end :react

## 3. Notre plan de travail

Étape 1 - Concevoir la base de données

Définir les tables, leurs attributs, les clés primaires et étrangères, puis les relations entre étudiants, encadreurs, soutenances et évaluations.

Étape 2 - Préparer le projet Eclipse

Créer le projet web, configurer WildFly et connecter l'application à MySQL.

Étape 3 - Créer l'authentification

Mettre en place la connexion, les rôles, les sessions et les redirections.

Étape 4 - Développer les fonctionnalités

Réaliser les CRUD, la planification, la composition des jurys et les évaluations.

Étape 5 - Tester et préparer la soutenance

Vérifier la sécurité, les fonctionnalités, le rapport et la démonstration orale.

JDBC + PreparedStatement - conforme aux TP et adapté pour apprendre les requêtes SQL.

JPA / Hibernate - pour travailler avec des entités Java et leurs relations.

| Entité      | Attributs principaux                                       | Relations                                                                                                                  |
|-------------|------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| Utilisateur | id, login, motDePasse, role (ADMIN / ENCADREUR / ETUDIANT) | Table de base pour l'authentification ; peut être fusionnée avec Étudiant / Encadreur ou reliée à eux, au choix du groupe. |

| Entité      | Attributs principaux                                         | Relations                                                                                                                  |
|-------------|--------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| Etudiant    | id, matricule, nom, prenom, filiere, email                   | 1 étudiant a 1 sujet et 1 soutenance (0 ou 1) ; 1 étudiant a 1 encadreur.                                                  |
| Encadreur   | id, nom, prenom, specialite, email                           | 1 encadreur encadre plusieurs étudiants (1-N) ; peut siéger dans plusieurs jurys (N-N avec Soutenance).                    |
| Soutenance  | id, dateHeure, salle, statut (PLANIFIEE / EVALUEE / ANNULEE) | 1 soutenance concerne 1 étudiant (1-1) ; 1 soutenance a plusieurs membres de jury (N-N avec Encadreur).                    |
| Evaluation  | id, note, avis                                               | Table de liaison entre Soutenance et Encadreur (jury) : porte la note donnée par chaque membre pour une soutenance donnée. |
| Utilisateur | id, login, motDePasse, role (ADMIN / ENCADREUR / ETUDIANT)   | Table de base pour l'authentification ; peut être fusionnée avec Étudiant / Encadreur ou reliée à eux, au choix du groupe. |
| Etudiant    | id, matricule, nom, prenom, filiere, email                   | 1 étudiant a 1 sujet et 1 soutenance (0 ou 1) ; 1 étudiant a 1 encadreur.                                                  |

Bon, les gars, j'ai mis à jour le MLD. On passe à 11 tables bien propres pour que tout soit nickel et normalisé :

## 1. Config &amp; Références

Utilisateur (id\_utilisateur, login, mot\_de\_passe, role)

Departement (id\_departement, code, nom)

Grade (id\_grade, code, libelle)

Filiere (id\_filiere, code, libelle, #departement\_id) -&gt; rattachée à son département Salle (id\_salle, nom\_salle, capacite, #departement\_id) -&gt; rattachée à son département

2. Acteurs &amp; Mémoires

Encadreur (id\_encadreur, nom, prenom, email, #grade\_id, #departement\_id, #utilisateur\_id) -&gt; rattaché à son compte, son grade et son dep

Memoire (id\_memoire, titre, description, date\_depot, #encadreur\_id) -&gt; stocke le projet et l'encadreur principal

Etudiant (id\_etudiant, matricule, nom, prenom, email, #filiere\_id, #memoire\_id, #departement\_id, #utilisateur\_id) -&gt; lié à sa filière

3. Soutenances, Jurys &amp; Logs

Soutenance (id\_soutenance, date\_heure, statut, #memoire\_id, #salle\_id) -&gt; liée au mémoire et à la salle

Evaluation (id\_evaluation, note, avis, #soutenance\_id, #encadreur\_id) -&gt; la table du jury pour lier les profs aux soutenances avec leurs notes

Historique\_Action (id\_historique, type\_action, date\_action, description, #utilisateur\_id) -&gt; pour tracer les actions dans le système