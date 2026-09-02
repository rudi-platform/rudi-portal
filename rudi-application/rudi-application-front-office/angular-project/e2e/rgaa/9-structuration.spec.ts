import { test, expect } from "@playwright/test";
import {
  RGAA_SELECTORS,
  RGAATestResult,
  generateRGAAReport,
} from "../utils/rgaa-helpers";

test.describe("RGAA 4.1.2 - Thématique 9: Structuration de l'information", () => {
  let testResults: RGAATestResult[] = [];

  test.afterAll(async () => {
    console.log(generateRGAAReport(testResults));
  });

  test("9.1 - Hiérarchie des titres cohérente", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    const headings = page.locator(RGAA_SELECTORS.headings);
    const count = await headings.count();

    if (count === 0) {
      testResults.push({
        criterion: "9.1",
        test: "Hiérarchie des titres",
        status: "non-applicable",
        details: "Aucun titre trouvé",
      });
      return;
    }

    // Vérifier la hiérarchie des titres
    const headingLevels: number[] = [];
    for (let i = 0; i < count; i++) {
      const heading = headings.nth(i);
      const tagName = await heading.evaluate((el) => el.tagName.toLowerCase());
      const level = parseInt(tagName.replace("h", ""));
      headingLevels.push(level);
    }

    // Vérifier qu'on commence par h1
    const startsWithH1 = headingLevels[0] === 1;

    // Vérifier qu'il n'y a pas de saut de niveau
    let hasSkippedLevels = false;
    const issues: string[] = [];

    for (let i = 1; i < headingLevels.length; i++) {
      const diff = headingLevels[i] - headingLevels[i - 1];
      if (diff > 1) {
        hasSkippedLevels = true;
        issues.push(`Saut de h${headingLevels[i - 1]} à h${headingLevels[i]}`);
      }
    }

    if (!startsWithH1) {
      issues.push(`Premier titre est h${headingLevels[0]} au lieu de h1`);
    }

    const isConforme = startsWithH1 && !hasSkippedLevels;

    testResults.push({
      criterion: "9.1",
      test: "Hiérarchie des titres",
      status: isConforme ? "conforme" : "non-conforme",
      details: isConforme
        ? `${count} titres avec hiérarchie correcte`
        : `Problèmes de hiérarchie détectés`,
      elements: issues,
    });

    expect(isConforme, `Problèmes: ${issues.join(", ")}`).toBe(true);
  });

  test("9.2 - Structure du document avec landmarks", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Vérifier la présence des landmarks principaux
    const main = page.locator('main, [role="main"]');
    const nav = page.locator('nav, [role="navigation"]');
    const header = page.locator('header, [role="banner"]');
    const footer = page.locator('footer, [role="contentinfo"]');

    const hasMain = (await main.count()) > 0;
    const hasNav = (await nav.count()) > 0;
    const hasHeader = (await header.count()) > 0;
    const hasFooter = (await footer.count()) > 0;

    const missingLandmarks: string[] = [];
    if (!hasMain) missingLandmarks.push("main");
    if (!hasNav) missingLandmarks.push("navigation");
    if (!hasHeader) missingLandmarks.push("header/banner");
    if (!hasFooter) missingLandmarks.push("footer/contentinfo");

    const isConforme = missingLandmarks.length === 0;

    testResults.push({
      criterion: "9.2",
      test: "Structure du document",
      status: isConforme ? "conforme" : "non-conforme",
      details: isConforme
        ? "Tous les landmarks principaux présents"
        : `Landmarks manquants: ${missingLandmarks.join(", ")}`,
      elements: missingLandmarks,
    });

    expect(isConforme).toBe(true);
  });

  test("9.3 - Listes structurées correctement", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    const listCheck = await page.evaluate(() => {
      function isTextContentMeaningful(n: Node): boolean {
        if (n.nodeType !== Node.TEXT_NODE) return false;
        const txt = n.textContent || "";
        if (txt.trim().length === 0) return false;
        const stripped = txt.replace(/\s+/g, "").replace(/[.,;:!?\-]/g, "");
        return stripped.length > 0;
      }

      const issues: string[] = [];
      const elements: string[] = [];

      const stdLists = Array.from(
        document.querySelectorAll("ul, ol, [role='list']"),
      );
      let stdCorrect = 0;
      for (let i = 0; i < stdLists.length; i++) {
        const list = stdLists[i] as HTMLElement;
        const tag = list.tagName.toLowerCase();
        const isAriaList =
          (list.getAttribute("role") || "").toLowerCase() === "list";
        const liChildren = Array.from(list.children).filter(
          (c) =>
            (c as Element).tagName &&
            (c as Element).tagName.toLowerCase() === "li",
        );
        const ariaItems = Array.from(list.children).filter(
          (c) =>
            (c as Element).getAttribute("role")?.toLowerCase() === "listitem",
        );
        const hasValidItems = isAriaList
          ? ariaItems.length > 0 || liChildren.length > 0
          : liChildren.length > 0;

        const badChildren = Array.from(list.childNodes).filter((n) => {
          if (n.nodeType === Node.ELEMENT_NODE) {
            const el = n as Element;
            const tn = el.tagName.toLowerCase();
            const role = (el.getAttribute("role") || "").toLowerCase();
            if (isAriaList) return tn !== "li" && role !== "listitem";
            return tn !== "li";
          }
          return isTextContentMeaningful(n);
        });

        const nestedLists = Array.from(
          list.querySelectorAll(
            ":scope > ul, :scope > ol, :scope > [role='list']",
          ),
        );
        const nestedOutsideItem = nestedLists.filter((nl) => {
          const parent = nl.parentElement;
          if (!parent) return true;
          const tn = parent.tagName.toLowerCase();
          const role = (parent.getAttribute("role") || "").toLowerCase();
          return tn !== "li" && role !== "listitem";
        });

        if (!hasValidItems) {
          issues.push(
            `${isAriaList ? "role=list" : tag} sans élément de liste (<li> ou role=listitem)`,
          );
          elements.push(`${isAriaList ? "role=list" : tag}#${i}`);
        } else if (badChildren.length > 0) {
          issues.push(
            `${isAriaList ? "role=list" : tag} contient des enfants non <li>/listitem ou texte direct`,
          );
          elements.push(`${isAriaList ? "role=list" : tag}#${i}`);
        } else if (nestedOutsideItem.length > 0) {
          issues.push(
            `${isAriaList ? "role=list" : tag} liste imbriquée non contenue dans <li>/listitem`,
          );
          elements.push(`${isAriaList ? "role=list" : tag}#${i}`);
        } else {
          stdCorrect++;
        }
      }

      const descLists = Array.from(document.querySelectorAll("dl"));
      let descCorrect = 0;
      for (let i = 0; i < descLists.length; i++) {
        const dl = descLists[i] as HTMLElement;
        const children = Array.from(dl.children);
        const hasDT = children.some(
          (c) => (c as Element).tagName.toLowerCase() === "dt",
        );
        const hasDD = children.some(
          (c) => (c as Element).tagName.toLowerCase() === "dd",
        );
        const badChildren = children.filter((c) => {
          const tn = (c as Element).tagName.toLowerCase();
          return tn !== "dt" && tn !== "dd";
        });
        let orderOk = true;
        for (let j = 0; j < children.length; j++) {
          const tn = (children[j] as Element).tagName.toLowerCase();
          if (tn === "dd") {
            const hasPrevDT = children
              .slice(0, j)
              .some((c) => (c as Element).tagName.toLowerCase() === "dt");
            if (!hasPrevDT) {
              orderOk = false;
              break;
            }
          }
        }
        if (!hasDT || !hasDD) {
          issues.push("dl sans dt ou dd");
          elements.push(`dl#${i}`);
        } else if (badChildren.length > 0) {
          issues.push("dl contient des enfants autres que dt/dd");
          elements.push(`dl#${i}`);
        } else if (!orderOk) {
          issues.push("dl: dd doit suivre un dt");
          elements.push(`dl#${i}`);
        } else {
          descCorrect++;
        }
      }

      const total = stdLists.length + descLists.length;
      const correctCount = stdCorrect + descCorrect;
      const applicable = total > 0;
      const ok = issues.length === 0;
      return {
        applicable,
        ok,
        total,
        correctCount,
        stdTotal: stdLists.length,
        descTotal: descLists.length,
        stdCorrect,
        descCorrect,
        issues,
        elements,
      };
    });

    const status = listCheck.applicable
      ? listCheck.ok
        ? "conforme"
        : "non-conforme"
      : "non-applicable";
    const details = listCheck.applicable
      ? listCheck.ok
        ? `${listCheck.correctCount}/${listCheck.total} listes correctement structurées (ul/ol/role=list: ${listCheck.stdCorrect}/${listCheck.stdTotal}, dl: ${listCheck.descCorrect}/${listCheck.descTotal})`
        : `Problèmes: ${listCheck.issues.join(" | ")}`
      : "Aucune liste détectée (ul/ol/role=list/dl): critère non applicable";

    testResults.push({
      criterion: "9.3",
      test: "Listes structurées",
      status,
      details,
      elements: listCheck.elements,
    });

    expect(status === "conforme").toBe(true);
  });

  test("9.4 - Citations identifiées", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    const quotes = page.locator("q, blockquote, cite");
    const count = await quotes.count();

    if (count === 0) {
      testResults.push({
        criterion: "9.4",
        test: "Citations identifiées",
        status: "non-applicable",
        details: "Aucune citation trouvée",
      });
      return;
    }

    let conformeCount = 0;
    const nonConformeElements: string[] = [];

    for (let i = 0; i < count; i++) {
      const quote = quotes.nth(i);
      const tagName = await quote.evaluate((el) => el.tagName.toLowerCase());
      const textContent = await quote.textContent();

      // Les citations doivent avoir du contenu
      const hasContent = textContent && textContent.trim().length > 0;

      if (hasContent) {
        conformeCount++;
      } else {
        nonConformeElements.push(`${tagName}-${i}`);
      }
    }

    const isConforme = nonConformeElements.length === 0;

    testResults.push({
      criterion: "9.4",
      test: "Citations identifiées",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeCount}/${count} citations avec contenu`,
      elements: nonConformeElements,
    });

    expect(nonConformeElements.length).toBe(0);
  });
});
