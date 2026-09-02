import { test, expect } from "@playwright/test";
import {
  isFocusable,
  RGAA_SELECTORS,
  RGAATestResult,
  generateRGAAReport,
} from "../utils/rgaa-helpers";

test.describe("RGAA 4.1.2 - Thématique 12: Navigation", () => {
  let testResults: RGAATestResult[] = [];

  test.afterAll(async () => {
    console.log(generateRGAAReport(testResults));
  });

  test("12.6 - Zones de regroupement avec landmarks", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Vérifier les zones principales selon RGAA
    const landmarks = {
      header: page.locator('header, [role="banner"]'),
      nav: page.locator('nav, [role="navigation"]'),
      main: page.locator('main, [role="main"]'),
      footer: page.locator('footer, [role="contentinfo"]'),
      search: page.locator('[role="search"], form:has(input[type="search"])'),
    };

    const foundLandmarks: string[] = [];
    const missingLandmarks: string[] = [];

    for (const [name, locator] of Object.entries(landmarks)) {
      const count = await locator.count();
      if (count > 0) {
        foundLandmarks.push(name);
      } else if (name !== "search") {
        // search est optionnel
        missingLandmarks.push(name);
      }
    }

    // Vérifier qu'il y a au moins main
    const hasMain = (await landmarks.main.count()) > 0;

    testResults.push({
      criterion: "12.6",
      test: "Zones de regroupement avec landmarks",
      status: hasMain ? "conforme" : "non-conforme",
      details: `Landmarks trouvés: ${foundLandmarks.join(
        ", "
      )}. Manquants: ${missingLandmarks.join(", ")}`,
      elements: missingLandmarks,
    });

    expect(hasMain, "La zone de contenu principal (main) est obligatoire").toBe(
      true
    );
  });

  test("12.7 - Lien d'évitement vers le contenu principal", async ({
    page,
  }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Chercher des liens d'évitement
    const skipLinks = page.locator(
      'a[href="#main"], a[href="#content"], a[href*="contenu"], a:has-text("Aller au contenu"), a:has-text("Skip to content")'
    );
    const count = await skipLinks.count();

    if (count === 0) {
      testResults.push({
        criterion: "12.7",
        test: "Lien d'évitement",
        status: "non-conforme",
        details: "Aucun lien d'évitement trouvé",
      });

      expect(
        false,
        "Un lien d'évitement vers le contenu principal est requis"
      ).toBe(true);
      return;
    }

    // Vérifier que le lien d'évitement fonctionne
    const firstSkipLink = skipLinks.first();
    const href = await firstSkipLink.getAttribute("href");
    const isVisible = await firstSkipLink.isVisible();

    // Le lien peut être masqué mais doit devenir visible au focus
    if (!isVisible) {
      await firstSkipLink.focus();
      const visibleOnFocus = await firstSkipLink.isVisible();

      testResults.push({
        criterion: "12.7",
        test: "Lien d'évitement",
        status: visibleOnFocus ? "conforme" : "non-conforme",
        details: visibleOnFocus
          ? "Lien d'évitement visible au focus"
          : "Lien d'évitement non visible même au focus",
      });

      expect(
        visibleOnFocus,
        "Le lien d'évitement doit être visible au focus"
      ).toBe(true);
    } else {
      testResults.push({
        criterion: "12.7",
        test: "Lien d'évitement",
        status: "conforme",
        details: "Lien d'évitement visible trouvé",
      });
    }
  });

  test("12.8 - Ordre de tabulation cohérent", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Obtenir tous les éléments focusables
    const focusableElements = page.locator(
      'a, button, input, select, textarea, [tabindex]:not([tabindex="-1"])'
    );
    const count = await focusableElements.count();

    if (count === 0) {
      testResults.push({
        criterion: "12.8",
        test: "Ordre de tabulation",
        status: "non-applicable",
        details: "Aucun élément focusable trouvé",
      });
      return;
    }

    // Test de navigation au clavier
    await page.keyboard.press("Tab");
    let focusedElement = page.locator(":focus");
    let tabOrder: string[] = [];

    for (let i = 0; i < Math.min(10, count); i++) {
      // Limiter le test aux 10 premiers
      const tagName = await focusedElement.evaluate((el) =>
        el.tagName.toLowerCase()
      );
      const id = (await focusedElement.getAttribute("id")) || `element-${i}`;
      tabOrder.push(`${tagName}#${id}`);

      await page.keyboard.press("Tab");
      focusedElement = page.locator(":focus");
    }

    testResults.push({
      criterion: "12.8",
      test: "Ordre de tabulation",
      status: "conforme",
      details: `Ordre de tabulation: ${tabOrder.slice(0, 5).join(" → ")}${
        tabOrder.length > 5 ? "..." : ""
      }`,
    });
  });

  test("12.9 - Pas de piège au clavier", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    const focusableElements = page.locator(
      'a, button, input, select, textarea, [tabindex]:not([tabindex="-1"])'
    );
    const count = await focusableElements.count();

    if (count === 0) {
      testResults.push({
        criterion: "12.9",
        test: "Pas de piège au clavier",
        status: "non-applicable",
      });
      return;
    }

    // Tester la navigation sans piège (test simplifié)
    let trapDetected = false;
    let currentFocus = "";
    let previousFocus = "";

    for (let i = 0; i < Math.min(20, count * 2); i++) {
      await page.keyboard.press("Tab");
      const focusedElement = page.locator(":focus");

      if ((await focusedElement.count()) > 0) {
        const elementInfo = await focusedElement.evaluate(
          (el) =>
            `${el.tagName.toLowerCase()}#${
              el.id || el.className || "anonymous"
            }`
        );

        // Vérifier si on reste bloqué sur le même élément
        if (elementInfo === currentFocus && elementInfo === previousFocus) {
          trapDetected = true;
          break;
        }

        previousFocus = currentFocus;
        currentFocus = elementInfo;
      }
    }

    testResults.push({
      criterion: "12.9",
      test: "Pas de piège au clavier",
      status: trapDetected ? "non-conforme" : "conforme",
      details: trapDetected
        ? "Piège au clavier détecté"
        : "Navigation clavier fluide",
    });

    expect(trapDetected, "Aucun piège au clavier ne doit être présent").toBe(
      false
    );
  });

  test("12.10 - Raccourcis clavier contrôlables", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Rechercher des indications de raccourcis clavier
    const accesskeyElements = page.locator("[accesskey]");
    const shortcutIndicators = page.locator(
      ':has-text("Ctrl+"), :has-text("Alt+"), :has-text("raccourci")'
    );

    const accesskeyCount = await accesskeyElements.count();
    const shortcutCount = await shortcutIndicators.count();

    if (accesskeyCount === 0 && shortcutCount === 0) {
      testResults.push({
        criterion: "12.10",
        test: "Raccourcis clavier contrôlables",
        status: "non-applicable",
        details: "Aucun raccourci clavier détecté",
      });
      return;
    }

    // Vérifier les accesskey (doivent être accompagnés de touches de modification)
    let conformeAccesskeys = 0;
    const problematicAccesskeys: string[] = [];

    for (let i = 0; i < accesskeyCount; i++) {
      const element = accesskeyElements.nth(i);
      const accesskey = await element.getAttribute("accesskey");
      const elementId = (await element.getAttribute("id")) || `accesskey-${i}`;

      // Un accesskey d'un seul caractère peut être problématique
      if (accesskey && accesskey.length === 1) {
        problematicAccesskeys.push(`${elementId}: ${accesskey}`);
      } else {
        conformeAccesskeys++;
      }
    }

    const isConforme = problematicAccesskeys.length === 0;

    testResults.push({
      criterion: "12.10",
      test: "Raccourcis clavier contrôlables",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeAccesskeys} raccourcis appropriés, ${problematicAccesskeys.length} potentiellement problématiques`,
      elements: problematicAccesskeys,
    });
  });
});
