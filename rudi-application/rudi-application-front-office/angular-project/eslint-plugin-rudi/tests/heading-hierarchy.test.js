'use strict';

const {describe, it} = require('node:test');
const {RuleTester} = require('eslint');
const templateParser = require('@angular-eslint/template-parser');
const rule = require('../rules/heading-hierarchy');

RuleTester.describe = describe;
RuleTester.it = it;

const ruleTester = new RuleTester({
    languageOptions: {parser: templateParser},
});

ruleTester.run('heading-hierarchy', rule, {
    valid: [
        '<h1>A</h1><h2>B</h2><h3>C</h3>',
        '<h2>A</h2><h2>B</h2>',
        '<h3>A</h3><h2>B</h2>',
        '<h1>Seul</h1>',
    ],
    invalid: [
        {code: '<h1>A</h1><h3>C</h3>', errors: [{messageId: 'skippedLevel'}]},
        {code: '<h2>A</h2><h4>D</h4>', errors: [{messageId: 'skippedLevel'}]},
    ],
});
