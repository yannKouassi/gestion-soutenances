-- ============================================================================
-- donnees-demo.sql — Jeu de données de DÉMONSTRATION
-- ----------------------------------------------------------------------------
-- Projet  : gestion_soutenances (M1 MIAGE, Jakarta EE 10 / Hibernate / MySQL)
-- Rôle    : peuple la base avec un jeu de test minimal mais parlant pour la
--           démonstration orale (module C : étudiants, encadreurs, soutenances).
-- Contenu : référentiels (départements, grades, filières, salles),
--           11 comptes, 4 encadreurs, 6 étudiants, 6 mémoires, 6 soutenances
--           (PLANIFIEE / EVALUEE / ANNULEE), 15 convocations de jury et
--           quelques lignes d'historique d'audit.
--
-- !!! AVERTISSEMENT — ENVIRONNEMENT DE DÉVELOPPEMENT UNIQUEMENT !!!
--   * Ce script crée des COMPTES PAR DÉFAUT avec un mot de passe CONNU.
--   * Il ne doit JAMAIS être exécuté sur une base de production ou partagée.
--   * Il ne fait AUCUN DDL : les tables sont créées par Hibernate
--     (hibernate.hbm2ddl.auto=update). Il suppose donc que Hibernate a déjà
--     généré le schéma (premier démarrage de WildFly sur une base vide).
--   * Il est réexécutable : il commence par supprimer les lignes qu'il insère
--     (identifiants fixes), dans un ordre qui respecte les clés étrangères.
--
-- Encodage : fichier UTF-8 (sans BOM) ; exécuter avec --default-character-set=utf8mb4.
-- Mots de passe : PBKDF2-HMAC-SHA256, 310000 itérations, sel 16 octets,
--   format réel <prefix>$<iterations>$<sel base64>$<hash base64>.
--   Mot de passe de démonstration commun à tous les comptes (voir bas de fichier).
--
-- Les identifiants (UUID) sont écrits EXPLICITEMENT en binaire 16 octets via
-- UNHEX(...), car les colonnes id_* sont mappées binary(16) par Hibernate 7
-- (dialecte MySQL, UUID stocké en binaire big-endian : msb puis lsb).
-- ============================================================================

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- S'assure que la colonne Evaluation.note accepte NULL (membre non encore évalué).
-- Nécessaire si le schéma Hibernate a été généré avec une version NOT NULL.
ALTER TABLE Evaluation MODIFY note DECIMAL(4,2) NULL;

-- Affiche la base visée : vérifie qu'il s'agit bien de ta base de développement.
SELECT CONCAT('ATTENTION : donnees-demo.sql va peupler la base de DÉMONSTRATION -> ',
              DATABASE()) AS avertissement;

-- ----------------------------------------------------------------------------
-- Identifiants fixes (variables de session)
-- ----------------------------------------------------------------------------
-- Utilisateurs
SET @usr_admin = UNHEX('00000005000040008000000000000001');
SET @usr_enc1  = UNHEX('00000005000040008000000000000002');
SET @usr_enc2  = UNHEX('00000005000040008000000000000003');
SET @usr_enc3  = UNHEX('00000005000040008000000000000004');
SET @usr_enc4  = UNHEX('00000005000040008000000000000005');
SET @usr_etu1  = UNHEX('00000005000040008000000000000006');
SET @usr_etu2  = UNHEX('00000005000040008000000000000007');
SET @usr_etu3  = UNHEX('00000005000040008000000000000008');
SET @usr_etu4  = UNHEX('00000005000040008000000000000009');
SET @usr_etu5  = UNHEX('0000000500004000800000000000000a');
SET @usr_etu6  = UNHEX('0000000500004000800000000000000b');

-- Référentiels
SET @dep_info = UNHEX('00000001000040008000000000000001');
SET @dep_sge  = UNHEX('00000001000040008000000000000002');
SET @gra_ass  = UNHEX('00000002000040008000000000000001');
SET @gra_mc   = UNHEX('00000002000040008000000000000002');
SET @gra_prof = UNHEX('00000002000040008000000000000003');
SET @fil_miage = UNHEX('00000003000040008000000000000001');
SET @fil_gl    = UNHEX('00000003000040008000000000000002');
SET @fil_sir   = UNHEX('00000003000040008000000000000003');
SET @fil_fcge  = UNHEX('00000003000040008000000000000004');
SET @sal_a = UNHEX('00000004000040008000000000000001');
SET @sal_b = UNHEX('00000004000040008000000000000002');
SET @sal_c = UNHEX('00000004000040008000000000000003');

-- Acteurs
SET @enc1 = UNHEX('00000006000040008000000000000001');
SET @enc2 = UNHEX('00000006000040008000000000000002');
SET @enc3 = UNHEX('00000006000040008000000000000003');
SET @enc4 = UNHEX('00000006000040008000000000000004');
SET @etu1 = UNHEX('00000007000040008000000000000001');
SET @etu2 = UNHEX('00000007000040008000000000000002');
SET @etu3 = UNHEX('00000007000040008000000000000003');
SET @etu4 = UNHEX('00000007000040008000000000000004');
SET @etu5 = UNHEX('00000007000040008000000000000005');
SET @etu6 = UNHEX('00000007000040008000000000000006');

-- Mémoires et soutenances
SET @mem1 = UNHEX('00000008000040008000000000000001');
SET @mem2 = UNHEX('00000008000040008000000000000002');
SET @mem3 = UNHEX('00000008000040008000000000000003');
SET @mem4 = UNHEX('00000008000040008000000000000004');
SET @mem5 = UNHEX('00000008000040008000000000000005');
SET @mem6 = UNHEX('00000008000040008000000000000006');
SET @sou1 = UNHEX('00000009000040008000000000000001');
SET @sou2 = UNHEX('00000009000040008000000000000002');
SET @sou3 = UNHEX('00000009000040008000000000000003');
SET @sou4 = UNHEX('00000009000040008000000000000004');
SET @sou5 = UNHEX('00000009000040008000000000000005');
SET @sou6 = UNHEX('00000009000040008000000000000006');

-- Convocations de jury (Evaluation)
SET @ev01 = UNHEX('0000000a000040008000000000000001');
SET @ev02 = UNHEX('0000000a000040008000000000000002');
SET @ev03 = UNHEX('0000000a000040008000000000000003');
SET @ev04 = UNHEX('0000000a000040008000000000000004');
SET @ev05 = UNHEX('0000000a000040008000000000000005');
SET @ev06 = UNHEX('0000000a000040008000000000000006');
SET @ev07 = UNHEX('0000000a000040008000000000000007');
SET @ev08 = UNHEX('0000000a000040008000000000000008');
SET @ev09 = UNHEX('0000000a000040008000000000000009');
SET @ev0a = UNHEX('0000000a00004000800000000000000a');
SET @ev0b = UNHEX('0000000a00004000800000000000000b');
SET @ev0c = UNHEX('0000000a00004000800000000000000c');
SET @ev0d = UNHEX('0000000a00004000800000000000000d');
SET @ev0e = UNHEX('0000000a00004000800000000000000e');
SET @ev0f = UNHEX('0000000a00004000800000000000000f');

-- Historique d'audit
SET @his1 = UNHEX('0000000b000040008000000000000001');
SET @his2 = UNHEX('0000000b000040008000000000000002');
SET @his3 = UNHEX('0000000b000040008000000000000003');
SET @his4 = UNHEX('0000000b000040008000000000000004');
SET @his5 = UNHEX('0000000b000040008000000000000005');
SET @his6 = UNHEX('0000000b000040008000000000000006');

-- ============================================================================
-- 1) Révocation des données précédentes (réexécution), enfants -> parents
-- ============================================================================
START TRANSACTION;

DELETE FROM Historique_Action
 WHERE id_historique IN (@his1, @his2, @his3, @his4, @his5, @his6);
DELETE FROM Evaluation
 WHERE id_evaluation IN (@ev01, @ev02, @ev03, @ev04, @ev05, @ev06, @ev07, @ev08,
                         @ev09, @ev0a, @ev0b, @ev0c, @ev0d, @ev0e, @ev0f);
DELETE FROM Soutenance
 WHERE id_soutenance IN (@sou1, @sou2, @sou3, @sou4, @sou5, @sou6);
DELETE FROM Etudiant
 WHERE id_etudiant IN (@etu1, @etu2, @etu3, @etu4, @etu5, @etu6);
DELETE FROM Memoire
 WHERE id_memoire IN (@mem1, @mem2, @mem3, @mem4, @mem5, @mem6);
DELETE FROM Encadreur
 WHERE id_encadreur IN (@enc1, @enc2, @enc3, @enc4);
DELETE FROM Salle
 WHERE id_salle IN (@sal_a, @sal_b, @sal_c);
DELETE FROM Filiere
 WHERE id_filiere IN (@fil_miage, @fil_gl, @fil_sir, @fil_fcge);
DELETE FROM Utilisateur
 WHERE id_utilisateur IN (@usr_admin, @usr_enc1, @usr_enc2, @usr_enc3, @usr_enc4,
                          @usr_etu1, @usr_etu2, @usr_etu3, @usr_etu4, @usr_etu5, @usr_etu6);
DELETE FROM Grade
 WHERE id_grade IN (@gra_ass, @gra_mc, @gra_prof);
DELETE FROM Departement
 WHERE id_departement IN (@dep_info, @dep_sge);

-- ============================================================================
-- 2) Référentiels
-- ============================================================================
INSERT INTO Departement (id_departement, code, nom) VALUES
    (@dep_info, 'INFO', 'Informatique'),
    (@dep_sge,  'SGE',  'Sciences de Gestion');

INSERT INTO Grade (id_grade, code, libelle) VALUES
    (@gra_ass,  'ASS',  'Assistant'),
    (@gra_mc,   'MC',   'Maître de Conférences'),
    (@gra_prof, 'PROF', 'Professeur Titulaire');

INSERT INTO Filiere (id_filiere, code, libelle, departement_id) VALUES
    (@fil_miage, 'MIAGE', 'Méthodes Informatiques Appliquées à la Gestion', @dep_info),
    (@fil_gl,    'GL',    'Génie Logiciel',                                  @dep_info),
    (@fil_sir,   'SIR',   'Systèmes d''Information et Réseaux',              @dep_info),
    (@fil_fcge,  'FCGE',  'Finance, Comptabilité et Gestion d''Entreprise',  @dep_sge);

INSERT INTO Salle (id_salle, nom_salle, capacite, departement_id) VALUES
    (@sal_a, 'SALLE-A',  40, @dep_info),
    (@sal_b, 'AMPHI-B', 120, @dep_info),
    (@sal_c, 'SALLE-C',  30, @dep_sge);

-- ============================================================================
-- 3) Comptes utilisateurs
--    (mot de passe de démonstration : voir note finale ; hash PBKDF2-SHA256)
-- ============================================================================
INSERT INTO Utilisateur (id_utilisateur, login, mot_de_passe, role, actif) VALUES
    (@usr_admin, 'admin', 'pbkdf2-sha256$310000$fmzb9VE6Onmcg2oOqE43wQ==$L61nj+WrvQRLI7u6CRSYKcn2RiL1paujkKuSCMX/CPs=', 'ADMIN', TRUE),
    (@usr_enc1, 'seriba.kone@univ.ci', 'pbkdf2-sha256$310000$J4+N1YM1J0J39fm6AbLRCA==$UgohxsxU0V40bU0zRvsUG/IDBWEsJbuDRda043L+wMY=', 'ENCADREUR', TRUE),
    (@usr_enc2, 'moussa.traore@univ.ci', 'pbkdf2-sha256$310000$YCLuruH+tg1vnsNepW0z4Q==$OozLlHklElt+i8VMFuxBepKgWdiizAXI2CwCgt5G6hM=', 'ENCADREUR', TRUE),
    (@usr_enc3, 'aminata.diallo@univ.ci', 'pbkdf2-sha256$310000$WgEj8qvx5uqcgR23Wi2Gfg==$KDCa7E4TfnYhvqnYwIw7LMLz0z/flW3Zh1M7AajCq68=', 'ENCADREUR', TRUE),
    (@usr_enc4, 'koffi.yao@univ.ci', 'pbkdf2-sha256$310000$mJAEZyKPWM/SNPgBh7Wppg==$mUhi/3GFRm08aZTk0nTRDyxF2IBeu8txkSCtyic0CyY=', 'ENCADREUR', TRUE),
    (@usr_etu1, 'marie.adou@etu.univ.ci', 'pbkdf2-sha256$310000$6nuYuPwQWErTIKHP4SqSzg==$3UqsqqnMLzM/eJJFCtB/3s7UzhLPHmVegdakVWVNZL8=', 'ETUDIANT', TRUE),
    (@usr_etu2, 'eric.kouassi@etu.univ.ci', 'pbkdf2-sha256$310000$FEYNXaA4vPnP2LDys++GCA==$B5oaVpyd2uUf5fpQ7Q9y2yWmp4K1IoMQZzj9dUj5shM=', 'ETUDIANT', TRUE),
    (@usr_etu3, 'fatoumata.bamba@etu.univ.ci', 'pbkdf2-sha256$310000$yxa4gergTTeqk2TZ+4tcAw==$uc+/Bs29PbnzWEUHcsK8PhDI/D2gFOd6tWA0pmFKCXE=', 'ETUDIANT', TRUE),
    (@usr_etu4, 'didier.konan@etu.univ.ci', 'pbkdf2-sha256$310000$FkE9GnAWeNBrgQUyHSXJ4Q==$kSKHcFNaf357ZSP8LyIV2Sz/cUpVEKyHm1bM0BD+c+U=', 'ETUDIANT', TRUE),
    (@usr_etu5, 'awa.cisse@etu.univ.ci', 'pbkdf2-sha256$310000$wWNe0v/PAjS1JMdpgZkfvg==$sqtsTIT+XoAo7zHm3wzEizItLQJ5nk41by9Gm57yym0=', 'ETUDIANT', TRUE),
    (@usr_etu6, 'yves.gnamien@etu.univ.ci', 'pbkdf2-sha256$310000$VySw/LqbJPS8audref5I3A==$fIszzDIeoHl4WfwQ0OdtxXcs/0YLcLxeQHi3o4Hks7E=', 'ETUDIANT', TRUE);

-- ============================================================================
-- 4) Encadreurs (4) — un encadreur (Koné) siège dans plusieurs jurys
-- ============================================================================
INSERT INTO Encadreur (id_encadreur, nom, prenom, email, grade_id, departement_id, utilisateur_id) VALUES
    (@enc1, 'Koné',    'Sériba',  'seriba.kone@univ.ci',   @gra_prof, @dep_info, @usr_enc1),
    (@enc2, 'Traoré',  'Moussa',  'moussa.traore@univ.ci', @gra_mc,   @dep_info, @usr_enc2),
    (@enc3, 'Diallo',  'Aminata', 'aminata.diallo@univ.ci',@gra_mc,   @dep_info, @usr_enc3),
    (@enc4, 'Yao',     'Koffi',   'koffi.yao@univ.ci',     @gra_ass,  @dep_sge,  @usr_enc4);

-- ============================================================================
-- 5) Mémoires (6, un par étudiant)
-- ============================================================================
INSERT INTO Memoire (id_memoire, titre, description, date_depot, encadreur_id) VALUES
    (@mem1, 'Conception d''une plateforme de gestion des soutenances en Jakarta EE',
     'Application web MVC (servlets/JSP) avec JPA/Hibernate sur MySQL.', '2026-06-15', @enc1),
    (@mem2, 'Sécurisation des applications web Java : authentification et gestion des sessions',
     'Étude des mécanismes d''authentification, de hachage des mots de passe et de session.', '2026-06-18', @enc2),
    (@mem3, 'Optimisation des requêtes JPA/Hibernate sur une base MySQL',
     'Analyse des chargements paresseux, jointures et index dans un contexte JPA.', '2026-06-20', @enc3),
    (@mem4, 'Mise en place d''une intégration continue pour un projet Maven',
     'Automatisation du build, des tests et du déploiement d''un WAR Jakarta EE.', '2026-06-22', @enc1),
    (@mem5, 'Digitalisation du suivi des mémoires dans une université',
     'Refonte du processus de dépôt et de suivi des mémoires de fin d''études.', '2026-06-25', @enc4),
    (@mem6, 'Tableau de bord décisionnel pour le pilotage des soutenances',
     'Indicateurs et statistiques sur les soutenances pour l''administration.', '2026-06-28', @enc4);

-- ============================================================================
-- 6) Étudiants (6), chacun rattaché à sa filière et à son mémoire
-- ============================================================================
INSERT INTO Etudiant (id_etudiant, matricule, nom, prenom, email, filiere_id, memoire_id, utilisateur_id) VALUES
    (@etu1, 'MIA26001',  'Adou',    'Marie',     'marie.adou@etu.univ.ci',     @fil_miage, @mem1, @usr_etu1),
    (@etu2, 'MIA26002',  'Kouassi', 'Éric',      'eric.kouassi@etu.univ.ci',   @fil_miage, @mem2, @usr_etu2),
    (@etu3, 'MIA26003',  'Bamba',   'Fatoumata', 'fatoumata.bamba@etu.univ.ci',@fil_miage, @mem3, @usr_etu3),
    (@etu4, 'GL26004',   'Konan',   'Didier',    'didier.konan@etu.univ.ci',   @fil_gl,    @mem4, @usr_etu4),
    (@etu5, 'SIR26005',  'Cissé',   'Awa',       'awa.cisse@etu.univ.ci',      @fil_sir,   @mem5, @usr_etu5),
    (@etu6, 'FCGE26006', 'Gnamien', 'Yves',      'yves.gnamien@etu.univ.ci',   @fil_fcge,  @mem6, @usr_etu6);

-- ============================================================================
-- 7) Soutenances (6) — un mémoire = au plus une soutenance
--    S1 PLANIFIEE (jury complet, aucune note) -> écran de saisie d'évaluation
--    S2 EVALUEE   (tous les membres ont noté) -> moyenne du jury
--    S3 PLANIFIEE (jury minimal de 2 membres)
--    S4 EVALUEE   (tous les membres ont noté)
--    S5 ANNULEE
--    S6 PLANIFIEE
-- ============================================================================
INSERT INTO Soutenance (id_soutenance, date_heure, statut, memoire_id, salle_id) VALUES
    (@sou1, '2026-11-10 09:00:00', 'PLANIFIEE', @mem1, @sal_b),
    (@sou2, '2026-10-06 14:00:00', 'EVALUEE',   @mem2, @sal_a),
    (@sou3, '2026-11-12 10:30:00', 'PLANIFIEE', @mem3, @sal_a),
    (@sou4, '2026-10-08 09:30:00', 'EVALUEE',   @mem4, @sal_b),
    (@sou5, '2026-10-15 11:00:00', 'ANNULEE',   @mem5, @sal_c),
    (@sou6, '2026-11-17 15:00:00', 'PLANIFIEE', @mem6, @sal_c);

-- ============================================================================
-- 8) Convocations de jury (Evaluation) — note NULL = membre non encore évaluateur
--    Le couple (soutenance, encadreur) est unique (uk_evaluation_soutenance_encadreur).
-- ============================================================================
INSERT INTO Evaluation (id_evaluation, note, avis, soutenance_id, encadreur_id) VALUES
    -- S1 : jury complet (3 membres), aucune note -> soutenance PLANIFIEE
    (@ev01, NULL,  NULL, @sou1, @enc1),
    (@ev02, NULL,  NULL, @sou1, @enc2),
    (@ev03, NULL,  NULL, @sou1, @enc3),
    -- S2 : jury complet (3 membres), tous ont noté -> moyenne 15.00
    (@ev04, 15.50, 'Bon mémoire, soutenance claire et bien argumentée.',   @sou2, @enc1),
    (@ev05, 16.00, 'Travail solide, démonstration convaincante.',          @sou2, @enc3),
    (@ev06, 13.50, 'Quelques faiblesses sur la partie tests, à améliorer.',@sou2, @enc4),
    -- S3 : jury minimal (2 membres), aucune note
    (@ev07, NULL,  NULL, @sou3, @enc2),
    (@ev08, NULL,  NULL, @sou3, @enc4),
    -- S4 : jury complet (3 membres), tous ont noté -> moyenne 13.00
    (@ev09, 12.00, 'Périmètre trop large, réponses parfois évasives.',      @sou4, @enc1),
    (@ev0a, 14.00, 'Bonne maîtrise de l''outillage et du processus.',       @sou4, @enc4),
    (@ev0b, 13.00, 'Rapport correct, soutenance honorable.',                @sou4, @enc3),
    -- S5 : soutenance annulée, aucune note
    (@ev0c, NULL,  NULL, @sou5, @enc2),
    (@ev0d, NULL,  NULL, @sou5, @enc4),
    -- S6 : aucune note
    (@ev0e, NULL,  NULL, @sou6, @enc4),
    (@ev0f, NULL,  NULL, @sou6, @enc3);

-- ============================================================================
-- 9) Historique d'audit (écrans / actions conformes à HistoriqueActionService)
--    type_action = <ECRAN>_<ACTION>, action ∈ CREATION/MODIFICATION/SUPPRESSION
-- ============================================================================
INSERT INTO Historique_Action (id_historique, type_action, ecran, action, date_action, description, utilisateur_id) VALUES
    (@his1, 'ENCADREUR_CREATION',     'ENCADREUR',  'CREATION',     '2026-10-01 08:30:00', 'Création de la fiche encadreur Yao Koffi.',                             @usr_admin),
    (@his2, 'ETUDIANT_CREATION',      'ETUDIANT',   'CREATION',     '2026-10-01 08:35:00', 'Création de la fiche étudiant Gnamien Yves.',                           @usr_admin),
    (@his3, 'MEMOIRE_CREATION',       'MEMOIRE',    'CREATION',     '2026-10-01 08:40:00', 'Dépôt du mémoire : tableau de bord décisionnel des soutenances.',       @usr_admin),
    (@his4, 'SOUTENANCE_CREATION',    'SOUTENANCE', 'CREATION',     '2026-10-02 10:15:00', 'Planification de la soutenance de Marie Adou (SALLE-A).',               @usr_admin),
    (@his5, 'JURY_CREATION',          'JURY',       'CREATION',     '2026-10-02 10:20:00', 'Constitution du jury de la soutenance de Marie Adou.',                  @usr_admin),
    (@his6, 'SOUTENANCE_MODIFICATION','SOUTENANCE', 'MODIFICATION', '2026-10-09 09:05:00', 'Passage de la soutenance de Éric Kouassi au statut EVALUEE.',           @usr_admin);

COMMIT;

-- ============================================================================
-- Vérification rapide (facultatif) — attendu : 6 étudiants, 4 encadreurs,
-- 6 soutenances, 15 évaluations.
-- ============================================================================
SELECT (SELECT COUNT(*) FROM Etudiant)   AS etudiants,
       (SELECT COUNT(*) FROM Encadreur)  AS encadreurs,
       (SELECT COUNT(*) FROM Soutenance) AS soutenances,
       (SELECT COUNT(*) FROM Evaluation) AS evaluations;
