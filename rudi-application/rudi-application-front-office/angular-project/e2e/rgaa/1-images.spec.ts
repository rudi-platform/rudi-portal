import { test, expect, Locator } from "@playwright/test";
import {
  checkImageAlternative,
  getLocatorHint,
  getElementAuditDetails,
  isRGAAException,
  RGAA_SELECTORS,
  RGAATestResult,
  generateRGAAReport,
  generateAuditHTML,
  AuditCriterion,
  AuditReport,
} from "../utils/rgaa-helpers";
import * as fs from "fs";
import * as path from "path";

test.describe("RGAA 4.1.2 - Thématique 1: Images", () => {
  let testResults: RGAATestResult[] = [];

  // Attendre qu'Angular ait fini le rendu avant chaque test
  test.beforeEach(async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/", { waitUntil: "networkidle" });
    // Attendre que le nombre d'éléments image se stabilise (Angular change detection terminée)
    const imageSelector = RGAA_SELECTORS.images;
    await page.waitForFunction(
      (selector) => {
        const w = window as any;
        const count = document.querySelectorAll(selector).length;
        if (w.__rgaaLastCount === undefined) {
          w.__rgaaLastCount = count;
          w.__rgaaStableChecks = 0;
          return false;
        }
        if (count === w.__rgaaLastCount) {
          w.__rgaaStableChecks++;
          return w.__rgaaStableChecks >= 3; // Stable pendant 3 vérifications (~1.5s)
        }
        w.__rgaaLastCount = count;
        w.__rgaaStableChecks = 0;
        return false;
      },
      imageSelector,
      { timeout: 20000, polling: 500 }
    ).catch(() => {});
  });

  test.afterAll(async () => {
    // Générer le rapport RGAA pour cette thématique
    console.log(generateRGAAReport(testResults));
  });

  test("1.1.1 - Images porteuses d'information avec alternatives textuelles", async ({
    page,
  }) => {
    const images = page.locator(RGAA_SELECTORS.images);
    const count = await images.count();

    if (count === 0) {
      testResults.push({
        criterion: "1.1.1",
        test: "Images avec alternatives textuelles",
        status: "non-applicable",
        details: "Aucune image trouvée sur la page",
      });
      return;
    }

    let conformeCount = 0;
    const nonConformeElements: string[] = [];

    for (let i = 0; i < count; i++) {
      const img = images.nth(i);
      const src =
        (await img.getAttribute("src")) ||
        (await img.getAttribute("data-src")) ||
        `element-${i}`;

      // Vérifier si c'est une image de décoration
      const isDecoration = await isRGAAException(img, "1.2");
      if (isDecoration) {
        continue; // Les images de décoration sont exclues de ce test
      }

      const result = await checkImageAlternative(img);

      if (result.hasAlternative && result.value.trim().length > 0) {
        conformeCount++;
      } else {
        nonConformeElements.push(src);
      }
    }

    const totalImages = count;
    const isConforme = nonConformeElements.length === 0 && totalImages > 0;

    testResults.push({
      criterion: "1.1.1",
      test: "Images avec alternatives textuelles",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeCount}/${totalImages} images avec alternatives valides`,
      elements: nonConformeElements,
    });

    expect(
      nonConformeElements.length,
      `${
        nonConformeElements.length
      } image(s) sans alternative: ${nonConformeElements.join(", ")}`
    ).toBe(0);
  });

  test("1.2 - Images de décoration correctement ignorées", async ({ page }) => {
    // Sélecteurs étendus selon le glossaire RGAA : img, svg, [role="img"], canvas, etc.
    const decorativeImages = page.locator(
      'img[alt=""], img[aria-hidden="true"], img[role="presentation"], svg[aria-hidden="true"], [role="img"][aria-hidden="true"], [role="presentation"]'
    );
    const count = await decorativeImages.count();

    if (count === 0) {
      testResults.push({
        criterion: "1.2",
        test: "Images de décoration ignorées",
        status: "non-applicable",
        details: "Aucune image de décoration détectée",
      });
      return;
    }

    let conformeCount = 0;
    const nonConformeElements: string[] = [];

    for (let i = 0; i < count; i++) {
      const img = decorativeImages.nth(i);
      const src = (await img.getAttribute("src")) || `decorative-${i}`;

      // Une image de décoration ne doit pas avoir d'alternative textuelle
      const alt = await img.getAttribute("alt");
      const ariaLabel = await img.getAttribute("aria-label");
      const ariaLabelledby = await img.getAttribute("aria-labelledby");
      const title = await img.getAttribute("title");

      const hasUnwantedAlternative =
        (alt && alt.trim().length > 0) ||
        (ariaLabel && ariaLabel.trim().length > 0) ||
        ariaLabelledby ||
        (title && title.trim().length > 0);

      if (!hasUnwantedAlternative) {
        conformeCount++;
      } else {
        nonConformeElements.push(src);
      }
    }

    const isConforme = nonConformeElements.length === 0;

    testResults.push({
      criterion: "1.2",
      test: "Images de décoration ignorées",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeCount}/${count} images de décoration correctement ignorées`,
      elements: nonConformeElements,
    });

    expect(
      nonConformeElements.length,
      `${
        nonConformeElements.length
      } image(s) de décoration avec alternative: ${nonConformeElements.join(
        ", "
      )}`
    ).toBe(0);
  });

  test("1.3 - Pertinence des alternatives textuelles", async ({ page }) => {
    // Sélecteur étendu selon le glossaire RGAA : tous les types d'images
    const allImages = page.locator(RGAA_SELECTORS.images);
    const totalCount = await allImages.count();

    // Filtrer les images qui ont une alternative textuelle
    const imagesWithAlt: { element: Locator; alt: Awaited<ReturnType<typeof checkImageAlternative>>; hint: string }[] = [];
    for (let i = 0; i < totalCount; i++) {
      const img = allImages.nth(i);
      const alternative = await checkImageAlternative(img);
      if (alternative.hasAlternative && alternative.value.trim().length > 0) {
        const hint = await getLocatorHint(img);
        imagesWithAlt.push({ element: img, alt: alternative, hint });
      }
    }

    if (imagesWithAlt.length === 0) {
      testResults.push({
        criterion: "1.3",
        test: "Pertinence des alternatives",
        status: "non-applicable",
        details: "Aucune image avec alternative trouvée",
      });
      return;
    }

    let conformeCount = 0;
    const problematicElements: string[] = [];

    for (const { alt: alternative, hint } of imagesWithAlt) {
      // Vérifications de base pour la pertinence
      const altText = alternative.value.toLowerCase();
      const isPertinent =
        altText.length >= 3 &&
        altText.length <= 80 && // Alternative courte et concise
        !altText.includes("image") &&
        !altText.includes("photo") &&
        !altText.includes("picture");

      if (isPertinent) {
        conformeCount++;
      } else {
        problematicElements.push(`${hint}: "${alternative.value}"`);
      }
    }
    const count = imagesWithAlt.length;

    const isConforme = problematicElements.length === 0;

    testResults.push({
      criterion: "1.3",
      test: "Pertinence des alternatives",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeCount}/${count} alternatives pertinentes`,
      elements: problematicElements,
    });

    // Test non bloquant - on log les problèmes mais on ne fait pas échouer
    if (problematicElements.length > 0) {
      console.warn(
        `Alternatives potentiellement non pertinentes:`,
        problematicElements
      );
    }
  });

  test("1.4 - CAPTCHA avec alternatives appropriées", async ({ page }) => {
    const captchaImages = page
      .locator(RGAA_SELECTORS.images)
      .filter({
        hasText: /captcha|verification|security/i,
      })
      .or(page.locator('[alt*="captcha" i], [alt*="verification" i], [aria-label*="captcha" i], [aria-label*="verification" i]'));

    const count = await captchaImages.count();

    if (count === 0) {
      testResults.push({
        criterion: "1.4",
        test: "CAPTCHA avec alternatives",
        status: "non-applicable",
        details: "Aucun CAPTCHA détecté",
      });
      return;
    }

    let conformeCount = 0;
    const nonConformeElements: string[] = [];

    for (let i = 0; i < count; i++) {
      const captcha = captchaImages.nth(i);
      const alternative = await checkImageAlternative(captcha);

      // Pour un CAPTCHA, l'alternative doit décrire la nature et la fonction
      const isValidCaptchaAlt =
        alternative.hasAlternative &&
        (alternative.value.toLowerCase().includes("captcha") ||
          alternative.value.toLowerCase().includes("verification") ||
          alternative.value.toLowerCase().includes("sécurité"));

      if (isValidCaptchaAlt) {
        conformeCount++;
      } else {
        nonConformeElements.push(`captcha-${i}: "${alternative.value}"`);
      }
    }

    const isConforme = nonConformeElements.length === 0;

    testResults.push({
      criterion: "1.4",
      test: "CAPTCHA avec alternatives",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeCount}/${count} CAPTCHA avec alternatives appropriées`,
      elements: nonConformeElements,
    });

    expect(
      nonConformeElements.length,
      `${nonConformeElements.length} CAPTCHA sans alternative appropriée`
    ).toBe(0);
  });

  test("1.5 - Alternative d'accès aux CAPTCHA", async ({ page }) => {
    const captchaImages = page.locator(RGAA_SELECTORS.images).filter({
      hasText: /captcha|verification/i,
    });

    const count = await captchaImages.count();

    if (count === 0) {
      testResults.push({
        criterion: "1.5",
        test: "Alternative d'accès CAPTCHA",
        status: "non-applicable",
        details: "Aucun CAPTCHA détecté",
      });
      return;
    }

    // Vérifier la présence d'alternatives (audio, autre forme)
    const audioAlternative = page.locator(
      'audio, [type="audio"], button:has-text("audio")'
    );
    const textAlternative = page.locator(
      'button:has-text("texte"), input[type="text"][placeholder*="captcha"]'
    );
    const refreshButton = page.locator(
      'button:has-text("nouveau"), button:has-text("refresh")'
    );

    const hasAudioAlt = (await audioAlternative.count()) > 0;
    const hasTextAlt = (await textAlternative.count()) > 0;
    const hasRefresh = (await refreshButton.count()) > 0;

    const hasAlternative = hasAudioAlt || hasTextAlt || hasRefresh;

    testResults.push({
      criterion: "1.5",
      test: "Alternative d'accès CAPTCHA",
      status: hasAlternative ? "conforme" : "non-conforme",
      details: hasAlternative
        ? `Alternative trouvée: ${hasAudioAlt ? "audio" : ""} ${
            hasTextAlt ? "texte" : ""
          } ${hasRefresh ? "refresh" : ""}`
        : "Aucune alternative d'accès trouvée",
    });

    expect(
      hasAlternative,
      "Aucune alternative d'accès au CAPTCHA trouvée"
    ).toBe(true);
  });

  // ============================================================
  // Génération du rapport d'audit détaillé (HTML + JSON)
  // ============================================================
  test("Génération du rapport d'audit", async ({ page, browserName }) => {
    const allImages = page.locator(RGAA_SELECTORS.images);
    const totalCount = await allImages.count();

    // --- 1.1.1 : Images porteuses d'information ---
    const criterion111: AuditCriterion = {
      id: "1.1.1", title: "Alternative textuelle pour les images porteuses d'information",
      description: "Chaque image porteuse d'information a-t-elle une alternative textuelle ?",
      status: "non-applicable", conformeCount: 0, nonConformeCount: 0, totalCount: 0, elements: [],
    };
    for (let i = 0; i < totalCount; i++) {
      const img = allImages.nth(i);
      if (await isRGAAException(img, "1.2")) continue;
      criterion111.totalCount++;
      const details = await getElementAuditDetails(img, i);
      if (details.alternativeMethod !== "none" && details.alternativeValue.trim().length > 0) {
        criterion111.conformeCount++;
        criterion111.elements.push({ ...details, status: "conforme" });
      } else {
        criterion111.nonConformeCount++;
        const correction = details.tag === "img"
          ? `Ajouter un attribut alt descriptif : <img alt="Description de l'image" ...>`
          : details.tag === "svg"
            ? `Ajouter un <title> enfant : <svg><title>Description</title>...</svg> ou role="img" aria-label="Description"`
            : `Ajouter aria-label="Description" sur <${details.tag}>`;
        criterion111.elements.push({ ...details, status: "non-conforme",
          issue: `Image porteuse d'information sans alternative textuelle`, correction });
      }
    }
    criterion111.status = criterion111.totalCount === 0 ? "non-applicable"
      : criterion111.nonConformeCount === 0 ? "conforme" : "non-conforme";

    // --- 1.2 : Images de décoration ---
    const criterion12: AuditCriterion = {
      id: "1.2", title: "Images de décoration correctement ignorées",
      description: "Chaque image de décoration est-elle correctement ignorée par les technologies d'assistance ?",
      status: "non-applicable", conformeCount: 0, nonConformeCount: 0, totalCount: 0, elements: [],
    };
    for (let i = 0; i < totalCount; i++) {
      const img = allImages.nth(i);
      if (!(await isRGAAException(img, "1.2"))) continue;
      criterion12.totalCount++;
      const details = await getElementAuditDetails(img, i);
      const hasUnwantedAlt = (details.alt && details.alt.trim().length > 0)
        || (details.ariaLabel && details.ariaLabel.trim().length > 0)
        || (details.svgTitle && details.svgTitle.trim().length > 0);
      if (!hasUnwantedAlt) {
        criterion12.conformeCount++;
        criterion12.elements.push({ ...details, status: "conforme" });
      } else {
        criterion12.nonConformeCount++;
        criterion12.elements.push({ ...details, status: "non-conforme",
          issue: `Image de décoration avec alternative non vide ("${details.alt || details.ariaLabel || details.svgTitle}")`,
          correction: details.tag === "img"
            ? `Remplacer alt="${details.alt}" par alt="" et ajouter aria-hidden="true"`
            : `Retirer aria-label et s'assurer que aria-hidden="true" est présent` });
      }
    }
    criterion12.status = criterion12.totalCount === 0 ? "non-applicable"
      : criterion12.nonConformeCount === 0 ? "conforme" : "non-conforme";

    // --- 1.3 : Pertinence des alternatives ---
    const criterion13: AuditCriterion = {
      id: "1.3", title: "Pertinence des alternatives textuelles",
      description: "Pour chaque image porteuse d'information, l'alternative textuelle est-elle pertinente ?",
      status: "non-applicable", conformeCount: 0, nonConformeCount: 0, totalCount: 0, elements: [],
    };
    for (let i = 0; i < totalCount; i++) {
      const img = allImages.nth(i);
      if (await isRGAAException(img, "1.2")) continue;
      const details = await getElementAuditDetails(img, i);
      if (details.alternativeMethod === "none" || details.alternativeValue.trim().length === 0) continue;
      criterion13.totalCount++;
      const altText = details.alternativeValue.toLowerCase();
      const issues: string[] = [];
      if (altText.length < 3) issues.push("Alternative trop courte (< 3 caractères)");
      if (altText.length > 80) issues.push("Alternative trop longue (> 80 caractères)");
      if (/^image|^photo|^picture|^img/i.test(altText)) issues.push(`Alternative générique`);
      if (/\.(jpg|jpeg|png|gif|svg|webp)$/i.test(altText)) issues.push("Nom de fichier au lieu d'une description");
      if (/image|photo|picture/i.test(altText) && altText.split(/\s/).length <= 3) issues.push("Alternative générique — décrire le contenu");
      if (issues.length === 0) {
        criterion13.conformeCount++;
        criterion13.elements.push({ ...details, status: "conforme" });
      } else {
        criterion13.nonConformeCount++;
        criterion13.elements.push({ ...details, status: "non-conforme", issue: issues.join(" ; "),
          correction: `Remplacer "${details.alternativeValue}" par une description du contenu, ex: alt="${details.nearHeading ? `Illustration ${details.nearHeading}` : 'Description pertinente'}"` });
      }
    }
    criterion13.status = criterion13.totalCount === 0 ? "non-applicable"
      : criterion13.nonConformeCount === 0 ? "conforme" : "non-conforme";

    // --- 1.4 / 1.5 : CAPTCHA ---
    const criterion14: AuditCriterion = { id: "1.4", title: "CAPTCHA — Alternative textuelle",
      description: "Chaque CAPTCHA a-t-il une alternative décrivant sa nature ?",
      status: "non-applicable", conformeCount: 0, nonConformeCount: 0, totalCount: 0, elements: [] };
    const criterion15: AuditCriterion = { id: "1.5", title: "CAPTCHA — Accès alternatif",
      description: "Pour chaque CAPTCHA, une solution d'accès alternative est-elle proposée ?",
      status: "non-applicable", conformeCount: 0, nonConformeCount: 0, totalCount: 0, elements: [] };

    // --- Assemblage du rapport ---
    const criteria = [criterion111, criterion12, criterion13, criterion14, criterion15];
    const conforme = criteria.filter(c => c.status === "conforme").length;
    const nonConforme = criteria.filter(c => c.status === "non-conforme").length;
    const nonApplicable = criteria.filter(c => c.status === "non-applicable").length;
    const applicableCount = conforme + nonConforme;

    const report: AuditReport = {
      meta: {
        date: new Date().toISOString().split("T")[0],
        url: process.env.TARGET_URL || "/",
        browser: browserName,
        referentiel: "RGAA v4.1.2",
        thematique: "Thématique 1 — Images",
      },
      summary: {
        totalCriteria: criteria.length,
        conforme, nonConforme, nonApplicable,
        conformityRate: applicableCount > 0 ? Math.round((conforme / applicableCount) * 100) : 100,
      },
      criteria,
    };

    const reportsDir = path.resolve(__dirname, "../../reports");
    fs.mkdirSync(reportsDir, { recursive: true });

    const jsonPath = path.join(reportsDir, `audit-rgaa-images-${browserName}.json`);
    fs.writeFileSync(jsonPath, JSON.stringify(report, null, 2), "utf-8");

    const htmlPath = path.join(reportsDir, `audit-rgaa-images-${browserName}.html`);
    fs.writeFileSync(htmlPath, generateAuditHTML(report), "utf-8");

    console.log(`\n📄 Rapport JSON : ${jsonPath}`);
    console.log(`📄 Rapport HTML : ${htmlPath}`);
  });
});
