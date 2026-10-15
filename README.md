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

Pour l’envoi d’e-mails, chaque personne configure son propre serveur SMTP dans
l’environnement du processus WildFly : `SMTP_HOST`, `SMTP_FROM`,
`SMTP_USERNAME`, `SMTP_PASSWORD` et éventuellement `SMTP_PORT`, `SMTP_STARTTLS`
ou `SMTP_SSL`. Par défaut, le port est `587` avec STARTTLS ; pour TLS implicite,
utiliser le port `465`, `SMTP_SSL=true` et `SMTP_STARTTLS=false`. Les variables
d’identifiant et de mot de passe doivent être définies ensemble ; pour un relais
sans authentification, omettre les deux. Ne jamais ajouter ces secrets à GitHub.
La création d’un compte d’encadreur ou d’étudiant échoue et est annulée si les
paramètres SMTP manquent ou si le message ne peut pas être envoyé.

Dans Eclipse, chaque développeur peut définir ces variables dans **Run
Configurations > WildFly > Environment**, puis redémarrer le serveur. Pour un
lancement direct sous PowerShell, définir `$env:SMTP_HOST`, `$env:SMTP_PORT`,
`$env:SMTP_FROM`, `$env:SMTP_USERNAME`, `$env:SMTP_PASSWORD` et `$env:SMTP_STARTTLS`
dans le terminal avant de lancer `standalone.bat`. Chaque personne qui héberge
sa propre instance peut alors utiliser son propre SMTP. Pour une instance
partagée en ligne, seul l’administrateur du serveur configure le SMTP utilisé
par l’application.

## Module administrateur A — Paramètres & Référentiels

Les routes `/admin/grades`, `/admin/departements`, `/admin/filieres` et
`/admin/salles` proposent les opérations de liste, ajout, modification et
suppression. Les JSP correspondantes (`grade-list.jsp`, `grade-form.jsp`,
`departement-list.jsp`, `departement-form.jsp`, `filiere-list.jsp`,
`filiere-form.jsp`, `salle-list.jsp`, `salle-form.jsp`) se trouvent sous
`src/main/webapp/WEB-INF/views/admin`.

Ces routes sont réservées à une session contenant un `SessionUser` de rôle
`ADMIN` dans l’attribut `utilisateurConnecte`. Le filtre refuse les requêtes
non authentifiées et vérifie un jeton CSRF sur les formulaires POST.
Chaque formulaire affiche l’historique persistant des créations, modifications
et suppressions de son écran. Cet historique indique la date, le libellé,
l’action, l’utilisateur et l’écran, avec quatre entrées par page ; il n’apparaît
pas sur les listes.

## Module administrateur B — Acteurs & Comptes

- `/admin/encadreurs` : recherche et gestion des encadreurs, avec grade et
  département obligatoires.
- `/admin/etudiants` : recherche par nom/matricule, filière obligatoire et
  association facultative à un mémoire existant.
- `/admin/utilisateurs` : activation/désactivation des comptes et envoi d’un
  mot de passe temporaire neuf par e-mail.

À la création d’un encadreur ou d’un étudiant, l’adresse e-mail fournie est
utilisée comme login et un mot de passe temporaire aléatoire de six caractères
est généré. L’application envoie ces identifiants à cette adresse et ne stocke
que le mot de passe haché. Modifier l’adresse d’un acteur met également à jour
son login lié. La connexion est refusée pour les comptes désactivés.

## Connexion

1. Ouvrir `/login`.
2. Lors de la première utilisation, ouvrir le lien de création du premier
   administrateur depuis cette page. Cette configuration initiale ne répond
   qu'aux requêtes locales et se désactive dès qu'un administrateur est créé.
3. Créer le compte avec un mot de passe d'au moins 8 caractères, puis se
   connecter. Les mots de passe sont stockés hachés avec PBKDF2-HMAC-SHA256.
4. L'administrateur est redirigé vers `/admin/departements`. La déconnexion se
   fait par le bouton de session.

Les autres rôles peuvent se connecter, mais leurs écrans ne sont pas encore
implémentés ; ils verront une page temporaire qui l'indique.

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
