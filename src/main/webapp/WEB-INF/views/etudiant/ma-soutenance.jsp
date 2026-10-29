<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Ma soutenance | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261029">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/etudiant.css?v=20261029">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar-toggle.css?v=20261029">
</head>
<body>
<div class="admin-shell">
    <%@ include file="etudiant-layout.jspf" %>
    <main id="main-content" class="admin-main">
        <%@ include file="etudiant-topbar.jspf" %>
        <header class="page-heading">
            <div class="page-heading-copy"><span class="page-eyebrow">Espace étudiant / Ma soutenance</span>
                <div><h1>Ma soutenance</h1><p>Consulte les informations de ta soutenance, en lecture seule.</p></div>
            </div>
        </header>
        <c:choose><c:when test="${not memoirePresente}">
            <section class="card" aria-label="Mémoire">
                <div class="empty-state"><h2>Aucun mémoire rattaché</h2>
                    <p>Ton compte n’est associé à aucun mémoire pour le moment. Rapproche-toi de l’administration.</p></div>
            </section>
        </c:when><c:otherwise>
            <section class="card" aria-label="Mémoire">
                <div class="form-history-heading">
                    <div><span class="page-eyebrow">Mémoire</span><h2><c:out value="${memoire.titre}"/></h2></div>
                </div>
                <div class="card-body">
                    <dl class="info-list">
                        <div><dt>Encadreur</dt><dd><c:out value="${encadreurNomComplet}"/></dd></div>
                        <div><dt>Date de dépôt</dt><dd><c:out value="${dateDepotAffichee}"/></dd></div>
                        <c:if test="${not empty memoire.description}">
                            <div><dt>Description</dt><dd><c:out value="${memoire.description}"/></dd></div>
                        </c:if>
                    </dl>
                </div>
            </section>
            <c:choose><c:when test="${not soutenancePlanifiee}">
                <section class="card" aria-label="Planning de la soutenance">
                    <div class="empty-state"><h2>Soutenance non planifiée</h2>
                        <p>Ta soutenance n’a pas encore été planifiée par l’administration.</p></div>
                </section>
            </c:when><c:otherwise>
                <section class="card" aria-label="Informations générales">
                    <div class="form-history-heading">
                        <div><span class="page-eyebrow">Planning</span><h2>Informations générales</h2></div>
                        <span class="status-badge status-${soutenance.statut}"><c:out value="${soutenance.statut}"/></span>
                    </div>
                    <div class="card-body">
                        <dl class="info-list">
                            <div><dt>Date et heure</dt><dd><c:out value="${dateAffichee}"/></dd></div>
                            <div><dt>Salle</dt><dd><c:out value="${soutenance.salle.nomSalle}"/></dd></div>
                            <div><dt>Statut</dt><dd><span class="status-badge status-${soutenance.statut}"><c:out value="${soutenance.statut}"/></span></dd></div>
                        </dl>
                    </div>
                </section>
                <section class="card" aria-label="Composition du jury">
                    <div class="form-history-heading">
                        <div><span class="page-eyebrow">Jury</span><h2>Composition du jury</h2></div>
                        <span class="history-total"><c:out value="${jury.taille}"/> membres</span>
                    </div>
                    <div class="card-body">
                        <c:choose><c:when test="${not juryCompose}">
                            <p class="form-history-empty">Le jury n’est pas encore composé : au moins deux membres sont requis.</p>
                        </c:when><c:otherwise>
                            <ul class="member-list"><c:forEach var="membre" items="${jury.membres}">
                                <li><c:out value="${membre.nomComplet}"/> — <c:choose><c:when test="${membre.note}">note remise</c:when><c:otherwise>en attente</c:otherwise></c:choose></li>
                            </c:forEach></ul>
                        </c:otherwise></c:choose>
                    </div>
                </section>
                <section class="card" aria-label="Résultat de la soutenance">
                    <div class="form-history-heading">
                        <div><span class="page-eyebrow">Résultat</span><h2>Résultat de la soutenance</h2></div>
                    </div>
                    <div class="card-body">
                        <c:choose><c:when test="${evaluee and not empty noteFinale}">
                            <dl class="info-list"><div><dt>Note finale</dt><dd><c:out value="${noteFinale}"/>/20</dd></div></dl>
                        </c:when><c:otherwise>
                            <p class="form-history-empty">Ta note finale sera affichée une fois ta soutenance évaluée.</p>
                        </c:otherwise></c:choose>
                    </div>
                </section>
            </c:otherwise></c:choose>
        </c:otherwise></c:choose>
    </main>
</div>
</body>
</html>
