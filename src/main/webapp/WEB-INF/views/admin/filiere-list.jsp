<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Liste des Filières | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261015-admin-menu">
</head>
<body>
<div class="admin-shell">
    <%@ include file="admin-layout.jspf" %>
    <main class="admin-main">
        <%@ include file="admin-topbar.jspf" %>
        <header class="page-heading">
            <div class="page-heading-copy">
                <span class="page-eyebrow">Paramètres généraux / Filières</span>
                <div><h1>Liste des Filières</h1><p>Consulte les formations et leur département de rattachement.</p></div>
            </div>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/filieres?vue=form"><span class="button-icon" aria-hidden="true">+</span> Ajouter une filière</a>
        </header>
        <c:if test="${not empty message}"><div class="alert alert-success" role="status"><c:out value="${message}"/></div></c:if>
        <c:if test="${not empty erreur}"><div class="alert alert-error" role="alert"><c:out value="${erreur}"/></div></c:if>
        <section class="card" aria-label="Liste des filières">
            <div class="list-toolbar">
                <label class="search-field">
                    <span class="search-icon" aria-hidden="true"></span>
                    <span class="visually-hidden">Rechercher une filière</span>
                    <input type="search" data-list-search placeholder="Rechercher un code, une filière ou un département…" autocomplete="off">
                    <button class="search-clear" type="button" data-search-clear aria-label="Effacer la recherche" hidden>&times;</button>
                </label>
                <span class="list-count" data-list-count aria-live="polite"></span>
            </div>
            <c:choose><c:when test="${empty elements}">
                <div class="empty-state">
                    <span class="empty-state-icon" aria-hidden="true"><svg viewBox="0 0 24 24" focusable="false"><circle cx="5" cy="5" r="2"/><circle cx="19" cy="5" r="2"/><circle cx="12" cy="19" r="2"/><path d="M7 5h10M6 7l5 10m7-10-5 10"/></svg></span>
                    <h2>Aucune filière pour le moment</h2>
                    <p>Ajoute une filière et rattache-la à son département.</p>
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/filieres?vue=form">Ajouter une filière</a>
                </div>
            </c:when><c:otherwise>
                <div class="table-wrap"><table>
                    <thead><tr><th>Code</th><th>Libellé</th><th>Département</th><th>Actions</th></tr></thead>
                    <tbody data-list-body><c:forEach var="filiere" items="${elements}">
                        <tr data-list-row>
                            <td data-searchable><span class="code-badge"><c:out value="${filiere.code}"/></span></td>
                            <td data-searchable class="primary-cell"><c:out value="${filiere.libelle}"/></td>
                            <td data-searchable><c:out value="${filiere.departement.nom}"/></td>
                            <td><div class="actions">
                                <a class="row-action row-action-edit" href="${pageContext.request.contextPath}/admin/filieres?vue=form&amp;id=${filiere.id}"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="m14 5 5 5M4 20l4.5-1 10-10a2.1 2.1 0 0 0-3-3l-10 10L4 20Z"/></svg>Modifier</a>
                                <form class="inline-form" method="post" action="${pageContext.request.contextPath}/admin/filieres/supprimer" onsubmit="return confirm('Supprimer cette filière ?');">
                                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                                    <input type="hidden" name="id" value="<c:out value='${filiere.id}'/>">
                                    <button class="row-action row-action-delete" type="submit"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h16M10 11v6m4-6v6M6 7l1 14h10l1-14M9 7V4h6v3"/></svg>Supprimer</button>
                                </form>
                            </div></td>
                        </tr>
                    </c:forEach><tr class="search-no-results" data-search-no-results hidden><td colspan="4">Aucune filière ne correspond à cette recherche.</td></tr></tbody>
                </table></div>
            </c:otherwise></c:choose>
        </section>
    </main>
</div>
<script src="${pageContext.request.contextPath}/assets/js/admin-list-search.js" defer></script>
</body>
</html>
