<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Détail du jury | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261021-shell-light">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/encadreur.css?v=20261021-shell-light">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar-toggle.css?v=20261027-3">
</head>
<body>
<div class="admin-shell">
    <%@ include file="encadreur-layout.jspf" %>
    <main class="admin-main">
        <%@ include file="encadreur-topbar.jspf" %>
        <header class="page-heading">
            <div class="page-heading-copy">
                <span class="page-eyebrow">Menu Encadreur / Liste des Jurys / Détail</span>
                <div>
                    <h1>Détail de la soutenance</h1>
                    <p>Consulte les informations du mémoire, de la planification et les évaluations du jury.</p>
                </div>
            </div>
            <div class="actions">
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/encadreur/mes-jurys">Retour à la liste</a>
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/encadreur/mes-jurys?vue=note&amp;id=${soutenance.id}">
                    <c:choose><c:when test="${empty maConvocation.note}">Saisir ma note</c:when><c:otherwise>Voir ma note</c:otherwise></c:choose>
                </a>
            </div>
        </header>

        <section class="card">
            <div class="card-body">
                <div class="form-history-heading">
                    <div><span class="page-eyebrow">Soutenance</span><h2><c:out value="${soutenance.memoire.titre}"/></h2></div>
                    <span class="status-badge status-${soutenance.statut}"><c:out value="${soutenance.statut}"/></span>
                </div>
                <dl class="info-list">
                    <div><dt>Date et heure</dt><dd><c:out value="${dateAffichee}"/></dd></div>
                    <div><dt>Salle</dt><dd><c:out value="${soutenance.salle.nomSalle}"/></dd></div>
                    <div><dt>Capacité</dt><dd><c:out value="${soutenance.salle.capacite}"/> places</dd></div>
                    <div><dt>Département de la salle</dt><dd><c:out value="${soutenance.salle.departement.nom}"/></dd></div>
                </dl>
            </div>
        </section>

        <section class="card">
            <div class="card-body">
                <div class="form-history-heading">
                    <div><span class="page-eyebrow">Mémoire</span><h2>Informations du mémoire</h2></div>
                </div>
                <dl class="info-list">
                    <div><dt>Titre</dt><dd><c:out value="${soutenance.memoire.titre}"/></dd></div>
                    <div><dt>Description</dt><dd><c:choose><c:when test="${not empty soutenance.memoire.description}"><c:out value="${soutenance.memoire.description}"/></c:when><c:otherwise>Aucune description renseignée.</c:otherwise></c:choose></dd></div>
                    <div><dt>Date de dépôt</dt><dd><c:out value="${soutenance.memoire.dateDepot}" default="Non déposé"/></dd></div>
                    <div><dt>Encadreur principal</dt><dd><c:out value="${soutenance.memoire.encadreur.prenom}"/> <c:out value="${soutenance.memoire.encadreur.nom}"/></dd></div>
                    <div><dt>Grade</dt><dd><c:out value="${soutenance.memoire.encadreur.grade.libelle}"/></dd></div>
                    <div><dt>Département</dt><dd><c:out value="${soutenance.memoire.encadreur.departement.nom}"/></dd></div>
                </dl>
            </div>
        </section>

        <section class="card">
            <div class="card-body">
                <div class="form-history-heading">
                    <div><span class="page-eyebrow">Étudiant</span><h2>Étudiant associé</h2></div>
                </div>
                <c:choose>
                    <c:when test="${empty soutenance.memoire.etudiant}">
                        <p class="form-history-empty">Aucun étudiant n’est encore rattaché à ce mémoire.</p>
                    </c:when>
                    <c:otherwise>
                        <dl class="info-list">
                            <div><dt>Nom</dt><dd><c:out value="${soutenance.memoire.etudiant.prenom}"/> <c:out value="${soutenance.memoire.etudiant.nom}"/></dd></div>
                            <div><dt>Matricule</dt><dd><c:out value="${soutenance.memoire.etudiant.matricule}"/></dd></div>
                            <div><dt>E-mail</dt><dd><c:out value="${soutenance.memoire.etudiant.email}"/></dd></div>
                            <div><dt>Filière</dt><dd><c:out value="${soutenance.memoire.etudiant.filiere.code}"/> — <c:out value="${soutenance.memoire.etudiant.filiere.libelle}"/></dd></div>
                            <div><dt>Département</dt><dd><c:out value="${soutenance.memoire.etudiant.filiere.departement.nom}"/></dd></div>
                        </dl>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

        <section class="card">
            <div class="card-body">
                <div class="form-history-heading">
                    <div><span class="page-eyebrow">Jury &amp; évaluations</span><h2>Composition du jury</h2></div>
                    <span class="history-total"><c:out value="${juryResume.taille}"/> membre<c:if test="${juryResume.taille ne 1}">s</c:if> · <c:out value="${juryResume.notesSoumises}"/> note<c:if test="${juryResume.notesSoumises ne 1}">s</c:if> soumise<c:if test="${juryResume.notesSoumises ne 1}">s</c:if></span>
                </div>
                <c:choose>
                    <c:when test="${empty jury}">
                        <p class="form-history-empty">Le jury n’a pas encore été composé.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="table-wrap">
                            <table>
                                <thead><tr><th>Membre</th><th>Grade</th><th>Note</th><th>Avis</th></tr></thead>
                                <tbody>
                                    <c:forEach var="membre" items="${jury}">
                                        <tr>
                                            <td class="primary-cell">
                                                <c:out value="${membre.encadreur.prenom}"/> <c:out value="${membre.encadreur.nom}"/>
                                                <c:if test="${membre.encadreur.id eq encadreur.id}"><span class="history-total">(moi)</span></c:if>
                                            </td>
                                            <td><c:out value="${membre.encadreur.grade.libelle}" default="—"/></td>
                                            <td><c:choose><c:when test="${not empty membre.note}"><c:out value="${membre.note}"/> / 20</c:when><c:otherwise>En attente</c:otherwise></c:choose></td>
                                            <td><c:out value="${membre.avis}" default="—"/></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
                <dl class="info-list">
                    <div><dt>Ma note</dt><dd><c:choose><c:when test="${not empty maConvocation.note}"><c:out value="${maConvocation.note}"/> / 20</c:when><c:otherwise>En attente</c:otherwise></c:choose></dd></div>
                    <div><dt>Moyenne des notes soumises</dt><dd><c:choose><c:when test="${not empty moyenne}"><c:out value="${moyenne}"/> / 20</c:when><c:otherwise>En attente des évaluations</c:otherwise></c:choose></dd></div>
                </dl>
            </div>
        </section>
    </main>
</div>
</body>
</html>
