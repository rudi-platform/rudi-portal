import { test, expect } from "@playwright/test";
import {
  RGAA_SELECTORS,
  RGAATestResult,
  generateRGAAReport,
  getAccessibleName,
} from "../utils/rgaa-helpers";

test.describe("RGAA 4.1.2 - Thématique 6: Liens", () => {
  let testResults: RGAATestResult[] = [];

  test.afterAll(async () => {
    console.log(generateRGAAReport(testResults));
  });

  test("6.1 - Intitulés de liens explicites", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    const links = page.locator(RGAA_SELECTORS.links);
    const count = await links.count();

    if (count === 0) {
      testResults.push({
        criterion: "6.1",
        test: "Intitulés de liens explicites",
        status: "non-applicable",
        details: "Aucun lien trouvé",
      });
      return;
    }

    let conformeCount = 0;
    const nonConformeElements: string[] = [];

    for (let i = 0; i < count; i++) {
      const link = links.nth(i);
      const href = (await link.getAttribute("href")) || `link-${i}`;

      // Obtenir le nom accessible du lien
      const accessibleName = await getAccessibleName(link);

      // Un lien doit avoir un intitulé explicite
      const isExplicit =
        accessibleName.length > 0 &&
        !accessibleName.toLowerCase().includes("cliquez ici") &&
        !accessibleName.toLowerCase().includes("en savoir plus") &&
        !accessibleName.toLowerCase().includes("lire la suite") &&
        accessibleName.toLowerCase() !== "ici";

      if (isExplicit) {
        conformeCount++;
      } else {
        nonConformeElements.push(`${href}: "${accessibleName}"`);
      }
    }

    const isConforme = nonConformeElements.length === 0;

    testResults.push({
      criterion: "6.1",
      test: "Intitulés de liens explicites",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeCount}/${count} liens avec intitulés explicites`,
      elements: nonConformeElements.slice(0, 10),
    });

    if (nonConformeElements.length > 0) {
      console.warn(
        `${nonConformeElements.length} lien(s) avec intitulé non explicite`
      );
    }
  });

  test("6.2 - Liens identiques avec destinations différentes", async ({
    page,
  }) => {
    await page.goto(process.env.TARGET_URL || "/");

    const links = page.locator(RGAA_SELECTORS.links);
    const count = await links.count();

    if (count === 0) {
      testResults.push({
        criterion: "6.0",
        test: "Liens identiques et destinations (information)",
        status: "non-applicable",
        details: "Aucun lien trouvé",
      });
      return;
    }

    // Grouper les liens par intitulé
    const linksByText = new Map<string, string[]>();

    for (let i = 0; i < count; i++) {
      const link = links.nth(i);
      const text = await getAccessibleName(link);
      const href = (await link.getAttribute("href")) || "";

      if (text && href) {
        if (!linksByText.has(text)) {
          linksByText.set(text, []);
        }
        linksByText.get(text)!.push(href);
      }
    }

    // Vérifier les liens avec même intitulé mais destinations différentes
    const problematicLinks: string[] = [];

    for (const [text, hrefs] of linksByText.entries()) {
      const uniqueHrefs = new Set(hrefs);
      if (uniqueHrefs.size > 1) {
        problematicLinks.push(
          `"${text}": ${uniqueHrefs.size} destinations différentes`
        );
      }
    }

    // Critère informatif (hors RGAA) : on l'affiche en informations complémentaires
    testResults.push({
      criterion: "6.0",
      test: "Liens identiques et destinations (information)",
      status: "non-applicable",
      details:
        problematicLinks.length > 0
          ? `${problematicLinks.length} lien(s) avec intitulé identique mais destinations différentes`
          : "Aucune incohérence détectée",
      elements: problematicLinks.slice(0, 10),
    });
  });

  test("6.2 - Chaque lien a un intitulé", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    const links = page.locator(RGAA_SELECTORS.links);
    const count = await links.count();

    if (count === 0) {
      testResults.push({
        criterion: "6.2",
        test: "Chaque lien a un intitulé",
        status: "non-applicable",
        details: "Aucun lien trouvé",
      });
      return;
    }

    let conformeCount = 0;
    const missingLabel: string[] = [];

    for (let i = 0; i < count; i++) {
      const link = links.nth(i);
      const href = (await link.getAttribute("href")) || `link-${i}`;
      const accessibleName = await getAccessibleName(link);
      if (accessibleName && accessibleName.trim().length > 0) {
        conformeCount++;
      } else {
        missingLabel.push(`${href}`);
      }
    }

    const isConforme = missingLabel.length === 0;

    testResults.push({
      criterion: "6.2",
      test: "Chaque lien a un intitulé",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeCount}/${count} liens avec un intitulé`,
      elements: missingLabel.slice(0, 10),
    });
  });
});
