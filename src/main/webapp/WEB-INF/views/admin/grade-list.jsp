<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Liste des Grades | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261031">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261031">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar-toggle.css?v=20261031">
</head>
<body>
<div class="admin-shell">
    <%@ include file="admin-layout.jspf" %>
    <main id="main-content" class="admin-main">
        <%@ include file="admin-topbar.jspf" %>
        <header class="page-heading">
            <div class="page-heading-copy">
                <span class="page-eyebrow">Paramètres généraux / Grades</span>
                <div><h1>Liste des Grades</h1><p>Consulte et gère les grades des enseignants et encadreurs.</p></div>
            </div>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/grades?vue=form"><span class="button-icon" aria-hidden="true">+</span> Ajouter un grade</a>
        </header>
        <c:if test="${not empty message}"><div class="alert alert-success" role="status"><c:out value="${message}"/></div></c:if>
        <c:if test="${not empty erreur}"><div class="alert alert-error" role="alert"><c:out value="${erreur}"/></div></c:if>
        <section class="card" aria-label="Liste des grades">
            <div class="list-toolbar">
                <label class="search-field">
                    <span class="search-icon" aria-hidden="true"></span>
                    <span class="visually-hidden">Rechercher un grade</span>
                    <input type="search" data-list-search placeholder="Rechercher un code ou un libellé…" autocomplete="off">
                    <button class="search-clear" type="button" data-search-clear aria-label="Effacer la recherche" hidden>&times;</button>
                </label>
                <span class="list-count" data-list-count aria-live="polite"></span>
            </div>
            <c:choose><c:when test="${empty elements}">
                <div class="empty-state">
                    <span class="empty-state-icon" aria-hidden="true"><svg viewBox="0 0 24 24" focusable="false"><path d="m12 3 2.7 5.5 6.1.9-4.4 4.3 1 6.1-5.4-2.9-5.4 2.9 1-6.1-4.4-4.3 6.1-.9L12 3Z"/></svg></span>
                    <h2>Aucun grade pour le moment</h2>
                    <p>Ajoute le premier grade pour compléter ce référentiel.</p>
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/grades?vue=form">Ajouter un grade</a>
                </div>
            </c:when><c:otherwise>
                <div class="table-wrap"><table>
                    <thead><tr><th>Code</th><th>Libellé</th><th>Actions</th></tr></thead>
                    <tbody data-list-body><c:forEach var="grade" items="${elements}">
                        <tr data-list-row>
                            <td data-searchable><span class="code-badge"><c:out value="${grade.code}"/></span></td>
                            <td data-searchable class="primary-cell"><c:out value="${grade.libelle}"/></td>
                            <td><div class="actions">
                                <a class="row-action row-action-edit" href="${pageContext.request.contextPath}/admin/grades?vue=form&amp;id=${grade.id}"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="m14 5 5 5M4 20l4.5-1 10-10a2.1 2.1 0 0 0-3-3l-10 10L4 20Z"/></svg>Modifier</a>
                                <form class="inline-form" method="post" action="${pageContext.request.contextPath}/admin/grades/supprimer" onsubmit="return confirm('Supprimer ce grade ?');">
                                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                                    <input type="hidden" name="id" value="<c:out value='${grade.id}'/>">
                                    <button class="row-action row-action-delete" type="submit"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h16M10 11v6m4-6v6M6 7l1 14h10l1-14M9 7V4h6v3"/></svg>Supprimer</button>
                                </form>
                            </div></td>
                        </tr>
                    </c:forEach><tr class="search-no-results" data-search-no-results hidden><td colspan="3">Aucun grade ne correspond à cette recherche.</td></tr></tbody>
                </table></div>
            </c:otherwise></c:choose>
        </section>
    </main>
</div>
<script src="${pageContext.request.contextPath}/assets/js/admin-list-search.js" defer></script>
</body>
</html>
