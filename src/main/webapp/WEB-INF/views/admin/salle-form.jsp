<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Ajout / Édition Salle | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css">
</head>
<body>
<div class="admin-shell">
    <%@ include file="admin-layout.jspf" %>
    <main class="admin-main admin-form-page">
        <%@ include file="admin-topbar.jspf" %>
        <header class="page-heading"><div class="page-heading-copy">
            <span class="page-eyebrow">Paramètres généraux / Salles / Formulaire</span>
            <div><h1>Ajout / Édition Salle</h1>
            <p>Renseigne le nom, la capacité et le département de la salle.</p></div>
        </div></header>
        <c:if test="${not empty erreur}"><div class="alert alert-error" role="alert"><c:out value="${erreur}"/></div></c:if>
        <section class="card"><div class="card-body">
            <div class="form-card-heading">
                <c:choose><c:when test="${empty element.id}"><span class="form-mode">Création</span></c:when><c:otherwise><span class="form-mode">Modification</span></c:otherwise></c:choose>
                <h2>Informations de la salle</h2>
                <p>Indique sa capacité et son département de rattachement.</p>
            </div>
            <form class="form-grid" method="post" action="${pageContext.request.contextPath}/admin/salles/enregistrer">
                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                <input type="hidden" name="id" value="<c:out value='${element.id}'/>">
                <div class="field"><label for="nomSalle">Nom de la salle <span class="required-mark" aria-hidden="true">*</span></label><input id="nomSalle" name="nomSalle" maxlength="100" required placeholder="Ex. Amphi A" value="<c:out value='${not empty param.nomSalle ? param.nomSalle : element.nomSalle}'/>"></div>
                <div class="field"><label for="capacite">Capacité <span class="required-mark" aria-hidden="true">*</span></label><input id="capacite" name="capacite" type="number" min="1" step="1" required placeholder="Ex. 100" value="<c:out value='${not empty param.capacite ? param.capacite : element.capacite}'/>"><small class="field-hint">Nombre maximal de personnes pouvant être accueillies.</small></div>
                <div class="field"><label for="departementId">Département <span class="required-mark" aria-hidden="true">*</span></label>
                    <select id="departementId" name="departementId" required>
                        <option value="">Choisir un département</option>
                        <c:forEach var="departement" items="${departements}">
                            <option value="${departement.id}" ${selectedDepartementId eq departement.id ? 'selected' : ''}><c:out value="${departement.code} — ${departement.nom}"/></option>
                        </c:forEach>
                    </select>
                    <small class="field-hint"><c:choose><c:when test="${empty departements}">Crée d’abord un département pour pouvoir rattacher cette salle.</c:when><c:otherwise>Le département qui gère cette salle.</c:otherwise></c:choose></small>
                </div>
                <div class="form-actions">
                    <button class="btn btn-primary" type="submit" ${empty departements ? 'disabled' : ''}>Enregistrer</button>
                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/admin/salles">Annuler</a>
                </div>
            </form>
        </div></section>
        <%@ include file="admin-history.jspf" %>
    </main>
</div>
</body>
</html>
