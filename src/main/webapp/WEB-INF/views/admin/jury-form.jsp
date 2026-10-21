<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Composition du jury | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261021-shell-light">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261021-shell-light">
</head>
<body>
<div class="admin-shell">
    <%@ include file="admin-layout.jspf" %>
    <main class="admin-main admin-form-page">
        <%@ include file="admin-topbar.jspf" %>
        <header class="page-heading"><div class="page-heading-copy">
            <span class="page-eyebrow">Académique &amp; Soutenances / Soutenances / Jury</span>
            <div><h1>Composition du jury</h1>
            <p>Un jury comporte au moins deux membres ; chaque membre ne peut être ajouté qu’une fois.</p></div>
        </div></header>
        <c:if test="${not empty message}"><div class="alert alert-success" role="status"><c:out value="${message}"/></div></c:if>
        <c:if test="${not empty erreur}"><div class="alert alert-error" role="alert"><c:out value="${erreur}"/></div></c:if>
        <c:if test="${membres.size() lt 2}"><div class="alert alert-warning" role="alert">Jury incomplet : ajoute au moins un autre membre avant toute évaluation.</div></c:if>
        <section class="card"><div class="card-body">
            <div class="form-card-heading">
                <span class="form-mode"><c:out value="${dateAffichee}"/></span>
                <h2><c:out value="${soutenance.memoire.titre}"/></h2>
                <p>Salle <c:out value="${soutenance.salle.nomSalle}"/> — <span class="status-badge status-${soutenance.statut}"><c:out value="${soutenance.statut}"/></span></p>
            </div>
            <dl class="info-list">
                <div><dt>Mémoire</dt><dd><c:out value="${soutenance.memoire.titre}"/></dd></div>
                <div><dt>Encadreur du mémoire</dt><dd><c:out value="${soutenance.memoire.encadreur.prenom}"/> <c:out value="${soutenance.memoire.encadreur.nom}"/></dd></div>
                <div><dt>Étudiant</dt><dd><c:choose><c:when test="${empty etudiant}">Étudiant non rattaché à ce mémoire</c:when><c:otherwise><c:out value="${etudiant.prenom}"/> <c:out value="${etudiant.nom}"/> — <c:out value="${etudiant.matricule}"/></c:otherwise></c:choose></dd></div>
                <div><dt>Date et heure</dt><dd><c:out value="${dateAffichee}"/></dd></div>
                <div><dt>Salle</dt><dd><c:out value="${soutenance.salle.nomSalle}"/></dd></div>
            </dl>
        </div></section>
        <section class="card"><div class="card-body">
            <div class="form-card-heading">
                <span class="form-mode">Membres du jury</span>
                <h2>Jury en place</h2>
                <p>Le retrait est refusé si le jury devait descendre sous deux membres.</p>
            </div>
            <c:choose><c:when test="${empty membres}">
                <p class="form-history-empty">Aucun membre pour le moment. Ajoute au moins deux encadreurs.</p>
            </c:when><c:otherwise>
                <ul class="member-list"><c:forEach var="membre" items="${membres}">
                    <li><c:out value="${membre.encadreur.prenom}"/> <c:out value="${membre.encadreur.nom}"/> — <c:choose><c:when test="${not empty membre.note}">note <c:out value="${membre.note}"/>/20</c:when><c:otherwise>note en attente</c:otherwise></c:choose>
                        <form class="inline-form" method="post" action="${pageContext.request.contextPath}/admin/jury/retirer" onsubmit="return confirm('Retirer ce membre du jury ?');">
                            <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                            <input type="hidden" name="soutenance" value="<c:out value='${soutenance.id}'/>">
                            <input type="hidden" name="encadreurId" value="<c:out value='${membre.encadreur.id}'/>">
                            <button class="btn-danger-link" type="submit">Retirer</button>
                        </form>
                    </li>
                </c:forEach></ul>
            </c:otherwise></c:choose>
        </div></section>
        <section class="card"><div class="card-body">
            <div class="form-card-heading">
                <span class="form-mode">Ajout</span>
                <h2>Ajouter un membre</h2>
                <p>Seuls les encadreurs qui ne sont pas encore membres sont proposés.</p>
            </div>
            <c:choose><c:when test="${empty encadreurs}">
                <p class="form-history-empty">Tous les encadreurs disponibles sont déjà membres de ce jury.</p>
            </c:when><c:otherwise>
                <form class="form-grid" method="post" action="${pageContext.request.contextPath}/admin/jury/ajouter">
                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                    <input type="hidden" name="soutenance" value="<c:out value='${soutenance.id}'/>">
                    <div class="float-field field-full">
                        <div class="float-field-box">
                            <select id="encadreurId" name="encadreurId" required class="float-field-select"><option value=""></option><c:forEach var="encadreur" items="${encadreurs}"><option value="<c:out value='${encadreur.id}'/>" ${encadreur.id eq selectedEncadreurId ? 'selected' : ''}><c:out value="${encadreur.prenom}"/> <c:out value="${encadreur.nom}"/> — <c:out value="${encadreur.grade.libelle}"/></option></c:forEach></select>
                            <label class="float-field-label" for="encadreurId">Encadreur <span class="required-mark" aria-hidden="true">*</span></label>
                        </div>
                    </div>
                    <div class="form-actions">
                        <button class="btn btn-primary" type="submit">Ajouter au jury</button>
                        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/admin/planification?id=${soutenance.id}">Modifier la planification</a>
                    </div>
                </form>
            </c:otherwise></c:choose>
        </div></section>
        <%@ include file="admin-history.jspf" %>
    </main>
</div>
</body>
</html>
