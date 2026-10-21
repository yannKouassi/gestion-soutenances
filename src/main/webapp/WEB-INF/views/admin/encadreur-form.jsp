<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Ajout / Édition Encadreur | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261021-shell-light">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261021-shell-light">
</head>
<body>
<div class="admin-shell">
    <%@ include file="admin-layout.jspf" %>
    <main class="admin-main admin-form-page">
        <%@ include file="admin-topbar.jspf" %>
        <header class="page-heading"><div class="page-heading-copy"><span class="page-eyebrow">Acteurs &amp; Comptes / Encadreurs / Formulaire</span>
            <div><h1>Ajout / Édition Encadreur</h1><p>Renseigne les informations professionnelles de l’encadreur.</p></div>
        </div></header>
        <c:if test="${not empty message}"><div class="alert alert-success" role="status"><c:out value="${message}"/></div></c:if>
        <c:if test="${not empty erreur}"><div class="alert alert-error" role="alert"><c:out value="${erreur}"/></div></c:if>
        <section class="card"><div class="card-body">
            <div class="form-card-heading"><c:choose><c:when test="${empty element.id}"><span class="form-mode">Création</span></c:when><c:otherwise><span class="form-mode">Modification</span></c:otherwise></c:choose>
                <h2>Informations de l’encadreur</h2><p>Un compte actif sera créé et ses identifiants temporaires envoyés par e-mail.</p>
            </div>
            <form class="form-grid" method="post" action="${pageContext.request.contextPath}/admin/encadreurs/enregistrer">
                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>"><input type="hidden" name="id" value="<c:out value='${element.id}'/>">
                <div class="field"><label for="nom">Nom <span class="required-mark">*</span></label><input id="nom" name="nom" maxlength="100" required value="<c:out value='${not empty param.nom ? param.nom : element.nom}'/>"></div>
                <div class="field"><label for="prenom">Prénom <span class="required-mark">*</span></label><input id="prenom" name="prenom" maxlength="100" required value="<c:out value='${not empty param.prenom ? param.prenom : element.prenom}'/>"></div>
                <div class="field"><label for="email">Adresse e-mail <span class="required-mark">*</span></label><input id="email" name="email" type="email" maxlength="180" required autocomplete="email" value="<c:out value='${not empty param.email ? param.email : element.email}'/>"><small class="field-hint">Le login et le mot de passe temporaire seront envoyés à cette adresse.</small></div>
                <div class="field"><label for="gradeId">Grade <span class="required-mark">*</span></label><select id="gradeId" name="gradeId" required><option value="">Choisir un grade</option><c:forEach var="grade" items="${grades}"><option value="<c:out value='${grade.id}'/>" ${grade.id eq selectedGradeId ? 'selected' : ''}><c:out value="${grade.libelle}"/></option></c:forEach></select></div>
                <div class="field"><label for="departementId">Département <span class="required-mark">*</span></label><select id="departementId" name="departementId" required><option value="">Choisir un département</option><c:forEach var="departement" items="${departements}"><option value="<c:out value='${departement.id}'/>" ${departement.id eq selectedDepartementId ? 'selected' : ''}><c:out value="${departement.nom}"/></option></c:forEach></select></div>
                <div class="form-actions"><button class="btn btn-primary" type="submit">Enregistrer</button><a class="btn btn-secondary" href="${pageContext.request.contextPath}/admin/encadreurs">Annuler</a></div>
            </form>
        </div></section>
        <%@ include file="admin-history.jspf" %>
    </main>
</div>
</body>
</html>
