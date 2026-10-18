
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
     <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/etudiant.css">
    <title>Espace Étudiant</title>
</head>

<body>

    <!-- En-tête de l'espace étudiant -->
    <header>
        <h1>Gestion des soutenances</h1>
        <p>Bienvenue dans votre espace étudiant</p>
    </header>

    <!-- Menu de navigation -->
    <nav>
        <ul>
            <li>
                <a href="ma-soutenance.jsp">Ma soutenance</a>
            </li>

            <li>
                <a href="profil.jsp">Mon profil</a>
            </li>
        </ul>
    </nav>

    <!-- Contenu principal -->
    <main>

        <section>
            <h2>Accueil étudiant</h2>
            <p>
                Bienvenue dans votre espace personnel.
                Vous pouvez consulter les informations de votre
                soutenance et vos données personnelles.
            </p>
        </section>

        <section>
            <h2>Ma soutenance</h2>
            <p>
                Consultez la date, l'heure, la salle,
                le sujet du mémoire et la composition du jury.
            </p>
            <a href="ma-soutenance.jsp">Consulter ma soutenance</a>
        </section>

        <section>
            <h2>Mon profil</h2>
            <p>
                Consultez votre matricule, votre filière
                et vos informations personnelles.
            </p>
            <a href="profil.jsp">Consulter mon profil</a>
        </section>

    </main>

    <!-- Pied de page -->
    <footer>
        <p>Application de gestion des soutenances</p>
    </footer>

</body>
</html>
