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
        // Texte porté par une seule branche d'un bloc @if/@else (ex: button-disclosure)
        '<button>@if (hasKey) { <span>{{ label }}</span> } @else { <span>Texte</span> }</button>',
        // Texte dans la branche @else uniquement
        '<button>@if (false) { <mat-icon>delete</mat-icon> } @else { Texte }</button>',
        // Texte dans un bloc @switch
        '<button>@switch (type) { @case (1) { Un } @default { Autre } }</button>',
    ],
    invalid: [
        {code: '<button><mat-icon>delete</mat-icon></button>', errors: [{messageId: 'missingButtonName'}]},
        {code: '<button></button>', errors: [{messageId: 'missingButtonName'}]},
        {code: '<input type="submit" />', errors: [{messageId: 'missingButtonName'}]},
        {code: '<input type="image" src="x" />', errors: [{messageId: 'missingButtonName'}]},
        // Icône imbriquée dans un <span>, sans texte réel
        {code: '<button><span><mat-icon>close</mat-icon></span></button>', errors: [{messageId: 'missingButtonName'}]},
        // Aucune branche du @if/@else n'a de texte réel (icônes seules)
        {
            code: '<button>@if (x) { <mat-icon>delete</mat-icon> } @else { <mat-icon>close</mat-icon> }</button>',
            errors: [{messageId: 'missingButtonName'}],
        },
    ],
});
