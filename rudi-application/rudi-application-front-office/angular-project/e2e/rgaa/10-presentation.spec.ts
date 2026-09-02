import { test, expect } from "@playwright/test";
import {
  RGAA_SELECTORS,
  RGAATestResult,
  generateRGAAReport,
} from "../utils/rgaa-helpers";

test.describe("RGAA 4.1.2 - Thématique 10: Présentation de l'information", () => {
  let testResults: RGAATestResult[] = [];

  test.afterAll(async () => {
    console.log(generateRGAAReport(testResults));
  });

  test("10.1 - Utilisation de feuilles de styles pour la présentation", async ({
    page,
  }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Vérifier l'absence de balises de présentation dépréciées
    const presentationalTags = page.locator(
      "font, basefont, center, u, s, strike, big, small"
    );
    const count = await presentationalTags.count();

    testResults.push({
      criterion: "10.1",
      test: "Utilisation de CSS pour la présentation",
      status: count === 0 ? "conforme" : "non-conforme",
      details:
        count === 0
          ? "Aucune balise de présentation dépréciée"
          : `${count} balise(s) de présentation dépréciée(s)`,
    });

    expect(count).toBe(0);
  });

  test("10.2 - Absence d'attributs de présentation", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Vérifier l'absence d'attributs de présentation
    const elementsWithPresentationAttrs = page.locator(
      "[align], [bgcolor], [border], [cellpadding], [cellspacing], [valign], [width]:not(img):not(video):not(canvas)"
    );
    const count = await elementsWithPresentationAttrs.count();

    testResults.push({
      criterion: "10.2",
      test: "Absence d'attributs de présentation",
      status: count === 0 ? "conforme" : "non-conforme",
      details:
        count === 0
          ? "Aucun attribut de présentation HTML"
          : `${count} attribut(s) de présentation détecté(s)`,
    });

    expect(count).toBe(0);
  });

  test("10.3 - Informations compréhensibles sans CSS", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Désactiver le CSS et vérifier que le contenu reste accessible
    await page.addStyleTag({
      content: "* { all: unset !important; display: block !important; }",
    });

    // Vérifier que le texte est toujours présent
    const bodyText = await page.locator("body").textContent();
    const hasContent = bodyText && bodyText.trim().length > 100;

    // Vérifier que les liens sont toujours des liens
    const links = page.locator("a[href]");
    const linkCount = await links.count();

    testResults.push({
      criterion: "10.3",
      test: "Informations compréhensibles sans CSS",
      status: hasContent && linkCount > 0 ? "conforme" : "non-conforme",
      details: hasContent
        ? `Contenu accessible sans CSS (${linkCount} liens)`
        : "Contenu non accessible sans CSS",
    });

    expect(hasContent).toBe(true);
  });

  test("10.4 - Texte redimensionnable", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Vérifier que le texte utilise des unités relatives
    const textElements = page.locator("p, h1, h2, h3, h4, h5, h6, span, div");
    const sampleSize = Math.min(await textElements.count(), 20);

    let relativeUnitsCount = 0;

    for (let i = 0; i < sampleSize; i++) {
      const element = textElements.nth(i);
      const fontSize = await element.evaluate((el) => {
        return window.getComputedStyle(el).fontSize;
      });

      // Unités relatives : em, rem, %, vw, vh
      if (fontSize && !fontSize.includes("px")) {
        relativeUnitsCount++;
      }
    }

    // Note: Cette vérification est approximative car les px calculés peuvent venir de em/rem
    const ratio = relativeUnitsCount / sampleSize;

    testResults.push({
      criterion: "10.4",
      test: "Texte redimensionnable",
      status: ratio >= 0.5 ? "conforme" : "non-conforme",
      details: `${relativeUnitsCount}/${sampleSize} éléments avec unités adaptées`,
    });

    // Test non bloquant car la détection est imparfaite
    if (ratio < 0.5) {
      console.warn("Peu d'unités relatives détectées pour le texte");
    }
  });

  test("10.7 - Visibilité de la prise de focus", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    const focusableElements = page.locator(
      "a[href], button, input, select, textarea, [tabindex]:not([tabindex='-1'])"
    );
    const count = await focusableElements.count();

    if (count === 0) {
      testResults.push({
        criterion: "10.7",
        test: "Visibilité de la prise de focus",
        status: "non-applicable",
      });
      return;
    }

    let visibleFocusCount = 0;
    const elementsWithoutFocus: string[] = [];
    const maxTests = Math.min(count, 20);

    for (let i = 0; i < maxTests; i++) {
      const element = focusableElements.nth(i);

      await element.focus();

      const hasVisibleFocus = await element.evaluate((el) => {
        const styles = window.getComputedStyle(el);
        return (
          (styles.outlineWidth !== "0px" && styles.outlineStyle !== "none") ||
          styles.boxShadow !== "none"
        );
      });

      if (hasVisibleFocus) {
        visibleFocusCount++;
      } else {
        const identifier = await element.evaluate(
          (el) => el.className || el.id || el.tagName
        );
        elementsWithoutFocus.push(identifier);
      }
    }

    const isConforme = elementsWithoutFocus.length === 0;

    testResults.push({
      criterion: "10.7",
      test: "Visibilité de la prise de focus",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${visibleFocusCount}/${maxTests} éléments avec focus visible`,
      elements: elementsWithoutFocus.slice(0, 10),
    });

    expect(elementsWithoutFocus.length).toBe(0);
  });

  test("10.11 - Contenus cachés correctement", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Vérifier que les contenus cachés utilisent les bonnes techniques
    const hiddenElements = page.locator("[aria-hidden='true']");
    const count = await hiddenElements.count();

    if (count === 0) {
      testResults.push({
        criterion: "10.11",
        test: "Contenus cachés correctement",
        status: "non-applicable",
      });
      return;
    }

    let conformeCount = 0;
    const issues: string[] = [];

    for (let i = 0; i < count; i++) {
      const element = hiddenElements.nth(i);

      // Vérifier que l'élément n'est pas focusable
      const hasFocusableChildren = await element.evaluate((el) => {
        const focusable = el.querySelectorAll(
          'a[href], button, input, select, textarea, [tabindex]:not([tabindex="-1"])'
        );
        return focusable.length > 0;
      });

      if (!hasFocusableChildren) {
        conformeCount++;
      } else {
        const identifier = await element.evaluate(
          (el) => el.className || el.id || "hidden-element"
        );
        issues.push(`${identifier}: contient des éléments focusables`);
      }
    }

    const isConforme = issues.length === 0;

    testResults.push({
      criterion: "10.11",
      test: "Contenus cachés correctement",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeCount}/${count} contenus cachés sans éléments focusables`,
      elements: issues,
    });

    expect(issues.length).toBe(0);
  });

  test("10.12 - Espacement du texte redéfinissable (WCAG 1.4.12)", async ({
    page,
  }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Compter quelques éléments interactifs avant (pour détecter perte de fonctionnalité)
    const interactors = page.locator(
      "a[href], button, input:not([type='hidden']), select, textarea, [role='button']"
    );
    const beforeCount = await interactors.count();

    // Appliquer les surcharges d'espacement de texte recommandées (WCAG 1.4.12)
    await page.addStyleTag({
      content: `
        /* Valeurs recommandées WCAG 1.4.12 */
        html, body, body * {
          line-height: 1.5 !important;
          letter-spacing: 0.12em !important;
          word-spacing: 0.16em !important;
        }
        /* Espacement des paragraphes */
        p, li, blockquote { margin-bottom: 2em !important; }
      `,
    });

    await page.waitForTimeout(300);

    // Vérifier absence de perte de contenu (détection de clipping courant)
    const issues: string[] = [];
    const textContainers = page.locator(
      "p, li, dd, dt, blockquote, article, section, main, aside, div, span"
    );

    const total = Math.min(await textContainers.count(), 80);
    for (let i = 0; i < total; i++) {
      const el = textContainers.nth(i);
      const problem = await el.evaluate((node: HTMLElement) => {
        // Exclure bannières/overlays de consentement et CMP connus
        const EXCLUDE_SELECTORS = [
          // OneTrust
          "#onetrust-consent-sdk",
          "#onetrust-banner-sdk",
          // Didomi
          "#didomi-host",
          ".didomi-popup",
          // Sourcepoint
          '[id^="sp_message_container"]',
          '[id^="sp_message_iframe"]',
          // Axeptio
          "#axeptio_overlay",
          "#axeptio_modal",
          "#axeptio-iframe",
          // Tarteaucitron
          "#tarteaucitronRoot",
          "#tarteaucitron",
          ".tarteaucitron-root",
          // Quantcast
          "#qc-cmp2-container",
          "#qc-cmp2-ui",
          // Osano
          ".osano-cm-window",
          ".osano-cm-dialog",
          // Générique cookies/consent
          "#cookieconsent",
          ".cc-window",
          ".cookie-consent",
          ".cookie-banner",
          "#cookie-banner",
          ".cookies-banner",
          ".cmp-container",
          ".cmp-modal",
          "[data-consent]",
          // iframes de consentement courants
          'iframe[src*="consent" i]',
          'iframe[src*="tarteaucitron" i]',
          'iframe[src*="didomi" i]',
          'iframe[src*="onetrust" i]',
        ];
        if (EXCLUDE_SELECTORS.some((sel) => (node.closest as any)?.(sel))) {
          return "";
        }
        const cs = window.getComputedStyle(node);
        const overflowX = cs.overflowX;
        const overflowY = cs.overflowY;
        const clipX = node.scrollWidth - node.clientWidth > 1;
        const clipY = node.scrollHeight - node.clientHeight > 1;
        const hidesX = overflowX === "hidden" || overflowX === "clip";
        const hidesY = overflowY === "hidden" || overflowY === "clip";

        // Cas de découpe probable: contenu déborde et masqué
        const clipped = (clipX && hidesX) || (clipY && hidesY);

        // Ignorer conteneurs scrollers (autorisé)
        const isScrollable =
          overflowY === "auto" ||
          overflowY === "scroll" ||
          overflowX === "auto" ||
          overflowX === "scroll";

        if (clipped && !isScrollable) {
          const id = node.id ? `#${node.id}` : "";
          const cls =
            node.className && typeof node.className === "string"
              ? `.${node.className.split(" ").slice(0, 2).join(".")}`
              : "";
          const tag = node.tagName.toLowerCase();
          const sample = (node.textContent || "").trim().slice(0, 60);
          return `${tag}${id}${cls} → contenu masqué (extrait: "${sample}")`;
        }
        return "";
      });
      if (problem) issues.push(problem);
      if (issues.length >= 10) break;
    }

    // Vérifier conservation des éléments interactifs
    const afterCount = await interactors.count();
    if (afterCount < beforeCount) {
      issues.push(
        `Perte d'éléments interactifs: avant=${beforeCount}, après=${afterCount}`
      );
    }

    const isConforme = issues.length === 0;

    testResults.push({
      criterion: "10.12",
      test: "Espacement du texte redéfinissable (1.4.12)",
      status: isConforme ? "conforme" : "non-conforme",
      details: isConforme
        ? "Aucune perte de contenu ou de fonctionnalité détectée avec les espacements"
        : `${issues.length} problème(s) détecté(s) (extraits limités)`,
      elements: issues,
    });

    expect(issues.length).toBe(0);
  });

  test("10.2 - Contenu visible présent sans CSS", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Baseline: relever un échantillon de contenus visibles
    const baseline = await page.evaluate(() => {
      const SEL = [
        "h1",
        "h2",
        "h3",
        "h4",
        "h5",
        "h6",
        "p",
        "li",
        "dt",
        "dd",
        "th",
        "td",
        "a",
        "button",
        "label",
        "summary",
        "figcaption",
        "caption",
      ].join(",");
      const uniq = new Set<string>();
      function visible(el: HTMLElement) {
        const cs = getComputedStyle(el);
        const rect = el.getBoundingClientRect();
        return (
          cs.visibility !== "hidden" &&
          cs.display !== "none" &&
          rect.width > 0 &&
          rect.height > 0
        );
      }
      for (const el of Array.from(
        document.querySelectorAll(SEL)
      ) as HTMLElement[]) {
        if (!visible(el)) continue;
        const txt = (el.innerText || "").trim().replace(/\s+/g, " ");
        if (txt.length >= 3) {
          uniq.add(txt.toLowerCase());
          if (uniq.size >= 80) break;
        }
      }
      return Array.from(uniq);
    });

    // Ouvrir une nouvelle page avec CSS désactivé
    const ctx = page.context();
    const p2 = await ctx.newPage();
    await p2.route("**/*", (route) => {
      const req = route.request();
      if (req.resourceType() === "stylesheet") return route.abort();
      return route.continue();
    });
    await p2.goto(process.env.TARGET_URL || "/");

    await p2.evaluate(() => {
      document
        .querySelectorAll('style, link[rel="stylesheet" i]')
        .forEach((n) => n.remove());
      const all = document.querySelectorAll("*");
      for (const el of Array.from(all) as HTMLElement[]) {
        if (el.hasAttribute("style")) el.removeAttribute("style");
      }
    });

    const noCss = await p2.evaluate(() => {
      const SEL = [
        "h1",
        "h2",
        "h3",
        "h4",
        "h5",
        "h6",
        "p",
        "li",
        "dt",
        "dd",
        "th",
        "td",
        "a",
        "button",
        "label",
        "summary",
        "figcaption",
        "caption",
      ].join(",");
      const uniq = new Set<string>();
      function visible(el: HTMLElement) {
        const cs = getComputedStyle(el);
        const rect = el.getBoundingClientRect();
        return (
          cs.visibility !== "hidden" &&
          cs.display !== "none" &&
          rect.width > 0 &&
          rect.height > 0
        );
      }
      for (const el of Array.from(
        document.querySelectorAll(SEL)
      ) as HTMLElement[]) {
        if (!visible(el)) continue;
        const txt = (el.innerText || "").trim().replace(/\s+/g, " ");
        if (txt.length >= 3) {
          uniq.add(txt.toLowerCase());
          if (uniq.size >= 120) break;
        }
      }
      return Array.from(uniq);
    });

    await p2.close();

    const missing = baseline.filter((t) => !noCss.includes(t));
    const isConforme = missing.length === 0;
    testResults.push({
      criterion: "10.2",
      test: "Contenu visible présent sans CSS",
      status: isConforme ? "conforme" : "non-conforme",
      details: isConforme
        ? "Les contenus visibles porteurs d’information restent présents une fois les styles désactivés."
        : `${missing.length} contenu(s) absents sans CSS (extraits limités)`,
      elements: missing,
    });

    expect(missing.length).toBe(0);
  });
});
