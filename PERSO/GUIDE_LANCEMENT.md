# Guide de lancement — Gestion des soutenances

Comment l'application démarre, pourquoi on passe par WildFly, et ce qui est
configuré concrètement sur ce poste. En français, sans jargon inutile.

## 1. Vue d'ensemble : qui fait quoi

```
Navigateur ──:8080──> WildFly ──WAR──> servlets / JSP / JPA
                                   │
                                   │ JNDI java:jboss/datasources/GestionSoutenancesDS
                                   ▼
                             pool de connexions ──:3306──> MySQL (gestion_soutenances)
```

Trois acteurs, trois rôles :

| Acteur | Rôle |
|---|---|
| **MySQL 5.7** (WAMP) | Stocke les données. Base `gestion_soutenances`, port `3306`. |
| **WildFly 41** (`C:\MULTIVxxvRS\xxv\servers\wildfly-41.0.1.Final`) | Serveur d'application Jakarta EE : il **exécute** ton code, gère le HTTP, les transactions (JTA), le pool de connexions et Hibernate. |
| **Le WAR** (`gestion-soutenances-0.0.1-SNAPSHOT.war`) | Ton application packagée (compilée par Maven). **Il n'est pas exécutable seul** : pas de `main`, pas de `java -jar`. Il vit *à l'intérieur* de WildFly. |

## 2. Pourquoi on passe forcément par le serveur d'application

C'est obligatoire, pas un choix de confort :

1. **Servlets et JSP = composants gérés.** Une servlet n'a pas de `main` ; c'est le
   conteneur web de WildFly (Undertow) qui reçoit le HTTP sur le port `8080` et
   appelle ton code.
2. **La datasource JNDI n'existe que dans le serveur.** Ton `persistence.xml`
   déclare `<jta-data-source>java:jboss/datasources/GestionSoutenancesDS</jta-data-source>` :
   ce nom n'est résolu que parce que WildFly le crée au démarrage depuis son
   `standalone.xml`. Sans serveur → JNDI inexistant → déploiement en échec.
3. **WildFly fournit les services** : HTTP (`8080`), transactions JTA,
   Hibernate/JPA, pool de connexions, console d'admin (`9990`).

Donc pour « voir l'URL de l'appli », le chemin est toujours :
**MySQL démarré → WildFly démarré → WAR déployé → ouvrir
`http://localhost:8080/gestion-soutenances-0.0.1-SNAPSHOT/`**.

## 3. Ordre de démarrage (à respecter)

1. **MySQL d'abord** (port `3306`). Sinon WildFly affiche
   `Connection refused` et le WAR échoue au déploiement.
2. **WildFly ensuite** : `bin\standalone.bat` lit `standalone\configuration\standalone.xml`,
   charge le pilote MySQL, crée le pool `GestionSoutenancesDS`, puis déploie
   tout WAR présent dans `standalone\deployments\`.
3. **Hibernate démarre en dernier** : il récupère la connexion via JNDI,
   détermine le dialecte MySQL, et crée/met à jour les tables
   (`hibernate.hbm2ddl.auto=update` — aucune table à créer à la main).

## 4. Ce qui est configuré concrètement (et pourquoi)

| Élément | Où | Pourquoi |
|---|---|---|
| Module `com.mysql` | `modules\com\mysql\main\` (JAR Connector/J 8.4.0 + `module.xml`) | WildFly isole chaque librairie en module JBoss : sans ça, le serveur ne « voit » pas le driver MySQL. |
| `<driver name="mysql">` | `standalone.xml`, section `<drivers>` | Déclare le pilote JDBC (`com.mysql.cj.jdbc.Driver`) utilisable par les datasources. |
| Datasource `GestionSoutenancesDS` | `standalone.xml`, JNDI `java:jboss/datasources/GestionSoutenancesDS` | URL, login, pool (2–10), test `SELECT 1`. C'est le pont entre `persistence.xml` et MySQL. |
| Paramètres URL JDBC | `useSSL=false`, `serverTimezone=UTC`, `characterEncoding=UTF-8` | Local sans SSL ; timezone explicite (sinon erreur du driver) ; **UTF-8 côté Java** — `utf8mb4` est un charset MySQL, pas Java : l'avoir mis a cassé le 1er démarrage (`Unsupported character encoding 'utf8mb4'`). |
| Compte MySQL `soutenances` | Serveur MySQL, droits complets sur `gestion_soutenances` uniquement | Le README interdit le compte `root` pour l'appli. Mot de passe défini **localement**, jamais committé. |
| Ports | `8080` appli, `9990` console admin, `3306` MySQL | `8080` imposé par le projet : si occupé, libérer le port, ne pas reconfigurer WildFly. |

## 5. Lancer sans Eclipse (procédure de ce poste)

Pré-requis shell : `JAVA_HOME` → JDK 21.

**a) Démarrer MySQL** — deux options, **une seule à la fois** (conflit de port sinon) :
- Recommandé : le manager WAMP (icône barre des tâches → *Start All Services*).
- Secours (utilisé ici quand les services WAMP refusaient de démarrer) :
  `mysqld.exe --console --datadir=C:/wamp/bin/mysql/mysql5.7.40/data --port=3306`
  en arrière-plan. Avant de relancer WAMP plus tard, tuer ce processus manuel.

**b) Démarrer WildFly** : exécuter `bin\standalone.bat` (fenêtre réduite).
Surveiller `standalone\log\server.log` jusqu'à
`WFLYSRV0025: WildFly ... started` et `Bound data source [java:jboss/datasources/GestionSoutenancesDS]`.

**c) Déployer** : copier le WAR buildé (`target\gestion-soutenances-0.0.1-SNAPSHOT.war`)
dans `standalone\deployments\`. Le scanner le déploie seul (~5 s) :
`nom.war.deployed` = OK, `nom.war.failed` = lire `server.log`.

**d) Vérifier** :
- Appli : `http://localhost:8080/gestion-soutenances-0.0.1-SNAPSHOT/`
- Console admin : `http://localhost:9990` (nécessite un user créé via `bin\add-user.bat`)
- Test datasource via CLI :
  `bin\jboss-cli.bat --connect --command="/subsystem=datasources/data-source=GestionSoutenancesDS:test-connection-in-pool"`

**e) Arrêter** : `bin\jboss-cli.bat --connect --command=":shutdown"`.
Ne jamais juste fermer la fenêtre en plein déploiement.

## 6. Lancer avec Eclipse (rappel README)

Rien n'est à copier dans le projet. Dans Eclipse (package *Enterprise Java*) :
1. *Window > Preferences > Server > Runtime Environments > Add* → WildFly 41,
   chemin `C:\MULTIVxxvRS\xxv\servers\wildfly-41.0.1.Final`, JDK 21.
2. Vue *Servers > New > Server* → le runtime, *Add/Remove* → ajouter le projet.
3. *Run* : Eclipse publie le WAR lui-même et ouvre l'URL.
   (Serveur externe type WildFly Tools requis — pré-installé dans le package JEE ;
   sinon l'installer depuis le Marketplace.)

## 7. Dépannage (erreurs déjà rencontrées sur ce poste)

| Symptôme dans `server.log` | Cause | Fix |
|---|---|---|
| `Connection refused: connect` / `IJ000453` | MySQL éteint | Démarrer MySQL (§5a), puis redéployer (supprimer le `.failed`, « toucher » le WAR). |
| `Unsupported character encoding 'utf8mb4'` | Paramètre JDBC invalide côté Java | `characterEncoding=UTF-8`, jamais `utf8mb4`. |
| `Unable to determine Dialect` | Conséquence d'une connexion HS, pas un problème Hibernate | Réparer la datasource, pas le `persistence.xml`. |
| JVM `paging file too small` / G1 `mmap failed` | RAM épuisée (< 300 Mo libres vus ici) | Libérer de la mémoire, ou forcer `-XX:+UseSerialGC -Xms128M -Xmx512M` via `JBOSS_JAVA_SIZING`. |
| `service 'wampmysqld' cannot be started` | Droits insuffisants pour piloter le service Windows | Lancer WAMP en admin, ou `mysqld.exe` manuel (§5a). |

## 8. Règles à ne jamais enfreindre (README + AGENTS.md)

- Ne committer **ni** mot de passe, **ni** `standalone.xml`, **ni** WAR, **ni** `target/`.
- Le dossier `C:\MULTIVxxvRS\xxv\servers\` est **hors du dépôt Git** : c'est voulu.
- Port `8080` fixe. Une branche par fonctionnalité, PR vers `main`.
