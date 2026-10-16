<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Mes étudiants encadrés | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261015-admin-menu">
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
                <c:when test="${empty etudiants}">
                    <div class="empty-state"><h2>Aucun étudiant encadré</h2><p>Les étudiants associés à tes mémoires apparaîtront ici.</p></div>
                </c:when>
                <c:otherwise>
                    <div class="table-wrap">
                        <table>
                            <thead><tr><th>Étudiant</th><th>Matricule</th><th>Filière</th><th>Mémoire</th><th>État d’avancement</th></tr></thead>
                            <tbody>
                                <c:forEach var="etudiant" items="${etudiants}">
                                    <tr>
                                        <td class="primary-cell"><c:out value="${etudiant.prenom}"/> <c:out value="${etudiant.nom}"/></td>
                                        <td><c:out value="${etudiant.matricule}"/></td>
                                        <td><c:out value="${etudiant.filiere.libelle}"/></td>
                                        <td><c:out value="${etudiant.memoire.titre}"/></td>
                                        <td><c:choose><c:when test="${empty etudiant.memoire.dateDepot}"><span class="status-badge">En cours</span></c:when><c:otherwise><span class="status-badge">Déposé le <c:out value="${etudiant.memoire.dateDepot}"/></span></c:otherwise></c:choose></td>
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
