Bon, les gars, j'ai affiné le MLD. On est sur 11 tables bien propres et entièrement normalisées (j'ai viré la redondance du département dans Etudiant vu qu'un étudiant est déjà lié à sa filière, et que la filière appartient à un département) :

1. Config & Références

Utilisateur (id_utilisateur, login, mot_de_passe, role)

Departement (id_departement, code, nom)

Grade (id_grade, code, libelle)

Filiere (id_filiere, code, libelle, #departement_id) -> rattachée au département

Salle (id_salle, nom_salle, capacite, #departement_id) -> rattachée au département

2. Acteurs & Mémoires

Encadreur (id_encadreur, nom, prenom, email, #grade_id, #departement_id, #utilisateur_id)

Memoire (id_memoire, titre, description, date_depot, #encadreur_id)

Etudiant (id_etudiant, matricule, nom, prenom, email, #filiere_id, #memoire_id, #utilisateur_id) -> plus besoin de #departement_id ici, on l'a via la filière !

3. Soutenances, Jurys & Logs

Soutenance (id_soutenance, date_heure, statut, #memoire_id, #salle_id)

Evaluation (id_evaluation, note, avis, #soutenance_id, #encadreur_id) -> la table du jury pour lier les profs aux soutenances avec leurs notes

Historique_Action (id_historique, type_action, date_action, description, #utilisateur_id)

Dites-moi si vous voyez d'autres détails à ajuster ou si c'est bon pour vous !
