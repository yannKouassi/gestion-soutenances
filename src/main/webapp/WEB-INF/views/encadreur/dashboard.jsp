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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/encadreur.css?v=20261021-shell-light">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar-toggle.css?v=20261027-3">
</head>
<body>
<div class="admin-shell">
    <%@ include file="encadreur-layout.jspf" %>
    <main class="admin-main">
        <%@ include file="encadreur-topbar.jspf" %>
        <header class="page-heading">
            <div class="page-heading-copy"><span class="page-eyebrow">Espace encadreur / Tableau de bord</span>
                <div><h1>Bonjour <c:out value="${encadreur.prenom}"/> <c:out value="${encadreur.nom}"/></h1><p>Suivi de tes convocations aux jurys et de tes notes soumises.</p></div>
            </div>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/encadreur/mes-jurys">Voir mes jurys</a>
        </header>
        <section class="stat-grid" aria-label="Indicateurs de l’encadreur">
            <div class="stat-card"><span class="stat-label">Convocations aux jurys</span><span class="stat-value"><c:out value="${totalConvocations}"/></span></div>
            <div class="stat-card"><span class="stat-label">Notes soumises</span><span class="stat-value"><c:out value="${notesSoumises}"/></span></div>
            <div class="stat-card"><span class="stat-label">En attente de ma note</span><span class="stat-value"><c:out value="${notesEnAttente}"/></span></div>
            <div class="stat-card"><span class="stat-label">Soutenances évaluées</span><span class="stat-value"><c:out value="${soutenancesEvaluees}"/></span></div>
        </section>
        <section class="card" aria-label="Jurys en attente de ma note">
            <div class="form-history-heading">
                <div><span class="page-eyebrow">À traiter</span><h2>Jurys en attente de ma note</h2></div>
                <span class="history-total"><c:out value="${notesEnAttente}"/> en attente</span>
            </div>
            <c:choose><c:when test="${empty aNoter}">
                <p class="form-history-empty">Aucune note en attente. Toutes tes convocations sont notées.</p>
            </c:when><c:otherwise>
                <div class="table-wrap"><table><thead><tr><th>Date</th><th>Mémoire</th><th>Salle</th><th>Statut</th><th>Action</th></tr></thead>
                    <tbody><c:forEach var="ligne" items="${aNoter}">
                        <tr>
                            <td class="history-date"><c:out value="${ligne.dateAffichee}"/></td>
                            <td class="primary-cell" data-searchable><c:out value="${ligne.convocation.soutenance.memoire.titre}"/></td>
                            <td><c:out value="${ligne.convocation.soutenance.salle.nomSalle}"/></td>
                            <td><span class="status-badge status-${ligne.convocation.soutenance.statut}"><c:out value="${ligne.convocation.soutenance.statut}"/></span></td>
                            <td><a class="row-action row-action-edit" href="${pageContext.request.contextPath}/encadreur/mes-jurys?vue=note&amp;id=${ligne.convocation.soutenance.id}">Saisir ma note</a></td>
                        </tr>
                    </c:forEach></tbody>
                </table></div>
            </c:otherwise></c:choose>
        </section>
        <section class="card" aria-label="Toutes mes convocations">
            <div class="form-history-heading">
                <div><span class="page-eyebrow">Récapitulatif</span><h2>Toutes mes convocations</h2></div>
                <span class="history-total"><c:out value="${totalConvocations}"/> convocation<c:if test="${totalConvocations ne 1}">s</c:if></span>
            </div>
            <c:choose><c:when test="${empty lignes}">
                <p class="form-history-empty">Tu n’es convoqué à aucun jury pour le moment.</p>
            </c:when><c:otherwise>
                <div class="table-wrap"><table><thead><tr><th>Date</th><th>Mémoire</th><th>Jury</th><th>Ma note</th><th>Moyenne</th></tr></thead>
                    <tbody><c:forEach var="ligne" items="${lignes}">
                        <tr>
                            <td class="history-date"><c:out value="${ligne.dateAffichee}"/></td>
                            <td class="primary-cell"><c:out value="${ligne.convocation.soutenance.memoire.titre}"/></td>
                            <td><c:out value="${ligne.jury.notesSoumises}"/>/<c:out value="${ligne.jury.taille}"/> notes</td>
                            <td><c:choose><c:when test="${empty ligne.convocation.note}">—</c:when><c:otherwise><c:out value="${fmt2:note(ligne.convocation.note)}"/>/20</c:otherwise></c:choose></td>
                            <td><c:choose><c:when test="${empty ligne.jury.moyenne}">—</c:when><c:otherwise><c:out value="${ligne.jury.moyenne}"/>/20</c:otherwise></c:choose></td>
                        </tr>
                    </c:forEach></tbody>
                </table></div>
            </c:otherwise></c:choose>
        </section>
    </main>
</div>
</body>
</html>
