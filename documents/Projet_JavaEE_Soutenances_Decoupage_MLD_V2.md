## Découpage Fonctionnel &amp; Arborescence des Vues (Java EE / JSP)

| Rôle / Menu Central   | Grand Module                 | Sous-Menu / Gestion              |
|-----------------------|------------------------------|----------------------------------|
| Administrateur        | A. Paramètres & Référentiels | Gestion des Grades               |
| Administrateur        | A. Paramètres & Référentiels | Gestion des Grades               |
| Administrateur        | A. Paramètres & Référentiels | Gestion des Départements         |
| Administrateur        | A. Paramètres & Référentiels | Gestion des Départements         |
| Administrateur        | A. Paramètres & Référentiels | Gestion des Filières             |
| Administrateur        | A. Paramètres & Référentiels | Gestion des Filières             |
| Administrateur        | A. Paramètres & Référentiels | Gestion des Salles               |
| Administrateur        | A. Paramètres & Référentiels | Gestion des Salles               |
| Administrateur        | B. Acteurs & Comptes         | Gestion des Encadreurs           |
| Administrateur        | B. Acteurs & Comptes         | Gestion des Encadreurs           |
| Administrateur        | B. Acteurs & Comptes         | Gestion des Étudiants            |
| Administrateur        | B. Acteurs & Comptes         | Gestion des Étudiants            |
| Administrateur        | B. Acteurs & Comptes         | Gestion des Comptes Utilisateurs |
| Administrateur        | C. Académique & Soutenances  | Gestion des Mémoires             |
| Administrateur        | C. Académique & Soutenances  | Gestion des Mémoires             |
| Administrateur        | C. Académique & Soutenances  | Planification & Jurys            |
| Administrateur        | C. Académique & Soutenances  | Planification & Jurys            |
| Administrateur        | C. Académique & Soutenances  | Gestion Globale des Soutenances  |
| Administrateur        | D. Pilotage & Audit          | Tableau de Bord & Statistiques   |
| Administrateur        | D. Pilotage & Audit          | Historique & Traçabilité         |
| Encadreur / Jury      | Menu Encadreur               | Tableau de Bord                  |
| Encadreur / Jury      | Menu Encadreur               | Mes Étudiants Encadrés           |
| Encadreur / Jury      | Menu Encadreur               | Mes Jurys & Évaluations          |
| Encadreur / Jury      | Menu Encadreur               | Mes Jurys & Évaluations          |
| Encadreur / Jury      | Menu Encadreur               | Mon Profil                       |
| Étudiant              | Menu Étudiant                | Ma Soutenance                    |
| Étudiant              | Menu Étudiant                | Ma Soutenance                    |

| Étudiant   | Menu Étudiant   | Mon Profil   |
|------------|-----------------|--------------|

EE / JSP)

| Page / Écran                           | Fichier JSP / Route                                       |
|----------------------------------------|-----------------------------------------------------------|
| Liste des Grades Ajout / Édition Grade | grade-list.jsp (/admin/grades) grade-form.jsp             |
| Liste des Départements                 | departement-list.jsp                                      |
|                                        | (/admin/departements) departement-form.jsp                |
| Ajout / Édition Département            |                                                           |
| Liste des Filières                     | filiere-list.jsp (/admin/filieres)                        |
| Ajout / Édition Filière                | filiere-form.jsp salle-list.jsp (/admin/salles)           |
| Liste des Salles                       |                                                           |
| Ajout / Édition Salle                  | salle-form.jsp                                            |
| Liste des Encadreurs                   | encadreur-list.jsp (/admin/encadreurs)                    |
| Ajout / Édition Encadreur              | encadreur-form.jsp                                        |
| Liste des Étudiants                    | etudiant-list.jsp (/admin/etudiants)                      |
| Ajout / Édition Étudiant               | etudiant-form.jsp                                         |
| Liste des Comptes                      | utilisateur-list.jsp (/admin/utilisateurs)                |
| Liste des Mémoires                     | memoire-list.jsp (/admin/memoires)                        |
| Ajout / Édition Mémoire                | memoire-form.jsp                                          |
| Formulaire de Soutenance               | soutenance-form.jsp (/admin/planification)                |
| Composition du Jury                    | jury-form.jsp soutenance-list.jsp                         |
| Vue Globale Soutenances                | (/admin/soutenances)                                      |
| Indicateurs & Graphiques               | dashboard.jsp (/admin/dashboard)                          |
| Journal d'Audit                        | historique-list.jsp (/admin/historique) dashboard.jsp     |
| Résumé / Alertes                       | (/encadreur/dashboard) mes-etudiants.jsp (/encadreur/mes- |
| Liste Étudiants Encadrés               | etudiants)                                                |
| Liste des Jurys                        | mes-jurys.jsp (/encadreur/mes- jurys)                     |
| Saisie d'Évaluation                    | evaluation-form.jsp                                       |
| Édition Profil                         | profil.jsp (/encadreur/profil)                            |
| Détails de Soutenance                  | ma-soutenance.jsp (/etudiant/ma- soutenance)              |
| Résultat                               | ma-soutenance.jsp                                         |

| Consulter Profil   | profil.jsp (/etudiant/profil)   |
|--------------------|---------------------------------|

| Description & Fonctionnalités                                      |
|--------------------------------------------------------------------|
| Tableau des grades (Code, Libellé)                                 |
| Formulaire de création et modification d'un grade                  |
| Tableau des départements (Code, Nom)                               |
| Formulaire de création et modification d'un département            |
| Tableau des filières avec leur département d'attachement           |
| Formulaire avec code, libellé et sélecteur de département          |
| Tableau des salles (Nom, Capacité, Département)                    |
| Formulaire avec nom, capacité et sélecteur de département          |
| Tableau complet avec filtres par département ou grade              |
| Formulaire (infos, grade, dep + création automatique compte)       |
| Moteur de recherche (nom/matricule) et filtres par filière         |
| Formulaire (infos, filière, mémoire + création compte)             |
| Activation/Désactivation et réinitialisation de mot de passe       |
| Consultation des thèmes déposés                                    |
| Saisie du titre, description, date de dépôt et encadreur principal |
| Sélection du mémoire, date/heure et attribution de la salle        |
| Sélection d'au moins deux encadreurs (remplit Evaluation)          |
| Filtres par statut (PLANIFIEE, EVALUEE, ANNULEE), date/filière     |
| Indicateurs : nb soutenances, taux validation, répartition dep     |
| Consultation de la table Historique_Action (auteur, action, date)  |
| Résumé des prochaines soutenances et alertes                       |
| Liste des étudiants encadrés et état d'avancement du mémoire       |
| Soutenances où il intervient comme juré                            |
| Formulaire pour saisir sa note sur 20 et son avis textuel          |
| Modification des données personnelles et du mot de passe           |
| Affichage de la note finale moyenne dès que statut = EVALUEE       |
| Affichage de la date, heure, salle, composition du jury et sujet   |

| Consultation des données personnelles, matricule, filière, mot de   |
|---------------------------------------------------------------------|
| passe                                                               |

## Modèle Logique de Données (MLD Normalisé - 11 Tables)

| Catégorie                    | Nom Table         | Clé Primaire (PK)   |
|------------------------------|-------------------|---------------------|
| 1. Paramètres & Référentiels | Utilisateur       | id_utilisateur      |
| 1. Paramètres & Référentiels | Departement       | id_departement      |
| 1. Paramètres & Référentiels | Grade             | id_grade            |
| 1. Paramètres & Référentiels | Filiere           | id_filiere          |
| 1. Paramètres & Référentiels | Salle             | id_salle            |
| 2. Acteurs & Mémoires        | Encadreur         | id_encadreur        |
| 2. Acteurs & Mémoires        | Memoire           | id_memoire          |
| 2. Acteurs & Mémoires        | Etudiant          | id_etudiant         |
| 3. Soutenances & Audit       | Soutenance        | id_soutenance       |
| 3. Soutenances & Audit       | Evaluation        | id_evaluation       |
| 3. Soutenances & Audit       | Historique_Action | id_historique       |

## é - 11 Tables)

| Clés Étrangères (FK)                        | Attributs Propres                     |
|---------------------------------------------|---------------------------------------|
| -                                           | login, mot_de_passe, role             |
| -                                           | code, nom                             |
| -                                           | code, libelle                         |
| #departement_id                             | code, libelle                         |
| #departement_id                             | nom_salle, capacite                   |
| #grade_id, #departement_id, #utilisateur_id | nom, prenom, email                    |
| #encadreur_id                               | titre, description, date_depot        |
| #filiere_id, #memoire_id, #utilisateur_id   | matricule, nom, prenom, email         |
| #memoire_id, #salle_id                      | date_heure, statut                    |
| #soutenance_id, #encadreur_id               | note, avis                            |
| #utilisateur_id                             | type_action, date_action, description |

| Rôle / Description                                                                          |
|---------------------------------------------------------------------------------------------|
| Compte d'accès central (Admin, Encadreur, Etudiant)                                         |
| Départements académiques de l'université Grades des enseignants/encadreurs (ex: Professeur, |
| MAO)                                                                                        |
| Filières d'études rattachées à un département                                               |
| Salles de soutenance gérées par département                                                 |
| Enseignants encadrants et membres des jurys                                                 |
| Sujet/Projet de fin d'études avec tuteur principal                                          |
| Étudiant réalisant le mémoire (département via sa filière)                                  |
| Session de soutenance (statut: PLANIFIEE, EVALUEE...)                                       |
| Table de liaison du jury : note et avis de chaque juré                                      |
| Journal d'audit pour traçabilité de toutes les opérations                                   |