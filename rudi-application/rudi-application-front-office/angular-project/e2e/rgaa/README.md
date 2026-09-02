# Tests RGAA v4.1.2 avec Playwright

Tests automatisés d'accessibilité numérique conformes au Référentiel Général d'Amélioration de l'Accessibilité (RGAA) version 4.1.2.

## Structure des tests

Les tests sont organisés par thématiques RGAA :

- **Thématique 1** : Images (`1-images.spec.ts`)
- **Thématique 2** : Cadres (`2-cadres.spec.ts`)
- **Thématique 3** : Couleurs (`colors.spec.ts`, `3-couleurs.spec.ts`)
- **Thématique 4** : Multimédia (`4-multimedia.spec.ts`)
- **Thématique 5** : Tableaux (`5-tableaux.spec.ts`)
- **Thématique 6** : Liens (`6-liens.spec.ts`)
- **Thématique 7** : Scripts (`7-scripts.spec.ts`, `scripts.spec.ts`)
- **Thématique 8** : Éléments obligatoires (`8-elements-obligatoires.spec.ts`, `mandatory.spec.ts`)
- **Thématique 9** : Structuration (`9-structuration.spec.ts`, `structure.spec.ts`)
- **Thématique 10** : Présentation (`10-presentation.spec.ts`)
- **Thématique 11** : Formulaires (`11-formulaires.spec.ts`, `forms.spec.ts`)
- **Thématique 12** : Navigation (`12-navigation.spec.ts`, `navigation.spec.ts`)
- **Thématique 13** : Consultation (`13-consultation.spec.ts`, `consultation.spec.ts`)

## Environnement de test RGAA

Conformément au RGAA v4.1.2, les tests sont exécutés sur :

- **Chromium** (Chrome/Edge)
- **Firefox**
- **WebKit** (Safari)

Technologies d'assistance simulées :

- Lecteur d'écran
- Navigation clavier
- Contrôle vocal

## Utilisation

### Exécuter tous les tests RGAA

```bash
npx playwright test tests/rgaa/
```

### Exécuter une thématique spécifique

```bash
# Images
npx playwright test tests/rgaa/1-images.spec.ts

# Formulaires
npx playwright test tests/rgaa/11-formulaires.spec.ts

# Navigation
npx playwright test tests/rgaa/12-navigation.spec.ts
```

### Exécuter avec un navigateur spécifique

```bash
npx playwright test --project=chromium
npx playwright test --project=firefox
npx playwright test --project=webkit
```

### Filtrer par critère

```bash
# Tester uniquement les images
npx playwright test --grep "Images RGAA"

# Tester uniquement les contrastes
npx playwright test --grep "Contraste"
```

### Générer un rapport HTML

```bash
npx playwright test --reporter=html
npx playwright show-report
```

### Générer un rapport RGAA

```bash
npm run generate-report
```

## Critères techniques

### Images (Thématique 1)

- **1.1.1** : Alternatives textuelles selon hiérarchie RGAA
- **1.2** : Images de décoration correctement ignorées
- **1.3** : Pertinence des alternatives
- **1.4** : CAPTCHA avec alternatives
- **1.5** : Alternatives d'accès aux CAPTCHA

### Couleurs (Thématique 3)

- **3.1** : Information non donnée uniquement par la couleur
- **3.2** : Contraste minimum 4.5:1 pour texte normal
- **3.2.1** : Contraste minimum 3:1 pour texte agrandi
- **3.3** : Contraste minimum 3:1 pour composants d'interface

### Scripts (Thématique 7)

- **7.1** : Composants compatibles avec technologies d'assistance
- **7.3** : Contrôle par le clavier
- **7.4** : Changements de contexte contrôlés
- **7.5** : Messages de statut restitués

### Formulaires (Thématique 11)

- **11.1** : Étiquettes de champs selon ordre RGAA
- **11.2** : Pertinence des étiquettes
- **11.10** : Contrôle de saisie des champs obligatoires

### Navigation (Thématique 12)

- **12.1** : Systèmes de navigation
- **12.6** : Liens d'évitement
- **12.7** : Ordre de tabulation cohérent

## Rapports conformes RGAA

Chaque fichier de test génère un rapport selon le format RGAA :

```typescript
interface RGAATestResult {
  criterion: string; // Numéro du critère (ex: "1.1.1")
  test: string; // Description du test
  status: "conforme" | "non-conforme" | "non-applicable";
  details?: string; // Détails du résultat
  elements?: string[]; // Éléments concernés
}
```

Le rapport affiche :

- Taux de conformité global
- Détail par critère
- Statistiques (conformes, non-conformes, non-applicables)

## Cas particuliers et dérogations RGAA

Le système gère automatiquement les cas particuliers :

- **Images de décoration** : ignorées dans les tests d'alternatives
- **CAPTCHA** : règles spécifiques d'alternatives
- **Contenus tiers** : marqués comme non applicables si nécessaire
- **Charge disproportionnée** : peut être documentée

## Intégration CI/CD

### GitHub Actions

```yaml
name: Audit RGAA

on: [push, pull_request]

jobs:
  rgaa-audit:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: "18"
      - run: npm ci
      - run: npx playwright install --with-deps
      - run: npx playwright test tests/rgaa/
      - name: Generate RGAA Report
        if: always()
        run: npm run generate-report
      - uses: actions/upload-artifact@v3
        if: always()
        with:
          name: rgaa-report
          path: reports/
```

### Seuil de conformité

Définir un seuil minimum dans vos tests :

```typescript
test("Vérification complète RGAA", async ({ page }) => {
  await page.goto("/");

  const auditResults = await runFullRGAAAudit(page);

  // Seuil de conformité minimum (configurable)
  const minConformityRate = 75; // 75%
  expect(auditResults.conformityRate).toBeGreaterThanOrEqual(minConformityRate);
});
```

## Bonnes pratiques

### Sélecteurs robustes

Les tests utilisent des sélecteurs sémantiques RGAA :

```typescript
const RGAA_SELECTORS = {
  landmarks:
    '[role="banner"], [role="navigation"], [role="main"], [role="contentinfo"]',
  headings: 'h1, h2, h3, h4, h5, h6, [role="heading"]',
  links: 'a[href], [role="link"]',
  buttons: 'button, input[type="button"], [role="button"]',
  formFields: 'input:not([type="hidden"]), select, textarea',
};
```

### Hiérarchie d'alternatives textuelles

Ordre de priorité selon RGAA :

1. `aria-labelledby`
2. `aria-label`
3. `alt` (pour `<img>`)
4. `title`

### Tests de restitution

Vérification de la compatibilité avec les technologies d'assistance :

- Navigation clavier (Tab, Shift+Tab, Enter, Espace)
- Rôles ARIA appropriés
- Noms accessibles
- États et propriétés ARIA

## Limitations

Ces tests automatisés couvrent les critères testables automatiquement. Selon le RGAA v4.1.2, certains critères nécessitent un audit manuel :

- Pertinence sémantique des contenus
- Qualité des transcriptions et audiodescriptions
- Cohérence de la navigation
- Compréhensibilité des messages d'erreur

**Les tests automatisés doivent être complétés par des audits manuels conformément aux exigences RGAA v4.1.2.**

## Ressources

- [RGAA v4.1.2](https://www.numerique.gouv.fr/publications/rgaa-accessibilite/)
- [Playwright Documentation](https://playwright.dev/)
- [WAI-ARIA Authoring Practices](https://www.w3.org/WAI/ARIA/apg/)
- [WCAG 2.1](https://www.w3.org/TR/WCAG21/)

## Support

Pour toute question concernant l'implémentation des tests RGAA :

- Consulter la documentation officielle RGAA
- Vérifier les exemples dans les fichiers de test
- Adapter les tests selon le contexte spécifique de votre site
