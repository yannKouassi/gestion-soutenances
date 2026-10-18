
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Ma soutenance - Espace Étudiant</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/etudiant.css">
</head>

<body>

    <!-- En-tête de la page -->
    <header>
        <h1>Ma soutenance</h1>
        <p>Consultez les informations relatives à votre soutenance.</p>
    </header>

    <!-- Menu de navigation -->
    <nav>
        <a href="menu.jsp">Accueil</a>
        <a href="ma-soutenance.jsp">Ma soutenance</a>
        <a href="profil.jsp">Mon profil</a>
    </nav>

    <!-- Informations sur la soutenance -->
    <main>

        <section>
            <h2>Informations générales</h2>

            <p><strong>Sujet du mémoire :</strong> Non renseigné</p>
            <p><strong>Date :</strong> Non planifiée</p>
            <p><strong>Heure :</strong> Non planifiée</p>
            <p><strong>Salle :</strong> Non attribuée</p>
            <p><strong>Statut :</strong>
                <span class="statut-planifiee">PLANIFIÉE</span>
            </p>
        </section>

        <!-- Composition du jury -->
        <section>
            <h2>Composition du jury</h2>

            <table>
                <thead>
                    <tr>
                        <th>Nom et prénom</th>
                        <th>Rôle</th>
                    </tr>
                </thead>

                <tbody>
                    <tr>
                        <td>Non renseigné</td>
                        <td>Président du jury</td>
                    </tr>
                    <tr>
                        <td>Non renseigné</td>
                        <td>Examinateur</td>
                    </tr>
                </tbody>
            </table>
        </section>

        <!-- Résultat de la soutenance -->
        <section>
            <h2>Résultat de la soutenance</h2>

            <p><strong>Note finale :</strong> Non disponible</p>
            <p>
                Votre note finale sera affichée lorsque votre
                soutenance aura été évaluée.
            </p>
        </section>

    </main>

    <!-- Pied de page -->
    <footer>
        <p>Application de gestion des soutenances</p>
    </footer>

    <script src="${pageContext.request.contextPath}/js/etudiant.js"></script>

</body>
</html>
