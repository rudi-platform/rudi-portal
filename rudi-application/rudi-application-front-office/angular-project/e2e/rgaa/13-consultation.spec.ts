import { test, expect } from "@playwright/test";
import {
  RGAA_SELECTORS,
  RGAATestResult,
  generateRGAAReport,
} from "../utils/rgaa-helpers";

test.describe("RGAA 4.1.2 - Thématique 13: Consultation", () => {
  let testResults: RGAATestResult[] = [];

  test.afterAll(async () => {
    console.log(generateRGAAReport(testResults));
  });

  test("13.1 - Limite de temps modifiable", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Détecter les limites de temps (meta refresh, setTimeout visible)
    const metaRefresh = page.locator('meta[http-equiv="refresh"]');
    const hasMetaRefresh = (await metaRefresh.count()) > 0;

    if (!hasMetaRefresh) {
      testResults.push({
        criterion: "13.1",
        test: "Limite de temps modifiable",
        status: "non-applicable",
        details: "Aucune limite de temps détectée",
      });
      return;
    }

    // Vérifier si l'utilisateur peut contrôler la limite de temps
    const controlElements = page.locator(
      'button:has-text("prolonger"), button:has-text("pause"), button:has-text("arrêter")'
    );
    const hasControls = (await controlElements.count()) > 0;

    testResults.push({
      criterion: "13.1",
      test: "Limite de temps modifiable",
      status: hasControls ? "conforme" : "non-conforme",
      details: hasControls
        ? "Contrôles de temps disponibles"
        : "Aucun contrôle de temps trouvé",
    });

    expect(hasControls).toBe(true);
  });

  test("13.3 - Documents téléchargeables accessibles", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    const downloadLinks = page.locator(
      'a[href$=".pdf"], a[href$=".doc"], a[href$=".docx"], a[href$=".xls"], a[href$=".xlsx"], a[download]'
    );
    const count = await downloadLinks.count();

    if (count === 0) {
      testResults.push({
        criterion: "13.3",
        test: "Documents téléchargeables",
        status: "non-applicable",
        details: "Aucun document téléchargeable trouvé",
      });
      return;
    }

    let conformeCount = 0;
    const issues: string[] = [];

    for (let i = 0; i < count; i++) {
      const link = downloadLinks.nth(i);
      const href = (await link.getAttribute("href")) || "";
      const text = await link.textContent();

      // Vérifier que le format et la taille sont indiqués
      const extension = href.split(".").pop()?.toUpperCase();
      const mentionsFormat =
        text?.includes(extension || "") ||
        text?.toLowerCase().includes("pdf") ||
        text?.toLowerCase().includes("doc");

      if (mentionsFormat) {
        conformeCount++;
      } else {
        issues.push(`${href}: format non indiqué dans le libellé`);
      }
    }

    const isConforme = issues.length === 0;

    testResults.push({
      criterion: "13.3",
      test: "Documents téléchargeables",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeCount}/${count} documents avec format indiqué`,
      elements: issues.slice(0, 10),
    });

    if (issues.length > 0) {
      console.warn("Documents sans indication de format:", issues);
    }
  });

  test("13.7 - Contenus en mouvement contrôlables", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Détecter les animations CSS et les carrousels
    const animations = page.locator(
      '[class*="carousel"], [class*="slider"], [class*="animate"], [class*="slide"]'
    );
    const count = await animations.count();

    if (count === 0) {
      testResults.push({
        criterion: "13.7",
        test: "Contenus en mouvement contrôlables",
        status: "non-applicable",
        details: "Aucun contenu en mouvement détecté",
      });
      return;
    }

    let conformeCount = 0;
    const issues: string[] = [];

    for (let i = 0; i < count; i++) {
      const animated = animations.nth(i);

      // Chercher des contrôles (play, pause, stop)
      const controls = animated.locator(
        'button, [role="button"], [aria-label*="pause"], [aria-label*="play"], [aria-label*="stop"]'
      );
      const hasControls = (await controls.count()) > 0;

      if (hasControls) {
        conformeCount++;
      } else {
        const identifier = await animated.evaluate(
          (el) => el.className || el.id || "animated-content"
        );
        issues.push(`${identifier}: pas de contrôles détectés`);
      }
    }

    const isConforme = issues.length === 0;

    testResults.push({
      criterion: "13.7",
      test: "Contenus en mouvement contrôlables",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeCount}/${count} contenus en mouvement avec contrôles`,
      elements: issues,
    });

    expect(issues.length).toBe(0);
  });

  test("13.8 - Contenus clignotants limités", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Détecter les éléments avec animations rapides
    const blinkingElements = page.locator(
      '[class*="blink"], [class*="flash"], blink'
    );
    const count = await blinkingElements.count();

    testResults.push({
      criterion: "13.8",
      test: "Contenus clignotants limités",
      status: count === 0 ? "conforme" : "non-conforme",
      details:
        count === 0
          ? "Aucun contenu clignotant détecté"
          : `${count} élément(s) clignotant(s) détecté(s)`,
    });

    expect(count).toBe(0);
  });

  test("13.9 - Contenu accessible au clavier", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Tester la navigation au clavier
    await page.keyboard.press("Tab");
    const firstFocusedElement = page.locator(":focus");
    const hasFocus = (await firstFocusedElement.count()) > 0;

    // Tester plusieurs Tab
    let focusableCount = 0;
    for (let i = 0; i < 10; i++) {
      await page.keyboard.press("Tab");
      const focused = page.locator(":focus");
      if ((await focused.count()) > 0) {
        focusableCount++;
      }
    }

    const isAccessibleByKeyboard = hasFocus && focusableCount >= 5;

    testResults.push({
      criterion: "13.9",
      test: "Contenu accessible au clavier",
      status: isAccessibleByKeyboard ? "conforme" : "non-conforme",
      details: isAccessibleByKeyboard
        ? `Navigation clavier fonctionnelle (${focusableCount} éléments atteints)`
        : "Navigation clavier limitée ou absente",
    });

    expect(isAccessibleByKeyboard).toBe(true);
  });

  test("13.10 - Raccourcis clavier sans conflit", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Vérifier la présence d'accesskey
    const elementsWithAccesskey = page.locator("[accesskey]");
    const count = await elementsWithAccesskey.count();

    if (count === 0) {
      testResults.push({
        criterion: "13.10",
        test: "Raccourcis clavier",
        status: "non-applicable",
        details: "Aucun raccourci clavier détecté",
      });
      return;
    }

    // Vérifier qu'il n'y a pas de doublons
    const accesskeys: string[] = [];
    for (let i = 0; i < count; i++) {
      const element = elementsWithAccesskey.nth(i);
      const key = await element.getAttribute("accesskey");
      if (key) accesskeys.push(key);
    }

    const duplicates = accesskeys.filter(
      (key, index) => accesskeys.indexOf(key) !== index
    );
    const hasDuplicates = duplicates.length > 0;

    testResults.push({
      criterion: "13.10",
      test: "Raccourcis clavier",
      status: hasDuplicates ? "non-conforme" : "conforme",
      details: hasDuplicates
        ? `Raccourcis en double: ${Array.from(new Set(duplicates)).join(", ")}`
        : `${count} raccourci(s) sans conflit`,
    });

    expect(hasDuplicates).toBe(false);
  });
});
