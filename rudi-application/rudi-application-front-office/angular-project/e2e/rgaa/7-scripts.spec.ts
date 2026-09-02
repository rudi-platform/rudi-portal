import { test, expect } from "@playwright/test";
import {
  RGAA_SELECTORS,
  RGAATestResult,
  generateRGAAReport,
  getAccessibleName,
  isFocusable,
} from "../utils/rgaa-helpers";

test.describe("RGAA 4.1.2 - Thématique 7: Scripts", () => {
  let testResults: RGAATestResult[] = [];

  test.afterAll(async () => {
    console.log(generateRGAAReport(testResults));
  });

  test("7.1 - Composants compatibles avec les technologies d'assistance", async ({
    page,
  }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Vérifier nom, rôle, valeur selon RGAA
    const interactiveElements = page.locator(
      "[role], button, input, select, textarea, [tabindex]"
    );
    const count = await interactiveElements.count();

    if (count === 0) {
      testResults.push({
        criterion: "7.1",
        test: "Composants JavaScript accessibles",
        status: "non-applicable",
        details: "Aucun composant interactif trouvé",
      });
      return;
    }

    let conformeCount = 0;
    const nonConformeElements: string[] = [];
    const maxTests = Math.min(count, 50);

    for (let i = 0; i < maxTests; i++) {
      const element = interactiveElements.nth(i);

      // Nom accessible (selon ordre RGAA)
      const name = await getAccessibleName(element);

      // Rôle approprié
      const role =
        (await element.getAttribute("role")) ||
        (await element.evaluate((el) => el.tagName.toLowerCase()));

      const validRoles = [
        "button",
        "link",
        "textbox",
        "combobox",
        "checkbox",
        "radio",
        "slider",
        "tab",
        "menuitem",
        "option",
        "input",
        "select",
        "textarea",
        "a",
      ];

      const hasValidRole = validRoles.some((r) =>
        role.toLowerCase().includes(r)
      );
      const hasName = name.length > 0;

      if (hasValidRole && hasName) {
        conformeCount++;
      } else {
        const identifier = await element.evaluate(
          (el) => el.className || el.id || el.tagName
        );
        nonConformeElements.push(
          `${identifier}: role="${role}", name="${name}"`
        );
      }
    }

    const isConforme = nonConformeElements.length === 0;

    testResults.push({
      criterion: "7.1",
      test: "Composants JavaScript accessibles",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeCount}/${maxTests} composants avec nom, rôle et valeur appropriés`,
      elements: nonConformeElements.slice(0, 10),
    });

    expect(
      nonConformeElements.length,
      `${nonConformeElements.length} composant(s) non accessible(s)`
    ).toBe(0);
  });

  test("7.3 - Contrôle de scripts par le clavier", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    const interactiveElements = page.locator(
      'button, [role="button"], [onclick], [onkeydown], [tabindex]:not([tabindex="-1"])'
    );
    const count = await interactiveElements.count();

    if (count === 0) {
      testResults.push({
        criterion: "7.3",
        test: "Contrôle de scripts par le clavier",
        status: "non-applicable",
      });
      return;
    }

    let conformeCount = 0;
    const nonConformeElements: string[] = [];
    const maxTests = Math.min(count, 30);

    for (let i = 0; i < maxTests; i++) {
      const element = interactiveElements.nth(i);
      const isFocusableElement = await isFocusable(element);

      if (isFocusableElement) {
        conformeCount++;
      } else {
        const identifier = await element.evaluate(
          (el) => el.className || el.id || el.tagName
        );
        nonConformeElements.push(identifier);
      }
    }

    const isConforme = nonConformeElements.length === 0;

    testResults.push({
      criterion: "7.3",
      test: "Contrôle de scripts par le clavier",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeCount}/${maxTests} éléments interactifs accessibles au clavier`,
      elements: nonConformeElements,
    });

    expect(nonConformeElements.length).toBe(0);
  });

  test("7.4 - Changements de contexte contrôlés", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Vérifier que les changements de contexte sont initiés par l'utilisateur
    const autoSubmitForms = page.locator(
      'form[onchange*="submit"], select[onchange*="submit"]'
    );
    const autoRedirects = page.locator(
      '[onload*="location"], [onload*="redirect"]'
    );

    const autoSubmitCount = await autoSubmitForms.count();
    const autoRedirectCount = await autoRedirects.count();

    const totalAutoChanges = autoSubmitCount + autoRedirectCount;

    if (totalAutoChanges === 0) {
      testResults.push({
        criterion: "7.4",
        test: "Changements de contexte contrôlés",
        status: "conforme",
        details: "Aucun changement de contexte automatique détecté",
      });
      return;
    }

    testResults.push({
      criterion: "7.4",
      test: "Changements de contexte contrôlés",
      status: "non-conforme",
      details: `${totalAutoChanges} changement(s) de contexte automatique(s) détecté(s)`,
    });

    expect(totalAutoChanges).toBe(0);
  });

  test("7.5 - Messages de statut restitués", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Vérifier que les messages de statut utilisent aria-live ou role="status"
    const statusMessages = page.locator(
      '[role="status"], [role="alert"], [aria-live]'
    );
    const count = await statusMessages.count();

    if (count === 0) {
      testResults.push({
        criterion: "7.5",
        test: "Messages de statut restitués",
        status: "non-applicable",
        details: "Aucun message de statut détecté",
      });
      return;
    }

    let conformeCount = 0;
    const nonConformeElements: string[] = [];

    for (let i = 0; i < count; i++) {
      const message = statusMessages.nth(i);
      const ariaLive = await message.getAttribute("aria-live");
      const role = await message.getAttribute("role");

      const isValid =
        role === "status" ||
        role === "alert" ||
        ariaLive === "polite" ||
        ariaLive === "assertive";

      if (isValid) {
        conformeCount++;
      } else {
        const identifier = await message.evaluate(
          (el) => el.className || el.id || "message"
        );
        nonConformeElements.push(identifier);
      }
    }

    const isConforme = nonConformeElements.length === 0;

    testResults.push({
      criterion: "7.5",
      test: "Messages de statut restitués",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeCount}/${count} messages de statut correctement restitués`,
      elements: nonConformeElements,
    });

    expect(nonConformeElements.length).toBe(0);
  });
});
