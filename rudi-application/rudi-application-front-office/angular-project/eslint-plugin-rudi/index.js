/**
 * eslint-plugin-rudi
 *
 * Plugin ESLint local regroupant les règles custom du projet RUDI.
 * Principalement des règles d'accessibilité RGAA pour les templates Angular Material.
 *
 * Usage dans eslint.config.js :
 *   const rudiPlugin = require('./eslint-plugin-rudi');
 *   // puis dans la config :
 *   plugins: { 'rudi': rudiPlugin },
 *   rules: { 'rudi/mat-icon-requires-aria': 'warn' }
 */

'use strict';

module.exports = {
    rules: {
        'mat-icon-requires-aria': require('./rules/mat-icon-requires-aria'),
        'form-field-requires-label': require('./rules/form-field-requires-label'),
        'no-positive-tabindex': require('./rules/no-positive-tabindex'),
        'form-button-requires-name': require('./rules/form-button-requires-name'),
        'autocomplete-on-personal-fields': require('./rules/autocomplete-on-personal-fields'),
        'heading-hierarchy': require('./rules/heading-hierarchy'),
        'list-structure': require('./rules/list-structure'),
    },
};
