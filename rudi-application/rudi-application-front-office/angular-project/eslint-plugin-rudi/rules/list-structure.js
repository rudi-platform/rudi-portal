/**
 * Règle ESLint custom : list-structure
 *
 * RGAA 9.3 — Les listes sont-elles correctement structurées ?
 *
 * <ul> et <ol> ne doivent contenir que des <li> comme enfants directs.
 * <dl> ne doit contenir que des <dt> et <dd> comme enfants directs.
 * Les éléments <li> avec role="listitem" dans un conteneur role="list" sont aussi acceptés.
 *
 * ✅ <ul><li>Item</li></ul>
 * ✅ <ol><li>Item</li></ol>
 * ✅ <dl><dt>Terme</dt><dd>Définition</dd></dl>
 * ✅ <div role="list"><div role="listitem">Item</div></div>
 * ❌ <ul><div>Item</div></ul>                   (div directement dans ul)
 * ❌ <ol><span>Item</span></ol>
 * ❌ <dl><li>Item</li></dl>                      (li dans dl au lieu de dt/dd)
 *
 * Note : les <ng-container>, <ng-template>, et directives structurelles Angular
 * (*ngFor, *ngIf) sont ignorés car ils ne génèrent pas de nœuds DOM.
 */

'use strict';

/** Éléments Angular qui ne génèrent pas de nœud DOM */
const ANGULAR_STRUCTURAL = ['ng-container', 'ng-template', 'ng-content'];

/** Enfants valides pour <ul> et <ol> */
const VALID_LIST_CHILDREN = ['li'];

/** Enfants valides pour <dl> */
const VALID_DL_CHILDREN = ['dt', 'dd', 'div']; // <div> est autorisé dans <dl> en HTML5

/** @type {import('@angular-eslint/utils').TemplateRuleModule} */
module.exports = {
    meta: {
        type: 'suggestion',
        docs: {
            description: 'Les listes (<ul>, <ol>, <dl>) ne doivent contenir que des enfants valides (RGAA 9.3).',
            recommended: true,
        },
        schema: [],
        messages: {
            invalidListChild:
                'RGAA 9.3 : <{{ child }}> n\'est pas un enfant valide de <{{ parent }}>. ' +
                'Utilisez <li> pour <ul>/<ol>, ou <dt>/<dd> pour <dl>.',
            invalidAriaListChild:
                'RGAA 9.3 : Les enfants d\'un élément role="list" devraient avoir role="listitem".',
        },
    },

    create(context) {
        const visitors = {};
        for (const tag of ['ul', 'ol']) {
            visitors[`Element$1[name="${tag}"]`] = (node) => checkListChildren(context, node, VALID_LIST_CHILDREN);
            visitors[`Element[name="${tag}"]`] = (node) => checkListChildren(context, node, VALID_LIST_CHILDREN);
        }
        visitors['Element$1[name="dl"]'] = (node) => checkListChildren(context, node, VALID_DL_CHILDREN);
        visitors['Element[name="dl"]'] = (node) => checkListChildren(context, node, VALID_DL_CHILDREN);
        return visitors;
    },
};

function checkListChildren(context, node, validChildren) {
    const children = node.children || [];

    for (const child of children) {
        // Ignorer les nœuds texte (espaces, retours à la ligne)
        if (!child.name) continue;

        // Ignorer les éléments structurels Angular
        if (ANGULAR_STRUCTURAL.includes(child.name)) continue;

        // Vérifier que l'enfant est dans la liste des enfants valides
        if (!validChildren.includes(child.name)) {
            // Vérifier si l'enfant a un role="listitem" (pour les listes ARIA)
            const attrs = child.attributes || [];
            const hasListitemRole = attrs.some(a => a.name === 'role' && a.value === 'listitem');
            if (hasListitemRole) continue;

            context.report({
                node: child,
                messageId: 'invalidListChild',
                data: {
                    child: child.name,
                    parent: node.name,
                },
            });
        }
    }
}
