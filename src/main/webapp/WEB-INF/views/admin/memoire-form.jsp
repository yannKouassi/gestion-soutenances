<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Ajout / Édition Mémoire | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261020-cartoon-ui">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261020-cartoon-ui">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/tailwind.css?v=20261020-cartoon-ui">
</head>
<body>
<div class="admin-shell">
    <%@ include file="admin-layout.jspf" %>
    <main class="admin-main admin-form-page">
        <%@ include file="admin-topbar.jspf" %>
        <header class="page-heading"><div class="page-heading-copy">
            <span class="page-eyebrow">Académique &amp; Soutenances / Mémoires / Formulaire</span>
            <div><h1>Ajout / Édition Mémoire</h1>
            <p>Renseigne le titre du mémoire, son encadreur et sa date de dépôt.</p></div>
        </div></header>
        <c:if test="${not empty message}"><div class="alert alert-success" role="status"><c:out value="${message}"/></div></c:if>
        <c:if test="${not empty erreur}"><div class="alert alert-error" role="alert"><c:out value="${erreur}"/></div></c:if>
        <section class="card"><div class="card-body">
            <div class="form-card-heading">
                <c:choose><c:when test="${empty element.id}"><span class="form-mode">Création</span></c:when><c:otherwise><span class="form-mode">Modification</span></c:otherwise></c:choose>
                <h2>Informations du mémoire</h2>
                <p>Un mémoire est rattaché à un seul encadreur et peut recevoir sa soutenance ultérieurement.</p>
            </div>
            <form class="form-grid" method="post" action="${pageContext.request.contextPath}/admin/memoires/enregistrer">
                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                <input type="hidden" name="id" value="<c:out value='${element.id}'/>">
                <div class="field"><label for="titre">Titre <span class="required-mark" aria-hidden="true">*</span></label><input id="titre" name="titre" maxlength="200" required autocomplete="off" placeholder="Ex. Plateforme de gestion des soutenances" value="<c:out value='${not empty param.titre ? param.titre : element.titre}'/>"><small class="field-hint">Titre du projet de fin d’études, 200 caractères maximum.</small></div>
                <div class="field"><label for="encadreurId">Encadreur <span class="required-mark" aria-hidden="true">*</span></label><select id="encadreurId" name="encadreurId" required><option value="">Choisir un encadreur</option><c:forEach var="encadreur" items="${encadreurs}"><option value="<c:out value='${encadreur.id}'/>" ${encadreur.id eq selectedEncadreurId ? 'selected' : ''}><c:out value="${encadreur.prenom}"/> <c:out value="${encadreur.nom}"/> — <c:out value="${encadreur.grade.libelle}"/></option></c:forEach></select></div>
                <div class="field"><label for="dateDepot">Date de dépôt</label><input id="dateDepot" name="dateDepot" type="date" value="<c:out value='${not empty param.dateDepot ? param.dateDepot : element.dateDepot}'/>"><small class="field-hint">Laisse vide si le mémoire n’est pas encore déposé.</small></div>
                <div class="field field-full"><label for="description">Description</label><textarea id="description" name="description" rows="4" placeholder="Résumé du sujet, technologies utilisées…"><c:out value="${not empty param.description ? param.description : element.description}"/></textarea></div>
                <div class="form-actions">
                    <button class="btn btn-primary" type="submit">Enregistrer</button>
                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/admin/memoires">Annuler</a>
                </div>
            </form>
        </div></section>
        <%@ include file="admin-history.jspf" %>
    </main>
</div>
</body>
</html>
