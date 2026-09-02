/* global process, console */
import { test, expect } from '@playwright/test';
import {
  checkFieldLabel,
  checkRequiredIndication,
  RGAA_SELECTORS,
  RGAATestResult,
  generateRGAAReport,
  getDescribedByTexts,
  checkFieldsetLegend,
  checkErrorAssociation,
  checkInputHelp,
  checkTabOrder,
  getAccessibleName,
  getButtonLabelSources,
  getButtonVisibleLabel,
  isLabelPertinent,
  isSelectLabelPertinent,
  gotoAuthenticated,
  describeField,
} from '../utils/rgaa-helpers';
import type { Locator, Page } from '@playwright/test';

async function collectDuplicateButtonNames(page: Page): Promise<string[]> {
  const duplicateNames: string[] = [];
  const forms = page.locator('form');
  const formCount = await forms.count();
  for (let fi = 0; fi < formCount; fi++) {
    const form = forms.nth(fi);
    const formButtons = form.locator(
      'button, input[type="submit"], input[type="reset"], input[type="button"], input[type="image"], [role="button"]',
    );
    const fbCount = await formButtons.count();
    const names: string[] = [];
    for (let j = 0; j < fbCount; j++) {
      const name = (await getAccessibleName(formButtons.nth(j))) || '';
      if (name.trim().length > 0) names.push(name.trim().toLowerCase());
    }
    const seen = new Map<string, number>();
    for (const n of names) {
      seen.set(n, (seen.get(n) || 0) + 1);
    }
    for (const [n, c] of seen.entries()) {
      if (c > 1) duplicateNames.push(`form-${fi}:${n}:${c}x`);
    }
  }
  return duplicateNames;
}

async function checkButtonLabelCoherence(buttonsInForms: Locator, count: number) {
  let coherentCount = 0;
  const mismatches: string[] = [];
  for (let i = 0; i < count; i++) {
    const btn = buttonsInForms.nth(i);
    const visible = (await getButtonVisibleLabel(btn)) || '';
    if (!visible) continue;
    const acc = (await getAccessibleName(btn)) || '';
    const id =
      (await btn.getAttribute('id')) ||
      (await btn.getAttribute('name')) ||
      `btn-${i}`;
    const contains = acc.toLowerCase().includes(visible.toLowerCase());
    if (contains) {
      coherentCount++;
    } else {
      mismatches.push(`${id}: acc='${acc}' vs vis='${visible}'`);
    }
  }
  return { coherentCount, mismatches };
}

test.describe('RGAA 4.1.2 - ThÃ©matique 11: Formulaires', () => {
  let testResults: RGAATestResult[] = [];

  const targetUrl = process.env.TARGET_URL || '/';

  /**
   * Navigue vers la page cible. Si la page nÃ©cessite une authentification
   * (TARGET_URL dÃ©finie sur une route protÃ©gÃ©e), injecte les tokens JWT
   * dans le sessionStorage avant de naviguer.
   */
  async function navigateToTarget(page: Page): Promise<void> {
    if (targetUrl === '/') {
      await page.goto('/');
    } else {
      await gotoAuthenticated(page, targetUrl);
    }
  }

  test.afterAll(async () => {
    console.log(generateRGAAReport(testResults));
  });

  test("11.9.1 - Pertinence de l'intitulÃ© des boutons (sources ARIA/HTML)", async ({
    page,
  }) => {
    await navigateToTarget(page);

    const buttonsInForms = page.locator(
      'form button, form input[type="submit"], form input[type="reset"], form input[type="button"], form input[type="image"], form [role="button"]',
    );
    const count = await buttonsInForms.count();
    if (count === 0) {
      testResults.push({
        criterion: '11.9.1',
        test: "Pertinence de l'intitulÃ© (sources)",
        status: 'non-applicable',
        details: 'Aucun bouton dans un formulaire',
      });
      return;
    }

    let pertinentCount = 0;
    const nonPertinent: string[] = [];

    for (let i = 0; i < count; i++) {
      const btn = buttonsInForms.nth(i);
      const id =
        (await btn.getAttribute('id')) ||
        (await getAccessibleName(btn)) ||
        `btn-${i}`;
      const sources = await getButtonLabelSources(btn);
      const values = Object.values(sources);
      // Au moins une des sources prÃ©sentes doit Ãªtre pertinente
      const anyPertinent = values.some(
        (v) => v && v.trim().length > 0 && isLabelPertinent(v),
      );
      if (anyPertinent) {
        pertinentCount++;
      } else {
        nonPertinent.push(id);
      }
    }

    testResults.push({
      criterion: '11.9.1',
      test: "Pertinence de l'intitulÃ© (sources)",
      status: nonPertinent.length === 0 ? 'conforme' : 'non-conforme',
      details: `${pertinentCount}/${count} boutons avec intitulÃ© pertinent (aria-label/labelledby/value/text/alt/title)`,
      elements: nonPertinent,
    });
  });

  test('11.9.2 - CohÃ©rence nom accessible â‰¥ intitulÃ© visible', async ({
    page,
  }) => {
    await navigateToTarget(page);

    const buttonsInForms = page.locator(
      'form button, form input[type="submit"], form input[type="reset"], form input[type="button"], form input[type="image"], form [role="button"]',
    );
    const count = await buttonsInForms.count();
    if (count === 0) {
      testResults.push({
        criterion: '11.9.2',
        test: 'Nom accessible contient intitulÃ© visible',
        status: 'non-applicable',
        details: 'Aucun bouton dans un formulaire',
      });
      return;
    }

    const duplicateNames = await collectDuplicateButtonNames(page);
    const { coherentCount, mismatches } = await checkButtonLabelCoherence(buttonsInForms, count);

    testResults.push({
      criterion: '11.9.2',
      test: 'Nom accessible contient intitulÃ© visible',
      status: mismatches.length === 0 ? 'conforme' : 'non-conforme',
      details: `${coherentCount}/${count} boutons cohÃ©rents; doublons: ${duplicateNames.join(', ') || 'aucun'}`,
      elements: mismatches,
    });
  });

  test('11.1 - Champs de formulaire avec Ã©tiquettes', async ({ page }) => {
    await navigateToTarget(page);

    const formFields = page.locator(RGAA_SELECTORS.formFields);
    const count = await formFields.count();

    if (count === 0) {
      testResults.push({
        criterion: '11.1',
        test: 'Champs avec Ã©tiquettes',
        status: 'non-applicable',
        details: 'Aucun champ de formulaire trouvÃ©',
      });
      return;
    }

    let conformeCount = 0;
    const nonConformeElements: string[] = [];

    for (let i = 0; i < count; i++) {
      const field = formFields.nth(i);

      const labelInfo = await checkFieldLabel(field);

      if (labelInfo.hasLabel && labelInfo.value.trim().length > 0) {
        conformeCount++;
      } else {
        nonConformeElements.push(await describeField(field, i));
      }
    }

    const isConforme = nonConformeElements.length === 0;

    testResults.push({
      criterion: '11.1',
      test: 'Champs avec Ã©tiquettes',
      status: isConforme ? 'conforme' : 'non-conforme',
      details: `${conformeCount}/${count} champs avec Ã©tiquettes valides`,
      elements: nonConformeElements,
    });

    expect(
      nonConformeElements.length,
      `${
        nonConformeElements.length
      } champ(s) sans Ã©tiquette: ${nonConformeElements.join(', ')}`,
    ).toBe(0);
  });

  test('11.1 (select) - Listes dÃ©roulantes avec Ã©tiquette associÃ©e', async ({
    page,
  }) => {
    await navigateToTarget(page);

    const selects = page.locator('select');
    const count = await selects.count();

    if (count === 0) {
      testResults.push({
        criterion: '11.1',
        test: 'Ã‰tiquette des listes dÃ©roulantes',
        status: 'non-applicable',
        details: 'Aucun menu dÃ©roulant (select) trouvÃ©',
      });
      return;
    }

    let labelled = 0;
    const unlabelled: string[] = [];

    for (let i = 0; i < count; i++) {
      const sel = selects.nth(i);
      const id = (await sel.getAttribute('id')) || `select-${i}`;
      const info = await checkFieldLabel(sel);
      if (info.hasLabel && info.value.trim().length > 0) {
        labelled++;
      } else {
        unlabelled.push(id);
      }
    }

    testResults.push({
      criterion: '11.1',
      test: 'Ã‰tiquette des listes dÃ©roulantes',
      status: unlabelled.length === 0 ? 'conforme' : 'non-conforme',
      details: `${labelled}/${count} listes dÃ©roulantes avec Ã©tiquette associÃ©e`,
      elements: unlabelled,
    });
  });

  test("11.2 (select) - Pertinence de l'Ã©tiquette du menu dÃ©roulant", async ({
    page,
  }) => {
    await navigateToTarget(page);

    const selects = page.locator('select');
    const count = await selects.count();

    if (count === 0) {
      testResults.push({
        criterion: '11.2',
        test: "Pertinence de l'Ã©tiquette du menu dÃ©roulant",
        status: 'non-applicable',
        details: 'Aucun menu dÃ©roulant (select) trouvÃ©',
      });
      return;
    }

    let pertinent = 0;
    const nonPertinent: string[] = [];

    for (let i = 0; i < count; i++) {
      const sel = selects.nth(i);
      const id = (await sel.getAttribute('id')) || `select-${i}`;
      const info = await checkFieldLabel(sel);
      if (info.hasLabel && info.value.trim().length > 0) {
        const ok = isSelectLabelPertinent(info.value);
        if (ok) {
          pertinent++;
        } else {
          nonPertinent.push(`${id}: "${info.value}"`);
        }
      }
    }

    testResults.push({
      criterion: '11.2',
      test: "Pertinence de l'Ã©tiquette du menu dÃ©roulant",
      status: nonPertinent.length === 0 ? 'conforme' : 'non-conforme',
      details: `${pertinent}/${count} menus dÃ©roulants avec Ã©tiquette pertinente (ex: "Choisissez une catÃ©gorie :")`,
      elements: nonPertinent,
    });
  });

  test('11.2 - Pertinence des Ã©tiquettes', async ({ page }) => {
    await navigateToTarget(page);

    const formFields = page.locator(RGAA_SELECTORS.formFields);
    const count = await formFields.count();

    if (count === 0) {
      testResults.push({
        criterion: '11.2',
        test: 'Pertinence des Ã©tiquettes',
        status: 'non-applicable',
      });
      return;
    }

    let conformeCount = 0;
    const problematicElements: string[] = [];

    for (let i = 0; i < count; i++) {
      const field = formFields.nth(i);

      const labelInfo = await checkFieldLabel(field);

      if (labelInfo.hasLabel) {
        const label = labelInfo.value.toLowerCase();

        // VÃ©rifications de base pour la pertinence
        const isPertinent =
          label.length >= 2 &&
          !label.includes('champ') &&
          !label.includes('input') &&
          !label.includes('saisir');

        if (isPertinent) {
          conformeCount++;
        } else {
          const desc = await describeField(field, i);
          problematicElements.push(`${desc} — label: "${labelInfo.value}"`);
        }
      }
    }

    testResults.push({
      criterion: '11.2',
      test: 'Pertinence des Ã©tiquettes',
      status: problematicElements.length === 0 ? 'conforme' : 'non-conforme',
      details: `${conformeCount} Ã©tiquettes pertinentes analysÃ©es`,
      elements: problematicElements,
    });
  });

  test('11.3 - Aide Ã  la saisie et indications associÃ©es (aria-describedby)', async ({
    page,
  }) => {
    await navigateToTarget(page);

    const formFields = page.locator(RGAA_SELECTORS.formFields);
    const count = await formFields.count();

    if (count === 0) {
      testResults.push({
        criterion: '11.3',
        test: 'Aide associÃ©e aux champs',
        status: 'non-applicable',
        details: 'Aucun champ de formulaire trouvÃ©',
      });
      return;
    }

    let conformeCount = 0;
    const withoutHelp: string[] = [];

    for (let i = 0; i < count; i++) {
      const field = formFields.nth(i);
      const help = await checkInputHelp(field);
      if (help.hasHelp) {
        conformeCount++;
      } else {
        withoutHelp.push(await describeField(field, i));
      }
    }

    testResults.push({
      criterion: '11.3',
      test: 'Aide associÃ©e aux champs',
      status: conformeCount > 0 ? 'conforme' : 'non-conforme',
      details: `${conformeCount}/${count} champs avec aide (aria-describedby/placeholder/title)`,
      elements: withoutHelp,
    });
  });

  test('11.4 - LÃ©gende de groupe (fieldset/legend ou aria-labelledby)', async ({
    page,
  }) => {
    await navigateToTarget(page);

    const radios = page.locator('input[type="radio"]');
    const checkboxes = page.locator('input[type="checkbox"]');
    const count = (await radios.count()) + (await checkboxes.count());

    if (count === 0) {
      testResults.push({
        criterion: '11.4',
        test: 'LÃ©gende de groupe',
        status: 'non-applicable',
        details: 'Aucun champ radio/checkbox',
      });
      return;
    }

    let groupsWithLegend = 0;
    const missingLegend: string[] = [];

    // VÃ©rifier sur quelques champs (Ã©chantillon)
    for (const f of [radios.first(), checkboxes.first()]) {
      if ((await f.count()) === 0) continue;
      const info = await checkFieldsetLegend(f);
      if (info.hasGroup && info.hasLegend) {
        groupsWithLegend++;
      } else if (info.hasGroup && !info.hasLegend) {
        const id =
          (await f.getAttribute('name')) ||
          (await f.getAttribute('id')) ||
          'group';
        missingLegend.push(id);
      }
    }

    testResults.push({
      criterion: '11.4',
      test: 'LÃ©gende de groupe',
      status: missingLegend.length === 0 ? 'conforme' : 'non-conforme',
      details: `${groupsWithLegend} groupe(s) avec lÃ©gende ou aria-labelledby`,
      elements: missingLegend,
    });
  });

  test("11.6 - Association des messages d'erreur aux champs", async ({
    page,
  }) => {
    await navigateToTarget(page);

    const invalidFields = page.locator('[aria-invalid="true"], :invalid');
    const count = await invalidFields.count();

    if (count === 0) {
      testResults.push({
        criterion: '11.6',
        test: "Association messages d'erreur",
        status: 'non-applicable',
        details: 'Aucun champ invalide/aria-invalid detectÃ©',
      });
      return;
    }

    let associated = 0;
    const notAssociated: string[] = [];

    for (let i = 0; i < count; i++) {
      const field = invalidFields.nth(i);
      const assoc = await checkErrorAssociation(field);
      if (assoc.hasErrorAssoc) {
        associated++;
      } else {
        notAssociated.push(await describeField(field, i));
      }
    }

    testResults.push({
      criterion: '11.6',
      test: "Association messages d'erreur",
      status: notAssociated.length === 0 ? 'conforme' : 'non-conforme',
      details: `${associated}/${count} champs avec erreurs associÃ©es (aria-describedby/alert)`,
      elements: notAssociated,
    });
  });

  test('11.7 - Annonce des erreurs (aria-live/role=alert)', async ({
    page,
  }) => {
    await navigateToTarget(page);
    const alerts = page.locator('[role="alert"], [aria-live]');
    const count = await alerts.count();
    testResults.push({
      criterion: '11.7',
      test: 'Annonce des erreurs',
      status: count > 0 ? 'conforme' : 'non-conforme',
      details:
        count > 0
          ? `${count} rÃ©gion(s) d'alerte ou live`
          : "Aucune rÃ©gion d'annonce trouvÃ©e",
    });
  });

  test('11.8 - Indication du format attendu (pattern/type/help)', async ({
    page,
  }) => {
    await navigateToTarget(page);
    const candidates = page.locator(
      'input[type="email"], input[type="tel"], input[type="date"], input[pattern]',
    );
    const count = await candidates.count();
    if (count === 0) {
      testResults.push({
        criterion: '11.8',
        test: 'Format attendu',
        status: 'non-applicable',
        details: 'Aucun champ formatÃ© (email/tel/date/pattern)',
      });
      return;
    }
    let indicated = 0;
    const missing: string[] = [];
    for (let i = 0; i < count; i++) {
      const field = candidates.nth(i);
      const described = await getDescribedByTexts(field);
      const placeholder = await field.getAttribute('placeholder');
      const pattern = await field.getAttribute('pattern');
      if (
        described.length > 0 ||
        (placeholder && placeholder.trim().length > 0) ||
        !!pattern
      ) {
        indicated++;
      } else {
        missing.push(await describeField(field, i));
      }
    }
    testResults.push({
      criterion: '11.8',
      test: 'Format attendu',
      status: missing.length === 0 ? 'conforme' : 'non-conforme',
      details: `${indicated}/${count} champs avec indication de format (help/placeholder/pattern)`,
      elements: missing,
    });
  });

  test('11.10 - ContrÃ´le de saisie des champs obligatoires', async ({
    page,
  }) => {
    await navigateToTarget(page);

    const requiredFields = page.locator(
      'input[required], input[aria-required="true"], select[required], textarea[required]',
    );
    const count = await requiredFields.count();

    if (count === 0) {
      testResults.push({
        criterion: '11.10',
        test: 'ContrÃ´le de saisie obligatoire',
        status: 'non-applicable',
        details: 'Aucun champ obligatoire trouvÃ©',
      });
      return;
    }

    let conformeCount = 0;
    const nonConformeElements: string[] = [];

    for (let i = 0; i < count; i++) {
      const field = requiredFields.nth(i);

      // VÃ©rifier indication visuelle AVANT validation
      const hasVisualIndication = await checkRequiredIndication(field);

      if (hasVisualIndication) {
        conformeCount++;
      } else {
        nonConformeElements.push(await describeField(field, i));
      }
    }

    const isConforme = nonConformeElements.length === 0;

    testResults.push({
      criterion: '11.10',
      test: 'ContrÃ´le de saisie obligatoire',
      status: isConforme ? 'conforme' : 'non-conforme',
      details: `${conformeCount}/${count} champs obligatoires avec indication visuelle`,
      elements: nonConformeElements,
    });

    expect(
      nonConformeElements.length,
      `${
        nonConformeElements.length
      } champ(s) obligatoire(s) sans indication: ${nonConformeElements.join(
        ', ',
      )}`,
    ).toBe(0);
  });

  test('11.5 - Regroupement des champs de mÃªme nature', async ({ page }) => {
    await navigateToTarget(page);

    const radioFields = page.locator('input[type="radio"]');
    const checkboxFields = page.locator('input[type="checkbox"]');

    const radioCount = await radioFields.count();
    const checkboxCount = await checkboxFields.count();

    if (radioCount === 0 && checkboxCount === 0) {
      testResults.push({
        criterion: '11.5',
        test: 'Regroupement des champs',
        status: 'non-applicable',
        details: 'Aucun groupe de champs dÃ©tectÃ©',
      });
      return;
    }

    // Pour ce test, on vÃ©rifie que s'il y a plusieurs boutons radio avec le mÃªme name,
    // ils sont dans un groupe appropriÃ©
    const radioNames = new Set<string>();
    for (let i = 0; i < radioCount; i++) {
      const name = await radioFields.nth(i).getAttribute('name');
      if (name) radioNames.add(name);
    }

    let properlyGrouped = 0;
    const ungroupedElements: string[] = [];

    for (const name of radioNames) {
      const sameNameRadios = page.locator(
        `input[type="radio"][name="${name}"]`,
      );
      const count = await sameNameRadios.count();

      if (count > 1) {
        // VÃ©rifier si dans un fieldset ou groupe WAI-ARIA
        const firstRadio = sameNameRadios.first();
        const fieldset = firstRadio.locator('xpath=ancestor::fieldset');
        const ariaGroup = firstRadio.locator(
          'xpath=ancestor::*[@role="group" or @role="radiogroup"]',
        );

        const isGrouped =
          (await fieldset.count()) > 0 || (await ariaGroup.count()) > 0;

        if (isGrouped) {
          properlyGrouped++;
        } else {
          ungroupedElements.push(`radio-group-${name}`);
        }
      }
    }

    const isConforme = ungroupedElements.length === 0;

    testResults.push({
      criterion: '11.5',
      test: 'Regroupement des champs',
      status: isConforme ? 'conforme' : 'non-conforme',
      details: `${properlyGrouped} groupe(s) de champs correctement structurÃ©s`,
      elements: ungroupedElements,
    });
  });

  test('11.11 - Navigation au clavier cohÃ©rente (tabindex)', async ({
    page,
  }) => {
    await navigateToTarget(page);
    const tab = await checkTabOrder(page);
    testResults.push({
      criterion: '11.11',
      test: 'Tabulation cohÃ©rente',
      status: tab.isValid ? 'conforme' : 'non-conforme',
      details: tab.isValid ? 'Aucun tabindex positif' : tab.issues.join(' | '),
    });
  });

  test('11.12 - Boutons de soumission accessibles', async ({ page }) => {
    await navigateToTarget(page);
    const forms = page.locator('form');
    const formCount = await forms.count();
    if (formCount === 0) {
      testResults.push({
        criterion: '11.12',
        test: 'Boutons de soumission',
        status: 'non-applicable',
        details: 'Aucun formulaire dÃ©tectÃ©',
      });
      return;
    }
    let accessibleSubmits = 0;
    const missingSubmitNames: string[] = [];
    for (let i = 0; i < formCount; i++) {
      const form = forms.nth(i);
      const submits = form.locator(
        'button[type="submit"], input[type="submit"], [role="button"]',
      );
      const submitCount = await submits.count();
      if (submitCount === 0) {
        missingSubmitNames.push(`form-${i}`);
        continue;
      }
      const submit = submits.first();
      const name = await getAccessibleName(submit);
      if (name && name.trim().length > 0) {
        accessibleSubmits++;
      } else {
        const id = (await submit.getAttribute('id')) || `submit-${i}`;
        missingSubmitNames.push(id);
      }
    }
    testResults.push({
      criterion: '11.12',
      test: 'Boutons de soumission',
      status: missingSubmitNames.length === 0 ? 'conforme' : 'non-conforme',
      details: `${accessibleSubmits}/${formCount} formulaires avec bouton de soumission nommÃ©`,
      elements: missingSubmitNames,
    });
  });

  test('11.13 - Attributs autocomplete pour faciliter le remplissage', async ({
    page,
  }) => {
    await navigateToTarget(page);

    // Champs concernant l'utilisateur selon WCAG
    const personalFields = page.locator(
      'input[name*="email"], input[type="email"], input[name*="name"], input[name*="phone"], input[name*="address"]',
    );
    const count = await personalFields.count();

    if (count === 0) {
      testResults.push({
        criterion: '11.13',
        test: 'Attributs autocomplete',
        status: 'non-applicable',
        details: "Aucun champ d'information personnelle dÃ©tectÃ©",
      });
      return;
    }

    let conformeCount = 0;
    const nonConformeElements: string[] = [];

    // Valeurs autocomplete valides selon la spec HTML5.2
    const validAutocompleteValues = new Set([
      'name',
      'email',
      'username',
      'new-password',
      'current-password',
      'tel',
      'address-line1',
      'address-line2',
      'country',
      'postal-code',
      'given-name',
      'family-name',
      'organization',
      'street-address',
      'locality',
      'region',
    ]);

    for (let i = 0; i < count; i++) {
      const field = personalFields.nth(i);
      const autocomplete = await field.getAttribute('autocomplete');
      const name =
        (await field.getAttribute('name')) ||
        (await field.getAttribute('id')) ||
        `personal-field-${i}`;

      if (autocomplete && validAutocompleteValues.has(autocomplete)) {
        conformeCount++;
      } else {
        nonConformeElements.push(name);
      }
    }

    // Ce test est informatif - pas obligatoirement bloquant
    testResults.push({
      criterion: '11.13',
      test: 'Attributs autocomplete',
      status: conformeCount > 0 ? 'conforme' : 'non-conforme',
      details: `${conformeCount}/${count} champs avec autocomplete appropriÃ©`,
      elements: nonConformeElements,
    });
  });
});
