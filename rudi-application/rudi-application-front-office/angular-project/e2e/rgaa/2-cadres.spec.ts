import { test, expect } from "@playwright/test";
import {
  RGAA_SELECTORS,
  RGAATestResult,
  generateRGAAReport,
  getAccessibleName,
} from "../utils/rgaa-helpers";

test.describe("RGAA 4.1.2 - Thématique 2: Cadres", () => {
  let testResults: RGAATestResult[] = [];

  test.afterAll(async () => {
    console.log(generateRGAAReport(testResults));
  });

  test("2.1 - Cadres avec titre pertinent", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    const frames = page.locator("iframe, frame");
    const count = await frames.count();

    if (count === 0) {
      testResults.push({
        criterion: "2.1",
        test: "Cadres avec titre",
        status: "non-applicable",
        details: "Aucun cadre (iframe/frame) trouvé",
      });
      return;
    }

    let conformeCount = 0;
    const nonConformeElements: string[] = [];

    for (let i = 0; i < count; i++) {
      const frame = frames.nth(i);
      const src = (await frame.getAttribute("src")) || `frame-${i}`;

      // Vérifier selon l'ordre RGAA :
      // 1. title
      // 2. aria-label
      // 3. aria-labelledby
      const title = await frame.getAttribute("title");
      const ariaLabel = await frame.getAttribute("aria-label");
      const ariaLabelledby = await frame.getAttribute("aria-labelledby");

      const hasTitle = title || ariaLabel || ariaLabelledby;

      if (hasTitle && (title?.trim().length ?? 0) > 0) {
        conformeCount++;
      } else {
        nonConformeElements.push(src);
      }
    }

    const isConforme = nonConformeElements.length === 0;

    testResults.push({
      criterion: "2.1",
      test: "Cadres avec titre",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeCount}/${count} cadres avec titre pertinent`,
      elements: nonConformeElements,
    });

    expect(
      nonConformeElements.length,
      `${
        nonConformeElements.length
      } cadre(s) sans titre: ${nonConformeElements.join(", ")}`
    ).toBe(0);
  });

  test("2.2 - Pertinence du titre des cadres", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    const frames = page.locator("iframe[title], frame[title]");
    const count = await frames.count();

    if (count === 0) {
      testResults.push({
        criterion: "2.2",
        test: "Pertinence des titres de cadres",
        status: "non-applicable",
        details: "Aucun cadre avec titre trouvé",
      });
      return;
    }

    let conformeCount = 0;
    const problematicElements: string[] = [];

    for (let i = 0; i < count; i++) {
      const frame = frames.nth(i);
      const title = (await frame.getAttribute("title")) || "";
      const src = (await frame.getAttribute("src")) || `frame-${i}`;

      // Vérifications de pertinence
      const isPertinent =
        title.length >= 3 &&
        !title.toLowerCase().includes("iframe") &&
        !title.toLowerCase().includes("frame") &&
        !title.toLowerCase().includes("cadre");

      if (isPertinent) {
        conformeCount++;
      } else {
        problematicElements.push(`${src}: "${title}"`);
      }
    }

    const isConforme = problematicElements.length === 0;

    testResults.push({
      criterion: "2.2",
      test: "Pertinence des titres de cadres",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeCount}/${count} titres de cadres pertinents`,
      elements: problematicElements,
    });

    if (problematicElements.length > 0) {
      console.warn(
        "Titres de cadres potentiellement non pertinents:",
        problematicElements
      );
    }
  });
});
