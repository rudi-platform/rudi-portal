'use strict';

const {describe, it} = require('node:test');
const {RuleTester} = require('eslint');
const templateParser = require('@angular-eslint/template-parser');
const rule = require('../rules/form-field-requires-label');

RuleTester.describe = describe;
RuleTester.it = it;

const ruleTester = new RuleTester({
    languageOptions: {parser: templateParser},
});

ruleTester.run('form-field-requires-label', rule, {
    valid: [
        '<input aria-label="Recherche" />',
        '<input [attr.aria-label]="label" />',
        '<label>Nom <input /></label>',
        '<mat-form-field><mat-label>Email</mat-label><input matInput /></mat-form-field>',
        '<input title="Recherche" />',
        // id explicitement associé à un <label for="...">
        '<label for="email">Email</label><input id="email" />',
        // types exclus
        '<input type="hidden" />',
        '<input type="submit" value="OK" />',
    ],
    invalid: [
        {code: '<input />', errors: [{messageId: 'missingLabel'}]},
        {code: '<select></select>', errors: [{messageId: 'missingLabel'}]},
        {code: '<textarea></textarea>', errors: [{messageId: 'missingLabel'}]},
        // id présent mais AUCUN <label for="id"> correspondant → doit être signalé
        {code: '<input id="orphan" />', errors: [{messageId: 'missingLabel'}]},
    ],
});
