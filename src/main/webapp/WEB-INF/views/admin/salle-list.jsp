<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Liste des Salles | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261020-cartoon-ui">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261020-cartoon-ui">
</head>
<body>
<div class="admin-shell">
    <%@ include file="admin-layout.jspf" %>
    <main class="admin-main">
        <%@ include file="admin-topbar.jspf" %>
        <header class="page-heading">
            <div class="page-heading-copy">
                <span class="page-eyebrow">Paramètres généraux / Salles</span>
                <div><h1>Liste des Salles</h1><p>Consulte les espaces de soutenance et leur capacité.</p></div>
            </div>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/salles?vue=form"><span class="button-icon" aria-hidden="true">+</span> Ajouter une salle</a>
        </header>
        <c:if test="${not empty message}"><div class="alert alert-success" role="status"><c:out value="${message}"/></div></c:if>
        <c:if test="${not empty erreur}"><div class="alert alert-error" role="alert"><c:out value="${erreur}"/></div></c:if>
        <section class="card" aria-label="Liste des salles">
            <div class="list-toolbar">
                <label class="search-field">
                    <span class="search-icon" aria-hidden="true"></span>
                    <span class="visually-hidden">Rechercher une salle</span>
                    <input type="search" data-list-search placeholder="Rechercher une salle, une capacité ou un département…" autocomplete="off">
                    <button class="search-clear" type="button" data-search-clear aria-label="Effacer la recherche" hidden>&times;</button>
                </label>
                <span class="list-count" data-list-count aria-live="polite"></span>
            </div>
            <c:choose><c:when test="${empty elements}">
                <div class="empty-state">
                    <span class="empty-state-icon" aria-hidden="true"><svg viewBox="0 0 24 24" focusable="false"><path d="M4 21V3h16v18M2 21h20M9 21v-5h6v5M8 7h.01M12 7h.01M16 7h.01M8 11h.01M16 11h.01"/></svg></span>
                    <h2>Aucune salle pour le moment</h2>
                    <p>Ajoute une salle pour pouvoir planifier les soutenances.</p>
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/salles?vue=form">Ajouter une salle</a>
                </div>
            </c:when><c:otherwise>
                <div class="table-wrap"><table>
                    <thead><tr><th>Nom de la salle</th><th>Capacité</th><th>Département</th><th>Actions</th></tr></thead>
                    <tbody data-list-body><c:forEach var="salle" items="${elements}">
                        <tr data-list-row>
                            <td data-searchable class="primary-cell"><c:out value="${salle.nomSalle}"/></td>
                            <td data-searchable><span class="capacity-badge"><c:out value="${salle.capacite}"/> places</span></td>
                            <td data-searchable><c:out value="${salle.departement.nom}"/></td>
                            <td><div class="actions">
                                <a class="row-action row-action-edit" href="${pageContext.request.contextPath}/admin/salles?vue=form&amp;id=${salle.id}"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="m14 5 5 5M4 20l4.5-1 10-10a2.1 2.1 0 0 0-3-3l-10 10L4 20Z"/></svg>Modifier</a>
                                <form class="inline-form" method="post" action="${pageContext.request.contextPath}/admin/salles/supprimer" onsubmit="return confirm('Supprimer cette salle ?');">
                                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                                    <input type="hidden" name="id" value="<c:out value='${salle.id}'/>">
                                    <button class="row-action row-action-delete" type="submit"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h16M10 11v6m4-6v6M6 7l1 14h10l1-14M9 7V4h6v3"/></svg>Supprimer</button>
                                </form>
                            </div></td>
                        </tr>
                    </c:forEach><tr class="search-no-results" data-search-no-results hidden><td colspan="4">Aucune salle ne correspond à cette recherche.</td></tr></tbody>
                </table></div>
            </c:otherwise></c:choose>
        </section>
    </main>
</div>
<script src="${pageContext.request.contextPath}/assets/js/admin-list-search.js" defer></script>
</body>
</html>
