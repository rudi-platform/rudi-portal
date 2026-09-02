const js = require('@eslint/js');
const tsParser = require('@typescript-eslint/parser');
const tsPlugin = require('@typescript-eslint/eslint-plugin');
const angularPlugin = require('@angular-eslint/eslint-plugin');
const angularTemplatePlugin = require('@angular-eslint/eslint-plugin-template');
const rudiPlugin = require('./eslint-plugin-rudi');


module.exports = [
    js.configs.recommended,
    {
        files: ['**/*.ts'],
        languageOptions: {
            parser: tsParser,
            parserOptions: {
                project: ['./tsconfig.json'],
                tsconfigRootDir: __dirname,
                sourceType: 'module',
                ecmaVersion: 'latest'
            }
        },
        plugins: {
            '@typescript-eslint': tsPlugin,
            '@angular-eslint': angularPlugin
        },
        rules: {
            'eol-last': ['error', 'always'],
            'no-empty': 'off',
            'no-console': ['error', {allow: ['log']}],
            'no-unused-vars': 'off',
            '@typescript-eslint/no-unused-vars': ['warn', {
                argsIgnorePattern: '^_',
                varsIgnorePattern: '^_',
                caughtErrorsIgnorePattern: '^_'
            }],
            'quotes': ['error', 'single', {avoidEscape: true}],
            'semi': ['error', 'always'],
            'curly': ['error', 'all'],
            'brace-style': ['error', '1tbs', { allowSingleLine: false }],
            'max-len': ['error', {code: 140, ignoreUrls: true, ignoreStrings: true, ignoreTemplateLiterals: true}],
            'space-before-function-paren': ['error', {
                anonymous: 'never',
                named: 'never',
                asyncArrow: 'always'
            }],
            '@typescript-eslint/array-type': ['error', {default: 'array'}],
            '@typescript-eslint/no-inferrable-types': ['error', {ignoreParameters: true}],
            '@typescript-eslint/no-non-null-assertion': 'error',
            '@typescript-eslint/no-var-requires': 'off',
            '@typescript-eslint/member-ordering': ['warn', {
                default: [
                    'signature',
                    'field',
                    'static-field',
                    'instance-field',
                    'constructor',
                    'method',
                    'static-method',
                    'instance-method'
                ]
            }],
            '@angular-eslint/component-class-suffix': ['error', {suffixes: ['Component']}],
            '@angular-eslint/directive-class-suffix': ['error', {suffixes: ['Directive']}],
            '@angular-eslint/directive-selector': ['error', {type: 'attribute', prefix: 'app', style: 'camelCase'}],
            '@angular-eslint/component-selector': ['error', {type: 'element', prefix: 'app', style: 'kebab-case'}],
            '@angular-eslint/contextual-lifecycle': 'error',
            '@angular-eslint/no-conflicting-lifecycle': 'error',
            '@angular-eslint/no-input-rename': 'error',
            '@angular-eslint/no-inputs-metadata-property': 'error',
            '@angular-eslint/no-output-native': 'error',
            '@angular-eslint/no-output-on-prefix': 'error',
            '@angular-eslint/no-output-rename': 'error',
            '@angular-eslint/no-outputs-metadata-property': 'error'
        }
    },
    {
        files: ['**/*.html'],
        plugins: {
            '@angular-eslint/template': angularTemplatePlugin,
            'rudi': rudiPlugin,
        },
        languageOptions: {
            parser: require('@angular-eslint/template-parser')
        },
        rules: {
            '@angular-eslint/template/banana-in-box': 'error',
            '@angular-eslint/template/no-negated-async': 'error',

            // ── Règles d'accessibilité RGAA ──────────────────────────────────
            // Images : alt obligatoire (RGAA 1.1.1)
            '@angular-eslint/template/alt-text': 'error',
            // Éléments interactifs : contenu accessible (RGAA 11.1, 11.2)
            '@angular-eslint/template/elements-content': 'error',
            // Liens : intitulé explicite (RGAA 6.1)
            '@angular-eslint/template/valid-aria': 'error',
            // Rôles ARIA valides (RGAA 7.1)
            '@angular-eslint/template/role-has-required-aria': 'error',
            // Labels de formulaires (RGAA 11.1)
            '@angular-eslint/template/label-has-associated-control': 'error',
            // Pas de tabindex positif (RGAA 12.8)
            '@angular-eslint/template/no-positive-tabindex': 'error',
            // Interactivité clavier : pas de gestionnaire de clic sans clavier (RGAA 7.1, 12.13)
            '@angular-eslint/template/click-events-have-key-events': 'error',
            '@angular-eslint/template/mouse-events-have-key-events': 'error',
            // Éléments interactifs accessibles au focus (RGAA 12.13)
            '@angular-eslint/template/interactive-supports-focus': 'error',
            // Structure : pas d'éléments dupliqués (RGAA 8.2)
            '@angular-eslint/template/no-duplicate-attributes': 'error',
            // Table : en-têtes accessibles (RGAA 5.7)
            '@angular-eslint/template/table-scope': 'error',
            // Autocomplétion sur les champs utilisateur (RGAA 11.13)
            '@angular-eslint/template/no-autofocus': 'warn',
            // Icônes : aria-hidden="true" (décoratif) ou aria-label (informatif) (RGAA 1.1)
            'rudi/mat-icon-requires-aria': 'warn',
            // Champs de formulaire : étiquette obligatoire (RGAA 11.1)
            'rudi/form-field-requires-label': 'warn',
            // Tabindex positif interdit (RGAA 11.11)
            'rudi/no-positive-tabindex': 'warn',
            // Boutons : intitulé accessible obligatoire (RGAA 11.9 / 11.12)
            'rudi/form-button-requires-name': 'warn',
            // Champs personnels : autocomplete recommandé (RGAA 11.13)
            'rudi/autocomplete-on-personal-fields': 'warn',
            // Hiérarchie des titres cohérente (RGAA 9.1)
            'rudi/heading-hierarchy': 'warn',
            // Structure des listes valide (RGAA 9.3)
            'rudi/list-structure': 'warn',
        }
    }

];

