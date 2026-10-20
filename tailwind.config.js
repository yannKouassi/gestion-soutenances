/** @type {import('tailwindcss').Config} */

/*
 * Couleur adossee a un token de theme.css.
 * theme.css reste l'unique source de verite des couleurs : Tailwind ne fait que
 * pointer dessus, aucune valeur hexadecimale n'est dupliquee ici.
 *
 * La valeur est une fonction et non une chaine `var(...)` brute, pour que les
 * modificateurs d'opacite (bg-primary/50) restent fonctionnels : Tailwind passe
 * alors `opacityValue`. Comme var(--c-primary) contient un hex, la syntaxe
 * `rgb(var(--x) / <alpha>)` est impossible ; on passe donc par color-mix().
 */
const token = (cssVar) => ({ opacityValue }) =>
  opacityValue === undefined
    ? `var(${cssVar})`
    : `color-mix(in srgb, var(${cssVar}) calc(${opacityValue} * 100%), transparent)`;

module.exports = {
  /*
   * Les vues sont des JSP/JSPF : Tailwind doit les lire pour ne garder que les
   * classes reellement ecrites. Sans ces globs, le JIT purgerait tout.
   */
  content: [
    './src/main/webapp/**/*.jsp',
    './src/main/webapp/**/*.jspf',
    './src/main/webapp/**/*.tag',
    './src/main/webapp/**/*.html',
    './src/main/webapp/assets/js/**/*.js',
  ],

  corePlugins: {
    /*
     * DESACTIVE : le reset de base (box-sizing, marges, typographie) est fourni
     * par theme.css. Activer preflight dupliquerait/ecraserait ce reset.
     * `@tailwind base` reste present dans input.css : il emet les variables
     * --tw-* necessaires aux utilitaires (ring, shadow, transform, gradient).
     */
    preflight: false,
  },

  theme: {
    /*
     * On REMPLACE la palette par defaut (et non `extend`) : toute couleur hors
     * charte (bg-slate-100, bg-red-500...) compile vers rien, ce qui rend une
     * eventuelle derive graphique visible immediatement.
     * Pour ajouter une couleur : nouveau token dans theme.css, puis ici.
     */
    colors: {
      transparent: 'transparent',
      current: 'currentColor',
      white: '#FFFFFF',
      black: '#000000',
      primary: token('--c-primary'),
      accent: token('--c-accent'),
      indigo: token('--c-indigo'),
      bg: token('--c-bg'),
      surface: token('--c-surface'),
      // --c-text = texte courant -> utilitaire `text-body` (evite `text-text`).
      body: token('--c-text'),
      muted: token('--c-muted'),
      ink: token('--c-ink'),
      line: token('--c-line'),
      danger: token('--c-danger'),
      success: token('--c-success'),
      warning: token('--c-warning'),
    },
  },

  /*
   * Les JSP actuelles utilisent uniquement les classes BEM de admin.css : aucune
   * utilitaire n'est encore presente dans le balisage, donc un scan `content`
   * seul produirait un fichier vide. Cette liste amorce le socle d'utilitaires
   * de la charte pour la refonte des JSP a venir ; elle est a reduire au fur et
   * a mesure que les JSP adoptent ces classes (elles seront alors detectees par
   * le scan).
   */
  safelist: [
    // Fonds
    'bg-primary',
    'bg-accent',
    'bg-indigo',
    'bg-bg',
    'bg-surface',
    'bg-danger',
    'bg-success',
    'bg-warning',
    // Textes
    'text-primary',
    'text-accent',
    'text-body',
    'text-muted',
    'text-ink',
    'text-white',
    'text-danger',
    'text-success',
    'text-warning',
    // Bordures
    'border-line',
    'border-primary',
    'border-danger',
    'border-success',
    // Interactions
    'hover:bg-primary',
    'hover:bg-accent',
    'hover:text-primary',
    'hover:text-accent',
    'focus:outline-none',
    'focus:ring-2',
    'focus:ring-accent',
  ],

  plugins: [],
};
