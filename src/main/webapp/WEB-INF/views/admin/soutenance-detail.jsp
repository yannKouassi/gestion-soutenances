<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Détail de la soutenance | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261021-shell-light">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261021-shell-light">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar-toggle.css?v=20261027-3">
</head>
<body>
<div class="admin-shell">
    <%@ include file="admin-layout.jspf" %>
    <main class="admin-main">
        <%@ include file="admin-topbar.jspf" %>
        <header class="page-heading">
            <div class="page-heading-copy">
                <span class="page-eyebrow">Académique &amp; Soutenances / Gestion Globale / Détail</span>
                <div>
                    <h1>Détail de la soutenance</h1>
                    <p>Consulte toutes les informations du mémoire, de l’étudiant, de la planification et du jury.</p>
                </div>
            </div>
            <div class="actions">
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/admin/soutenances">Retour à la liste</a>
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/admin/planification?id=${vueSoutenance.soutenance.id}">Modifier la planification</a>
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/jury?soutenance=${vueSoutenance.soutenance.id}">Gérer le jury</a>
                <c:if test="${vueSoutenance.soutenance.statut eq 'PLANIFIEE'}">
                    <form method="post" action="${pageContext.request.contextPath}/admin/soutenances/annuler" onsubmit="return confirm('Annuler cette soutenance ? Aucune note ne pourra plus être saisie.');">
                        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                        <input type="hidden" name="id" value="<c:out value='${vueSoutenance.soutenance.id}'/>">
                        <button class="btn btn-cancel" type="submit">Annuler la soutenance</button>
                    </form>
                </c:if>
            </div>
        </header>
        <c:if test="${not empty message}"><div class="alert alert-success" role="status"><c:out value="${message}"/></div></c:if>
        <c:if test="${not empty erreur}"><div class="alert alert-error" role="alert"><c:out value="${erreur}"/></div></c:if>

        <section class="card">
            <div class="card-body">
                <div class="form-history-heading">
                    <div>
                        <span class="page-eyebrow">Mémoire</span>
                        <h2><c:out value="${vueSoutenance.soutenance.memoire.titre}"/></h2>
                    </div>
                    <span class="status-badge status-${vueSoutenance.soutenance.statut}">
                        <c:out value="${vueSoutenance.soutenance.statut}"/>
                    </span>
                </div>
                <dl class="info-list">
                    <div><dt>Date de dépôt</dt><dd><c:out value="${vueSoutenance.soutenance.memoire.dateDepot}" default="Non renseignée"/></dd></div>
                    <div><dt>Encadreur principal</dt><dd><c:out value="${vueSoutenance.soutenance.memoire.encadreur.prenom}"/> <c:out value="${vueSoutenance.soutenance.memoire.encadreur.nom}"/></dd></div>
                    <div><dt>Grade</dt><dd><c:out value="${vueSoutenance.soutenance.memoire.encadreur.grade.libelle}"/></dd></div>
                    <div><dt>Département de l’encadreur</dt><dd><c:out value="${vueSoutenance.soutenance.memoire.encadreur.departement.nom}"/></dd></div>
                    <c:if test="${not empty vueSoutenance.soutenance.memoire.description}">
                        <div><dt>Description</dt><dd><c:out value="${vueSoutenance.soutenance.memoire.description}"/></dd></div>
                    </c:if>
                </dl>
            </div>
        </section>

        <section class="card">
            <div class="card-body">
                <div class="form-history-heading">
                    <div><span class="page-eyebrow">Étudiant</span><h2>Étudiant associé</h2></div>
                </div>
                <c:choose>
                    <c:when test="${empty vueSoutenance.etudiant}">
                        <p class="form-history-empty">Aucun étudiant n’est encore rattaché à ce mémoire.</p>
                    </c:when>
                    <c:otherwise>
                        <dl class="info-list">
                            <div><dt>Nom</dt><dd><c:out value="${vueSoutenance.etudiant.prenom}"/> <c:out value="${vueSoutenance.etudiant.nom}"/></dd></div>
                            <div><dt>Matricule</dt><dd><c:out value="${vueSoutenance.etudiant.matricule}"/></dd></div>
                            <div><dt>E-mail</dt><dd><c:out value="${vueSoutenance.etudiant.email}"/></dd></div>
                            <div><dt>Filière</dt><dd><c:out value="${vueSoutenance.etudiant.filiere.code}"/> — <c:out value="${vueSoutenance.etudiant.filiere.libelle}"/></dd></div>
                            <div><dt>Département</dt><dd><c:out value="${vueSoutenance.etudiant.filiere.departement.nom}"/></dd></div>
                        </dl>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

        <section class="card">
            <div class="card-body">
                <div class="form-history-heading">
                    <div><span class="page-eyebrow">Planification</span><h2>Date et lieu</h2></div>
                </div>
                <dl class="info-list">
                    <div><dt>Date et heure</dt><dd><c:out value="${dateAffichee}" default="Non renseignée"/></dd></div>
                    <div><dt>Salle</dt><dd><c:out value="${vueSoutenance.soutenance.salle.nomSalle}"/></dd></div>
                    <div><dt>Capacité</dt><dd><c:out value="${vueSoutenance.soutenance.salle.capacite}"/> places</dd></div>
                    <div><dt>Département de la salle</dt><dd><c:out value="${vueSoutenance.soutenance.salle.departement.nom}"/></dd></div>
                </dl>
            </div>
        </section>

        <section class="card">
            <div class="card-body">
                <div class="form-history-heading">
                    <div><span class="page-eyebrow">Jury &amp; évaluations</span><h2>Composition du jury</h2></div>
                    <span class="history-total"><c:out value="${membres.size()}"/> membre<c:if test="${membres.size() ne 1}">s</c:if></span>
                </div>
                <c:choose>
                    <c:when test="${empty membres}">
                        <p class="form-history-empty">Le jury n’a pas encore été composé.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="table-wrap">
                            <table>
                                <thead><tr><th>Membre</th><th>Grade</th><th>Note</th><th>Avis</th></tr></thead>
                                <tbody>
                                <c:forEach var="membre" items="${membres}">
                                    <tr>
                                        <td class="primary-cell"><c:out value="${membre.encadreur.prenom}"/> <c:out value="${membre.encadreur.nom}"/></td>
                                        <td><c:out value="${membre.encadreur.grade.libelle}" default="—"/></td>
                                        <td><c:choose><c:when test="${empty membre.note}">En attente</c:when><c:otherwise><c:out value="${membre.note}"/> / 20</c:otherwise></c:choose></td>
                                        <td><c:out value="${membre.avis}" default="Aucun avis renseigné"/></td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
                <div class="form-history-heading">
                    <div><span class="page-eyebrow">Résultat</span><h2>Note finale</h2></div>
                    <span class="note-badge"><c:choose><c:when test="${empty noteFinale}">En attente</c:when><c:otherwise><c:out value="${noteFinale}"/> / 20</c:otherwise></c:choose></span>
                </div>
                <p class="form-history-empty">La moyenne est disponible lorsque tous les membres du jury ont saisi leur note.</p>
            </div>
        </section>
    </main>
</div>
</body>
</html>
