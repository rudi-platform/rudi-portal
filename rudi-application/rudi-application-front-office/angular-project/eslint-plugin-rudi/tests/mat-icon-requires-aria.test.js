'use strict';

const {describe, it} = require('node:test');
const {RuleTester} = require('eslint');
const templateParser = require('@angular-eslint/template-parser');
const rule = require('../rules/mat-icon-requires-aria');

RuleTester.describe = describe;
RuleTester.it = it;

const ruleTester = new RuleTester({
    languageOptions: {parser: templateParser},
});

ruleTester.run('mat-icon-requires-aria', rule, {
    valid: [
        '<mat-icon aria-hidden="true">close</mat-icon>',
        '<mat-icon aria-label="Fermer">close</mat-icon>',
        '<mat-icon [attr.aria-label]="label">close</mat-icon>',
        '<mat-icon matSuffix>search</mat-icon>',
        // aria-label porté par un ancêtre <button> (icône décorative dans ce contexte)
        '<button aria-label="Fermer"><span><mat-icon>close</mat-icon></span></button>',
    ],
    invalid: [
        {code: '<mat-icon>close</mat-icon>', errors: [{messageId: 'missingAccessibility'}]},
        {code: '<button><mat-icon>close</mat-icon></button>', errors: [{messageId: 'missingAccessibility'}]},
    ],
});
