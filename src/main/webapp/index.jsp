<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Gestion des soutenances | PGS-PFE</title>
    <!-- Socle commun de tokens et primitives -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=20261030">
    <!-- Feuille de styles dédiée à la page d'accueil plein écran -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/index.css?v=20261030">
</head>
<body>

<a class="skip-link" href="#main-content">Aller au contenu principal</a>
    <main id="main-content" class="index-screen">
        
        <!-- En-tête discret supérieur -->
        <header class="index-topbar">
            <span class="index-brand-tag">
                <span class="index-brand-dot" aria-hidden="true"></span>
                <span>PGS-PFE &middot; Master MIAGE</span>
            </span>
            <span style="font-size: var(--fs-xs); color: var(--c-muted); font-weight: 700;">
                Soutenances PFE
            </span>
        </header>

        <!-- Centre : Gauche (Slogan + Bouton) & Droite (SVG Velo direct sans card) -->
        <section class="index-center-wrap" aria-label="Accueil PGS-PFE">
            
            <!-- Colonne Gauche : Slogan percutant et bouton de connexion immédiatement dessous -->
            <div class="index-left">
                <div class="index-slogan-wrap">
                    <h1 class="index-slogan">
                        Du mémoire à la soutenance,<br>
                        <span class="index-slogan-accent">en route vers le diplôme.</span>
                    </h1>
                    <p class="index-desc">
                        Plateforme universitaire de planification des jurys et d’évaluation des projets de fin d’études.
                    </p>
                </div>

                <div class="index-cta-wrap">
                    <c:choose>
                        <c:when test="${not empty sessionScope.utilisateurConnecte}">
                            <a href="${pageContext.request.contextPath}/dashboard" class="index-login-btn">
                                <span>Accéder à mon espace</span>
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
                            </a>
                            <span class="index-session-chip">
                                Connecté : <strong><c:out value="${sessionScope.utilisateurConnecte.login}"/></strong>
                                (<c:out value="${sessionScope.utilisateurConnecte.role}"/>) &middot;
                                <form class="inline-form" method="post" action="${pageContext.request.contextPath}/logout" style="display:inline;">
                                    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.adminCsrfToken != null ? sessionScope.adminCsrfToken : (sessionScope.encadreurCsrfToken != null ? sessionScope.encadreurCsrfToken : sessionScope.etudiantCsrfToken)}'/>">
                                    <button type="submit" style="background:none;border:0;padding:0;color:var(--c-accent);text-decoration:underline;cursor:pointer;font:inherit;">Quitter</button>
                                </form>
                            </span>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/login" class="index-login-btn">
                                <span>Se connecter</span>
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
                            </a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <!-- Colonne Droite : Le SVG velo.svg posé directement sans aucune carte autour -->
            <div class="index-right">
                <div class="index-velo-stage">
                    <object data="${pageContext.request.contextPath}/assets/img/velo.svg" 
                            type="image/svg+xml" 
                            class="index-velo-object" 
                            aria-label="Illustration vectorielle animée : En route pour la soutenance">
                        <img src="${pageContext.request.contextPath}/assets/img/velo.svg" 
                             alt="Illustration vectorielle animée : En route pour la soutenance" 
                             class="index-velo-img">
                    </object>
                </div>
            </div>

        </section>

        <!-- Pied de page minimaliste -->
        <footer class="index-footer-note">
            <span>Université &middot; Master 1 MIAGE &middot; Programmation JavaEE</span>
            <span>Session Académique 2026-2027</span>
        </footer>

    </main>

<script src="${pageContext.request.contextPath}/assets/js/prevent-double-submit.js" defer></script>
</body>
</html>
