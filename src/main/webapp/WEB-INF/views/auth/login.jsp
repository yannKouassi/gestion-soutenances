<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Connexion | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261020-cartoon-ui">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css?v=20261020-cartoon-ui">
</head>
<body class="auth-page">
<main class="auth-card">
    <img class="auth-illustration"
         src="${pageContext.request.contextPath}/assets/img/learning-to-sketch.png"
         alt="" aria-hidden="true">
    <a class="auth-brand" href="${pageContext.request.contextPath}/">PGS-PFE</a>
    <h1>Connexion</h1>
    <p class="auth-intro">Connecte-toi pour accéder à ton espace.</p>
    <c:if test="${param.message eq 'admin-created'}">
        <div class="ui-alert ui-alert--success" role="status">Le compte administrateur est créé. Tu peux te connecter.</div>
    </c:if>
    <c:if test="${not empty erreur}">
        <div class="ui-alert ui-alert--error" role="alert"><c:out value="${erreur}"/></div>
    </c:if>
    <form class="ui-form-grid" method="post" action="${pageContext.request.contextPath}/login">
        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
        <div class="ui-field">
            <label class="ui-label" for="login">Login</label>
            <input class="ui-input" id="login" name="login" autocomplete="username" maxlength="100" required
                   value="<c:out value='${param.login}'/>">
        </div>
        <div class="ui-field">
            <label class="ui-label" for="password">Mot de passe</label>
            <input class="ui-input" id="password" name="password" type="password" autocomplete="current-password"
                   maxlength="128" required>
        </div>
        <button class="ui-btn ui-btn--primary ui-btn--block" type="submit">Se connecter</button>
    </form>
    <c:if test="${firstAdminSetupRequired}">
        <p class="auth-setup-link">
            Première utilisation ?
            <a href="${pageContext.request.contextPath}/setup-admin">Créer le premier compte administrateur</a>
        </p>
    </c:if>
</main>
</body>
</html>
