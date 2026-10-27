<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Saisie de note | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261021-shell-light">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/encadreur.css?v=20261021-shell-light">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar-toggle.css?v=20261027-3">
</head>
<body>
<div class="admin-shell">
    <%@ include file="encadreur-layout.jspf" %>
    <main class="admin-main admin-form-page">
        <%@ include file="encadreur-topbar.jspf" %>
        <header class="page-heading"><div class="page-heading-copy"><span class="page-eyebrow">Espace encadreur / Mes jurys / Note</span>
            <div><h1>Saisie de ma note</h1><p>Une seule note par membre du jury. La soutenance est évaluée quand tous les membres ont noté.</p></div>
        </div></header>
        <c:if test="${not empty message}"><div class="alert alert-success" role="status"><c:out value="${message}"/></div></c:if>
        <c:if test="${not empty erreur}"><div class="alert alert-error" role="alert"><c:out value="${erreur}"/></div></c:if>
        <c:if test="${dejaNote}"><div class="alert alert-success" role="status">Tu as déjà soumis ta note pour cette soutenance. Elle est affichée ci-dessous en lecture seule.</div></c:if>
        <c:if test="${not dejaNote and not saisieAutorisee and empty erreur}"><div class="alert alert-error" role="alert"><c:out value="${raisonSaisieBloquee}"/></div></c:if>
        <section class="card"><div class="card-body">
            <div class="form-card-heading"><span class="form-mode"><c:out value="${dateAffichee}"/></span>
                <h2><c:out value="${soutenance.memoire.titre}"/></h2>
                <p>Salle <c:out value="${soutenance.salle.nomSalle}"/> — <span class="status-badge status-${soutenance.statut}"><c:out value="${soutenance.statut}"/></span></p>
            </div>
            <dl class="info-list">
                <div><dt>Mémoire</dt><dd><c:out value="${soutenance.memoire.titre}"/></dd></div>
                <div><dt>Jury</dt><dd><c:out value="${jury.taille}"/> membres — <c:out value="${jury.notesSoumises}"/> note<c:if test="${jury.notesSoumises ne 1}">s</c:if> soumise<c:if test="${jury.notesSoumises ne 1}">s</c:if></dd></div>
                <div><dt>Moyenne du jury</dt><dd><c:choose><c:when test="${empty moyenne}">En attente de toutes les notes</c:when><c:otherwise><c:out value="${moyenne}"/>/20</c:otherwise></c:choose></dd></div>
            </dl>
            <ul class="member-list"><c:forEach var="membre" items="${jury.membres}">
                <li><c:out value="${membre.nomComplet}"/><c:if test="${membre.actuel}"> (moi)</c:if> — <c:choose><c:when test="${membre.note}">noté</c:when><c:otherwise>en attente</c:otherwise></c:choose></li>
            </c:forEach></ul>
        </div></section>
        <section class="card"><div class="card-body">
            <div class="form-card-heading"><c:choose><c:when test="${dejaNote}"><span class="form-mode">Note soumise</span></c:when><c:otherwise><span class="form-mode">Saisie</span></c:otherwise></c:choose>
                <h2>Ma note</h2><p>Note entre 0 et 20, arrondie à deux décimales à l’affichage.</p>
            </div>
            <form class="form-grid" method="post" action="${pageContext.request.contextPath}/encadreur/mes-jurys/noter">
                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>"><input type="hidden" name="soutenanceId" value="<c:out value='${soutenance.id}'/>">
                <div class="float-field">
                    <div class="float-field-box">
                        <input class="float-field-input" id="note" name="note" type="number" min="0" max="20" step="0.01" required placeholder=" " ${dejaNote or not saisieAutorisee ? 'disabled' : ''} value="<c:out value='${maConvocation.note}'/>">
                        <label class="float-field-label" for="note">Note sur 20 <span class="required-mark">*</span></label>
                    </div>
                    <small class="float-field-hint">Utilise le point comme séparateur décimal (ex. 14.50).</small>
                </div>
                <div class="float-field">
                    <div class="float-field-box">
                        <textarea class="float-field-textarea" id="avis" name="avis" rows="4" placeholder=" " ${dejaNote or not saisieAutorisee ? 'disabled' : ''}><c:out value="${maConvocation.avis}"/></textarea>
                        <label class="float-field-label" for="avis">Avis</label>
                    </div>
                </div>
                <div class="form-actions"><c:if test="${not dejaNote and saisieAutorisee}"><button class="btn btn-primary" type="submit">Enregistrer ma note</button></c:if><a class="btn btn-secondary" href="${pageContext.request.contextPath}/encadreur/mes-jurys">Retour à mes jurys</a></div>
            </form>
        </div></section>
    </main>
</div>
</body>
</html>
