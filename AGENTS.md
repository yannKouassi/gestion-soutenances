# AGENTS.md — Gestion des soutenances (PGS-PFE)

Projet académique M1 MIAGE, cours "Programmation JavaEE" : plateforme de gestion des soutenances de projets de fin d'études. Équipe de 3–4, ~6 semaines, noté sur 20 points. Une partie de la note porte sur la capacité de chaque membre à défendre oralement son propre code : tout code ajouté doit pouvoir être expliqué par son auteur.

Ce document décrit l'état réel du dépôt et les règles à respecter. Ce qui n'est pas listé comme existant n'existe pas.

## Environnement et build

- Projet Maven WAR : `ci.univ.soutenances:gestion-soutenances:0.0.1-SNAPSHOT`, packaging `war`.
- Java 17 (`maven.compiler.release=17`). APIs Jakarta EE 10 en scope `provided` : `jakarta.servlet-api` 6.0.0, `jakarta.persistence-api` 3.1.0, `mysql-connector-j` 8.4.0. Il n'y a pas d'autre dépendance dans le `pom.xml` — toute dépendance ajoutée (JSTL, etc.) doit y être déclarée explicitement.
- Base package `ci.univ.soutenances` : sources dans `src/main/java/ci/univ/soutenances/`, web dans `src/main/webapp/`, ressources dans `src/main/resources/`.
- IDE et serveur de dev : Eclipse IDE for Enterprise Java and Web + WildFly 37.x, port fixé à 8080. URL de déploiement : `http://localhost:8080/gestion-soutenances-0.0.1-SNAPSHOT/`.
- Unité de persistance JPA `GestionSoutenancesPU`, `transaction-type="JTA"`, datasource JNDI `java:jboss/datasources/GestionSoutenancesDS`, `hibernate.hbm2ddl.auto=update`, `show_sql=false`, `format_sql=true` — fichier `src/main/resources/META-INF/persistence.xml`.
- `src/main/resources/db/create-database.sql` ne crée que le schéma MySQL `gestion_soutenances` (utf8mb4 / utf8mb4_unicode_ci). Les tables sont générées par Hibernate (`hbm2ddl.auto=update`), **pas** par un script DDL. N'ajoutez pas de DDL de tables et ne supposez pas qu'un tel script existe.
- Build : `target/` est du output de build, jamais committé (voir `.gitignore`).
- Git : workflow attendu : une branche par fonctionnalité, PR vers `main`, commit uniquement du code et de la config non secrète. Format des messages et journal : voir « Commits et changelog » plus bas.

## Commits et changelog

Les règles d'écriture des commits (branches, format des messages, corps en
puces, gabarit) et le fonctionnement du changelog sont dans [[references/changelog-rules.md]].

En résumé : une branche `feat/...` est une feature, ses commits propres forment
la feature, chaque commit non cosmétique porte un corps de 2 à 6 puces en
français, et `CHANGELOGS.md` porte une section par feature dont le squelette est
posé par `scripts/nouvelle-changelog.ps1`. Avant d'écrire un commit ou une
section de changelog, lis ce fichier : c'est là que sont les gabarits.

## Modèle de données

Référentiels : `Utilisateur`, `Departement`, `Grade`, `Filiere`, `Salle`. Acteurs et mémoires : `Encadreur`, `Memoire`, `Etudiant`. Soutenances et audit : `Soutenance`, `Evaluation`, `HistoriqueAction`.

Relation centrale et plus technique du projet : `Soutenance` × `Encadreur` est N-N **exprimée par l'entité `Evaluation`**, qui porte `note` (`BigDecimal`, precision 4, scale 2) et `avis` (`TEXT`). Contrainte unique `uk_evaluation_soutenance_encadreur` sur (`soutenance_id`, `encadreur_id`). Un DAO dédié pour cette relation est explicitement exigé.

Règles métier du cahier des charges :

- Un jury comporte au moins deux membres.
- Une soutenance n'est déclarée `EVALUEE` qu'une fois que **tous** les membres du jury ont soumis une note ; la note finale affichée est la moyenne des notes du jury.
- Une soutenance porte sur un seul mémoire, et un mémoire a au moins une soutenance (`Soutenance.memoire` est `@OneToOne` + unique).
- Un étudiant ne voit que les données de sa propre soutenance ; un membre du jury ne voit que les soutenances auxquelles il est convoqué.

## Conventions du modèle (à respecter pour toute nouvelle entité)

Observées dans `model/`, à reproduire à l'identique :

- Vocabulaire métier français, identifiants techniques anglais. Champs et getters Java en `camelCase` ; tables SQL en PascalCase identique au nom de la classe ; colonnes en `snake_case`.
- Toute entité étend `BaseEntity` et redéfinit sa colonne d'identifiant :
  `@AttributeOverride(name = "id", column = @Column(name = "id_<table_snake>", nullable = false, updatable = false))`
- Déclaration `@Table` avec `@UniqueConstraint` explicites là où le cahier des charges l'exige.
- Constructeur sans argument `protected`.
- Toutes les associations en `FetchType.LAZY`.
- Pas de Javadoc exigée : les classes existantes n'en ont pas. N'en mandatez pas et n'en ajoutez pas par défaut.

## Règles techniques non négociables (cahier des charges)

Ces règles sont évaluées à l'oral. Toute contribution les enfreignant doit être refusée.

- **MVC strict** : Servlet = contrôleur, JSP = vue, classes Java = modèle + DAO/service. Aucune logique métier dans les JSP, aucun HTML dans les servlets.
- **JSP** : uniquement EL (`${...}`) et JSTL (`c:forEach`, `c:if`). Aucun scriptlet Java dans les JSP.
- **Persistance** : JPA/Hibernate (parti pris du groupe, au-dessus du JDBC brut) ou `PreparedStatement` pour tout SQL écrit à la main. Toute concaténation de chaînes dans une requête SQL est interdite — explicitement pénalisée à l'oral.
- **Mots de passe** : jamais stockés en clair. Un hash est requis, au minimum SHA-256.
- **Session** : `HttpSession`. Chaque page protégée doit vérifier la session **côté contrôleur** et rediriger selon le rôle.
- **DAO** : une classe DAO/repository dédiée, distincte des servlets, exigée au moins pour la relation N-N `Soutenance`/`Encadreur`.

## Architecture cible (à construire, pas encore existante)

Séparer sous `ci.univ.soutenances` : `model`, `dao`, `service` si nécessaire, `servlet` (ou un sous-package par acteur), et un taglib JSTL / helper de vue pour les JSP.

Écrans par acteur, avec le mapping JSP / URL déjà convenu en équipe. Créez chaque servlet et sa JSP à l'URL indiquée.

**A. Paramètres & Référentiels (Administrateur)**

- `grade-list.jsp` `/admin/grades`, `grade-form.jsp`
- `departement-list.jsp` `/admin/departements`, `departement-form.jsp`
- `filiere-list.jsp` `/admin/filieres`, `filiere-form.jsp`
- `salle-list.jsp` `/admin/salles`, `salle-form.jsp`

**B. Acteurs & Comptes (Administrateur)**

- `encadreur-list.jsp` `/admin/encadreurs`, `encadreur-form.jsp`
- `etudiant-list.jsp` `/admin/etudiants`, `etudiant-form.jsp`
- `utilisateur-list.jsp` `/admin/utilisateurs`

**C. Académique & Soutenances (Administrateur)**

- `memoire-list.jsp` `/admin/memoires`, `memoire-form.jsp`
- `soutenance-form.jsp` `/admin/planification`, `jury-form.jsp`
- `soutenance-list.jsp` `/admin/soutenances`

**D. Pilotage & Audit (Administrateur)**

- `dashboard.jsp` `/admin/dashboard`
- `historique-list.jsp` `/admin/historique`

**Encadreur**

- `dashboard.jsp` `/encadreur/dashboard`
- `mes-etudiants.jsp` `/encadreur/mes-etudiants`
- `mes-jurys.jsp` `/encadreur/mes-jurys`, `evaluation-form.jsp`
- `profil.jsp` `/encadreur/profil`

**Étudiant**

- `ma-soutenance.jsp` `/etudiant/ma-soutenance`
- `profil.jsp` `/etudiant/profil`

## Charte graphique (palette imposée)

Tokens exacts issus de `documents/Charte_Graphique_Couleurs_UI_App.xlsx`. Utilisez ces valeurs en dur, sans les modifier :

- Primaire "Bleu Marine Académique" `#1E3A8A` — barre de nav, en-têtes de table, boutons principaux, titres.
- Accent "Bleu Cyan" `#0284C7` — liens actifs, boutons d'action secondaires, icônes des cartes de stats.
- Accent 2 "Indigo" `#4F46E5` — boutons d'action alternatifs, accents interactifs.
- Fond "Gris Très Clair" `#F8FAFC` — fond de l'application.
- Surfaces `#FFFFFF` — tables, formulaires, cartes de dashboard.
- Texte principal "Gris Ardoise Foncé" `#1E293B`.
- Badges de statut (texte / fond) : `PLANIFIEE` `#B45309` / `#FEF3C7` ; `EVALUEE` `#047857` / `#D1FAE5` ; `ANNULEE` `#B91C1C` / `#FEE2E2`.
- Badges de rôle (texte / fond) : `ADMIN` `#6D28D9` / `#EDE9FE` ; `ENCADREUR` `#1D4ED8` / `#DBEAFE` ; `ETUDIANT` `#0F766E` / `#CCFBF1`.
- Police utilisée dans la spec : Segoe UI.

## Documents de référence

`documents/` contient le cahier des charges (`CAHIER DE CHARGE.pdf`/`.docx`), le découpage et MLD (`Projet_JavaEE_Soutenances_Decoupage_MLD_V2.pdf`/`.xlsx`, `MLD.md`), la charte graphique (`Charte_Graphique_Couleurs_UI_App.pdf`/`.xlsx`) et le sujet complet (`Projet_Master_JavaEE_Soutenances.pdf`/`.docx`). En cas d'écart entre ce dépôt et les documents, les documents font foi pour les règles métier.
