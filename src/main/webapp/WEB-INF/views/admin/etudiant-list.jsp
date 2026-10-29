<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Liste des Étudiants | PGS-PFE</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261021-shell-light">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261021-shell-light">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar-toggle.css?v=20261027-3">
</head>
<body><div class="admin-shell">
    <%@ include file="admin-layout.jspf" %>
    <main class="admin-main"><%@ include file="admin-topbar.jspf" %>
        <header class="page-heading"><div class="page-heading-copy"><span class="page-eyebrow">Acteurs &amp; Comptes / Étudiants</span><div><h1>Liste des Étudiants</h1><p>Recherche par nom ou matricule et consulte les inscriptions par filière.</p></div></div>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/etudiants?vue=form"><span class="button-icon" aria-hidden="true">+</span> Ajouter un étudiant</a>
        </header>
        <c:if test="${not empty message}"><div class="alert alert-success" role="status"><c:out value="${message}"/></div></c:if><c:if test="${not empty erreur}"><div class="alert alert-error" role="alert"><c:out value="${erreur}"/></div></c:if>
        <section class="card" aria-label="Liste des étudiants"><div class="list-toolbar"><label class="search-field"><span class="search-icon" aria-hidden="true"></span><span class="visually-hidden">Rechercher un étudiant</span>
            <input type="search" data-list-search placeholder="Nom, matricule, e-mail…" autocomplete="off"><button class="search-clear" type="button" data-search-clear aria-label="Effacer la recherche" hidden>&times;</button>
        </label><div class="list-filters"><label class="visually-hidden" for="filter-filiere">Filtrer par filière</label><select id="filter-filiere" class="list-filter" data-list-filter="filiere"><option value="">Toutes les filières</option><c:forEach var="filiere" items="${filieres}"><option value="<c:out value='${filiere.id}'/>"><c:out value="${filiere.libelle}"/></option></c:forEach></select></div><span class="list-count" data-list-count aria-live="polite"></span></div>
        <c:choose><c:when test="${empty elements}"><div class="empty-state"><h2>Aucun étudiant pour le moment</h2><p>Ajoute un étudiant ; ses identifiants seront envoyés par e-mail.</p><a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/etudiants?vue=form">Ajouter un étudiant</a></div></c:when>
        <c:otherwise><div class="table-wrap"><table><thead><tr><th>Matricule</th><th>Nom</th><th>E-mail</th><th>Filière</th><th>Mémoire</th><th>Compte</th><th>Action</th></tr></thead><tbody data-list-body>
            <c:forEach var="etudiant" items="${elements}"><tr data-list-row data-filter-filiere="<c:out value='${etudiant.filiere.id}'/>">
                <td data-searchable><span class="code-badge"><c:out value="${etudiant.matricule}"/></span></td><td data-searchable class="primary-cell"><c:out value="${etudiant.prenom}"/> <c:out value="${etudiant.nom}"/></td>
                <td data-searchable><c:out value="${etudiant.email}"/></td><td data-searchable><c:out value="${etudiant.filiere.libelle}"/></td>
                <td data-searchable><c:out value="${etudiant.memoire.titre}"/></td><td><span class="status-badge ${etudiant.utilisateur.actif ? 'status-active' : 'status-inactive'}"><c:out value="${etudiant.utilisateur.actif ? 'Actif' : 'Désactivé'}"/></span></td>
                <td><a class="row-action row-action-edit" href="${pageContext.request.contextPath}/admin/etudiants?vue=form&amp;id=${etudiant.id}">Modifier</a></td>
            </tr></c:forEach><tr class="search-no-results" data-search-no-results hidden><td colspan="7">Aucun étudiant ne correspond à cette recherche.</td></tr>
        </tbody></table></div></c:otherwise></c:choose></section>
    </main>
</div><script src="${pageContext.request.contextPath}/assets/js/admin-list-search.js" defer></script></body>
</html>
