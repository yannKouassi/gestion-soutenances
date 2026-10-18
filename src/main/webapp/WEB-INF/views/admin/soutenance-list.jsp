<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Liste des Soutenances | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261015-admin-menu">
</head>
<body>
<div class="admin-shell">
    <%@ include file="admin-layout.jspf" %>
    <main class="admin-main">
        <%@ include file="admin-topbar.jspf" %>
        <header class="page-heading">
            <div class="page-heading-copy">
                <span class="page-eyebrow">Académique &amp; Soutenances / Soutenances</span>
                <div><h1>Liste des Soutenances</h1><p>Filtre les soutenances par statut, date, étudiant ou filière, puis consulte la composition du jury et la note finale.</p></div>
            </div>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/planification"><span class="button-icon" aria-hidden="true">+</span> Planifier une soutenance</a>
        </header>
        <c:if test="${not empty message}"><div class="alert alert-success" role="status"><c:out value="${message}"/></div></c:if>
        <c:if test="${not empty erreur}"><div class="alert alert-error" role="alert"><c:out value="${erreur}"/></div></c:if>
        <section class="card filters-card" aria-label="Filtres des soutenances">
            <div class="card-body">
                <form method="get" action="${pageContext.request.contextPath}/admin/soutenances">
                    <div class="filter-grid">
                        <div class="field">
                            <label for="filtre-statut">Statut</label>
                            <select id="filtre-statut" name="statut">
                                <option value="">Tous les statuts</option>
                                <c:forEach var="statut" items="${statuts}">
                                    <option value="<c:out value='${statut}'/>" ${statut eq filtreStatut ? 'selected' : ''}><c:out value="${statut}"/></option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="field">
                            <label for="filtre-date">Date</label>
                            <input id="filtre-date" type="date" name="date" value="<c:out value='${filtreDate}'/>">
                        </div>
                        <div class="field">
                            <label for="filtre-etudiant">Étudiant</label>
                            <select id="filtre-etudiant" name="etudiant">
                                <option value="">Tous les étudiants</option>
                                <c:forEach var="etudiant" items="${etudiants}">
                                    <option value="<c:out value='${etudiant.id}'/>" ${etudiant.id eq filtreEtudiant ? 'selected' : ''}><c:out value="${etudiant.prenom}"/> <c:out value="${etudiant.nom}"/> — <c:out value="${etudiant.matricule}"/></option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="field">
                            <label for="filtre-filiere">Filière</label>
                            <select id="filtre-filiere" name="filiere">
                                <option value="">Toutes les filières</option>
                                <c:forEach var="filiere" items="${filieres}">
                                    <option value="<c:out value='${filiere.id}'/>" ${filiere.id eq filtreFiliere ? 'selected' : ''}><c:out value="${filiere.code}"/> — <c:out value="${filiere.libelle}"/></option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                    <div class="filter-actions">
                        <button class="btn btn-primary" type="submit">Filtrer</button>
                        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/admin/soutenances">Réinitialiser</a>
                    </div>
                </form>
            </div>
        </section>
        <section class="card" aria-label="Liste des soutenances">
            <c:choose><c:when test="${empty entries}">
                <div class="empty-state">
                    <span class="empty-state-icon" aria-hidden="true"><svg viewBox="0 0 24 24" focusable="false"><path d="M4 5h16v15H4z"/><path d="M8 3v4m8-4v4M4 10h16"/><path d="m9 15 2 2 4-4"/></svg></span>
                    <h2>Aucune soutenance à afficher</h2>
                    <p>Aucune soutenance ne correspond aux critères sélectionnés.</p>
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/planification">Planifier une soutenance</a>
                </div>
            </c:when><c:otherwise>
                <div class="table-wrap"><table class="journal-table">
                    <thead><tr><th>Mémoire</th><th>Étudiant</th><th>Filière</th><th>Salle</th><th>Date</th><th>Statut</th><th>Jury</th><th>Note finale</th></tr></thead>
                    <tbody><c:forEach var="entry" items="${entries}">
                        <tr>
                            <td class="primary-cell"><c:out value="${entry.soutenance.memoire.titre}"/></td>
                            <td><c:out value="${entry.etudiant.prenom}"/> <c:out value="${entry.etudiant.nom}"/></td>
                            <td><span class="code-badge"><c:out value="${entry.etudiant.filiere.code}"/></span> <c:out value="${entry.etudiant.filiere.libelle}"/></td>
                            <td><c:out value="${entry.soutenance.salle.nomSalle}"/></td>
                            <td class="history-date"><c:out value="${entry.dateAffichee}" default="—"/></td>
                            <td><span class="status-badge status-${entry.soutenance.statut}"><c:out value="${entry.soutenance.statut}"/></span></td>
                            <td><span class="capacity-badge"><c:out value="${entry.tailleJury}"/> membre<c:if test="${entry.tailleJury ne 1}">s</c:if></span></td>
                            <td><c:choose><c:when test="${empty entry.noteAffichee}">—</c:when><c:otherwise><span class="note-badge"><c:out value="${entry.noteAffichee}"/> / 20</span></c:otherwise></c:choose></td>
                        </tr>
                    </c:forEach></tbody>
                </table></div>
            </c:otherwise></c:choose>
        </section>
    </main>
</div>
</body>
</html>
