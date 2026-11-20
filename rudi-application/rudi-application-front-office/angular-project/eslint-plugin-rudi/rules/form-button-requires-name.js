/**
 * Règle ESLint custom : form-button-requires-name
 *
 * RGAA 11.9 / 11.12 — Les boutons de formulaire ont-ils un intitulé accessible ?
 * Un <button>, input[type="submit"], input[type="reset"], input[type="button"],
 * input[type="image"] ou [role="button"] dans un formulaire doit avoir un nom accessible via :
 *   - Un contenu textuel (pour <button>)
 *   - aria-label / [attr.aria-label]
 *   - aria-labelledby / [attr.aria-labelledby]
 *   - value (pour input[type="submit|reset|button"])
 *   - alt (pour input[type="image"])
 *   - title
 *
 * ✅ <button>Envoyer</button>
 * ✅ <button aria-label="Supprimer"><mat-icon>delete</mat-icon></button>
 * ✅ <input type="submit" value="Valider" />
 * ✅ <input type="image" alt="Rechercher" src="..." />
 * ❌ <button><mat-icon>delete</mat-icon></button>                  (icône seule, pas de nom)
 * ❌ <button></button>
 * ❌ <input type="submit" />                                        (pas de value)
 */

'use strict';

const SUBMIT_TYPES = new Set(['submit', 'reset', 'button']);

/** @type {import('@angular-eslint/utils').TemplateRuleModule} */
module.exports = {
    meta: {
        type: 'suggestion',
        docs: {
            description: 'Un bouton de formulaire doit avoir un intitulé accessible (RGAA 11.9 / 11.12).',
            recommended: true,
        },
        schema: [],
        messages: {
            missingButtonName:
                'RGAA 11.9/11.12 : Ce bouton n\'a pas d\'intitulé accessible. ' +
                'Ajoutez un contenu textuel, aria-label, aria-labelledby, value, alt ou title.',
        },
    },

    create(context) {
        return {
            'Element$1[name="button"]'(node) { checkButton(context, node); },
            'Element[name="button"]'(node) { checkButton(context, node); },
            'Element$1[name="input"]'(node) { checkInputButton(context, node); },
            'Element[name="input"]'(node) { checkInputButton(context, node); },
        };
    },
};

function checkButton(context, node) {
    const attrs = node.attributes || [];
    const inputs = node.inputs || [];

    if (hasAccessibleNameAttr(attrs, inputs)) return;

    // matTooltip fournit un contexte accessible
    if (hasStaticAttr(attrs, 'matTooltip') || hasBoundAttr(inputs, 'matTooltip')) return;

    // Contenu textuel enfant
    if (hasTextContent(node)) return;

    context.report({
        node,
        messageId: 'missingButtonName',
    });
}

function checkInputButton(context, node) {
    const attrs = node.attributes || [];
    const inputs = node.inputs || [];

    const typeAttr = attrs.find(a => a.name === 'type');
    if (!typeAttr) return;
    const type = typeAttr.value;

    if (type === 'image') {
        // input[type="image"] nécessite alt
        if (hasStaticAttr(attrs, 'alt') || hasBoundAttr(inputs, 'alt')) return;
        if (hasAccessibleNameAttr(attrs, inputs)) return;

        context.report({ node, messageId: 'missingButtonName' });
        return;
    }

    if (!SUBMIT_TYPES.has(type)) return;

    // input[type="submit|reset|button"] nécessite value
    if (hasStaticAttr(attrs, 'value') || hasBoundAttr(inputs, 'value')) return;
    if (hasAccessibleNameAttr(attrs, inputs)) return;

    context.report({ node, messageId: 'missingButtonName' });
}

/**
 * Vérifie si un nœud a du contenu textuel (Text node non vide) parmi ses descendants.
 * Descend récursivement dans les éléments enfants (ex: <button><span>Envoyer</span></button>),
 * mais ignore le contenu des <mat-icon> (le ligature d'une icône n'est pas un intitulé).
 */
function hasTextContent(node) {
    // Blocs de contrôle Angular @if/@else : le nœud expose `branches` (IfBlockBranch[])
    // au lieu de `children` directement (ex: @if (x) {...} @else {...}).
    if (node.branches && node.branches.length > 0) {
        return node.branches.some(branch => hasTextContent(branch));
    }

    // Bloc @switch : selon la version du parser, le nœud expose soit `cases`
    // (SwitchBlockCase[], chaque case ayant directement `children` - forme produite par
    // @angular-eslint/template-parser), soit `groups` (SwitchBlockCaseGroup[] du compilateur
    // Angular brut, chaque groupe ayant ses propres `children`).
    if (node.cases && node.cases.length > 0) {
        return node.cases.some(switchCase => hasTextContent(switchCase));
    }
    if (node.groups && node.groups.length > 0) {
        return node.groups.some(group => hasTextContent(group));
    }

    const children = node.children || [];
    return children.some(child => {
        // Nœud texte avec contenu non vide
        if (child.type === 'Text$3' || child.type === 'Text') {
            return child.value && child.value.trim().length > 0;
        }
        // Interpolation Angular {{ expression }}
        if (child.type === 'BoundText' || child.type === 'BoundText$3') {
            return true;
        }
        // Élément enfant : on descend récursivement (sauf <mat-icon>, dont le texte est un ligature)
        if (child.name === 'mat-icon') {
            return false;
        }
        // Bloc @if/@else imbriqué
        if (child.branches && child.branches.length > 0) {
            return hasTextContent(child);
        }
        // Bloc @switch imbriqué
        if ((child.cases && child.cases.length > 0) || (child.groups && child.groups.length > 0)) {
            return hasTextContent(child);
        }
        if (child.children && child.children.length > 0) {
            return hasTextContent(child);
        }
        return false;
    });
}

/**
 * Vrai si l'élément porte un nom accessible via aria-label, aria-labelledby ou title
 * (statique ou lié), commun à tous les types de boutons/input vérifiés par cette règle.
 */
function hasAccessibleNameAttr(attrs, inputs) {
    if (hasStaticAttr(attrs, 'aria-label') || hasBoundAttr(inputs, 'aria-label')) return true;
    if (hasStaticAttr(attrs, 'aria-labelledby') || hasBoundAttr(inputs, 'aria-labelledby')) return true;
    if (hasStaticAttr(attrs, 'title') || hasBoundAttr(inputs, 'title')) return true;
    return false;
}

function hasStaticAttr(attrs, name, expectedValue) {
    return attrs.some(attr => {
        if (attr.name !== name) return false;
        return expectedValue === undefined || attr.value === expectedValue;
    });
}

function hasBoundAttr(inputs, name) {
    return inputs.some(input => input.name === name);
}
