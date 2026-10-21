<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Liste des Comptes | PGS-PFE</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261020-cartoon-ui">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261020-cartoon-ui">
    </head>
<body><div class="admin-shell">
    <%@ include file="admin-layout.jspf" %>
    <main class="admin-main"><%@ include file="admin-topbar.jspf" %>
        <header class="page-heading"><div class="page-heading-copy"><span class="page-eyebrow">Acteurs &amp; Comptes / Comptes Utilisateurs</span><div><h1>Liste des Comptes</h1><p>Active ou désactive les accès et envoie un nouveau mot de passe temporaire.</p></div></div></header>
        <c:if test="${not empty message}"><div class="alert alert-success" role="status"><c:out value="${message}"/></div></c:if><c:if test="${not empty erreur}"><div class="alert alert-error" role="alert"><c:out value="${erreur}"/></div></c:if>
        <section class="card" aria-label="Comptes utilisateurs"><div class="list-toolbar"><label class="search-field"><span class="search-icon" aria-hidden="true"></span><span class="visually-hidden">Rechercher un compte</span><input type="search" data-list-search placeholder="Login, rôle ou e-mail…" autocomplete="off"><button class="search-clear" type="button" data-search-clear aria-label="Effacer la recherche" hidden>&times;</button></label><span class="list-count" data-list-count aria-live="polite"></span></div>
            <c:choose><c:when test="${empty accounts}"><div class="empty-state"><h2>Aucun compte utilisateur</h2><p>Les comptes apparaîtront ici après la création d’encadreurs ou d’étudiants.</p></div></c:when>
            <c:otherwise><div class="table-wrap"><table><thead><tr><th>Login</th><th>Rôle</th><th>E-mail</th><th>État</th><th>Actions</th></tr></thead><tbody data-list-body>
                <c:forEach var="account" items="${accounts}"><tr data-list-row><td data-searchable class="primary-cell"><c:out value="${account.login}"/></td><td data-searchable><c:choose><c:when test="${account.role eq 'ADMIN'}">Administrateur</c:when><c:when test="${account.role eq 'ENCADREUR'}">Encadreur</c:when><c:otherwise>Étudiant</c:otherwise></c:choose></td><td data-searchable><c:out value="${account.email}"/></td>
                    <td><span class="status-badge ${account.actif ? 'status-active' : 'status-inactive'}"><c:out value="${account.actif ? 'Actif' : 'Désactivé'}"/></span></td>
                    <td><div class="actions">
                        <c:choose><c:when test="${account.actif}"><form class="inline-form" method="post" action="${pageContext.request.contextPath}/admin/utilisateurs/desactiver" onsubmit="return confirm('Désactiver ce compte ?');"><input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>"><input type="hidden" name="id" value="<c:out value='${account.id}'/>"><button class="row-action row-action-delete" type="submit">Désactiver</button></form></c:when><c:otherwise><form class="inline-form" method="post" action="${pageContext.request.contextPath}/admin/utilisateurs/activer"><input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>"><input type="hidden" name="id" value="<c:out value='${account.id}'/>"><button class="row-action row-action-edit" type="submit">Activer</button></form></c:otherwise></c:choose>
                        <c:if test="${not empty account.email and account.actif}"><form class="inline-form" method="post" action="${pageContext.request.contextPath}/admin/utilisateurs/reinitialiser-mot-de-passe" onsubmit="return confirm('Envoyer un nouveau mot de passe temporaire par e-mail ?');"><input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>"><input type="hidden" name="id" value="<c:out value='${account.id}'/>"><button class="row-action row-action-edit" type="submit">Réinitialiser le mot de passe</button></form></c:if>
                    </div></td></tr></c:forEach><tr class="search-no-results" data-search-no-results hidden><td colspan="5">Aucun compte ne correspond à cette recherche.</td></tr>
            </tbody></table></div></c:otherwise></c:choose>
        </section>
    </main>
</div><script src="${pageContext.request.contextPath}/assets/js/admin-list-search.js" defer></script></body>
</html>
