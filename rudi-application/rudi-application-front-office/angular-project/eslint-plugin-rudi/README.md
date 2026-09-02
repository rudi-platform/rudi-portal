# eslint-plugin-rudi — Linter d'accessibilité RGAA

Plugin ESLint **local** regroupant des règles custom d'accessibilité (RGAA) pour les
templates Angular du portail RUDI. Il complète les règles `@angular-eslint/template-*`
afin de **signaler au plus tôt** (dès l'écriture du code, dans l'IDE et en CI) les
manquements RGAA les plus fréquents.

> Ce linter statique est complémentaire des **tests d'accessibilité Playwright**
> (`e2e/rgaa/`, script `npm run test:rgaa`) : le linter analyse le **code source des
> templates** (analyse statique), les tests Playwright vérifient le **rendu réel** dans
> le navigateur (analyse dynamique).

---

## 1. Mise en place

Aucune installation supplémentaire : le plugin est un module local du projet Angular,
chargé par la configuration ESLint « flat config ».

### Structure

```
eslint-plugin-rudi/
├── index.js                 # point d'entrée : expose les règles
├── rules/                   # une règle par fichier
│   ├── mat-icon-requires-aria.js
│   ├── form-field-requires-label.js
│   ├── form-button-requires-name.js
│   ├── no-positive-tabindex.js
│   ├── autocomplete-on-personal-fields.js
│   ├── heading-hierarchy.js
│   └── list-structure.js
└── tests/                   # tests unitaires (RuleTester) — un fichier par règle
```

### Câblage (`eslint.config.js`)

Le plugin est enregistré sur les fichiers `*.html` (templates Angular), aux côtés du
parser de template `@angular-eslint/template-parser` :

```js
const rudiPlugin = require('./eslint-plugin-rudi');

module.exports = [
  {
    files: ['**/*.html'],
    plugins: {
      '@angular-eslint/template': angularTemplatePlugin,
      'rudi': rudiPlugin,
    },
    languageOptions: { parser: require('@angular-eslint/template-parser') },
    rules: {
      'rudi/mat-icon-requires-aria': 'warn',
      'rudi/form-field-requires-label': 'warn',
      // … (voir le fichier pour la liste complète)
    },
  },
];
```

---

## 2. Règles fournies

| Règle | Critère RGAA | Sévérité | Rôle |
|-------|--------------|----------|------|
| `rudi/mat-icon-requires-aria` | 1.1 | `warn` | Un `<mat-icon>` doit être décoratif (`aria-hidden="true"`) **ou** accessible (`aria-label`/`aria-labelledby`). Remonte les ancêtres `button`/`a` porteurs d'un nom accessible. |
| `rudi/form-field-requires-label` | 11.1 | `warn` | Un `<input>`/`<select>`/`<textarea>` doit avoir une étiquette (`aria-label`, `<label>` englobant, `<label for>` associé, `<mat-form-field>`, `title`). |
| `rudi/form-button-requires-name` | 11.9 / 11.12 | `warn` | Un bouton doit avoir un intitulé accessible (texte — y compris imbriqué —, `aria-label`, `value`, `alt`, `title`, `matTooltip`). Le ligature d'un `<mat-icon>` n'est **pas** compté comme texte. |
| `rudi/no-positive-tabindex` | 11.11 | `warn` | Interdit `tabindex` > 0 (perturbe l'ordre de tabulation). Seuls `0` et `-1` sont admis. |
| `rudi/autocomplete-on-personal-fields` | 11.13 | `warn` | Les champs d'information personnelle (email, tel, nom, adresse…) doivent porter un `autocomplete` valide. Détection par **tokens** (pas de faux positifs type « hotel » → tel). |
| `rudi/heading-hierarchy` | 9.1 | `warn` | Les niveaux de titres (`h1`–`h6`) ne doivent pas sauter de niveau (h1 → h3 interdit). |
| `rudi/list-structure` | 9.3 | `warn` | `<ul>`/`<ol>` ne contiennent que des `<li>` ; `<dl>` que des `<dt>`/`<dd>` (ou `role="list"`/`role="listitem"`). |

Règles `@angular-eslint/template-*` also activées en `error` (rappel) : `alt-text`,
`elements-content`, `valid-aria`, `role-has-required-aria`,
`label-has-associated-control`, `no-positive-tabindex`, `click-events-have-key-events`,
`interactive-supports-focus`, `table-scope`, etc. (voir `eslint.config.js`).

---

## 3. Utilisation

### Lancer le linter sur tout le projet

```bash
npm run lint            # = ng lint (analyse *.ts et *.html : RGAA + règles TS/style)
```

### Lancer UNIQUEMENT les règles RGAA

```bash
npm run lint:rgaa       # eslint --config eslint.rgaa.config.js "src/**/*.html"
```

Utilise la config dédiée `eslint.rgaa.config.js` qui n'active que les règles
d'accessibilité (`rudi/*` + `@angular-eslint/template/*` a11y), sur les templates
uniquement. Pour rendre les manquements bloquants (CI) : ajouter `-- --max-warnings=0`.

### Cibler un fichier / dossier

```bash
npx eslint src/app/features/home/**/*.html
npx eslint --config eslint.rgaa.config.js src/app/features/home/**/*.html   # RGAA seul
```

### Dans l'IDE (VS Code)

Avec l'extension **ESLint**, les manquements s'affichent en temps réel : soulignés
ondulés dans le template et entrées dans le panneau **Problems**. Aucune configuration
supplémentaire (la flat config est prise en compte automatiquement).

---

## 4. Résultats affichés

Chaque règle émet un message préfixé du critère RGAA concerné. Exemple de sortie
`npm run lint` :

```
/…/hero-section.component.html
  12:13  warning  RGAA : <mat-icon> sans aria-hidden="true" est considéré comme porteur
                  de sens. Ajoutez aria-label/aria-labelledby, ou marquez-le comme
                  décoratif avec aria-hidden="true".  rudi/mat-icon-requires-aria
  20:9   warning  RGAA 11.9/11.12 : Ce bouton n'a pas d'intitulé accessible…  rudi/form-button-requires-name

✖ 2 problems (0 errors, 2 warnings)
```

- **Sévérité** : les règles `rudi/*` sont en `warn` (signalement non bloquant, pour
  accompagner le dev). Les règles `@angular-eslint/template-*` critiques sont en `error`.
- **En CI** : `ng lint` remonte les warnings ; on peut échouer le job sur warnings via
  `eslint --max-warnings=0` si l'on souhaite rendre les règles RGAA bloquantes.
- **Dans l'IDE** : warning = souligné jaune ; error = souligné rouge.

---

## 5. Tests unitaires des règles

Chaque règle est couverte par des tests `RuleTester` (cas valides / invalides), basés sur
les exemples ✅/❌ documentés dans chaque règle.

```bash
npm run test:lint-rules      # node --test "eslint-plugin-rudi/tests/*.test.js"
```

Exemple de sortie :

```
✔ mat-icon-requires-aria (valid: 5, invalid: 2)
✔ form-button-requires-name (valid: 7, invalid: 5)
…
ℹ tests 62  ℹ pass 62  ℹ fail 0
```

Ces tests protègent contre les régressions et les faux positifs lors des évolutions des
règles. À lancer en CI avant tout merge touchant `eslint-plugin-rudi/`.

---

## 6. Ajouter une nouvelle règle

1. Créer `eslint-plugin-rudi/rules/<ma-regle>.js` (structure `meta` + `create`, avec un
   docblock listant le critère RGAA et des exemples ✅/❌).
2. L'enregistrer dans `eslint-plugin-rudi/index.js`.
3. L'activer dans `eslint.config.js` (bloc `files: ['**/*.html']`).
4. Ajouter `eslint-plugin-rudi/tests/<ma-regle>.test.js` (RuleTester) couvrant les cas
   valides/invalides.
5. Vérifier : `npm run test:lint-rules` puis `npm run lint`.

> Astuce : les nœuds de template exposent `attributes` (attributs statiques),
> `inputs` (bindings `[attr.x]`), `children`, `parent`, `name`, `type`. Les sélecteurs
> `Element$1[name="…"]` / `Element[name="…"]` couvrent les variantes d'AST
> `@angular-eslint`.

---

## 7. Complémentarité avec les tests Playwright RGAA

| | Linter (`eslint-plugin-rudi`) | Playwright (`e2e/rgaa/`) |
|---|---|---|
| Nature | Analyse statique du code | Analyse dynamique du rendu |
| Quand | À l'écriture / au commit / en CI | Sur l'app en fonctionnement (`localhost:4200`) |
| Détecte | Manquements dans les templates | Problèmes réels (contraste, focus, DOM final…) |
| Commande | `npm run lint` / `npm run test:lint-rules` | `npm run test:rgaa` / `npm run test:rgaa:report` |

Les deux approches sont **complémentaires** : le linter attrape tôt les erreurs
structurelles ; Playwright valide le comportement effectif sur les pages.
