<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Espace utilisateur | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css">
</head>
<body class="auth-page">
<main class="auth-card">
    <a class="auth-brand" href="${pageContext.request.contextPath}/">PGS-PFE</a>
    <h1>Bonjour <c:out value="${utilisateur.login}"/></h1>
    <p class="auth-intro">Tu es connecté avec le rôle <strong><c:out value="${utilisateur.role}"/></strong>.
        Cet espace sera disponible lorsque le module correspondant sera implémenté.</p>
    <form method="post" action="${pageContext.request.contextPath}/logout">
        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
        <button class="btn btn-primary auth-submit" type="submit">Se déconnecter</button>
    </form>
</main>
</body>
</html>
