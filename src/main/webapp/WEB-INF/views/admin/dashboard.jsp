<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt2" uri="http://ci.univ.soutenances/tags/format" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Tableau de bord | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261021-shell-light">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261029-dashboard">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar-toggle.css?v=20261027-3">
</head>
<body>
<div class="admin-shell">
    <%@ include file="admin-layout.jspf" %>
    <main class="admin-main">
        <%@ include file="admin-topbar.jspf" %>
        <header class="page-heading">
            <div class="page-heading-copy">
                <span class="page-eyebrow">Pilotage &amp; Audit / Tableau de bord</span>
                <div>
                    <h1>Tableau de bord</h1>
                    <p>Vue d’ensemble des soutenances, des jurys incomplets et des prochains créneaux planifiés.</p>
                </div>
            </div>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/planification"><span class="button-icon" aria-hidden="true">+</span> Planifier une soutenance</a>
        </header>

        <section class="dashboard-grid" aria-label="Indicateurs des soutenances">
            <article class="dashboard-card">
                <span class="dashboard-label">Soutenances</span>
                <span class="dashboard-value"><c:out value="${totalSoutenances}"/></span>
            </article>
            <article class="dashboard-card">
                <span class="dashboard-label">Planifiées</span>
                <span class="dashboard-value"><c:out value="${soutenancesPlanifiees}"/></span>
            </article>
            <article class="dashboard-card">
                <span class="dashboard-label">Évaluées</span>
                <span class="dashboard-value"><c:out value="${soutenancesEvaluees}"/></span>
            </article>
            <article class="dashboard-card">
                <span class="dashboard-label">Annulées</span>
                <span class="dashboard-value"><c:out value="${soutenancesAnnulees}"/></span>
            </article>
            <article class="dashboard-card ${jurysIncomplets gt 0 ? 'dashboard-card-alert' : ''}">
                <span class="dashboard-label">Jurys incomplets</span>
                <span class="dashboard-value"><c:out value="${jurysIncomplets}"/></span>
            </article>
        </section>

        <section class="dashboard-grid" aria-label="Indicateurs des référentiels">
            <article class="dashboard-card">
                <span class="dashboard-label">Mémoires</span>
                <span class="dashboard-value"><c:out value="${totalMemoires}"/></span>
            </article>
            <article class="dashboard-card">
                <span class="dashboard-label">Étudiants</span>
                <span class="dashboard-value"><c:out value="${totalEtudiants}"/></span>
            </article>
            <article class="dashboard-card">
                <span class="dashboard-label">Encadreurs</span>
                <span class="dashboard-value"><c:out value="${totalEncadreurs}"/></span>
            </article>
            <article class="dashboard-card">
                <span class="dashboard-label">Salles</span>
                <span class="dashboard-value"><c:out value="${totalSalles}"/></span>
            </article>
        </section>

        <c:if test="${jurysIncomplets gt 0}">
            <div class="alert alert-warning" role="status">
                <c:out value="${jurysIncomplets}"/> soutenance<c:if test="${jurysIncomplets ne 1}">s</c:if> planifiée<c:if test="${jurysIncomplets ne 1}">s</c:if> n’a pas encore un jury complet.
                <a href="${pageContext.request.contextPath}/admin/soutenances?vue=jurys">Composer les jurys</a>
            </div>
        </c:if>

        <section class="card" aria-label="Prochaines soutenances">
            <div class="card-body">
                <div class="form-history-heading">
                    <div>
                        <span class="page-eyebrow">Planning</span>
                        <h2>Prochaines soutenances</h2>
                    </div>
                    <a class="row-action row-action-edit" href="${pageContext.request.contextPath}/admin/soutenances">Voir toutes les soutenances</a>
                </div>
                <c:choose>
                    <c:when test="${empty prochaines}">
                        <p class="form-history-empty">Aucune soutenance planifiée à venir.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="table-wrap">
                            <table class="journal-table">
                                <thead><tr><th>Date</th><th>Mémoire</th><th>Étudiant</th><th>Salle</th><th>Statut</th><th>Actions</th></tr></thead>
                                <tbody>
                                <c:forEach var="vue" items="${prochaines}">
                                    <tr>
                                        <td class="history-date"><c:out value="${fmt2:dateHeure(vue.soutenance.dateHeure)}"/></td>
                                        <td class="primary-cell"><c:out value="${vue.soutenance.memoire.titre}"/></td>
                                        <td><c:choose><c:when test="${empty vue.etudiant}">Non rattaché</c:when><c:otherwise><c:out value="${vue.etudiant.prenom}"/> <c:out value="${vue.etudiant.nom}"/></c:otherwise></c:choose></td>
                                        <td><c:out value="${vue.soutenance.salle.nomSalle}"/></td>
                                        <td><span class="status-badge status-${vue.soutenance.statut}"><c:out value="${vue.soutenance.statut}"/></span></td>
                                        <td><div class="actions">
                                            <a class="row-action row-action-edit" href="${pageContext.request.contextPath}/admin/soutenances?vue=detail&amp;id=${vue.soutenance.id}">Détails</a>
                                            <a class="row-action row-action-edit" href="${pageContext.request.contextPath}/admin/jury?soutenance=${vue.soutenance.id}">Gérer le jury</a>
                                        </div></td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>
    </main>
</div>
</body>
</html>
