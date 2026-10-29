<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt2" uri="http://ci.univ.soutenances/tags/format" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${titre}"/> | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261032">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/encadreur.css?v=20261032">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar-toggle.css?v=20261032">
</head>
<body>
<div class="admin-shell">
    <%@ include file="encadreur-layout.jspf" %>
    <main id="main-content" class="admin-main">
        <%@ include file="encadreur-topbar.jspf" %>
        <header class="page-heading">
            <div class="page-heading-copy"><span class="page-eyebrow">Menu Encadreur / <c:out value="${titre}"/></span>
                <div><h1><c:out value="${titre}"/></h1><p><c:choose><c:when test="${type eq 'evaluation'}">Choisis un jury pour consulter ou saisir ton évaluation.</c:when><c:otherwise>Les soutenances auxquelles tu es convoqué. Un jury comporte au moins deux membres.</c:otherwise></c:choose></p></div>
            </div>
        </header>
        <c:if test="${not empty message}"><div class="alert alert-success" role="status"><c:out value="${message}"/></div></c:if>
        <c:if test="${not empty erreur}"><div class="alert alert-error" role="alert"><c:out value="${erreur}"/></div></c:if>
        <section class="card" aria-label="Soutenances où je suis convoqué">
            <c:choose><c:when test="${empty lignes}">
                <div class="empty-state"><h2>Aucune convocation pour le moment</h2><p>Les soutenances auxquelles tu seras convoqué apparaîtront ici.</p></div>
            </c:when><c:otherwise>
                <div class="table-wrap"><table><thead><tr><th>Date</th><th>Mémoire</th><th>Salle</th><th>Statut</th><th>Jury</th><th>Ma note</th><th>Moyenne du jury</th><th>Action</th></tr></thead>
                    <tbody><c:forEach var="ligne" items="${lignes}">
                        <tr>
                            <td class="history-date"><c:out value="${ligne.dateAffichee}"/></td>
                            <td class="primary-cell"><c:out value="${ligne.convocation.soutenance.memoire.titre}"/></td>
                            <td><c:out value="${ligne.convocation.soutenance.salle.nomSalle}"/></td>
                            <td><span class="status-badge status-${ligne.convocation.soutenance.statut}"><c:out value="${ligne.convocation.soutenance.statut}"/></span></td>
                            <td><c:out value="${ligne.jury.taille}"/> membres <span class="history-total"><c:out value="${ligne.jury.notesSoumises}"/>/<c:out value="${ligne.jury.taille}"/> notes</span></td>
                            <td><c:choose><c:when test="${empty ligne.convocation.note}">—</c:when><c:otherwise><c:out value="${fmt2:note(ligne.convocation.note)}"/>/20</c:otherwise></c:choose></td>
                            <td><c:choose><c:when test="${empty ligne.jury.moyenne}">—</c:when><c:otherwise><c:out value="${ligne.jury.moyenne}"/>/20</c:otherwise></c:choose></td>
                            <td class="actions">
                                <a class="row-action" href="${pageContext.request.contextPath}/encadreur/mes-jurys?vue=detail&amp;id=${ligne.convocation.soutenance.id}">Détails</a>
                                <a class="row-action row-action-edit" href="${pageContext.request.contextPath}/encadreur/mes-jurys?vue=note&amp;id=${ligne.convocation.soutenance.id}"><c:choose><c:when test="${empty ligne.convocation.note}">Saisir ma note</c:when><c:otherwise>Voir ma note</c:otherwise></c:choose></a>
                            </td>
                        </tr>
                    </c:forEach></tbody>
                </table></div>
            </c:otherwise></c:choose>
        </section>
    </main>
</div>
</body>
</html>
