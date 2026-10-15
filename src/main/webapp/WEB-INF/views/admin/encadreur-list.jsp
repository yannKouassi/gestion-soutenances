<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Liste des Encadreurs | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261015-admin-menu">
</head>
<body>
<div class="admin-shell">
    <%@ include file="admin-layout.jspf" %>
    <main class="admin-main">
        <%@ include file="admin-topbar.jspf" %>
        <header class="page-heading">
            <div class="page-heading-copy"><span class="page-eyebrow">Acteurs &amp; Comptes / Encadreurs</span>
                <div><h1>Liste des Encadreurs</h1><p>Gère les enseignants, leurs grades et leurs départements.</p></div>
            </div>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/encadreurs?vue=form"><span class="button-icon" aria-hidden="true">+</span> Ajouter un encadreur</a>
        </header>
        <c:if test="${not empty message}"><div class="alert alert-success" role="status"><c:out value="${message}"/></div></c:if>
        <c:if test="${not empty erreur}"><div class="alert alert-error" role="alert"><c:out value="${erreur}"/></div></c:if>
        <section class="card" aria-label="Liste des encadreurs">
            <div class="list-toolbar">
                <label class="search-field"><span class="search-icon" aria-hidden="true"></span><span class="visually-hidden">Rechercher un encadreur</span>
                    <input type="search" data-list-search placeholder="Nom, prénom, e-mail, grade ou département…" autocomplete="off">
                    <button class="search-clear" type="button" data-search-clear aria-label="Effacer la recherche" hidden>&times;</button>
                </label>
                <div class="list-filters">
                    <label class="visually-hidden" for="filter-grade">Filtrer par grade</label>
                    <select id="filter-grade" class="list-filter" data-list-filter="grade"><option value="">Tous les grades</option><c:forEach var="grade" items="${grades}"><option value="<c:out value='${grade.id}'/>"><c:out value="${grade.libelle}"/></option></c:forEach></select>
                    <label class="visually-hidden" for="filter-departement">Filtrer par département</label>
                    <select id="filter-departement" class="list-filter" data-list-filter="departement"><option value="">Tous les départements</option><c:forEach var="departement" items="${departements}"><option value="<c:out value='${departement.id}'/>"><c:out value="${departement.nom}"/></option></c:forEach></select>
                </div>
                <span class="list-count" data-list-count aria-live="polite"></span>
            </div>
            <c:choose><c:when test="${empty elements}">
                <div class="empty-state"><h2>Aucun encadreur pour le moment</h2><p>Ajoute un encadreur ; ses identifiants seront envoyés par e-mail.</p>
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/encadreurs?vue=form">Ajouter un encadreur</a></div>
            </c:when><c:otherwise>
                <div class="table-wrap"><table><thead><tr><th>Nom</th><th>E-mail</th><th>Grade</th><th>Département</th><th>Compte</th><th>Action</th></tr></thead>
                    <tbody data-list-body><c:forEach var="encadreur" items="${elements}">
                        <tr data-list-row data-filter-grade="<c:out value='${encadreur.grade.id}'/>" data-filter-departement="<c:out value='${encadreur.departement.id}'/>">
                            <td class="primary-cell" data-searchable><c:out value="${encadreur.prenom}"/> <c:out value="${encadreur.nom}"/></td>
                            <td data-searchable><c:out value="${encadreur.email}"/></td>
                            <td data-searchable><c:out value="${encadreur.grade.libelle}"/></td>
                            <td data-searchable><c:out value="${encadreur.departement.nom}"/></td>
                            <td><span class="status-badge ${encadreur.utilisateur.actif ? 'status-active' : 'status-inactive'}"><c:out value="${encadreur.utilisateur.actif ? 'Actif' : 'Désactivé'}"/></span></td>
                            <td><a class="row-action row-action-edit" href="${pageContext.request.contextPath}/admin/encadreurs?vue=form&amp;id=${encadreur.id}">Modifier</a></td>
                        </tr>
                    </c:forEach><tr class="search-no-results" data-search-no-results hidden><td colspan="6">Aucun encadreur ne correspond à cette recherche.</td></tr></tbody>
                </table></div>
            </c:otherwise></c:choose>
        </section>
    </main>
</div>
<script src="${pageContext.request.contextPath}/assets/js/admin-list-search.js" defer></script>
</body>
</html>
