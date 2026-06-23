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
    },
};
