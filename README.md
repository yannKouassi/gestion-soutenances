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

### Configurer l’envoi d’e-mails (chaque développeur)

L’application envoie les identifiants des nouveaux comptes par le serveur SMTP
du processus WildFly. Chaque développeur configure ses propres identifiants
localement ; il n’y a rien à modifier dans le code et aucune adresse de
fournisseur n’est imposée.

1. Récupérer auprès de son fournisseur de messagerie le nom d’hôte SMTP, le
   port et le mode de chiffrement. Exemple pour Gmail : `smtp.gmail.com`,
   port `587`, STARTTLS. Le login est généralement l’adresse complète du compte
   et l’adresse `SMTP_FROM` doit être autorisée par ce compte.
2. Dans Eclipse, ouvrir **Run > Run Configurations… > WildFly**, sélectionner
   la configuration du serveur utilisé, puis l’onglet **Environment**. Cliquer
   sur **New…** pour ajouter chaque variable ci-dessous. Saisir uniquement la
   valeur dans la colonne « Valeur », sans guillemets :

   | Variable | Valeur |
   | --- | --- |
   | `SMTP_HOST` | Hôte SMTP du fournisseur, par ex. `smtp.gmail.com` |
   | `SMTP_PORT` | `587` avec STARTTLS, ou `465` avec TLS implicite |
   | `SMTP_FROM` | Adresse d’expédition autorisée |
   | `SMTP_USERNAME` | Login SMTP, souvent l’adresse e-mail complète |
   | `SMTP_PASSWORD` | Mot de passe SMTP ou mot de passe d’application |
   | `SMTP_STARTTLS` | `true` pour le port `587`; `false` pour le port `465` |
   | `SMTP_SSL` | `false` pour le port `587`; `true` pour le port `465` |

   Pour Gmail, si Google demande une vérification supplémentaire, activer la
   validation en deux étapes et créer un **mot de passe d’application** à
   utiliser comme `SMTP_PASSWORD` ; ne pas utiliser ni partager son mot de passe
   Google habituel. Google affiche ce mot de passe en groupes : le copier sans
   les espaces. Les comptes Google Workspace peuvent être soumis aux règles de
   leur administrateur.
3. Cliquer sur **Apply**, arrêter puis redémarrer WildFly depuis Eclipse. Les
   variables définies dans Windows ou dans un autre terminal ne sont pas
   automatiquement reprises par un Eclipse déjà ouvert.
4. Tester la création d’un compte avec une adresse e-mail que l’on peut
   consulter ; l’application envoie les identifiants temporaires par courriel.

#### Lancer WildFly depuis PowerShell (alternative à Eclipse)

Cette méthode évite d’enregistrer le mot de passe dans la configuration de
lancement Eclipse. Remplacer l’adresse par son compte autorisé à envoyer des
messages et adapter le chemin de WildFly à son installation. Exécuter les
commandes dans **la même fenêtre PowerShell** :

```powershell
$env:SMTP_HOST = "smtp.gmail.com"
$env:SMTP_PORT = "587"
$env:SMTP_FROM = "votre-adresse@gmail.com"
$env:SMTP_USERNAME = "votre-adresse@gmail.com"
$env:SMTP_STARTTLS = "true"
$env:SMTP_SSL = "false"
$motDePasse = Read-Host "Mot de passe d’application SMTP" -AsSecureString
$env:SMTP_PASSWORD = [System.Net.NetworkCredential]::new("", $motDePasse).Password
& "C:\wildfly\wildfly-37.0.1.Final\bin\standalone.bat"
```

Garder la fenêtre ouverte pendant l’utilisation du serveur. Dans ce mode, ne pas
démarrer en parallèle une autre instance WildFly depuis Eclipse sur le même
port. Fermer la fenêtre arrête le serveur et les variables SMTP de ce processus.

Avec un autre fournisseur, remplacer l’hôte, le port, le mode TLS et les
identifiants par les paramètres SMTP documentés par ce fournisseur. Le port
`587` avec STARTTLS est utilisé par défaut ; le port `465` exige
`SMTP_SSL=true` et `SMTP_STARTTLS=false`. `SMTP_USERNAME` et `SMTP_PASSWORD`
doivent être fournis ensemble ; pour un relais sans authentification, omettre
les deux. Ne jamais mettre ces secrets dans le dépôt, le README ou une pull
request. La création d’un compte d’encadreur ou d’étudiant échoue et est annulée
si la configuration SMTP manque ou si le message ne peut pas être envoyé.

Pour une instance partagée en ligne, seul l’administrateur du serveur configure
le SMTP utilisé par l’application ; les variables de lancement locales de
chaque développeur ne s’y appliquent pas.

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

## Module administrateur D — Pilotage & Audit

Le menu suit la hiérarchie du classeur de découpage :

- **Tableau de Bord & Statistiques** → Indicateurs & Graphiques (à venir).
- **Historique & Traçabilité** → `/admin/historique` (Journal d’Audit).

Le journal global permet de filtrer et paginer les actions enregistrées.

## Espace encadreur

L'espace encadreur est accessible aux comptes de rôle `ENCADREUR` :

- `/encadreur/dashboard` : tableau de bord et convocations à venir.
- `/encadreur/mes-etudiants` : liste des étudiants associés aux mémoires dont
  l'encadreur est le responsable.
- `/encadreur/mes-jurys` : jurys et détails des soutenances auxquelles
  l'encadreur est convoqué.
- `/encadreur/saisie-evaluation` : sélection d'un jury pour saisir ou consulter
  l'évaluation de l'encadreur.
- `/encadreur/profil` : profil de l'encadreur connecté.

Depuis le détail d'un jury, un encadreur peut saisir sa note (de 0 à 20) une
seule fois. Il ne peut consulter que les soutenances auxquelles il est convoqué ;
les notes et avis individuels des autres membres ne sont pas exposés. Le statut
de la soutenance passe à `EVALUEE` lorsque tous les membres d'un jury d'au moins
deux personnes ont noté.

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
