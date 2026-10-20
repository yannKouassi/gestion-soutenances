<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Planification des soutenances | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin.css?v=20261015-admin-menu">
</head>
<body>
<div class="admin-shell">
    <%@ include file="admin-layout.jspf" %>
    <main class="admin-main admin-form-page">
        <%@ include file="admin-topbar.jspf" %>
        <header class="page-heading"><div class="page-heading-copy">
            <span class="page-eyebrow">Académique &amp; Soutenances / Soutenances / Planification</span>
            <div><h1>Planifier une soutenance</h1>
            <p>Choisis un mémoire non soutenu, une salle et un créneau horaire, puis compose le jury.</p></div>
        </div></header>
        <c:if test="${not empty message}"><div class="alert alert-success" role="status"><c:out value="${message}"/></div></c:if>
        <c:if test="${not empty erreur}"><div class="alert alert-error" role="alert"><c:out value="${erreur}"/></div></c:if>
        <section class="card"><div class="card-body">
            <div class="form-card-heading">
                <c:choose><c:when test="${empty element.id}"><span class="form-mode">Création</span></c:when><c:otherwise><span class="form-mode">Modification</span></c:otherwise></c:choose>
                <h2>Informations de la soutenance</h2>
                <p>Un mémoire ne peut être soutenu qu’une seule fois ; une salle ne peut pas accueillir deux soutenances sur le même créneau.</p>
            </div>
            <form class="form-grid" method="post" action="${pageContext.request.contextPath}/admin/planification/enregistrer">
                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                <input type="hidden" name="id" value="<c:out value='${element.id}'/>">
                <div class="field field-full"><label for="memoireId">Mémoire à soutenir <span class="required-mark" aria-hidden="true">*</span></label><select id="memoireId" name="memoireId" required><option value="">Choisir un mémoire</option><c:forEach var="memoire" items="${memoires}"><option value="<c:out value='${memoire.id}'/>" data-date-depot="<c:out value='${memoire.dateDepot}'/>" ${memoire.id eq selectedMemoireId ? 'selected' : ''}><c:out value="${memoire.titre}"/> — <c:out value="${memoire.encadreur.prenom}"/> <c:out value="${memoire.encadreur.nom}"/></option></c:forEach></select><small class="field-hint">Seuls les mémoires encore sans soutenance sont proposés.</small></div>
                <div class="field"><label for="dateHeure">Date et heure <span class="required-mark" aria-hidden="true">*</span></label><input id="dateHeure" name="dateHeure" type="datetime-local" required min="<c:out value='${dateHeureMin}'/>" value="<c:out value='${dateHeureValue}'/>"></div>
                <div class="field"><label for="salleId">Salle <span class="required-mark" aria-hidden="true">*</span></label><select id="salleId" name="salleId" required><option value="">Choisir une salle</option><c:forEach var="salle" items="${salles}"><option value="<c:out value='${salle.id}'/>" ${salle.id eq selectedSalleId ? 'selected' : ''}><c:out value="${salle.nomSalle}"/> — <c:out value="${salle.departement.nom}"/></option></c:forEach></select></div>
                <div class="form-actions">
                    <button class="btn btn-primary" type="submit">Enregistrer</button>
                    <c:if test="${not empty element.id}"><a class="btn btn-secondary" href="${pageContext.request.contextPath}/admin/jury?soutenance=${element.id}">Composer le jury</a></c:if>
                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/admin/soutenances">Annuler</a>
                </div>
            </form>
        </div></section>
        <%@ include file="admin-history.jspf" %>
    </main>
</div>
<script>
(function () {
    var memoireSelect = document.getElementById('memoireId');
    var dateInput = document.getElementById('dateHeure');
    if (!memoireSelect || !dateInput) {
        return;
    }
    function formatLocal(date) {
        var pad = function (value) { return value < 10 ? '0' + value : '' + value; };
        return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate())
            + 'T' + pad(date.getHours()) + ':' + pad(date.getMinutes());
    }
    function borneMemoire() {
        var option = memoireSelect.options[memoireSelect.selectedIndex];
        var depot = option ? option.getAttribute('data-date-depot') : null;
        if (!depot) {
            return null;
        }
        var borne = new Date(depot + 'T00:00:00');
        if (isNaN(borne.getTime())) {
            return null;
        }
        borne.setDate(borne.getDate() + 1);
        return borne;
    }
    function borneMin() {
        var borne = borneMemoire();
        var now = new Date();
        now.setSeconds(0, 0);
        return borne && borne > now ? borne : now;
    }
    memoireSelect.addEventListener('change', function () {
        dateInput.min = formatLocal(borneMin());
    });
})();
</script>
</body>
</html>
