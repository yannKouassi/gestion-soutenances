<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Créer l’administrateur initial | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css">
</head>
<body class="auth-page">
<main class="auth-card">
    <a class="auth-brand" href="${pageContext.request.contextPath}/">PGS-PFE</a>
    <h1>Premier compte administrateur</h1>
    <p class="auth-intro">Cette étape est disponible uniquement tant qu’aucun compte administrateur n’existe.</p>
    <c:if test="${not empty erreur}">
        <div class="alert alert-error" role="alert"><c:out value="${erreur}"/></div>
    </c:if>
    <form class="form-grid" method="post" action="${pageContext.request.contextPath}/setup-admin">
        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
        <div class="field">
            <label for="login">Login administrateur</label>
            <input id="login" name="login" autocomplete="username" minlength="3" maxlength="100"
                   pattern="[A-Za-z0-9._-]{3,100}" required value="<c:out value='${param.login}'/>">
            <small>3 à 100 caractères : lettres, chiffres, point, tiret ou souligné.</small>
        </div>
        <div class="field">
            <label for="password">Mot de passe</label>
            <input id="password" name="password" type="password" autocomplete="new-password"
                   minlength="8" maxlength="128" required>
            <small>Au moins 8 caractères. Choisis un mot de passe unique.</small>
        </div>
        <div class="field">
            <label for="confirmation">Confirmer le mot de passe</label>
            <input id="confirmation" name="confirmation" type="password" autocomplete="new-password"
                   minlength="8" maxlength="128" required>
        </div>
        <button class="btn btn-primary auth-submit" type="submit">Créer le compte administrateur</button>
    </form>
    <p class="auth-setup-link"><a href="${pageContext.request.contextPath}/login">Retour à la connexion</a></p>
</main>
</body>
</html>
