import { test, expect } from "@playwright/test";
import {
  RGAA_SELECTORS,
  RGAATestResult,
  generateRGAAReport,
} from "../utils/rgaa-helpers";

test.describe("RGAA 4.1.2 - Thématique 8: Éléments obligatoires", () => {
  let testResults: RGAATestResult[] = [];

  test.afterAll(async () => {
    console.log(generateRGAAReport(testResults));
  });

  test("8.1 - Validité du code HTML", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Vérifier la présence de <!DOCTYPE>
    const hasDoctype = await page.evaluate(() => {
      return document.doctype !== null;
    });

    testResults.push({
      criterion: "8.1",
      test: "Validité du code HTML",
      status: hasDoctype ? "conforme" : "non-conforme",
      details: hasDoctype ? "DOCTYPE présent" : "DOCTYPE absent",
    });

    expect(hasDoctype, "La page doit avoir une déclaration DOCTYPE").toBe(true);
  });

  test("8.2 - Code HTML valide", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Vérifications de base de la validité HTML
    const htmlValidation = await page.evaluate(() => {
      const issues: string[] = [];

      // Vérifier l'attribut lang
      if (!document.documentElement.hasAttribute("lang")) {
        issues.push("Attribut lang manquant sur <html>");
      }

      // Vérifier les IDs uniques
      const ids = Array.from(document.querySelectorAll("[id]")).map(
        (el) => el.id
      );
      const duplicateIds = ids.filter((id, index) => ids.indexOf(id) !== index);
      if (duplicateIds.length > 0) {
        issues.push(
          `IDs dupliqués: ${Array.from(new Set(duplicateIds)).join(", ")}`
        );
      }

      return { isValid: issues.length === 0, issues };
    });

    testResults.push({
      criterion: "8.2",
      test: "Code HTML valide",
      status: htmlValidation.isValid ? "conforme" : "non-conforme",
      details: htmlValidation.isValid
        ? "Vérifications HTML de base passées"
        : `${htmlValidation.issues.length} problème(s) détecté(s)`,
      elements: htmlValidation.issues,
    });

    expect(
      htmlValidation.isValid,
      `Problèmes HTML: ${htmlValidation.issues.join(", ")}`
    ).toBe(true);
  });

  test("8.3 - Langue par défaut définie", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    const lang = await page.evaluate(() => {
      return document.documentElement.getAttribute("lang");
    });

    const hasLang = lang !== null && lang.length > 0;

    testResults.push({
      criterion: "8.3",
      test: "Langue par défaut définie",
      status: hasLang ? "conforme" : "non-conforme",
      details: hasLang ? `Langue définie: ${lang}` : "Attribut lang absent",
    });

    expect(hasLang, "La page doit avoir un attribut lang").toBe(true);
  });

  test("8.5 - Titre de page pertinent", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    const title = await page.title();
    const hasTitle = title && title.length > 0;

    // Vérifications de pertinence
    const isPertinent =
      hasTitle &&
      title.length >= 3 &&
      !title.toLowerCase().includes("untitled") &&
      !title.toLowerCase().includes("sans titre");

    testResults.push({
      criterion: "8.5",
      test: "Titre de page pertinent",
      status: isPertinent ? "conforme" : "non-conforme",
      details: isPertinent
        ? `Titre: "${title}"`
        : hasTitle
        ? `Titre non pertinent: "${title}"`
        : "Pas de titre",
    });

    expect(isPertinent, `Titre non pertinent: "${title}"`).toBe(true);
  });

  test("8.6 - Balises utilisées uniquement à des fins de présentation", async ({
    page,
  }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Détecter les balises sémantiques mal utilisées
    const presentationalTags = page.locator(
      "blockquote:not(:has-text), cite:empty, q:empty"
    );
    const count = await presentationalTags.count();

    testResults.push({
      criterion: "8.6",
      test: "Balises à des fins de présentation",
      status: count === 0 ? "conforme" : "non-conforme",
      details:
        count === 0
          ? "Aucune balise sémantique mal utilisée"
          : `${count} balise(s) sémantique(s) vide(s) détectée(s)`,
    });

    expect(count).toBe(0);
  });

  test("8.9 - Balises à usage unique non répétées", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    const uniqueTags = await page.evaluate(() => {
      const issues: string[] = [];

      // Vérifier qu'il n'y a qu'une seule balise <main>
      const mains = document.querySelectorAll("main, [role='main']");
      if (mains.length > 1) {
        issues.push(`${mains.length} balises <main> trouvées (max: 1)`);
      }

      // Vérifier qu'il n'y a qu'une seule balise <h1>
      const h1s = document.querySelectorAll("h1");
      if (h1s.length > 1) {
        issues.push(`${h1s.length} balises <h1> trouvées (recommandation: 1)`);
      }

      return { isValid: issues.length === 0, issues };
    });

    testResults.push({
      criterion: "8.9",
      test: "Balises à usage unique",
      status: uniqueTags.isValid ? "conforme" : "non-conforme",
      details: uniqueTags.isValid
        ? "Balises uniques correctement utilisées"
        : `${uniqueTags.issues.length} problème(s)`,
      elements: uniqueTags.issues,
    });

    expect(uniqueTags.isValid).toBe(true);
  });
});
