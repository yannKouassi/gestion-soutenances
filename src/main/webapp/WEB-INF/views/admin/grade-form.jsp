<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Ajout / Édition Grade | PGS-PFE</title>
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
            <span class="page-eyebrow">Paramètres généraux / Grades / Formulaire</span>
            <div><h1>Ajout / Édition Grade</h1>
            <p>Renseigne le code et le libellé du grade.</p></div>
        </div></header>
        <c:if test="${not empty erreur}"><div class="alert alert-error" role="alert"><c:out value="${erreur}"/></div></c:if>
        <section class="card"><div class="card-body">
            <div class="form-card-heading">
                <c:choose><c:when test="${empty element.id}"><span class="form-mode">Création</span></c:when><c:otherwise><span class="form-mode">Modification</span></c:otherwise></c:choose>
                <h2>Informations du grade</h2>
                <p>Les champs marqués sont nécessaires pour enregistrer le référentiel.</p>
            </div>
            <form class="form-grid" method="post" action="${pageContext.request.contextPath}/admin/grades/enregistrer">
                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                <input type="hidden" name="id" value="<c:out value='${element.id}'/>">
                <div class="field"><label for="code">Code <span class="required-mark" aria-hidden="true">*</span></label><input id="code" name="code" maxlength="30" required autocomplete="off" placeholder="Ex. PROF" value="<c:out value='${not empty param.code ? param.code : element.code}'/>"><small class="field-hint">Code court et distinctif pour identifier le grade.</small></div>
                <div class="field"><label for="libelle">Libellé <span class="required-mark" aria-hidden="true">*</span></label><input id="libelle" name="libelle" maxlength="120" required placeholder="Ex. Professeur titulaire" value="<c:out value='${not empty param.libelle ? param.libelle : element.libelle}'/>"></div>
                <div class="form-actions">
                    <button class="btn btn-primary" type="submit">Enregistrer</button>
                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/admin/grades">Annuler</a>
                </div>
            </form>
        </div></section>
        <%@ include file="admin-history.jspf" %>
    </main>
</div>
</body>
</html>
