<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Ajout / Édition Étudiant | PGS-PFE</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261020-cartoon-ui">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261020-cartoon-ui">
    </head>
<body><div class="admin-shell">
    <%@ include file="admin-layout.jspf" %>
    <main class="admin-main admin-form-page"><%@ include file="admin-topbar.jspf" %>
        <header class="page-heading"><div class="page-heading-copy"><span class="page-eyebrow">Acteurs &amp; Comptes / Étudiants / Formulaire</span><div><h1>Ajout / Édition Étudiant</h1><p>Inscris l’étudiant dans une filière et associe éventuellement son mémoire.</p></div></div></header>
        <c:if test="${not empty message}"><div class="alert alert-success" role="status"><c:out value="${message}"/></div></c:if>
        <c:if test="${not empty erreur}"><div class="alert alert-error" role="alert"><c:out value="${erreur}"/></div></c:if>
        <section class="card"><div class="card-body"><div class="form-card-heading"><c:choose><c:when test="${empty element.id}"><span class="form-mode">Création</span></c:when><c:otherwise><span class="form-mode">Modification</span></c:otherwise></c:choose><h2>Informations de l’étudiant</h2><p>À la création, son identifiant et son mot de passe temporaire seront envoyés par e-mail.</p></div>
            <form class="form-grid" method="post" action="${pageContext.request.contextPath}/admin/etudiants/enregistrer">
                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>"><input type="hidden" name="id" value="<c:out value='${element.id}'/>">
                <div class="field"><label for="matricule">Matricule <span class="required-mark">*</span></label><input id="matricule" name="matricule" maxlength="40" required value="<c:out value='${not empty param.matricule ? param.matricule : element.matricule}'/>"></div>
                <div class="field"><label for="nom">Nom <span class="required-mark">*</span></label><input id="nom" name="nom" maxlength="100" required value="<c:out value='${not empty param.nom ? param.nom : element.nom}'/>"></div>
                <div class="field"><label for="prenom">Prénom <span class="required-mark">*</span></label><input id="prenom" name="prenom" maxlength="100" required value="<c:out value='${not empty param.prenom ? param.prenom : element.prenom}'/>"></div>
                <div class="field"><label for="email">Adresse e-mail <span class="required-mark">*</span></label><input id="email" name="email" type="email" maxlength="180" required autocomplete="email" value="<c:out value='${not empty param.email ? param.email : element.email}'/>"><small class="field-hint">Les identifiants de connexion seront envoyés à cette adresse.</small></div>
                <div class="field"><label for="filiereId">Filière <span class="required-mark">*</span></label><select id="filiereId" name="filiereId" required><option value="">Choisir une filière</option><c:forEach var="filiere" items="${filieres}"><option value="<c:out value='${filiere.id}'/>" ${filiere.id eq selectedFiliereId ? 'selected' : ''}><c:out value="${filiere.libelle}"/> — <c:out value="${filiere.departement.nom}"/></option></c:forEach></select></div>
                <div class="field"><label for="memoireId">Mémoire</label><select id="memoireId" name="memoireId"><option value="">Aucun mémoire associé</option><c:forEach var="memoire" items="${memoires}"><option value="<c:out value='${memoire.id}'/>" ${memoire.id eq selectedMemoireId ? 'selected' : ''}><c:out value="${memoire.titre}"/></option></c:forEach></select><small class="field-hint">Tu pourras associer un mémoire ultérieurement.</small></div>
                <div class="form-actions"><button class="btn btn-primary" type="submit">Enregistrer</button><a class="btn btn-secondary" href="${pageContext.request.contextPath}/admin/etudiants">Annuler</a></div>
            </form>
        </div></section><%@ include file="admin-history.jspf" %>
    </main>
</div></body></html>
