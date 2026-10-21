<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Gestion des soutenances | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261020-cartoon-ui">
    <style>
        /* theme.css remet p { margin: 0 } : on rétablit l'air entre les
           paragraphes de la page d'accueil uniquement (pas de marge globale). */
        main > h1 + p { margin-top: var(--space-3); }
        main > p + p { margin-top: var(--space-3); }
    </style>
</head>
<body>
    <main>
        <h1>Gestion des soutenances</h1>
        <p>Plateforme de gestion des soutenances de projets de fin d’études.</p>
        <p><a href="${pageContext.request.contextPath}/login">Se connecter</a></p>
    </main>
</body>
</html>
