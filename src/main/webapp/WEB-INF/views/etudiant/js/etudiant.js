
/* ==================================================
   APPLICATION DE GESTION DES SOUTENANCES
   JavaScript de l'espace Étudiant
   ================================================== */


/* 1. OUVERTURE ET FERMETURE DU MENU MOBILE */

// Récupération des éléments HTML
const menuToggle = document.getElementById("menuToggle");
const sidebar = document.getElementById("sidebar");

// Vérifier que les éléments existent sur la page
if (menuToggle && sidebar) {

    // Écouter le clic sur le bouton du menu
    menuToggle.addEventListener("click", function () {

        // Ajouter ou retirer la classe open
        const menuOuvert = sidebar.classList.toggle("open");

        // Mettre à jour l'état du bouton
        menuToggle.setAttribute("aria-expanded", menuOuvert);

        // Modifier le texte accessible du bouton
        menuToggle.setAttribute(
            "aria-label",
            menuOuvert ? "Fermer le menu" : "Ouvrir le menu"
        );
    });
}


/* 2. IDENTIFICATION DE LA PAGE ACTIVE */

// Récupérer le chemin de la page actuelle
const pageActuelle = window.location.pathname;

// Parcourir les liens du menu
document.querySelectorAll(".nav-link").forEach(function (lien) {

    // Récupérer le chemin associé au lien
    const urlLien = new URL(lien.href, window.location.origin);

    // Comparer le chemin du lien à celui de la page actuelle
    if (urlLien.pathname === pageActuelle) {

        // Mettre en évidence le lien actif
        lien.classList.add("active");

        // Indiquer la page actuelle pour l'accessibilité
        lien.setAttribute("aria-current", "page");
    }
});


/* 3. FERMETURE DU MENU APRÈS UN CLIC SUR UN LIEN */

// Récupérer tous les liens de la barre latérale
document.querySelectorAll(".sidebar .nav-link").forEach(function (lien) {

    lien.addEventListener("click", function () {

        // Fermer le menu uniquement s'il existe
        if (sidebar && menuToggle) {
            sidebar.classList.remove("open");
            menuToggle.setAttribute("aria-expanded", "false");
            menuToggle.setAttribute("aria-label", "Ouvrir le menu");
        }
    });
});


/* 4. MESSAGE DE CONFIRMATION AVANT DÉCONNEXION */

// Cette fonctionnalité s'active uniquement sur un lien
// portant l'attribut data-confirm-logout="true".

document.querySelectorAll("[data-confirm-logout='true']").forEach(function (lien) {

    lien.addEventListener("click", function (event) {

        const confirmation = window.confirm(
            "Voulez-vous vraiment vous déconnecter ?"
        );

        // Annuler la navigation si l'utilisateur refuse
        if (!confirmation) {
            event.preventDefault();
        }
    });
});
