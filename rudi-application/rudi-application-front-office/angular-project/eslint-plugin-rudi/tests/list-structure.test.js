'use strict';

const {describe, it} = require('node:test');
const {RuleTester} = require('eslint');
const templateParser = require('@angular-eslint/template-parser');
const rule = require('../rules/list-structure');

RuleTester.describe = describe;
RuleTester.it = it;

const ruleTester = new RuleTester({
    languageOptions: {parser: templateParser},
});

ruleTester.run('list-structure', rule, {
    valid: [
        '<ul><li>Item</li></ul>',
        '<ol><li>Item</li></ol>',
        '<dl><dt>Terme</dt><dd>Définition</dd></dl>',
        '<div role="list"><div role="listitem">Item</div></div>',
        // ng-container ignoré
        '<ul><ng-container><li>Item</li></ng-container></ul>',
    ],
    invalid: [
        {code: '<ul><div>Item</div></ul>', errors: [{messageId: 'invalidListChild'}]},
        {code: '<ol><span>Item</span></ol>', errors: [{messageId: 'invalidListChild'}]},
        {code: '<dl><li>Item</li></dl>', errors: [{messageId: 'invalidListChild'}]},
    ],
});
