'use strict';

const {describe, it} = require('node:test');
const {RuleTester} = require('eslint');
const templateParser = require('@angular-eslint/template-parser');
const rule = require('../rules/autocomplete-on-personal-fields');

RuleTester.describe = describe;
RuleTester.it = it;

const ruleTester = new RuleTester({
    languageOptions: {parser: templateParser},
});

ruleTester.run('autocomplete-on-personal-fields', rule, {
    valid: [
        '<input type="email" autocomplete="email" />',
        '<input name="phone" autocomplete="tel" />',
        '<input name="address" [attr.autocomplete]="\'street-address\'" />',
        '<input autocomplete="off" name="email" />',
        // Champs NON personnels : ne doivent pas être signalés (pas de faux positifs par sous-chaîne)
        '<input name="hotel" />',
        '<input name="capacity" />',
        '<input name="telemetry" />',
        '<input name="quantity" />',
    ],
    invalid: [
        {code: '<input type="email" />', errors: [{messageId: 'missingAutocomplete'}]},
        {code: '<input name="phone" />', errors: [{messageId: 'missingAutocomplete'}]},
        {code: '<input name="userFirstName" />', errors: [{messageId: 'missingAutocomplete'}]},
        {code: '<input type="email" autocomplete="courriel" />', errors: [{messageId: 'invalidAutocomplete'}]},
    ],
});
