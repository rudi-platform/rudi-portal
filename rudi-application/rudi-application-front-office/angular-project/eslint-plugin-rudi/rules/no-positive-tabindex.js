/**
 * Règle ESLint custom : no-positive-tabindex
 *
 * RGAA 11.11 — L'ordre de tabulation est-il cohérent ?
 * Un tabindex positif (> 0) modifie l'ordre naturel de navigation clavier
 * et doit être évité. Seuls tabindex="0" et tabindex="-1" sont acceptés.
 *
 * ✅ <input tabindex="0" />
 * ✅ <div tabindex="-1"></div>
 * ✅ <button></button>                   (pas de tabindex = OK)
 * ❌ <input tabindex="1" />
 * ❌ <input tabindex="5" />
 */

'use strict';

/** @type {import('@angular-eslint/utils').TemplateRuleModule} */
module.exports = {
    meta: {
        type: 'suggestion',
        docs: {
            description: 'Interdit les valeurs positives de tabindex qui perturbent l\'ordre de navigation clavier (RGAA 11.11).',
            recommended: true,
        },
        schema: [],
        messages: {
            positiveTabindex:
                'RGAA 11.11 : tabindex="{{ value }}" modifie l\'ordre de navigation clavier. ' +
                'Utilisez tabindex="0" (focusable dans l\'ordre naturel) ou tabindex="-1" (focusable par script uniquement).',
        },
    },

    create(context) {
        return {
            'Element$1'(node) { checkTabindex(context, node); },
            'Element'(node) { checkTabindex(context, node); },
        };
    },
};

function checkTabindex(context, node) {
    const attrs = node.attributes || [];

    const tabindexAttr = attrs.find(a => a.name === 'tabindex');
    if (!tabindexAttr) return;

    const value = parseInt(tabindexAttr.value, 10);
    if (isNaN(value) || value <= 0) return;

    context.report({
        node,
        messageId: 'positiveTabindex',
        data: { value: tabindexAttr.value },
    });
}
