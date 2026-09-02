'use strict';

const {describe, it} = require('node:test');
const {RuleTester} = require('eslint');
const templateParser = require('@angular-eslint/template-parser');
const rule = require('../rules/no-positive-tabindex');

RuleTester.describe = describe;
RuleTester.it = it;

const ruleTester = new RuleTester({
    languageOptions: {parser: templateParser},
});

ruleTester.run('no-positive-tabindex', rule, {
    valid: [
        '<input tabindex="0" />',
        '<div tabindex="-1"></div>',
        '<button>OK</button>',
    ],
    invalid: [
        {code: '<input tabindex="1" />', errors: [{messageId: 'positiveTabindex'}]},
        {code: '<input tabindex="5" />', errors: [{messageId: 'positiveTabindex'}]},
    ],
});
