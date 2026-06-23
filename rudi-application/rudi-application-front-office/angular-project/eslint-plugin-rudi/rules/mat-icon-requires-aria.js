/**
 * Règle ESLint custom : mat-icon-requires-aria
 *
 * RGAA 1.1.1 / 1.1.3 — Icône porteuse d'information :
 * Un <mat-icon> sans aria-hidden="true" est considéré comme porteur de sens
 * et DOIT avoir un aria-label ou aria-labelledby.
 *
 * ✅ <mat-icon aria-hidden="true">close</mat-icon>                          (décoratif)
 * ✅ <mat-icon aria-label="Fermer">close</mat-icon>                         (informatif, accessible)
 * ✅ <mat-icon aria-hidden="false" aria-label="Fermer">close</mat-icon>     (informatif, explicite)
 * ✅ <mat-icon [attr.aria-label]="label">close</mat-icon>                   (binding dynamique)
 * ❌ <mat-icon>close</mat-icon>                                              (ni décoratif, ni accessible)
 * ❌ <mat-icon aria-hidden="false">close</mat-icon>                          (informatif sans label)
 */

'use strict';

/** @type {import('@angular-eslint/utils').TemplateRuleModule} */
module.exports = {
    meta: {
        type: 'suggestion',
        docs: {
            description: 'Un <mat-icon> doit être soit décoratif (aria-hidden="true"), soit accessible (aria-label ou aria-labelledby).',
            recommended: true,
        },
        schema: [],
        messages: {
            missingAccessibility:
                'RGAA : <mat-icon> sans aria-hidden="true" est considéré comme porteur de sens. ' +
                'Ajoutez aria-label/aria-labelledby, ou marquez-le comme décoratif avec aria-hidden="true".',
        },
    },

    create(context) {
        // Nom de l'élément à vérifier (fonctionne aussi avec des alias si nécessaire)
        const TARGET_ELEMENT = 'mat-icon';

        return {
            [`Element$1[name="${TARGET_ELEMENT}"]`](node) {
                checkMatIcon(context, node);
            },
            // Fallback : certains parsers utilisent Element sans suffixe
            [`Element[name="${TARGET_ELEMENT}"]`](node) {
                checkMatIcon(context, node);
            },
        };
    },
};

/**
 * Vérifie qu'un nœud <mat-icon> est soit décoratif, soit accessible.
 */
function checkMatIcon(context, node) {
    const attrs = node.attributes || [];
    const inputs = node.inputs || [];

    // 1. Vérifier aria-hidden="true" → décoratif, OK
    if (hasStaticAttr(attrs, 'aria-hidden', 'true')) {
        return;
    }

    // 2. Vérifier si un aria-label ou aria-labelledby statique existe
    if (hasStaticAttr(attrs, 'aria-label') || hasStaticAttr(attrs, 'aria-labelledby')) {
        return;
    }

    // 3. Vérifier les bindings Angular : [attr.aria-label], [attr.aria-labelledby], [attr.aria-hidden]
    if (hasBoundAttr(inputs, 'aria-label') || hasBoundAttr(inputs, 'aria-labelledby') || hasBoundAttr(inputs, 'aria-hidden')) {
        return;
    }

    // 4. Vérifier matChipRemove / matTooltip qui fournissent un contexte accessible via le parent
    if (hasStaticAttr(attrs, 'matChipRemove') || hasStaticAttr(attrs, 'matTooltip') ||
        hasBoundAttr(inputs, 'matTooltip')) {
        return;
    }

    // 5. Vérifier si le parent (<button>, <a>) a un aria-label → l'icône est décorative dans ce contexte
    const parent = node.parent;
    if (parent && (parent.name === 'button' || parent.name === 'a')) {
        const parentAttrs = parent.attributes || [];
        const parentInputs = parent.inputs || [];
        if (hasStaticAttr(parentAttrs, 'aria-label') || hasStaticAttr(parentAttrs, 'aria-labelledby') ||
            hasBoundAttr(parentInputs, 'aria-label') || hasBoundAttr(parentInputs, 'aria-labelledby')) {
            return;
        }
    }

    // 6. Vérifier le suffixe matSuffix / matPrefix → icône décorative d'un champ Material
    if (hasStaticAttr(attrs, 'matSuffix') || hasStaticAttr(attrs, 'matPrefix')) {
        return;
    }

    // Aucun attribut d'accessibilité trouvé → erreur
    context.report({
        node,
        messageId: 'missingAccessibility',
    });
}

function hasStaticAttr(attrs, name, expectedValue) {
    return attrs.some(attr => {
        if (attr.name !== name) {
            return false;
        }
        return expectedValue === undefined || attr.value === expectedValue;
    });
}

function hasBoundAttr(inputs, name) {
    return inputs.some(input => input.name === name);
}
