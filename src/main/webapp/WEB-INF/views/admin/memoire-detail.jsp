<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Détail du mémoire | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261032">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261032">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar-toggle.css?v=20261032">
</head>
<body>
<div class="admin-shell">
    <%@ include file="admin-layout.jspf" %>
    <main id="main-content" class="admin-main">
        <%@ include file="admin-topbar.jspf" %>
        <header class="page-heading">
            <div class="page-heading-copy">
                <span class="page-eyebrow">Académique &amp; Soutenances / Mémoires / Détail</span>
                <div>
                    <h1>Détail du mémoire</h1>
                    <p>Consulte les informations complètes et la description du mémoire.</p>
                </div>
            </div>
            <div class="actions">
                <a class="btn btn-secondary" href="${pageContext.request.contextPath}/admin/memoires">Retour à la liste</a>
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/memoires?vue=form&amp;id=${element.id}">Modifier le mémoire</a>
            </div>
        </header>
        <section class="card">
            <div class="card-body">
                <div class="form-history-heading">
                    <div><span class="page-eyebrow">Mémoire</span><h2><c:out value="${element.titre}"/></h2></div>
                </div>
                <dl class="info-list">
                    <div>
                        <dt>Encadreur principal</dt>
                        <dd><c:out value="${element.encadreur.prenom}"/> <c:out value="${element.encadreur.nom}"/></dd>
                    </div>
                    <div>
                        <dt>Date de dépôt</dt>
                        <dd><c:out value="${element.dateDepot}" default="Non renseignée"/></dd>
                    </div>
                    <div>
                        <dt>Description</dt>
                        <dd><c:choose><c:when test="${not empty element.description}"><c:out value="${element.description}"/></c:when><c:otherwise>Aucune description renseignée.</c:otherwise></c:choose></dd>
                    </div>
                </dl>
            </div>
        </section>
    </main>
</div>
</body>
</html>
