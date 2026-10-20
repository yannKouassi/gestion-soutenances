<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Mon profil | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261020-cartoon-ui">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261020-cartoon-ui">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/tailwind.css?v=20261020-cartoon-ui">
</head>
<body>
<div class="admin-shell">
    <%@ include file="encadreur-layout.jspf" %>
    <main class="admin-main">
        <%@ include file="encadreur-topbar.jspf" %>
        <header class="page-heading">
            <div class="page-heading-copy"><span class="page-eyebrow">Espace encadreur / Mon profil</span>
                <div><h1>Mon profil</h1><p>Tes informations de compte et d’encadreur, en lecture seule.</p></div>
            </div>
        </header>
        <section class="card" aria-label="Informations du compte">
            <div class="form-history-heading">
                <div><span class="page-eyebrow">Compte</span><h2>Informations du compte</h2></div>
                <span class="role-badge role-ENCADREUR">ENCADREUR</span>
            </div>
            <div class="card-body">
                <dl class="info-list">
                    <div><dt>Login</dt><dd><c:out value="${encadreur.utilisateur.login}"/></dd></div>
                    <div><dt>Rôle</dt><dd>Encadreur</dd></div>
                    <div><dt>État du compte</dt><dd><span class="status-badge ${encadreur.utilisateur.actif ? 'status-active' : 'status-inactive'}"><c:out value="${encadreur.utilisateur.actif ? 'Actif' : 'Désactivé'}"/></span></dd></div>
                </dl>
            </div>
        </section>
        <section class="card" aria-label="Informations de l’encadreur">
            <div class="form-history-heading">
                <div><span class="page-eyebrow">Encadreur</span><h2>Informations professionnelles</h2></div>
            </div>
            <div class="card-body">
                <dl class="info-list">
                    <div><dt>Nom</dt><dd><c:out value="${encadreur.nom}"/></dd></div>
                    <div><dt>Prénom</dt><dd><c:out value="${encadreur.prenom}"/></dd></div>
                    <div><dt>Adresse e-mail</dt><dd><c:out value="${encadreur.email}"/></dd></div>
                    <div><dt>Grade</dt><dd><c:out value="${encadreur.grade.libelle}"/></dd></div>
                    <div><dt>Département</dt><dd><c:out value="${encadreur.departement.nom}"/></dd></div>
                </dl>
            </div>
        </section>
    </main>
</div>
</body>
</html>
