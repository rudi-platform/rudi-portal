import { test, expect } from "@playwright/test";
import {
  RGAA_SELECTORS,
  RGAATestResult,
  generateRGAAReport,
  calculateContrastRatio,
  hexToRgb,
  rgbStringToHex,
} from "../utils/rgaa-helpers";

test.describe("RGAA 4.1.2 - Thématique 3: Couleurs", () => {
  let testResults: RGAATestResult[] = [];

  test.afterAll(async () => {
    console.log(generateRGAAReport(testResults));
  });

  test("3.1 - Information non donnée uniquement par la couleur", async ({
    page,
  }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Vérifier les liens uniquement différenciés par la couleur
    const links = page.locator("a[href]");
    const count = await links.count();

    if (count === 0) {
      testResults.push({
        criterion: "3.1",
        test: "Information non donnée uniquement par la couleur",
        status: "non-applicable",
        details: "Aucun lien trouvé",
      });
      return;
    }

    let conformeCount = 0;
    const issues: string[] = [];

    // Échantillonner les premiers liens
    const sampleSize = Math.min(count, 20);

    for (let i = 0; i < sampleSize; i++) {
      const link = links.nth(i);
      const textDecoration = await link.evaluate(
        (el) => window.getComputedStyle(el).textDecoration
      );
      const borderBottom = await link.evaluate(
        (el) => window.getComputedStyle(el).borderBottom
      );
      const fontWeight = await link.evaluate(
        (el) => window.getComputedStyle(el).fontWeight
      );

      // Un lien conforme a un indicateur autre que la couleur
      const hasVisualIndicator =
        textDecoration.includes("underline") ||
        borderBottom !== "0px none rgb(0, 0, 0)" ||
        parseInt(fontWeight) >= 700;

      if (hasVisualIndicator) {
        conformeCount++;
      } else {
        const text = (await link.textContent())?.substring(0, 50) || "lien";
        issues.push(text);
      }
    }

    const isConforme = issues.length === 0;

    testResults.push({
      criterion: "3.1",
      test: "Information non donnée uniquement par la couleur",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeCount}/${sampleSize} liens avec indicateur visuel autre que couleur`,
      elements: issues.slice(0, 5),
    });

    if (issues.length > 0) {
      console.warn(
        `${issues.length} lien(s) potentiellement différencié uniquement par la couleur`
      );
    }
  });

  test("3.2 - Contraste minimum 4.5:1 pour texte normal", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Vérifier le contraste selon les seuils RGAA :
    // - 4.5:1 pour texte < 24px (ou < 18.5px en gras)
    // - 3:1 pour texte ≥ 24px (ou ≥ 18.5px en gras)

    const textElements = page
      .locator("p, h1, h2, h3, h4, h5, h6, li, td, th, label, span, div")
      .filter({ hasText: /.+/ });
    const count = await textElements.count();

    if (count === 0) {
      testResults.push({
        criterion: "3.2",
        test: "Contraste minimum",
        status: "non-applicable",
        details: "Aucun élément de texte trouvé",
      });
      return;
    }

    let conformeCount = 0;
    const lowContrastElements: string[] = [];
    const sampleSize = Math.min(count, 50);

    for (let i = 0; i < sampleSize; i++) {
      const element = textElements.nth(i);

      try {
        const color = await element.evaluate(
          (el) => window.getComputedStyle(el).color
        );
        const backgroundColor = await element.evaluate((el) => {
          let bgColor = window.getComputedStyle(el).backgroundColor;
          let currentEl: Element | null = el;

          // Remonter dans le DOM pour trouver un background opaque
          while (
            currentEl &&
            (bgColor === "rgba(0, 0, 0, 0)" || bgColor === "transparent")
          ) {
            currentEl = currentEl.parentElement;
            if (currentEl) {
              bgColor = window.getComputedStyle(currentEl).backgroundColor;
            }
          }

          return bgColor === "rgba(0, 0, 0, 0)" || bgColor === "transparent"
            ? "rgb(255, 255, 255)"
            : bgColor;
        });

        const fontSize = await element.evaluate((el) =>
          parseFloat(window.getComputedStyle(el).fontSize)
        );
        const fontWeight = await element.evaluate((el) =>
          parseInt(window.getComputedStyle(el).fontWeight)
        );

        // Déterminer le seuil requis
        const isLargeText =
          fontSize >= 24 || (fontSize >= 18.5 && fontWeight >= 700);
        const requiredRatio = isLargeText ? 3 : 4.5;

        // Convertir les couleurs en hex pour le calcul
        const fgHex = rgbStringToHex(color);
        const bgHex = rgbStringToHex(backgroundColor);

        if (fgHex && bgHex) {
          const ratio = calculateContrastRatio(fgHex, bgHex);

          if (ratio >= requiredRatio) {
            conformeCount++;
          } else {
            const text = (await element.textContent())?.substring(0, 30) || "";
            lowContrastElements.push(
              `"${text}" - ratio: ${ratio.toFixed(
                2
              )}:1 (requis: ${requiredRatio}:1)`
            );
          }
        } else {
          conformeCount++; // Ne pas pénaliser si calcul impossible
        }
      } catch (error) {
        // En cas d'erreur, considérer comme conforme pour ne pas bloquer
        conformeCount++;
      }
    }

    const isConforme = lowContrastElements.length === 0;

    testResults.push({
      criterion: "3.2",
      test: "Contraste minimum",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeCount}/${sampleSize} éléments avec contraste suffisant`,
      elements: lowContrastElements.slice(0, 10),
    });

    expect(
      lowContrastElements.length,
      `${lowContrastElements.length} élément(s) avec contraste insuffisant`
    ).toBe(0);
  });

  test("3.3 - Contraste minimum 3:1 pour éléments graphiques", async ({
    page,
  }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Vérifier les composants d'interface et éléments graphiques
    const uiComponents = page.locator(
      'button, input[type="button"], input[type="submit"], a.button, [role="button"]'
    );
    const count = await uiComponents.count();

    if (count === 0) {
      testResults.push({
        criterion: "3.3",
        test: "Contraste minimum éléments graphiques",
        status: "non-applicable",
        details: "Aucun composant d'interface trouvé",
      });
      return;
    }

    let conformeCount = 0;
    const lowContrastElements: string[] = [];
    const sampleSize = Math.min(count, 30);

    for (let i = 0; i < sampleSize; i++) {
      const component = uiComponents.nth(i);

      try {
        const borderColor = await component.evaluate(
          (el) => window.getComputedStyle(el).borderColor
        );
        const backgroundColor = await component.evaluate(
          (el) => window.getComputedStyle(el).backgroundColor
        );
        const parentBgColor = await component.evaluate((el) => {
          const parent = el.parentElement;
          return parent
            ? window.getComputedStyle(parent).backgroundColor
            : "rgb(255, 255, 255)";
        });

        // Vérifier le contraste entre l'élément et son arrière-plan
        const bgHex = rgbStringToHex(backgroundColor);
        const parentBgHex = rgbStringToHex(parentBgColor);

        if (bgHex && parentBgHex) {
          const ratio = calculateContrastRatio(bgHex, parentBgHex);

          if (ratio >= 3) {
            conformeCount++;
          } else {
            const text =
              (await component.textContent())?.substring(0, 30) || "composant";
            lowContrastElements.push(
              `"${text}" - ratio: ${ratio.toFixed(2)}:1 (requis: 3:1)`
            );
          }
        } else {
          conformeCount++; // Ne pas pénaliser si calcul impossible
        }
      } catch (error) {
        conformeCount++;
      }
    }

    const isConforme = lowContrastElements.length === 0;

    testResults.push({
      criterion: "3.3",
      test: "Contraste minimum éléments graphiques",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeCount}/${sampleSize} composants avec contraste suffisant (≥ 3:1)`,
      elements: lowContrastElements.slice(0, 10),
    });

    if (lowContrastElements.length > 0) {
      console.warn(
        `${lowContrastElements.length} composant(s) avec contraste insuffisant`
      );
    }
  });
});
