const js = require('@eslint/js');
const tsParser = require('@typescript-eslint/parser');
const tsPlugin = require('@typescript-eslint/eslint-plugin');
const angularPlugin = require('@angular-eslint/eslint-plugin');
const angularTemplatePlugin = require('@angular-eslint/eslint-plugin-template');


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
            '@angular-eslint/template': angularTemplatePlugin
        },
        languageOptions: {
            parser: require('@angular-eslint/template-parser')
        },
        rules: {
            '@angular-eslint/template/banana-in-box': 'error',
            '@angular-eslint/template/no-negated-async': 'error'
        }
    }

];

