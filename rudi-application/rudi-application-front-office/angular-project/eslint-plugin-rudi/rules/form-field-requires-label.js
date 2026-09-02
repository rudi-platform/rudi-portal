/**
 * Règle ESLint custom : form-field-requires-label
 *
 * RGAA 11.1 — Chaque champ de formulaire a-t-il une étiquette ?
 * Un <input>, <select> ou <textarea> doit avoir une étiquette associée via :
 *   - aria-label / [attr.aria-label]
 *   - aria-labelledby / [attr.aria-labelledby]
 *   - être dans un <label> englobant
 *   - être dans un <mat-form-field> (Angular Material fournit le label via <mat-label>)
 *   - avoir un attribut title
 *
 * ✅ <input aria-label="Recherche" />
 * ✅ <input [attr.aria-label]="label" />
 * ✅ <label>Nom <input /></label>
 * ✅ <mat-form-field><mat-label>Email</mat-label><input matInput /></mat-form-field>
 * ❌ <input />
 * ❌ <select></select>
 * ❌ <textarea></textarea>
 */

'use strict';

const FORM_FIELD_ELEMENTS = ['input', 'select', 'textarea'];

/** Types d'input exclus (pas besoin de label visible) */
const EXCLUDED_INPUT_TYPES = ['hidden', 'submit', 'reset', 'button', 'image'];

/** @type {import('@angular-eslint/utils').TemplateRuleModule} */
module.exports = {
    meta: {
        type: 'suggestion',
        docs: {
            description: 'Un champ de formulaire (<input>, <select>, <textarea>) doit avoir une étiquette associée (RGAA 11.1).',
            recommended: true,
        },
        schema: [],
        messages: {
            missingLabel:
                'RGAA 11.1 : Ce champ de formulaire n\'a pas d\'étiquette associée. ' +
                'Ajoutez aria-label, aria-labelledby, title, ou placez-le dans un <label> ou <mat-form-field>.',
        },
    },

    create(context) {
        // Deux passes : on collecte d'abord les `for` des <label>, puis on valide les champs à la sortie.
        const labelFors = new Set();
        const fields = [];

        const visitors = {
            'Element$1[name="label"]'(node) { collectLabelFor(node, labelFors); },
            'Element[name="label"]'(node) { collectLabelFor(node, labelFors); },
            'Program:exit'() {
                for (const node of fields) {
                    checkFormField(context, node, labelFors);
                }
            },
        };
        for (const element of FORM_FIELD_ELEMENTS) {
            visitors[`Element$1[name="${element}"]`] = (node) => fields.push(node);
            visitors[`Element[name="${element}"]`] = (node) => fields.push(node);
        }
        return visitors;
    },
};

function collectLabelFor(node, labelFors) {
    const attrs = node.attributes || [];
    const forAttr = attrs.find(a => a.name === 'for');
    if (forAttr && forAttr.value) {
        labelFors.add(forAttr.value);
    }
}

function checkFormField(context, node, labelFors) {
    const attrs = node.attributes || [];
    const inputs = node.inputs || [];

    // Exclure les types qui n'ont pas besoin de label
    if (node.name === 'input') {
        const typeAttr = attrs.find(a => a.name === 'type');
        if (typeAttr && EXCLUDED_INPUT_TYPES.includes(typeAttr.value)) {
            return;
        }
    }

    // 1. aria-label ou aria-labelledby statique
    if (hasStaticAttr(attrs, 'aria-label') || hasStaticAttr(attrs, 'aria-labelledby')) {
        return;
    }

    // 2. Bindings Angular : [attr.aria-label], [attr.aria-labelledby]
    if (hasBoundAttr(inputs, 'aria-label') || hasBoundAttr(inputs, 'aria-labelledby')) {
        return;
    }

    // 3. Attribut title
    if (hasStaticAttr(attrs, 'title') || hasBoundAttr(inputs, 'title')) {
        return;
    }

    // 4. id explicitement associé à un <label for="id"> présent dans le template
    const idAttr = attrs.find(a => a.name === 'id');
    if (idAttr && idAttr.value && labelFors.has(idAttr.value)) {
        return;
    }

    // 5. Dans un <label> englobant
    if (hasAncestor(node, 'label')) {
        return;
    }

    // 6. Dans un <mat-form-field> Angular Material (qui fournit <mat-label>)
    if (hasAncestor(node, 'mat-form-field')) {
        return;
    }

    // Aucune étiquette trouvée
    context.report({
        node,
        messageId: 'missingLabel',
    });
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

function hasAncestor(node, ancestorName) {
    let current = node.parent;
    while (current) {
        if (current.name === ancestorName) return true;
        current = current.parent;
    }
    return false;
}
