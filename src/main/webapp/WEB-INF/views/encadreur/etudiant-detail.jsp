<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Détail étudiant encadré | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261031">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/encadreur.css?v=20261031">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar-toggle.css?v=20261031">
</head>
<body>
<div class="admin-shell">
    <%@ include file="encadreur-layout.jspf" %>
    <main id="main-content" class="admin-main">
        <%@ include file="encadreur-topbar.jspf" %>
        <header class="page-heading">
            <div class="page-heading-copy">
                <span class="page-eyebrow">Menu Encadreur / Mes étudiants encadrés / Détail</span>
                <div>
                    <h1>Dossier de l’étudiant</h1>
                    <p>Informations de l’étudiant, de son mémoire et de sa soutenance.</p>
                </div>
            </div>
            <div class="actions">
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/encadreur/mes-etudiants">Retour à la liste</a>
            </div>
        </header>

        <section class="card">
            <div class="card-body">
                <div class="form-history-heading">
                    <div><span class="page-eyebrow">Étudiant</span><h2><c:out value="${ligne.etudiant.prenom}"/> <c:out value="${ligne.etudiant.nom}"/></h2></div>
                </div>
                <dl class="info-list">
                    <div><dt>Matricule</dt><dd><c:out value="${ligne.etudiant.matricule}"/></dd></div>
                    <div><dt>E-mail</dt><dd><c:out value="${ligne.etudiant.email}"/></dd></div>
                    <div><dt>Filière</dt><dd><c:out value="${ligne.etudiant.filiere.code}"/> — <c:out value="${ligne.etudiant.filiere.libelle}"/></dd></div>
                    <div><dt>Département</dt><dd><c:out value="${ligne.etudiant.filiere.departement.nom}"/></dd></div>
                </dl>
            </div>
        </section>

        <section class="card">
            <div class="card-body">
                <div class="form-history-heading">
                    <div><span class="page-eyebrow">Mémoire</span><h2>Détails du mémoire</h2></div>
                </div>
                <c:choose>
                    <c:when test="${empty ligne.etudiant.memoire}">
                        <p class="form-history-empty">Aucun mémoire n’est encore associé à cet étudiant.</p>
                    </c:when>
                    <c:otherwise>
                        <dl class="info-list">
                            <div><dt>Titre</dt><dd><c:out value="${ligne.etudiant.memoire.titre}"/></dd></div>
                            <div><dt>Description</dt><dd><c:choose><c:when test="${not empty ligne.etudiant.memoire.description}"><c:out value="${ligne.etudiant.memoire.description}"/></c:when><c:otherwise>Aucune description renseignée.</c:otherwise></c:choose></dd></div>
                            <div><dt>Date de dépôt</dt><dd><c:out value="${ligne.etudiant.memoire.dateDepot}" default="Non déposé"/></dd></div>
                            <div><dt>Encadreur principal</dt><dd><c:out value="${ligne.etudiant.memoire.encadreur.prenom}"/> <c:out value="${ligne.etudiant.memoire.encadreur.nom}"/></dd></div>
                            <div><dt>Grade</dt><dd><c:out value="${ligne.etudiant.memoire.encadreur.grade.libelle}"/></dd></div>
                            <div><dt>Département de l’encadreur</dt><dd><c:out value="${ligne.etudiant.memoire.encadreur.departement.nom}"/></dd></div>
                        </dl>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

        <section class="card">
            <div class="card-body">
                <div class="form-history-heading">
                    <div><span class="page-eyebrow">Soutenance</span><h2>Planification et résultats</h2></div>
                    <c:if test="${not empty ligne.soutenance}">
                        <span class="status-badge status-${ligne.soutenance.statut}"><c:out value="${ligne.soutenance.statut}"/></span>
                    </c:if>
                </div>
                <c:choose>
                    <c:when test="${empty ligne.soutenance}">
                        <p class="form-history-empty">Aucune soutenance n’est planifiée pour ce mémoire.</p>
                    </c:when>
                    <c:otherwise>
                        <dl class="info-list">
                            <div><dt>Date et heure</dt><dd><c:out value="${ligne.dateSoutenanceAffichee}"/></dd></div>
                            <div><dt>Salle</dt><dd><c:out value="${ligne.soutenance.salle.nomSalle}"/></dd></div>
                            <div><dt>Capacité</dt><dd><c:out value="${ligne.soutenance.salle.capacite}"/> places</dd></div>
                            <div><dt>Département de la salle</dt><dd><c:out value="${ligne.soutenance.salle.departement.nom}"/></dd></div>
                            <div><dt>Statut</dt><dd><c:out value="${ligne.soutenance.statut}"/></dd></div>
                            <div><dt>Note finale</dt><dd><c:choose><c:when test="${not empty ligne.noteFinale}"><c:out value="${ligne.noteFinale}"/> / 20</c:when><c:otherwise>En attente des évaluations</c:otherwise></c:choose></dd></div>
                        </dl>

                        <div class="form-history-heading">
                            <div><span class="page-eyebrow">Jury &amp; évaluations</span><h2>Composition du jury</h2></div>
                            <span class="history-total"><c:out value="${ligne.membresJury.size()}"/> membre<c:if test="${ligne.membresJury.size() ne 1}">s</c:if></span>
                        </div>
                        <c:choose>
                            <c:when test="${empty ligne.membresJury}">
                                <p class="form-history-empty">Le jury n’a pas encore été composé.</p>
                            </c:when>
                            <c:otherwise>
                                <div class="table-wrap">
                                    <table>
                                        <thead><tr><th>Membre</th><th>Grade</th><th>Note</th><th>Avis</th></tr></thead>
                                        <tbody>
                                            <c:forEach var="membre" items="${ligne.membresJury}">
                                                <tr>
                                                    <td class="primary-cell"><c:out value="${membre.encadreur.prenom}"/> <c:out value="${membre.encadreur.nom}"/></td>
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
                    </c:otherwise>
                </c:choose>
            </div>
        </section>
    </main>
</div>
</body>
</html>
