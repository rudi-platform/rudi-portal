/**
 * Règle ESLint custom : autocomplete-on-personal-fields
 *
 * RGAA 11.13 — Les champs d'information personnelle ont-ils un attribut autocomplete ?
 * Les champs concernant l'utilisateur (email, nom, téléphone, adresse…) doivent avoir
 * un attribut autocomplete avec une valeur appropriée selon la spécification HTML 5.2.
 *
 * ✅ <input type="email" autocomplete="email" />
 * ✅ <input name="phone" autocomplete="tel" />
 * ✅ <input name="address" [attr.autocomplete]="'street-address'" />
 * ❌ <input type="email" />
 * ❌ <input name="phone" />
 */

'use strict';

/** Valeurs autocomplete valides (HTML 5.2 / WCAG 1.3.5) */
const VALID_AUTOCOMPLETE_VALUES = [
    'name', 'honorific-prefix', 'given-name', 'additional-name', 'family-name', 'honorific-suffix',
    'nickname', 'email', 'username', 'new-password', 'current-password',
    'organization-title', 'organization',
    'street-address', 'address-line1', 'address-line2', 'address-line3',
    'address-level1', 'address-level2', 'address-level3', 'address-level4',
    'country', 'country-name', 'postal-code',
    'tel', 'tel-country-code', 'tel-national', 'tel-area-code', 'tel-local',
    'cc-name', 'cc-given-name', 'cc-additional-name', 'cc-family-name',
    'cc-number', 'cc-exp', 'cc-exp-month', 'cc-exp-year', 'cc-csc', 'cc-type',
    'bday', 'bday-day', 'bday-month', 'bday-year', 'sex', 'url', 'photo',
    'language', 'impp',
];

/**
 * Patterns pour détecter les champs d'information personnelle.
 * On vérifie le type et le name/id de l'input.
 */
const PERSONAL_INPUT_TYPES = ['email', 'tel'];

/**
 * Tokens (mots entiers) indiquant un champ d'information personnelle.
 * On matche par token exact (après découpage camelCase/kebab/snake) pour éviter
 * les faux positifs par sous-chaîne (ex: "hotel" → "tel", "capacity" → "city").
 */
const PERSONAL_NAME_TOKENS = new Set([
    'email', 'mail', 'courriel',
    'name', 'nom', 'prenom', 'firstname', 'lastname', 'fullname', 'surname',
    'phone', 'tel', 'telephone', 'mobile',
    'address', 'adresse', 'street', 'rue',
    'city', 'ville',
    'zip', 'postal', 'postalcode', 'codepostal', 'cp',
    'country', 'pays',
]);

/**
 * Découpe une valeur (name/id) en tokens : camelCase, kebab-case, snake_case, chiffres.
 * Ex: "userFirstName" → ["user", "first", "name"], "postal-code" → ["postal", "code"].
 */
function tokenize(value) {
    return String(value || '')
        .replace(/([a-z0-9])([A-Z])/g, '$1 $2')
        .split(/[^a-zA-Z]+/)
        .map(t => t.toLowerCase())
        .filter(Boolean);
}

/** @type {import('@angular-eslint/utils').TemplateRuleModule} */
module.exports = {
    meta: {
        type: 'suggestion',
        docs: {
            description: 'Les champs d\'information personnelle doivent avoir un attribut autocomplete approprié (RGAA 11.13).',
            recommended: false,
        },
        schema: [],
        messages: {
            missingAutocomplete:
                'RGAA 11.13 : Ce champ d\'information personnelle devrait avoir un attribut autocomplete ' +
                'avec une valeur appropriée (ex: "email", "tel", "name", "street-address").',
            invalidAutocomplete:
                'RGAA 11.13 : La valeur autocomplete="{{ value }}" n\'est pas une valeur standard. ' +
                'Utilisez une valeur définie par la spécification HTML 5.2.',
        },
    },

    create(context) {
        return {
            'Element$1[name="input"]'(node) { checkAutocomplete(context, node); },
            'Element[name="input"]'(node) { checkAutocomplete(context, node); },
        };
    },
};

function checkAutocomplete(context, node) {
    const attrs = node.attributes || [];
    const inputs = node.inputs || [];

    // Ne vérifier que les champs d'information personnelle
    if (!isPersonalField(attrs)) return;

    // Vérifier si autocomplete est présent
    const autocompleteAttr = attrs.find(a => a.name === 'autocomplete');
    const hasBoundAutocomplete = hasBoundAttr(inputs, 'autocomplete');

    if (hasBoundAutocomplete) {
        // Binding dynamique — on fait confiance
        return;
    }

    if (!autocompleteAttr) {
        // autocomplete="off" est aussi accepté si c'est un choix explicite
        context.report({
            node,
            messageId: 'missingAutocomplete',
        });
        return;
    }

    // Vérifier que la valeur est valide
    const value = autocompleteAttr.value;
    if (value === 'off' || value === 'on') return;
    if (!VALID_AUTOCOMPLETE_VALUES.includes(value)) {
        context.report({
            node,
            messageId: 'invalidAutocomplete',
            data: { value },
        });
    }
}

function isPersonalField(attrs) {
    // Vérifier le type
    const typeAttr = attrs.find(a => a.name === 'type');
    if (typeAttr && PERSONAL_INPUT_TYPES.includes(typeAttr.value)) {
        return true;
    }

    // Vérifier le name ou l'id, par token exact
    const nameAttr = attrs.find(a => a.name === 'name');
    const idAttr = attrs.find(a => a.name === 'id');
    const tokens = [
        ...tokenize(nameAttr && nameAttr.value),
        ...tokenize(idAttr && idAttr.value),
    ];
    return tokens.some(t => PERSONAL_NAME_TOKENS.has(t));
}

function hasBoundAttr(inputs, name) {
    return inputs.some(input => input.name === name);
}
