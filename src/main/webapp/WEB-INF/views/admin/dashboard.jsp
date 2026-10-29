<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Tableau de bord | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261030">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261030">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar-toggle.css?v=20261030">
</head>
<body>
<div class="admin-shell">
    <%@ include file="admin-layout.jspf" %>
    <main id="main-content" class="admin-main">
        <%@ include file="admin-topbar.jspf" %>
        <header class="page-heading">
            <div class="page-heading-copy">
                <span class="page-eyebrow">Pilotage &amp; audit</span>
                <div>
                    <h1>Tableau de bord</h1>
                    <p>Vue d'ensemble des soutenances et des référentiels — <c:out value="${dateDuJour}"/>.</p>
                </div>
            </div>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/planification"><span class="button-icon" aria-hidden="true">+</span> Planifier une soutenance</a>
        </header>

        <section class="dash-kpis" aria-label="Indicateurs clés">
            <article class="dash-kpi dash-kpi-primary">
                <span class="dash-kpi-icon" aria-hidden="true"><svg viewBox="0 0 24 24" focusable="false"><path d="M4 5h16v15H4z"/><path d="M8 3v4m8-4v4M4 10h16"/></svg></span>
                <span class="dash-kpi-label">Soutenances planifiées</span>
                <strong class="dash-kpi-value"><c:out value="${soutenancesPlanifiees}"/></strong>
                <span class="dash-kpi-note">sur <c:out value="${totalSoutenances}"/> soutenance<c:if test="${totalSoutenances ne 1}">s</c:if> au total</span>
            </article>
            <article class="dash-kpi">
                <span class="dash-kpi-icon" aria-hidden="true"><svg viewBox="0 0 24 24" focusable="false"><circle cx="12" cy="12" r="9"/><path d="m8 12 3 3 5-6"/></svg></span>
                <span class="dash-kpi-label">Soutenances évaluées</span>
                <strong class="dash-kpi-value"><c:out value="${soutenancesEvaluees}"/></strong>
                <span class="dash-kpi-note"><c:out value="${pctEvaluees}"/> % des soutenances</span>
            </article>
            <article class="dash-kpi">
                <span class="dash-kpi-icon" aria-hidden="true"><svg viewBox="0 0 24 24" focusable="false"><circle cx="9" cy="8" r="3"/><path d="M3 20a6 6 0 0 1 12 0m2-9h5m-2.5-2.5v5"/></svg></span>
                <span class="dash-kpi-label">Étudiants inscrits</span>
                <strong class="dash-kpi-value"><c:out value="${totalEtudiants}"/></strong>
                <span class="dash-kpi-note"><c:out value="${totalMemoires}"/> mémoire<c:if test="${totalMemoires ne 1}">s</c:if> déposé<c:if test="${totalMemoires ne 1}">s</c:if></span>
            </article>
            <article class="dash-kpi">
                <span class="dash-kpi-icon" aria-hidden="true"><svg viewBox="0 0 24 24" focusable="false"><circle cx="12" cy="8" r="3.5"/><path d="M5 21v-2a7 7 0 0 1 14 0v2"/></svg></span>
                <span class="dash-kpi-label">Encadreurs</span>
                <strong class="dash-kpi-value"><c:out value="${totalEncadreurs}"/></strong>
                <span class="dash-kpi-note">membres de jurys et encadreurs</span>
            </article>
        </section>

        <section class="card dash-block dash-agenda" aria-label="Prochaines soutenances">
                <div class="form-history-heading">
                    <div><span class="page-eyebrow">Agenda</span><h2>Prochaines soutenances</h2></div>
                    <a class="dash-link" href="${pageContext.request.contextPath}/admin/soutenances">Tout voir</a>
                </div>
                <c:choose><c:when test="${empty prochaines}">
                    <p class="dash-empty">Aucune soutenance planifiée à venir.</p>
                </c:when><c:otherwise>
                    <div class="table-wrap"><table>
                        <thead><tr><th>Date</th><th>Mémoire</th><th>Étudiant</th><th>Salle</th><th>Statut</th></tr></thead>
                        <tbody><c:forEach var="entree" items="${prochaines}">
                            <tr>
                                <td class="history-date"><c:out value="${entree.date}"/></td>
                                <td class="primary-cell"><c:out value="${entree.memoire}"/></td>
                                <td><c:out value="${entree.etudiant}"/></td>
                                <td><c:out value="${entree.salle}"/></td>
                                <td><span class="status-badge status-${entree.statut}"><c:out value="${entree.statut}"/></span></td>
                            </tr>
                        </c:forEach></tbody>
                    </table></div>
                </c:otherwise></c:choose>
        </section>

        <div class="dash-grid">
            <section class="card" aria-label="Répartition des soutenances">
                <div class="form-history-heading">
                    <div><span class="page-eyebrow">Répartition</span><h2>Statut des soutenances</h2></div>
                    <span class="history-total"><c:out value="${totalSoutenances}"/> au total</span>
                </div>
                <div class="dash-bars">
                    <div class="dash-bar-row">
                        <span class="dash-bar-head"><span class="status-badge status-PLANIFIEE">Planifiée</span><b><c:out value="${soutenancesPlanifiees}"/></b></span>
                        <span class="dash-bar-track"><span class="dash-bar-fill fill-planifiee" style="width: ${pctPlanifiees}%"></span></span>
                        <span class="dash-bar-pct"><c:out value="${pctPlanifiees}"/> %</span>
                    </div>
                    <div class="dash-bar-row">
                        <span class="dash-bar-head"><span class="status-badge status-EVALUEE">Évaluée</span><b><c:out value="${soutenancesEvaluees}"/></b></span>
                        <span class="dash-bar-track"><span class="dash-bar-fill fill-evaluee" style="width: ${pctEvaluees}%"></span></span>
                        <span class="dash-bar-pct"><c:out value="${pctEvaluees}"/> %</span>
                    </div>
                    <div class="dash-bar-row">
                        <span class="dash-bar-head"><span class="status-badge status-ANNULEE">Annulée</span><b><c:out value="${soutenancesAnnulees}"/></b></span>
                        <span class="dash-bar-track"><span class="dash-bar-fill fill-annulee" style="width: ${pctAnnulees}%"></span></span>
                        <span class="dash-bar-pct"><c:out value="${pctAnnulees}"/> %</span>
                    </div>
                </div>
                <dl class="dash-refs">
                    <div><dt>Départements</dt><dd><c:out value="${totalDepartements}"/></dd></div>
                    <div><dt>Filières</dt><dd><c:out value="${totalFilieres}"/></dd></div>
                    <div><dt>Salles</dt><dd><c:out value="${totalSalles}"/></dd></div>
                    <div><dt>Comptes</dt><dd><c:out value="${totalComptes}"/></dd></div>
                </dl>
                <p class="dash-note">Une soutenance passe au statut « Évaluée » uniquement lorsque tous les membres du jury ont saisi une note ; la note finale affichée est la moyenne du jury.</p>
            </section>

            <section class="card" aria-label="Activité récente">
                <div class="form-history-heading">
                    <div><span class="page-eyebrow">Traçabilité</span><h2>Activité récente</h2></div>
                    <a class="dash-link" href="${pageContext.request.contextPath}/admin/historique">Journal complet</a>
                </div>
                <c:choose><c:when test="${empty activites}">
                    <p class="dash-empty">Aucune action enregistrée pour le moment.</p>
                </c:when><c:otherwise>
                    <div class="table-wrap"><table>
                        <thead><tr><th>Date</th><th>Action</th><th>Auteur</th></tr></thead>
                        <tbody><c:forEach var="action" items="${activites}">
                            <tr>
                                <td class="history-date"><c:out value="${action.date}"/></td>
                                <td class="primary-cell"><c:out value="${action.label}"/></td>
                                <td><span class="role-badge role-${action.userRole}"><c:out value="${action.user}"/></span></td>
                            </tr>
                        </c:forEach></tbody>
                    </table></div>
                </c:otherwise></c:choose>
            </section>
        </div>

        <section class="card dash-block" aria-label="Accès rapides">
            <div class="form-history-heading">
                <div><span class="page-eyebrow">Raccourcis</span><h2>Accès rapides</h2></div>
            </div>
                <div class="dash-links">
                    <a class="dash-link-card" href="${pageContext.request.contextPath}/admin/planification">
                        <span class="dash-link-icon" aria-hidden="true"><svg viewBox="0 0 24 24" focusable="false"><path d="M4 5h16v15H4z"/><path d="M8 3v4m8-4v4M4 10h16"/><path d="M12 13v4m-2-2h4"/></svg></span>
                        Planifier une soutenance
                    </a>
                    <a class="dash-link-card" href="${pageContext.request.contextPath}/admin/memoires?vue=form">
                        <span class="dash-link-icon" aria-hidden="true"><svg viewBox="0 0 24 24" focusable="false"><path d="M4 4.5A2.5 2.5 0 0 1 6.5 2H20v17H6.5A2.5 2.5 0 0 0 4 21.5Z"/><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/></svg></span>
                        Ajouter un mémoire
                    </a>
                    <a class="dash-link-card" href="${pageContext.request.contextPath}/admin/encadreurs?vue=form">
                        <span class="dash-link-icon" aria-hidden="true"><svg viewBox="0 0 24 24" focusable="false"><circle cx="12" cy="8" r="3.5"/><path d="M5 21v-2a7 7 0 0 1 14 0v2"/><path d="M19 8h3m-1.5-1.5v3"/></svg></span>
                        Ajouter un encadreur
                    </a>
                    <a class="dash-link-card" href="${pageContext.request.contextPath}/admin/etudiants?vue=form">
                        <span class="dash-link-icon" aria-hidden="true"><svg viewBox="0 0 24 24" focusable="false"><circle cx="9" cy="8" r="3"/><path d="M3 20a6 6 0 0 1 12 0"/><path d="M19 9h3m-1.5-1.5v5"/></svg></span>
                        Ajouter un étudiant
                    </a>
                </div>
            </section>
    </main>
</div>
</body>
</html>
