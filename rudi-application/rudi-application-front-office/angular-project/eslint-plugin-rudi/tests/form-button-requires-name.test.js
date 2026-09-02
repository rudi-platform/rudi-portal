'use strict';

const {describe, it} = require('node:test');
const {RuleTester} = require('eslint');
const templateParser = require('@angular-eslint/template-parser');
const rule = require('../rules/form-button-requires-name');

RuleTester.describe = describe;
RuleTester.it = it;

const ruleTester = new RuleTester({
    languageOptions: {parser: templateParser},
});

ruleTester.run('form-button-requires-name', rule, {
    valid: [
        '<button>Envoyer</button>',
        '<button aria-label="Supprimer"><mat-icon>delete</mat-icon></button>',
        // Texte imbriqué dans un enfant (ex: <span>) — doit être détecté
        '<button><span>Envoyer</span></button>',
        '<button matTooltip="Fermer"><mat-icon>close</mat-icon></button>',
        '<input type="submit" value="Valider" />',
        '<input type="image" alt="Rechercher" src="x" />',
        // Interpolation
        '<button>{{ label }}</button>',
    ],
    invalid: [
        {code: '<button><mat-icon>delete</mat-icon></button>', errors: [{messageId: 'missingButtonName'}]},
        {code: '<button></button>', errors: [{messageId: 'missingButtonName'}]},
        {code: '<input type="submit" />', errors: [{messageId: 'missingButtonName'}]},
        {code: '<input type="image" src="x" />', errors: [{messageId: 'missingButtonName'}]},
        // Icône imbriquée dans un <span>, sans texte réel
        {code: '<button><span><mat-icon>close</mat-icon></span></button>', errors: [{messageId: 'missingButtonName'}]},
    ],
});
