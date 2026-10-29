<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Mes étudiants encadrés | PGS-PFE</title>
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
            <div class="page-heading-copy">
                <span class="page-eyebrow">Menu Encadreur / Mes étudiants encadrés</span>
                <div><h1>Mes étudiants encadrés</h1><p>Les étudiants rattachés aux mémoires dont tu es l’encadreur principal.</p></div>
            </div>
        </header>
        <section class="card" aria-label="Liste des étudiants encadrés">
            <c:choose>
                <c:when test="${empty encadrements}">
                    <div class="card-body empty-state"><h2>Aucun étudiant encadré</h2><p>Les étudiants associés à tes mémoires apparaîtront ici.</p></div>
                </c:when>
                <c:otherwise>
                    <div class="table-wrap">
                        <table>
                            <thead>
                                <tr><th>Étudiant</th><th>Matricule</th><th>Filière</th><th>Mémoire</th><th>État d’avancement</th><th>Actions</th></tr>
                            </thead>
                            <tbody>
                                <c:forEach var="ligne" items="${encadrements}">
                                    <tr>
                                        <td class="primary-cell"><c:out value="${ligne.etudiant.prenom}"/> <c:out value="${ligne.etudiant.nom}"/></td>
                                        <td><c:out value="${ligne.etudiant.matricule}"/></td>
                                        <td><c:out value="${ligne.etudiant.filiere.libelle}"/></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${empty ligne.etudiant.memoire}">Aucun mémoire associé</c:when>
                                                <c:otherwise><c:out value="${ligne.etudiant.memoire.titre}"/></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${empty ligne.soutenance}">
                                                    <span class="status-badge">Pas de soutenance</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="status-badge status-${ligne.soutenance.statut}"><c:out value="${ligne.soutenance.statut}"/></span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <a class="row-action row-action-edit" href="${pageContext.request.contextPath}/encadreur/mes-etudiants?vue=detail&amp;id=${ligne.etudiant.id}">Détails</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </main>
</div>
</body>
</html>
