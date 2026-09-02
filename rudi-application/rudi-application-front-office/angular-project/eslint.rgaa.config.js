/**
 * Configuration ESLint dédiée : n'exécute QUE les règles d'accessibilité RGAA.
 *
 * - Règles custom `rudi/*` (eslint-plugin-rudi)
 * - Règles a11y `@angular-eslint/template/*`
 *
 * Contrairement à `eslint.config.js` (qui applique aussi les règles TS/style),
 * cette config cible uniquement les templates `*.html` et les critères RGAA.
 *
 * Usage : npm run lint:rgaa
 *
 * ⚠️ Garder les règles en phase avec le bloc `files: ['**\/*.html']` de eslint.config.js.
 */
const angularTemplatePlugin = require('@angular-eslint/eslint-plugin-template');
const rudiPlugin = require('./eslint-plugin-rudi');

module.exports = [
    {
        files: ['**/*.html'],
        plugins: {
            '@angular-eslint/template': angularTemplatePlugin,
            'rudi': rudiPlugin,
        },
        languageOptions: {
            parser: require('@angular-eslint/template-parser'),
        },
        rules: {
            // ── Règles a11y @angular-eslint (RGAA) ───────────────────────────
            '@angular-eslint/template/alt-text': 'error',
            '@angular-eslint/template/elements-content': 'error',
            '@angular-eslint/template/valid-aria': 'error',
            '@angular-eslint/template/role-has-required-aria': 'error',
            '@angular-eslint/template/label-has-associated-control': 'error',
            '@angular-eslint/template/no-positive-tabindex': 'error',
            '@angular-eslint/template/click-events-have-key-events': 'error',
            '@angular-eslint/template/mouse-events-have-key-events': 'error',
            '@angular-eslint/template/interactive-supports-focus': 'error',
            '@angular-eslint/template/table-scope': 'error',
            '@angular-eslint/template/no-autofocus': 'warn',
            // ── Règles custom RGAA (rudi) ────────────────────────────────────
            'rudi/mat-icon-requires-aria': 'warn',
            'rudi/form-field-requires-label': 'warn',
            'rudi/no-positive-tabindex': 'warn',
            'rudi/form-button-requires-name': 'warn',
            'rudi/autocomplete-on-personal-fields': 'warn',
            'rudi/heading-hierarchy': 'warn',
            'rudi/list-structure': 'warn',
        },
    },
];
