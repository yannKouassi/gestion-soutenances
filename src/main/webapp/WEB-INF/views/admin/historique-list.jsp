<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Journal d'audit | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css">
</head>
<body>
<div class="admin-shell">
    <%@ include file="admin-layout.jspf" %>
    <main class="admin-main">
        <%@ include file="admin-topbar.jspf" %>
        <header class="page-heading">
            <div class="page-heading-copy">
                <span class="page-eyebrow">Pilotage &amp; audit</span>
                <div><h1>Journal d'audit</h1><p>Toutes les actions enregistrées sur les référentiels, triées par date décroissante.</p></div>
            </div>
            <span class="history-total"><c:out value="${totalActions}"/> action<c:if test="${totalActions ne 1}">s</c:if></span>
        </header>
        <section class="card filters-card" aria-label="Filtres du journal">
            <div class="card-body">
                <form method="get" action="${pageContext.request.contextPath}/admin/historique">
                <div class="filter-grid">
                    <div class="field">
                        <label for="filtre-ecran">Écran</label>
                        <select id="filtre-ecran" name="ecran">
                            <option value="">Tous les écrans</option>
                            <c:forEach var="screen" items="${screens}">
                                <option value="<c:out value='${screen.key}'/>" ${screen.key eq filtreEcran ? 'selected' : ''}><c:out value="${screen.value}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="field">
                        <label for="filtre-action">Action</label>
                        <select id="filtre-action" name="action">
                            <option value="">Toutes les actions</option>
                            <c:forEach var="filterAction" items="${actions}">
                                <option value="<c:out value='${filterAction.key}'/>" ${filterAction.key eq filtreAction ? 'selected' : ''}><c:out value="${filterAction.value}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="field">
                        <label for="filtre-auteur">Auteur</label>
                        <select id="filtre-auteur" name="auteur">
                            <option value="">Tous les auteurs</option>
                            <c:forEach var="auteur" items="${auteurs}">
                                <option value="<c:out value='${auteur}'/>" ${auteur eq filtreAuteur ? 'selected' : ''}><c:out value="${auteur}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="field">
                        <label for="filtre-du">Du</label>
                        <input id="filtre-du" type="date" name="du" value="<c:out value='${filtreDu}'/>">
                    </div>
                    <div class="field">
                        <label for="filtre-au">Au</label>
                        <input id="filtre-au" type="date" name="au" value="<c:out value='${filtreAu}'/>">
                    </div>
                    <div class="field">
                        <label for="filtre-taille">Lignes par page</label>
                        <select id="filtre-taille" name="taille">
                            <c:forEach var="choix" items="${tailles}">
                                <option value="<c:out value='${choix}'/>" ${choix eq taille ? 'selected' : ''}><c:out value="${choix}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                    </div>
                    <div class="filter-actions">
                        <button class="btn btn-primary" type="submit">Filtrer</button>
                        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/admin/historique">Réinitialiser</a>
                    </div>
                </form>
            </div>
        </section>
        <section class="card" aria-label="Journal des actions">
            <c:choose><c:when test="${empty entries}">
                <div class="empty-state">
                    <span class="empty-state-icon" aria-hidden="true"><svg viewBox="0 0 24 24" focusable="false"><circle cx="12" cy="12" r="8"/><path d="M12 8v4m0 3h.01"/></svg></span>
                    <h2>Aucune action à afficher</h2>
                    <p>Aucune action ne correspond aux critères sélectionnés.</p>
                </div>
            </c:when><c:otherwise>
                <div class="table-wrap"><table class="journal-table">
                    <thead><tr><th>Date</th><th>Libellé</th><th>Action</th><th>Effectuée par</th><th>Écran</th></tr></thead>
                    <tbody><c:forEach var="entry" items="${entries}">
                        <tr>
                            <td class="history-date"><c:out value="${entry.date}"/></td>
                            <td class="primary-cell"><c:out value="${entry.label}"/></td>
                            <td><span class="history-action"><c:out value="${entry.action}"/></span></td>
                            <td><span class="role-badge role-${entry.userRole}"><c:out value="${entry.user}"/></span></td>
                            <td><span class="code-badge"><c:out value="${entry.screen}"/></span></td>
                        </tr>
                    </c:forEach></tbody>
                </table></div>
                <c:if test="${totalPages gt 1}">
                    <nav class="form-history-pagination" aria-label="Pagination du journal">
                        <span>Page <c:out value="${page}"/> sur <c:out value="${totalPages}"/></span>
                        <div class="history-page-links">
                            <c:if test="${page gt 1}">
                                <c:url var="previousUrl" value="/admin/historique">
                                    <c:param name="ecran" value="${filtreEcran}"/><c:param name="action" value="${filtreAction}"/><c:param name="auteur" value="${filtreAuteur}"/><c:param name="du" value="${filtreDu}"/><c:param name="au" value="${filtreAu}"/><c:param name="taille" value="${taille}"/><c:param name="page" value="${page - 1}"/>
                                </c:url>
                                <a class="page-link" href="${previousUrl}" aria-label="Page précédente">Précédent</a>
                            </c:if>
                            <c:forEach var="pageNumber" begin="${firstPage}" end="${lastPage}">
                                <c:url var="pageUrl" value="/admin/historique">
                                    <c:param name="ecran" value="${filtreEcran}"/><c:param name="action" value="${filtreAction}"/><c:param name="auteur" value="${filtreAuteur}"/><c:param name="du" value="${filtreDu}"/><c:param name="au" value="${filtreAu}"/><c:param name="taille" value="${taille}"/><c:param name="page" value="${pageNumber}"/>
                                </c:url>
                                <a class="page-link ${pageNumber eq page ? 'current' : ''}" href="${pageUrl}" aria-label="Page ${pageNumber}" ${pageNumber eq page ? 'aria-current="page"' : ''}><c:out value="${pageNumber}"/></a>
                            </c:forEach>
                            <c:if test="${page lt totalPages}">
                                <c:url var="nextUrl" value="/admin/historique">
                                    <c:param name="ecran" value="${filtreEcran}"/><c:param name="action" value="${filtreAction}"/><c:param name="auteur" value="${filtreAuteur}"/><c:param name="du" value="${filtreDu}"/><c:param name="au" value="${filtreAu}"/><c:param name="taille" value="${taille}"/><c:param name="page" value="${page + 1}"/>
                                </c:url>
                                <a class="page-link" href="${nextUrl}" aria-label="Page suivante">Suivant</a>
                            </c:if>
                        </div>
                    </nav>
                </c:if>
            </c:otherwise></c:choose>
        </section>
    </main>
</div>
</body>
</html>
