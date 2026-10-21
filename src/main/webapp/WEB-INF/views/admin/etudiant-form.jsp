<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Ajout / Édition Étudiant | PGS-PFE</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261021-shell-light">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261021-shell-light">
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
                <div class="float-field">
                    <div class="float-field-box">
                        <input class="float-field-input" id="matricule" name="matricule" maxlength="40" required placeholder=" " value="<c:out value='${not empty param.matricule ? param.matricule : element.matricule}'/>">
                        <label class="float-field-label" for="matricule">Matricule <span class="required-mark">*</span></label>
                    </div>
                </div>
                <div class="float-field">
                    <div class="float-field-box">
                        <input class="float-field-input" id="nom" name="nom" maxlength="100" required placeholder=" " value="<c:out value='${not empty param.nom ? param.nom : element.nom}'/>">
                        <label class="float-field-label" for="nom">Nom <span class="required-mark">*</span></label>
                    </div>
                </div>
                <div class="float-field">
                    <div class="float-field-box">
                        <input class="float-field-input" id="prenom" name="prenom" maxlength="100" required placeholder=" " value="<c:out value='${not empty param.prenom ? param.prenom : element.prenom}'/>">
                        <label class="float-field-label" for="prenom">Prénom <span class="required-mark">*</span></label>
                    </div>
                </div>
                <div class="float-field">
                    <div class="float-field-box">
                        <input class="float-field-input" id="email" name="email" type="email" maxlength="180" required autocomplete="email" placeholder=" " value="<c:out value='${not empty param.email ? param.email : element.email}'/>">
                        <label class="float-field-label" for="email">Adresse e-mail <span class="required-mark">*</span></label>
                    </div>
                    <small class="float-field-hint">Les identifiants de connexion seront envoyés à cette adresse.</small>
                </div>
                <div class="float-field">
                    <div class="float-field-box">
                        <select id="filiereId" name="filiereId" required class="float-field-select"><option value=""></option><c:forEach var="filiere" items="${filieres}"><option value="<c:out value='${filiere.id}'/>" ${filiere.id eq selectedFiliereId ? 'selected' : ''}><c:out value="${filiere.libelle}"/> — <c:out value="${filiere.departement.nom}"/></option></c:forEach></select>
                        <label class="float-field-label" for="filiereId">Filière <span class="required-mark">*</span></label>
                    </div>
                </div>
                <div class="float-field">
                    <div class="float-field-box">
                        <select id="memoireId" name="memoireId" class="float-field-select"><option value=""></option><c:forEach var="memoire" items="${memoires}"><option value="<c:out value='${memoire.id}'/>" ${memoire.id eq selectedMemoireId ? 'selected' : ''}><c:out value="${memoire.titre}"/></option></c:forEach></select>
                        <label class="float-field-label" for="memoireId">Mémoire</label>
                    </div>
                    <small class="float-field-hint">Tu pourras associer un mémoire ultérieurement.</small>
                </div>
                <div class="form-actions"><button class="btn btn-primary" type="submit">Enregistrer</button><a class="btn btn-secondary" href="${pageContext.request.contextPath}/admin/etudiants">Annuler</a></div>
            </form>
        </div></section><%@ include file="admin-history.jspf" %>
    </main>
</div></body></html>
