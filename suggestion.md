# Suggestions & points à trancher

Liste de travail pour tout ce qui n'est pas la couche DAO (voir `DAO.md` pour celle-ci).
Convention : ce qui n'est pas encore fait est marqué **[À FAIRE]**.

---

## 1. Où on en est

- [x] Couche `model/` complète (11 entités JPA + 2 enums + `BaseEntity`)
- [x] `persistence.xml` (unité JTA, datasource JNDI)
- [x] `src/main/webapp/index.jsp` (placeholder)
- [ ] Tout le reste : `dao/`, `servlet/`, `service/`, JSP métier, CSS, auth

## 2. Arborescence cible

```
src/main/java/ci/univ/soutenances/
├── model/        ✅ existe
├── dao/          voir DAO.md
├── service/      AuthService, SoutenanceService, EvaluationService, PasswordHasher
├── servlet/
│   ├── LoginServlet.java          /login  /logout
│   ├── admin/…                    un fichier par URL /admin/*
│   ├── encadreur/…                un fichier par URL /encadreur/*
│   └── etudiant/…                 un fichier par URL /etudiant/*
└── web/ (utilitaires de vue si besoin)

src/main/webapp/
├── WEB-INF/
│   ├── jsp/admin/…                JSP inaccessibles en URL directe
│   ├── jsp/encadreur/…
│   ├── jsp/etudiant/…
│   └── beans.xml                  (uniquement si stratégie B, voir DAO.md §2)
└── css/style.css                  charte graphique
```

## 3. Le pattern d'une servlet, une seule forme à recopier

```java
@WebServlet("/admin/grades")
public class GradeServlet extends HttpServlet {

    @Inject private GradeDao gradeDao;   // ou new GradeDao(em) — voir DAO.md §2

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Contrôle de session CÔTÉ CONTRÔLEUR — obligatoire sur chaque page protégée
        HttpSession session = req.getSession(false);
        Utilisateur user = (session == null) ? null : (Utilisateur) session.getAttribute("utilisateur");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (user.getRole() != RoleUtilisateur.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/" + user.getRole().name().toLowerCase() + "/dashboard");
            return;
        }

        // 2. Métier via le DAO
        req.setAttribute("grades", gradeDao.findAll());

        // 3. Forward vers la vue (pas de HTML ici)
        req.getRequestDispatcher("/WEB-INF/jsp/admin/grade-list.jsp").forward(req, resp);
    }
}
```

Points non négociables :
- Aucun `out.println("<html>")` dans une servlet. Jamais.
- Aucun `<% ... %>` dans une JSP : uniquement `${...}` et JSTL.
- Le test de session est dans la servlet, pas dans la JSP.
- Un seul objet en session (`"utilisateur"`), et l'id de l'encadreur/étudiant lié récupéré via
  son DAO — pas de donnée dupliquée qui pourrait devenir obsolète.

## 4. JSTL — la seule dépendance à ajouter au `pom.xml`

Sans JSTL, impossible d'utiliser `c:forEach` / `c:if`. À ajouter (versions à confirmer au
premier `mvn package`, scope `provided` car WildFly les fournit) :

```xml
<dependency>
    <groupId>jakarta.servlet.jsp.jstl</groupId>
    <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
    <version>3.0.2</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>org.glassfish.web</groupId>
    <artifactId>jakarta.servlet.jsp.jstl</artifactId>
    <version>3.0.1</version>
</dependency>
```

Puis dans chaque JSP :
```jsp
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
```
Si WildFly râle sur cette URI au déploiement, essayer `java.sun.com/jsp/jstl/core` (ancienne
URI, encore acceptée par l'implémentation embarquée) et signaler-le.

## 5. Authentification — [À FAIRE]

- `PasswordHasher` : `MessageDigest.getInstance("SHA-256")` + encodage hexadécimal.
  Minimum exigé par le cahier des charges. À mentionner à l'oral comme limite connue
  (pas de sel, pas de bcrypt) — mieux vaut le dire que le cacher.
- `AuthService.authentifier(login, motDePasse)` : `UtilisateurDao.findByLogin(...)` puis
  comparaison des hashs avec `MessageDigest.isEqual(...)` (comparaison à temps constant).
- Après succès : `session.setAttribute("utilisateur", u)` puis redirection selon le rôle :
  `ADMIN` → `/admin/dashboard`, `ENCADREUR` → `/encadreur/dashboard`, `ETUDIANT` → `/etudiant/ma-soutenance`.
- `LogoutServlet` : `session.invalidate()` puis `/login`.
- Page `/login` en GET, traitement en POST.
- Pas de compte en base au départ : prévoir un jeu de données d'amorçage (script SQL d'insert
  dans `src/main/resources/db/`, à part de `create-database.sql` qui ne crée que le schéma,
  ou un `StartupSeed` chargé au déploiement).

## 6. Tables de correspondance JSP / URLs

Repris telles quelles du découpage d'équipe — **ne pas inventer d'autres URL**.

**Admin** : `grade-list`/`grade-form` → `/admin/grades` ; `departement-*` → `/admin/departements` ;
`filiere-*` → `/admin/filieres` ; `salle-*` → `/admin/salles` ; `encadreur-*` → `/admin/encadreurs` ;
`etudiant-*` → `/admin/etudiants` ; `utilisateur-list` → `/admin/utilisateurs` ;
`memoire-*` → `/admin/memoires` ; `soutenance-form` → `/admin/planification` ; `jury-form` ;
`soutenance-list` → `/admin/soutenances` ; `dashboard` → `/admin/dashboard` ;
`historique-list` → `/admin/historique`.

**Encadreur** : `dashboard` → `/encadreur/dashboard` ; `mes-etudiants` → `/encadreur/mes-etudiants` ;
`mes-jurys` → `/encadreur/mes-jurys` ; `evaluation-form` ; `profil` → `/encadreur/profil`.

**Étudiant** : `ma-soutenance` → `/etudiant/ma-soutenance` ; `profil` → `/etudiant/profil`.

## 7. Charte graphique

Un seul `style.css` avec des variables, valeurs imposées, à ne pas modifier :

```css
:root {
  --primaire: #1E3A8A;   /* nav, en-têtes de table, boutons principaux, titres */
  --accent:   #0284C7;   /* liens actifs, boutons secondaires, icônes de stats */
  --accent2:  #4F46E5;   /* boutons alternatifs, accents interactifs */
  --fond:     #F8FAFC;
  --surface:  #FFFFFF;
  --texte:    #1E293B;
  /* badges statut */
  --planifiee: #B45309; --planifiee-bg: #FEF3C7;
  --evaluee:   #047857; --evaluee-bg:   #D1FAE5;
  --annulee:   #B91C1C; --annulee-bg:   #FEE2E2;
  /* badges rôle */
  --admin:     #6D28D9; --admin-bg:     #EDE9FE;
  --encadreur: #1D4ED8; --encadreur-bg: #DBEAFE;
  --etudiant:  #0F766E; --etudiant-bg:  #CCFBF1;
}
body { font-family: "Segoe UI", sans-serif; background: var(--fond); color: var(--texte); }
```

Les badges se font avec un `<c:choose>` sur `${soutenance.statut}` (3 cas), pas de scriptlet.

## 8. Pièges techniques identifiés à l'avance

1. **`LazyInitializationException`** en JSP à cause des associations LAZY → `JOIN FETCH`
   dans les DAOs de liste (détail dans `DAO.md` §8).
2. **UUID en paramètre d'URL** : `req.getParameter("id")` rend une `String`, il faut
   `UUID.fromString(...)` entouré d'un `try/catch` → page 404 propre si format invalide.
3. **Contraintes UNIQUE** (`uk_grade_code`, `uk_encadreur_email`, `uk_etudiant_matricule`,
   `uk_utilisateur_login`, `uk_soutenance_memoire`, `uk_evaluation_soutenance_encadreur`) :
   toujours vérifier via le DAO avant insertion et afficher un message métier.
4. **`Soutenance.memoire` est `@OneToOne` unique** : un mémoire avec soutenance ne doit plus
   apparaître dans le select de planification → `MemoireDao.findSansSoutenance()`.
5. **Règle jury ≥ 2 membres** : à valider dans `SoutenanceService`, pas dans la JSP.
6. **Dates** : `Soutenance.dateHeure` est un `LocalDateTime`, pas un `java.util.Date`. Un
   `<input type="datetime-local">` produit `"2026-03-15T14:30"` →
   `LocalDateTime.parse(valeur)`.
7. **Encodage** : `request.setCharacterEncoding("UTF-8")` en tête de chaque `doPost` avant
   de lire les paramètres, sinon les accents des noms cassent.
8. **Conflits de salle/horaire** : `SoutenanceDao.findBySalleAndDateBetween(...)` évite deux
   soutenances dans la même salle au même moment.

## 9. Ordre de travail proposé par étapes

1. JSTL dans le `pom.xml` + arborescence `WEB-INF/jsp` + `style.css` (30 min, débloque tout)
2. Choix de la stratégie de transactions (DAO.md §2)
3. `BaseDao` + `UtilisateurDao` + `AuthService` + écran `/login`
4. CRUD `/admin/grades` complet (GET liste, GET formulaire, POST création, POST modification,
   POST suppression) → sert de modèle à recopier pour les 3 autres référentiels
5. `/admin/departements`, `/admin/filieres`, `/admin/salles`
6. `/admin/encadreurs`, `/admin/etudiants` (création du compte utilisateur + hash en même temps)
7. `/admin/memoires` → `/admin/planification` (+ jury) → `/admin/soutenances`
8. Écrans encadreur : dashboard, mes-etudiants, mes-jurys, evaluation-form
9. Écran étudiant : ma-soutenance, profil
10. `/admin/dashboard` + `/admin/historique` (à faire aussi : brancher l'écriture dans
    `HistoriqueAction` à chaque action d'écriture)

## 10. Git

- Une branche par fonctionnalité, PR vers `main`, pas de commit direct sur `main`.
- Découpage conseillé par écran/CRUD pour que chacun défende son code à l'oral.

---

## Questions à trancher en équipe

1. **Transactions** : stratégie A (`UserTransaction` explicite) ou B (CDI + `@Transactional`) ?
   → DAO.md §2. Bloque l'écriture du premier DAO.
2. **Amorçage des données** : script SQL d'insert (utilisateurs de test, grades, départements)
   ou chargement programmé au démarrage ? Sans ça, aucun login n'est testable.
3. **Premier écran livré** : `/login` d'abord, ou CRUD `/admin/grades` d'abord pour valider
   la chaîne complète Servlet → DAO → JSP ?
4. **Répartition des rôles** dans l'équipe de 3–4 : qui prend le bloc « soutenances/jurys »
   (le plus technique, `EvaluationDao` + services) et qui prend les référentiels ?
