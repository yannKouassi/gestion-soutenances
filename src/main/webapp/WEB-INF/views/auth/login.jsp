<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Connexion | PGS-PFE</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261021-strip-auth2">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css?v=20261021-strip-auth2">
</head>
<body class="auth-split-page">
<main class="auth-split">
    <section class="auth-showcase" aria-label="Présentation de la plateforme">
        <a class="auth-brand" href="${pageContext.request.contextPath}/">PGS-PFE</a>

        <div class="auth-carousel">
            <figure class="auth-slide">
                <div class="auth-slide-frame">
                    <img src="${pageContext.request.contextPath}/assets/img/business-chart_.png" alt="">
                </div>
                <figcaption class="auth-slide-caption">
                    <span class="auth-slide-kicker">Planifier</span>
                    <strong class="auth-slide-title">Une soutenance, un créneau</strong>
                    <span class="auth-slide-text">Dates, salles et jurys se posent sans conflit.</span>
                </figcaption>
                <span class="auth-float" aria-hidden="true">Soutenances</span>
            </figure>

            <figure class="auth-slide">
                <div class="auth-slide-frame">
                    <img src="${pageContext.request.contextPath}/assets/img/data-segmentation.png" alt="">
                </div>
                <figcaption class="auth-slide-caption">
                    <span class="auth-slide-kicker">Évaluer</span>
                    <strong class="auth-slide-title">Le jury note, la moyenne suit</strong>
                    <span class="auth-slide-text">La soutenance passe à évaluée quand le jury est complet.</span>
                </figcaption>
                <span class="auth-float" aria-hidden="true">Jurys</span>
            </figure>

            <figure class="auth-slide">
                <div class="auth-slide-frame">
                    <img src="${pageContext.request.contextPath}/assets/img/drawing.png" alt="">
                </div>
                <figcaption class="auth-slide-caption">
                    <span class="auth-slide-kicker">Encadrer</span>
                    <strong class="auth-slide-title">Un suivi simple et lisible</strong>
                    <span class="auth-slide-text">L'encadreur retrouve ses étudiants et ses jurys d'un coup d'œil.</span>
                </figcaption>
                <span class="auth-float" aria-hidden="true">Mémoires</span>
            </figure>

            <figure class="auth-slide">
                <div class="auth-slide-frame">
                    <img src="${pageContext.request.contextPath}/assets/img/learning-to-sketch.png" alt="">
                </div>
                <figcaption class="auth-slide-caption">
                    <span class="auth-slide-kicker">Consulter</span>
                    <strong class="auth-slide-title">Chacun son espace</strong>
                    <span class="auth-slide-text">L'étudiant voit sa soutenance, l'encadreur ses convocations.</span>
                </figcaption>
                <span class="auth-float" aria-hidden="true">Accès</span>
            </figure>
        </div>

        <div class="auth-showcase-foot">
            <span>MIAGE · Programmation Java EE</span>
            <span class="auth-slide-dots" aria-hidden="true">
                <span></span><span></span><span></span><span></span>
            </span>
        </div>
    </section>

    <section class="auth-panel">
        <div class="auth-panel-inner">
            <p class="auth-eyebrow">Espace sécurisé</p>
            <h1>Connexion</h1>
            <p class="auth-intro">Connecte-toi pour accéder à ton espace.</p>

            <c:if test="${param.message eq 'admin-created'}">
                <div class="ui-alert ui-alert--success" role="status">Le compte administrateur est créé. Tu peux te connecter.</div>
            </c:if>
            <c:if test="${not empty erreur}">
                <div class="ui-alert ui-alert--error" role="alert"><c:out value="${erreur}"/></div>
            </c:if>

            <form class="auth-form" method="post" action="${pageContext.request.contextPath}/login">
                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">

                <div class="float-field">
                    <div class="float-field-box">
                        <input class="float-field-input" id="login" name="login" type="text" placeholder=" "
                               autocomplete="username" maxlength="100" required
                               value="<c:out value='${param.login}'/>">
                        <label class="float-field-label" for="login">Login</label>
                    </div>
                </div>

                <div class="float-field">
                    <div class="float-field-box">
                        <input class="float-field-input" id="password" name="password" type="password" placeholder=" "
                               autocomplete="current-password" maxlength="128" required>
                        <label class="float-field-label" for="password">Mot de passe</label>
                    </div>
                </div>

                <button class="ui-btn ui-btn--primary ui-btn--lg ui-btn--block" type="submit">Se connecter</button>
            </form>

            <c:if test="${firstAdminSetupRequired}">
                <p class="auth-setup-link">
                    Première utilisation ?
                    <a href="${pageContext.request.contextPath}/setup-admin">Créer le premier compte administrateur</a>
                </p>
            </c:if>
        </div>
    </section>
</main>
<script src="${pageContext.request.contextPath}/assets/js/prevent-double-submit.js" defer></script>
</body>
</html>
