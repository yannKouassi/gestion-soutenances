<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt2" uri="http://ci.univ.soutenances/tags/format" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Liste des Mémoires | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261021-shell-light">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261021-shell-light">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar-toggle.css?v=20261027-3">
</head>
<body>
<div class="admin-shell">
    <%@ include file="admin-layout.jspf" %>
    <main class="admin-main">
        <%@ include file="admin-topbar.jspf" %>
        <header class="page-heading">
            <div class="page-heading-copy">
                <span class="page-eyebrow">Académique &amp; Soutenances / Mémoires</span>
                <div><h1>Liste des Mémoires</h1><p>Recherche un mémoire par titre ou encadreur et consulte sa date de dépôt.</p></div>
            </div>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/memoires?vue=form"><span class="button-icon" aria-hidden="true">+</span> Ajouter un mémoire</a>
        </header>
        <c:if test="${not empty message}"><div class="alert alert-success" role="status"><c:out value="${message}"/></div></c:if>
        <c:if test="${not empty erreur}"><div class="alert alert-error" role="alert"><c:out value="${erreur}"/></div></c:if>
        <section class="card" aria-label="Liste des mémoires">
            <div class="list-toolbar">
                <label class="search-field">
                    <span class="search-icon" aria-hidden="true"></span>
                    <span class="visually-hidden">Rechercher un mémoire</span>
                    <input type="search" data-list-search placeholder="Titre ou encadreur…" autocomplete="off">
                    <button class="search-clear" type="button" data-search-clear aria-label="Effacer la recherche" hidden>&times;</button>
                </label>
                <div class="list-filters">
                    <label class="visually-hidden" for="filter-encadreur">Filtrer par encadreur</label>
                    <select id="filter-encadreur" class="list-filter" data-list-filter="encadreur"><option value="">Tous les encadreurs</option><c:forEach var="encadreur" items="${encadreurs}"><option value="<c:out value='${encadreur.id}'/>"><c:out value="${encadreur.prenom}"/> <c:out value="${encadreur.nom}"/></option></c:forEach></select>
                </div>
                <span class="list-count" data-list-count aria-live="polite"></span>
            </div>
            <c:choose><c:when test="${empty elements}">
                <div class="empty-state">
                    <h2>Aucun mémoire pour le moment</h2>
                    <p>Ajoute un mémoire et rattache-le à un encadreur pour préparer sa soutenance.</p>
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/memoires?vue=form">Ajouter un mémoire</a>
                </div>
            </c:when><c:otherwise>
                <div class="table-wrap"><table>
                    <thead><tr><th>Titre</th><th>Encadreur</th><th>Date de dépôt</th><th>Actions</th></tr></thead>
                    <tbody data-list-body><c:forEach var="memoire" items="${elements}">
                        <tr data-list-row data-filter-encadreur="<c:out value='${memoire.encadreur.id}'/>">
                            <td data-searchable class="primary-cell"><c:out value="${memoire.titre}"/></td>
                            <td data-searchable><c:out value="${memoire.encadreur.prenom}"/> <c:out value="${memoire.encadreur.nom}"/></td>
                            <td data-searchable><c:out value="${fmt2:date(memoire.dateDepot)}" default="—"/></td>
                            <td><div class="actions">
                                <a class="row-action row-action-edit" href="${pageContext.request.contextPath}/admin/memoires?vue=detail&amp;id=${memoire.id}">Détails</a>
                                <a class="row-action row-action-edit" href="${pageContext.request.contextPath}/admin/memoires?vue=form&amp;id=${memoire.id}"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="m14 5 5 5M4 20l4.5-1 10-10a2.1 2.1 0 0 0-3-3l-10 10L4 20Z"/></svg>Modifier</a>
                                <form class="inline-form" method="post" action="${pageContext.request.contextPath}/admin/memoires/supprimer" onsubmit="return confirm('Supprimer ce mémoire ?');">
                                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                                    <input type="hidden" name="id" value="<c:out value='${memoire.id}'/>">
                                    <button class="row-action row-action-delete" type="submit"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h16M10 11v6m4-6v6M6 7l1 14h10l1-14M9 7V4h6v3"/></svg>Supprimer</button>
                                </form>
                            </div></td>
                        </tr>
                    </c:forEach><tr class="search-no-results" data-search-no-results hidden><td colspan="4">Aucun mémoire ne correspond à cette recherche.</td></tr></tbody>
                </table></div>
            </c:otherwise></c:choose>
        </section>
    </main>
</div>
<script src="${pageContext.request.contextPath}/assets/js/admin-list-search.js" defer></script>
</body>
</html>
