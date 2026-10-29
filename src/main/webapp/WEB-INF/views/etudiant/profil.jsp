<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Mon profil | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261032">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/etudiant.css?v=20261032">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar-toggle.css?v=20261032">
</head>
<body>
<div class="admin-shell">
    <%@ include file="etudiant-layout.jspf" %>
    <main id="main-content" class="admin-main">
        <%@ include file="etudiant-topbar.jspf" %>
        <header class="page-heading">
            <div class="page-heading-copy"><span class="page-eyebrow">Espace étudiant / Mon profil</span>
                <div><h1>Mon profil</h1><p>Tes informations de compte et d’étudiant, en lecture seule.</p></div>
            </div>
        </header>
        <section class="card" aria-label="Informations du compte">
            <div class="form-history-heading">
                <div><span class="page-eyebrow">Compte</span><h2>Informations du compte</h2></div>
                <span class="role-badge role-ETUDIANT">ETUDIANT</span>
            </div>
            <div class="card-body">
                <dl class="info-list">
                    <div><dt>Login</dt><dd><c:out value="${etudiant.utilisateur.login}"/></dd></div>
                    <div><dt>Rôle</dt><dd>Étudiant</dd></div>
                    <div><dt>État du compte</dt><dd><span class="status-badge ${etudiant.utilisateur.actif ? 'status-active' : 'status-inactive'}"><c:out value="${etudiant.utilisateur.actif ? 'Actif' : 'Désactivé'}"/></span></dd></div>
                </dl>
            </div>
        </section>
        <section class="card" aria-label="Informations de l’étudiant">
            <div class="form-history-heading">
                <div><span class="page-eyebrow">Étudiant</span><h2>Informations personnelles</h2></div>
            </div>
            <div class="card-body">
                <dl class="info-list">
                    <div><dt>Nom</dt><dd><c:out value="${etudiant.nom}"/></dd></div>
                    <div><dt>Prénom</dt><dd><c:out value="${etudiant.prenom}"/></dd></div>
                    <div><dt>Matricule</dt><dd><c:out value="${etudiant.matricule}"/></dd></div>
                    <div><dt>Adresse e-mail</dt><dd><c:out value="${etudiant.email}"/></dd></div>
                    <div><dt>Filière</dt><dd><c:out value="${etudiant.filiere.libelle}"/></dd></div>
                    <div><dt>Département</dt><dd><c:out value="${etudiant.filiere.departement.nom}"/></dd></div>
                    <div><dt>Mémoire</dt><dd><c:choose><c:when test="${empty etudiant.memoire}">Non rattaché</c:when><c:otherwise><c:out value="${etudiant.memoire.titre}"/></c:otherwise></c:choose></dd></div>
                </dl>
            </div>
        </section>
    </main>
</div>
</body>
</html>
