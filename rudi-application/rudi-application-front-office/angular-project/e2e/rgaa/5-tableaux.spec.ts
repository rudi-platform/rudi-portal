import { test, expect } from "@playwright/test";
import {
  RGAA_SELECTORS,
  RGAATestResult,
  generateRGAAReport,
  getAccessibleName,
} from "../utils/rgaa-helpers";

test.describe("RGAA 4.1.2 - Thématique 5: Tableaux", () => {
  let testResults: RGAATestResult[] = [];

  test.afterAll(async () => {
    console.log(generateRGAAReport(testResults));
  });

  test("5.1 - Résumé pour tableaux de données complexes", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    type ComplexInfo = {
      id: string;
      hasSummary: boolean;
      sources: string[];
      reasons: string[];
    };

    const complexTables: ComplexInfo[] = await page.evaluate(() => {
      function getTextByIds(root: Document, idsStr: string | null): string {
        const ids = (idsStr || "").trim().split(/\s+/).filter(Boolean);
        let s = "";
        for (const id of ids) {
          const el = root.getElementById(id);
          const txt = el?.textContent || "";
          if (txt.trim().length > 0) s += txt + " ";
        }
        return s.trim();
      }

      const infos: ComplexInfo[] = [] as any;

      // Native HTML tables
      const nativeTables = Array.from(document.querySelectorAll("table"));
      nativeTables.forEach((table, idx) => {
        const ths = Array.from(table.querySelectorAll("th"));
        if (ths.length === 0) {
          // Pas un tableau de données -> non applicable
          return;
        }

        const reasons: string[] = [];
        let isComplex = false;

        const thead = table.querySelector("thead");
        const theadRows = thead ? thead.querySelectorAll("tr").length : 0;
        if (theadRows > 1) {
          isComplex = true;
          reasons.push("en-têtes sur plusieurs lignes (thead)");
        }

        // <th> dans tbody au-delà de la 1ère colonne -> complexité (en-têtes internes)
        const tbodyThs = Array.from(table.querySelectorAll("tbody th"));
        for (const th of tbodyThs) {
          const cellIndex = (th as HTMLTableCellElement).cellIndex ?? -1;
          const scope = th.getAttribute("scope");
          // Autoriser th de ligne en 1ère colonne (scope=row)
          if (cellIndex > 0 || (scope && scope !== "row")) {
            isComplex = true;
            reasons.push("en-têtes dans tbody hors 1ère colonne");
            break;
          }
        }

        // Groupes d'en-têtes via colspan/rowspan
        for (const th of ths) {
          const colspan = parseInt(th.getAttribute("colspan") || "0", 10);
          const rowspan = parseInt(th.getAttribute("rowspan") || "0", 10);
          const scope = th.getAttribute("scope");
          if (
            colspan > 1 ||
            rowspan > 1 ||
            scope === "colgroup" ||
            scope === "rowgroup"
          ) {
            isComplex = true;
            reasons.push("groupes d'en-têtes (colspan/rowspan/scope)");
            break;
          }
        }

        // Cellules référencées par plusieurs en-têtes
        const cellsWithMultipleHeaders = Array.from(
          table.querySelectorAll("td[headers], th[headers]"),
        ).some((el) => {
          const headers = (el.getAttribute("headers") || "").trim();
          const ids = headers.split(/\s+/).filter(Boolean);
          return ids.length > 1;
        });
        if (cellsWithMultipleHeaders) {
          isComplex = true;
          reasons.push("cellules liées à plusieurs en-têtes (headers)");
        }

        // Indice supplémentaire de complexité : présence de colgroup
        if (table.querySelectorAll("colgroup").length > 0) {
          isComplex = true;
          reasons.push("groupement de colonnes (colgroup)");
        }

        // Détection de résumé
        const caption = table.querySelector("caption");
        const hasCaptionText = !!(
          caption &&
          caption.textContent &&
          caption.textContent.trim().length > 0
        );
        const summaryAttr = table.getAttribute("summary");
        const hasSummaryAttr = !!(summaryAttr && summaryAttr.trim().length > 0);
        const describedText = getTextByIds(
          document,
          table.getAttribute("aria-describedby"),
        );
        const hasAriaDescribedbyText = describedText.trim().length > 0;

        const hasSummary =
          hasCaptionText || hasSummaryAttr || hasAriaDescribedbyText;

        if (isComplex) {
          infos.push({
            id: table.getAttribute("id") || `table-${idx}`,
            hasSummary,
            sources: [
              hasCaptionText ? "caption" : null,
              hasSummaryAttr ? "summary" : null,
              hasAriaDescribedbyText ? "aria-describedby" : null,
            ].filter(Boolean) as string[],
            reasons,
          });
        }
      });

      // ARIA role="table"
      const roleTables = Array.from(
        document.querySelectorAll('[role="table"]'),
      );
      roleTables.forEach((t, idx) => {
        const rows = Array.from(t.querySelectorAll('[role="row"]'));
        // Lignes qui contiennent des columnheader
        const headerRowsCount = rows.filter(
          (r) => r.querySelectorAll('[role="columnheader"]').length > 0,
        ).length;
        const isComplex = headerRowsCount > 1; // plusieurs lignes d'en-têtes

        if (!isComplex) return;

        const describedText = getTextByIds(
          document,
          t.getAttribute("aria-describedby"),
        );
        const hasSummary = describedText.trim().length > 0;

        infos.push({
          id: t.getAttribute("id") || `role-table-${idx}`,
          hasSummary,
          sources: hasSummary ? ["aria-describedby"] : [],
          reasons: ["en-têtes ARIA sur plusieurs lignes"],
        });
      });

      return infos;
    });

    const complexCount = complexTables.length;
    if (complexCount === 0) {
      testResults.push({
        criterion: "5.1",
        test: "Résumé pour tableaux de données complexes",
        status: "non-applicable",
        details: "Aucun tableau de données complexe détecté",
      });
      return;
    }

    const nonConforme = complexTables.filter((t) => !t.hasSummary);
    const conformeCount = complexCount - nonConforme.length;

    testResults.push({
      criterion: "5.1",
      test: "Résumé pour tableaux de données complexes",
      status: nonConforme.length === 0 ? "conforme" : "non-conforme",
      details: `${conformeCount}/${complexCount} tableaux complexes avec résumé (caption/summary/aria-describedby)`,
      elements: nonConforme.map((t) => t.id),
    });

    expect
      .soft(
        nonConforme.length,
        `5.1: tableaux complexes sans résumé: ${nonConforme
          .map((t) => t.id)
          .join(", ")}`,
      )
      .toBe(0);
  });

  test("5.4 - Titres des tableaux correctement associés", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    type TitledInfo = {
      id: string;
      isValid: boolean;
      source: string;
      reason?: string;
    };

    const titledTables: TitledInfo[] = await page.evaluate(() => {
      function getTextByIds(root: Document, idsStr: string | null): string {
        const ids = (idsStr || "").trim().split(/\s+/).filter(Boolean);
        let s = "";
        for (const id of ids) {
          const el = root.getElementById(id);
          const txt = el?.textContent || "";
          if (txt.trim().length > 0) s += txt + " ";
        }
        return s.trim();
      }

      const infos: TitledInfo[] = [] as any;

      // 1) Tables HTML natives
      const nativeTables = Array.from(document.querySelectorAll("table"));
      nativeTables.forEach((table, idx) => {
        // Déterminer si le tableau a un titre et sa source
        const caption = table.querySelector(":scope > caption");
        const captionText = (caption?.textContent || "").trim();
        const titleAttr = (table.getAttribute("title") || "").trim();
        const ariaLabel = (table.getAttribute("aria-label") || "").trim();
        const ariaLabelledby = (
          table.getAttribute("aria-labelledby") || ""
        ).trim();

        let hasTitle = false;
        let isValid = false;
        let source = "";
        let reason = "";

        if (captionText.length > 0) {
          hasTitle = true;
          isValid = true;
          source = "caption";
        } else if (titleAttr.length > 0) {
          hasTitle = true;
          isValid = true;
          source = "title";
        } else if (ariaLabel.length > 0) {
          hasTitle = true;
          isValid = true;
          source = "aria-label";
        } else if (ariaLabelledby.length > 0) {
          hasTitle = true;
          const labelledText = getTextByIds(document, ariaLabelledby);
          isValid = labelledText.length > 0;
          source = "aria-labelledby";
          if (!isValid) reason = "aria-labelledby sans texte référencé";
        }

        if (hasTitle) {
          infos.push({
            id: table.getAttribute("id") || `table-${idx}`,
            isValid,
            source,
            reason: isValid ? undefined : reason,
          });
        }
      });

      // 2) Tables ARIA role="table"
      const ariaTables = Array.from(
        document.querySelectorAll('[role="table"]'),
      );
      ariaTables.forEach((t, idx) => {
        const titleAttr = (t.getAttribute("title") || "").trim();
        const ariaLabel = (t.getAttribute("aria-label") || "").trim();
        const ariaLabelledby = (t.getAttribute("aria-labelledby") || "").trim();

        let hasTitle = false;
        let isValid = false;
        let source = "";
        let reason = "";

        if (titleAttr.length > 0) {
          hasTitle = true;
          isValid = true;
          source = "title";
        } else if (ariaLabel.length > 0) {
          hasTitle = true;
          isValid = true;
          source = "aria-label";
        } else if (ariaLabelledby.length > 0) {
          hasTitle = true;
          const labelledText = getTextByIds(document, ariaLabelledby);
          isValid = labelledText.length > 0;
          source = "aria-labelledby";
          if (!isValid) reason = "aria-labelledby sans texte référencé";
        }

        if (hasTitle) {
          infos.push({
            id: t.getAttribute("id") || `role-table-${idx}`,
            isValid,
            source,
            reason: isValid ? undefined : reason,
          });
        }
      });

      return infos;
    });

    const titledCount = titledTables.length;
    if (titledCount === 0) {
      testResults.push({
        criterion: "5.4",
        test: "Titres des tableaux associés",
        status: "non-applicable",
        details: "Aucun tableau de données pourvu d'un titre détecté",
      });
      return;
    }

    const invalid = titledTables.filter((t) => !t.isValid);
    const conformeCount = titledCount - invalid.length;

    testResults.push({
      criterion: "5.4",
      test: "Titres des tableaux associés",
      status: invalid.length === 0 ? "conforme" : "non-conforme",
      details: `${conformeCount}/${titledCount} tableaux titulés correctement associés (sources: caption/title/aria-label/aria-labelledby)`,
      elements: invalid.map((t) => `${t.id}${t.reason ? ": " + t.reason : ""}`),
    });

    expect
      .soft(
        invalid.length,
        `5.4: tableaux titulés mal associés: ${invalid
          .map((t) => `${t.id}${t.reason ? ": " + t.reason : ""}`)
          .join(" | ")}`,
      )
      .toBe(0);
  });

  test("5.6 - En-têtes de colonnes et lignes identifiés", async ({ page }) => {
    await page.goto(process.env.TARGET_URL || "/");

    const tables = page.locator("table");
    const count = await tables.count();

    if (count === 0) {
      testResults.push({
        criterion: "5.6",
        test: "En-têtes de tableaux identifiés",
        status: "non-applicable",
      });
      return;
    }

    let conformeCount = 0;
    const nonConformeElements: string[] = [];
    let applicableCount = 0;
    let colHeaderOkCount = 0;
    let rowHeaderOkCount = 0;

    for (let i = 0; i < count; i++) {
      const table = tables.nth(i);

      // Présence d'au moins une balise <th>
      const thAll = table.locator("th");
      const thCount = await thAll.count();
      const hasHeaders = thCount > 0;

      // Au moins un <th> avec attribut scope OU <td> avec headers
      const anyThHasScope = (await table.locator("th[scope]").count()) > 0;
      const anyTdHeaders =
        (await table.locator("td[headers], th[headers]").count()) > 0;

      // Ligne d'en-tête détectée (des <th> dans thead ou dans la première ligne du tableau)
      const hasTheadTh = (await table.locator("thead th").count()) > 0;
      const firstRowHasTh = await table.evaluate((t) => {
        const firstTr =
          t.querySelector("thead tr") ||
          t.querySelector("tbody tr") ||
          t.querySelector("tr");
        return !!(firstTr && firstTr.querySelector("th"));
      });

      // En-têtes de lignes détectés (th dans tbody en première colonne ou scope=row)
      const hasRowHeader = await table.evaluate((t) => {
        const rows = Array.from(t.querySelectorAll("tbody tr, tr"));
        for (const r of rows) {
          const firstCell = (r as HTMLTableRowElement).cells?.[0];
          if (firstCell && firstCell.tagName.toLowerCase() === "th")
            return true;
          const ths = Array.from(r.querySelectorAll("th"));
          if (ths.some((th) => th.getAttribute("scope") === "row")) return true;
        }
        return false;
      });

      const hasColHeader =
        hasTheadTh ||
        firstRowHasTh ||
        (await table.locator("th[scope='col'], th[scope='colgroup']").count()) >
          0;

      // Heuristique: si le tableau semble être de mise en forme (pas de th, pas de thead, pas de caption, rôle non-table),
      // alors ce critère 5.6 n'est pas applicable.
      const seemsLayout =
        !hasHeaders &&
        !hasTheadTh &&
        (await table.locator("caption").count()) === 0 &&
        !((await table.getAttribute("role")) === "table");
      if (seemsLayout) {
        continue;
      }
      applicableCount++;

      const headerAssociationsOk =
        anyThHasScope ||
        anyTdHeaders ||
        hasTheadTh ||
        firstRowHasTh ||
        hasRowHeader ||
        hasColHeader;

      if (hasColHeader) colHeaderOkCount++;
      if (hasRowHeader) rowHeaderOkCount++;

      if (hasHeaders && headerAssociationsOk) {
        conformeCount++;
      } else {
        const hint = await table.evaluate((el) => {
          function cssPath(node: Element): string {
            if ((node as HTMLElement).id) return `#${(node as HTMLElement).id}`;
            const parts: string[] = [];
            let e: Element | null = node;
            while (
              e &&
              e.nodeType === 1 &&
              e.tagName.toLowerCase() !== "html"
            ) {
              let sel = e.tagName.toLowerCase();
              const cls = Array.from((e as HTMLElement).classList).slice(0, 3);
              if (cls.length) sel += "." + cls.join(".");
              const p = e.parentElement;
              if (p) {
                const same = Array.from(p.children).filter(
                  (c) => c.tagName === e!.tagName,
                );
                const idx = same.indexOf(e);
                if (same.length > 1) sel += `:nth-of-type(${idx + 1})`;
              }
              parts.unshift(sel);
              e = e.parentElement;
            }
            return parts.join(" > ");
          }
          function nearestHeadingText(node: Element): string {
            const headings = Array.from(
              document.querySelectorAll("h1,h2,h3,h4,h5,h6"),
            );
            let best = "";
            let bestDist = Infinity;
            const rect = node.getBoundingClientRect();
            for (const h of headings) {
              const r = h.getBoundingClientRect();
              const dy = Math.max(0, rect.top - r.bottom);
              if (dy >= 0 && dy < bestDist) {
                const t = (h.textContent || "").trim();
                if (t.length > 0) {
                  best = t;
                  bestDist = dy;
                }
              }
            }
            return best;
          }
          const r = el.getBoundingClientRect();
          const txt = (el.textContent || "").trim();
          return {
            css: cssPath(el),
            near: nearestHeadingText(el),
            rect: `${Math.round(r.left)},${Math.round(r.top)},${Math.round(r.width)}x${Math.round(r.height)}`,
            text: txt.slice(0, 80),
          };
        });
        const identifier = (await table.getAttribute("id")) || `table-${i}`;
        const reason = !hasHeaders
          ? "pas de <th>"
          : !hasColHeader && hasRowHeader
            ? "en-têtes de colonnes non identifiés"
            : !hasRowHeader && hasColHeader
              ? "en-têtes de lignes non identifiés"
              : "en-têtes non identifiés (scope/thead/headers/ligne/colonne)";
        nonConformeElements.push(
          `${identifier}: ${reason} | css: ${hint.css} | near: ${hint.near || "N/A"} | rect: ${hint.rect} | text: ${hint.text}`,
        );
      }
    }

    // Parcours des tableaux ARIA role="table"
    const ariaTables = await page.evaluate(() => {
      const infos: Array<{
        id: string;
        hasColHeader: boolean;
        hasRowHeader: boolean;
        css: string;
        near: string;
        rect: string;
      }> = [];
      const roleTables = Array.from(
        document.querySelectorAll('[role="table"]'),
      );
      for (let idx = 0; idx < roleTables.length; idx++) {
        const t = roleTables[idx] as HTMLElement;
        const id = t.getAttribute("id") || `role-table-${idx}`;
        const hasColHeader = t.querySelector('[role="columnheader"]') !== null;
        const hasRowHeader = t.querySelector('[role="rowheader"]') !== null;
        function cssPath(node: Element): string {
          if ((node as HTMLElement).id) return `#${(node as HTMLElement).id}`;
          const parts: string[] = [];
          let e: Element | null = node;
          while (e && e.nodeType === 1 && e.tagName.toLowerCase() !== "html") {
            let sel = e.tagName.toLowerCase();
            const cls = Array.from((e as HTMLElement).classList).slice(0, 3);
            if (cls.length) sel += "." + cls.join(".");
            const p = e.parentElement;
            if (p) {
              const same = Array.from(p.children).filter(
                (c) => c.tagName === e!.tagName,
              );
              const idx2 = same.indexOf(e);
              if (same.length > 1) sel += `:nth-of-type(${idx2 + 1})`;
            }
            parts.unshift(sel);
            e = e.parentElement;
          }
          return parts.join(" > ");
        }
        function nearestHeadingText(node: Element): string {
          const headings = Array.from(
            document.querySelectorAll("h1,h2,h3,h4,h5,h6"),
          );
          let best = "";
          let bestDist = Infinity;
          const rect = node.getBoundingClientRect();
          for (const h of headings) {
            const r = h.getBoundingClientRect();
            const dy = Math.max(0, rect.top - r.bottom);
            if (dy >= 0 && dy < bestDist) {
              const t = (h.textContent || "").trim();
              if (t.length > 0) {
                best = t;
                bestDist = dy;
              }
            }
          }
          return best;
        }
        const r = t.getBoundingClientRect();
        infos.push({
          id,
          hasColHeader,
          hasRowHeader,
          css: cssPath(t),
          near: nearestHeadingText(t),
          rect: `${Math.round(r.left)},${Math.round(r.top)},${Math.round(r.width)}x${Math.round(r.height)}`,
        });
      }
      return infos;
    });

    for (const it of ariaTables) {
      applicableCount++;
      const headerAssociationsOk = it.hasColHeader || it.hasRowHeader;
      if (headerAssociationsOk) {
        conformeCount++;
        if (it.hasColHeader) colHeaderOkCount++;
        if (it.hasRowHeader) rowHeaderOkCount++;
      } else {
        nonConformeElements.push(
          `${it.id}: en-têtes ARIA non identifiés (columnheader/rowheader) | css: ${it.css} | near: ${it.near || "N/A"} | rect: ${it.rect}`,
        );
      }
    }

    if (applicableCount === 0) {
      testResults.push({
        criterion: "5.6",
        test: "En-têtes de tableaux identifiés",
        status: "non-applicable",
        details: "Aucun tableau de données (avec en-têtes) détecté",
      });
      return;
    }

    const isConforme = nonConformeElements.length === 0;

    testResults.push({
      criterion: "5.6",
      test: "En-têtes de tableaux identifiés",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeCount}/${applicableCount} tableaux avec en-têtes correctement identifiés (colonnes: ${colHeaderOkCount}, lignes: ${rowHeaderOkCount})`,
      elements: nonConformeElements,
    });

    expect(nonConformeElements.length).toBe(0);
  });

  test("5.7 - Tableaux de mise en forme sans balises de données", async ({
    page,
  }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Les tableaux de mise en forme ne doivent pas avoir de balises th, thead, tfoot, caption
    const layoutTables = page
      .locator("table")
      .filter({ hasNot: page.locator("th") });
    const count = await layoutTables.count();

    if (count === 0) {
      testResults.push({
        criterion: "5.7",
        test: "Tableaux de mise en forme",
        status: "non-applicable",
      });
      return;
    }

    let conformeCount = 0;
    const nonConformeElements: string[] = [];

    for (let i = 0; i < count; i++) {
      const table = layoutTables.nth(i);

      // Vérifier l'absence de balises de structure de données
      const thead = table.locator("thead");
      const tfoot = table.locator("tfoot");
      const caption = table.locator("caption");
      const th = table.locator("th");

      const hasDataStructure =
        (await thead.count()) > 0 ||
        (await tfoot.count()) > 0 ||
        (await caption.count()) > 0 ||
        (await th.count()) > 0;

      // Les tableaux de mise en forme devraient avoir role="presentation"
      const role = await table.getAttribute("role");
      const hasPresentation = role === "presentation" || role === "none";

      if (!hasDataStructure || hasPresentation) {
        conformeCount++;
      } else {
        const hint = await table.evaluate((el) => {
          function cssPath(node: Element): string {
            if ((node as HTMLElement).id) return `#${(node as HTMLElement).id}`;
            const parts: string[] = [];
            let e: Element | null = node;
            while (
              e &&
              e.nodeType === 1 &&
              e.tagName.toLowerCase() !== "html"
            ) {
              let sel = e.tagName.toLowerCase();
              const cls = Array.from((e as HTMLElement).classList).slice(0, 3);
              if (cls.length) sel += "." + cls.join(".");
              const p = e.parentElement;
              if (p) {
                const same = Array.from(p.children).filter(
                  (c) => c.tagName === e!.tagName,
                );
                const idx = same.indexOf(e);
                if (same.length > 1) sel += `:nth-of-type(${idx + 1})`;
              }
              parts.unshift(sel);
              e = e.parentElement;
            }
            return parts.join(" > ");
          }
          function nearestHeadingText(node: Element): string {
            const headings = Array.from(
              document.querySelectorAll("h1,h2,h3,h4,h5,h6"),
            );
            let best = "";
            let bestDist = Infinity;
            const rect = node.getBoundingClientRect();
            for (const h of headings) {
              const r = h.getBoundingClientRect();
              const dy = Math.max(0, rect.top - r.bottom);
              if (dy >= 0 && dy < bestDist) {
                const t = (h.textContent || "").trim();
                if (t.length > 0) {
                  best = t;
                  bestDist = dy;
                }
              }
            }
            return best;
          }
          const r = el.getBoundingClientRect();
          const txt = (el.textContent || "").trim();
          return {
            css: cssPath(el),
            near: nearestHeadingText(el),
            rect: `${Math.round(r.left)},${Math.round(r.top)},${Math.round(r.width)}x${Math.round(r.height)}`,
            text: txt.slice(0, 80),
          };
        });
        const identifier =
          (await table.getAttribute("id")) || `layout-table-${i}`;
        nonConformeElements.push(
          `${identifier} | css: ${hint.css} | near: ${hint.near || "N/A"} | rect: ${hint.rect} | text: ${hint.text}`,
        );
      }
    }

    const isConforme = nonConformeElements.length === 0;

    testResults.push({
      criterion: "5.7",
      test: "Tableaux de mise en forme",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeCount}/${count} tableaux de mise en forme corrects`,
      elements: nonConformeElements,
    });

    expect(nonConformeElements.length).toBe(0);
  });

  test("5.3 - Tableaux de mise en forme: linéarisation et rôle présentation", async ({
    page,
  }) => {
    await page.goto(process.env.TARGET_URL || "/");

    // Sélection des tableaux de mise en forme (sans <th>)
    const layoutTables = page
      .locator("table")
      .filter({ hasNot: page.locator("th") });
    const count = await layoutTables.count();

    if (count === 0) {
      testResults.push({
        criterion: "5.3",
        test: "Linéarisation cohérente et rôle presentation",
        status: "non-applicable",
        details: "Aucun tableau de mise en forme détecté",
      });
      return;
    }

    let conformeCount = 0;
    const nonConformeElements: string[] = [];

    for (let i = 0; i < count; i++) {
      const table = layoutTables.nth(i);

      // 1) Vérifier role="presentation" ou role="none"
      const role = await table.getAttribute("role");
      const hasPresentationRole = role === "presentation" || role === "none";

      // 2) Vérifier absence d'ordre de tabulation non cohérent (tabindex > 0) à l'intérieur
      const hasPositiveTabindex = await table.evaluate((t) => {
        const focusables = t.querySelectorAll(
          'a[href], button:not([disabled]), input:not([disabled]):not([type="hidden"]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])',
        );
        for (const el of Array.from(focusables)) {
          const ti = el.getAttribute("tabindex");
          if (ti && parseInt(ti, 10) > 0) return true;
        }
        return false;
      });

      // 3) Heuristique simple: éviter des spans de cellules qui compliquent la lecture séquentielle
      const hasComplexSpans = await table.evaluate((t) => {
        const cells = t.querySelectorAll("td");
        for (const c of Array.from(cells)) {
          const cs = parseInt(c.getAttribute("colspan") || "0", 10);
          const rs = parseInt(c.getAttribute("rowspan") || "0", 10);
          if (cs > 1 || rs > 1) return true;
        }
        return false;
      });

      // Conforme si (linéarisation simple: pas de tabindex positif, pas de spans complexes)
      // OU si un rôle de présentation est explicitement défini.
      const ok =
        (!hasPositiveTabindex && !hasComplexSpans) || hasPresentationRole;
      if (ok) {
        conformeCount++;
      } else {
        const hint = await table.evaluate((el) => {
          function cssPath(node: Element): string {
            if ((node as HTMLElement).id) return `#${(node as HTMLElement).id}`;
            const parts: string[] = [];
            let e: Element | null = node;
            while (
              e &&
              e.nodeType === 1 &&
              e.tagName.toLowerCase() !== "html"
            ) {
              let sel = e.tagName.toLowerCase();
              const cls = Array.from((e as HTMLElement).classList).slice(0, 3);
              if (cls.length) sel += "." + cls.join(".");
              const p = e.parentElement;
              if (p) {
                const same = Array.from(p.children).filter(
                  (c) => c.tagName === e!.tagName,
                );
                const idx = same.indexOf(e);
                if (same.length > 1) sel += `:nth-of-type(${idx + 1})`;
              }
              parts.unshift(sel);
              e = e.parentElement;
            }
            return parts.join(" > ");
          }
          function nearestHeadingText(node: Element): string {
            const headings = Array.from(
              document.querySelectorAll("h1,h2,h3,h4,h5,h6"),
            );
            let best = "";
            let bestDist = Infinity;
            const rect = node.getBoundingClientRect();
            for (const h of headings) {
              const r = h.getBoundingClientRect();
              const dy = Math.max(0, rect.top - r.bottom);
              if (dy >= 0 && dy < bestDist) {
                const t = (h.textContent || "").trim();
                if (t.length > 0) {
                  best = t;
                  bestDist = dy;
                }
              }
            }
            return best;
          }
          const r = el.getBoundingClientRect();
          const txt = (el.textContent || "").trim();
          return {
            css: cssPath(el),
            near: nearestHeadingText(el),
            rect: `${Math.round(r.left)},${Math.round(r.top)},${Math.round(r.width)}x${Math.round(r.height)}`,
            text: txt.slice(0, 80),
          };
        });
        const identifier =
          (await table.getAttribute("id")) || `layout-table-${i}`;
        const reasons = [] as string[];
        if (!hasPresentationRole) reasons.push("role≠presentation/none");
        if (hasPositiveTabindex) reasons.push("tabindex positif trouvé");
        if (hasComplexSpans) reasons.push("colspan/rowspan détecté");
        nonConformeElements.push(
          `${identifier}: ${reasons.join(", ")} | css: ${hint.css} | near: ${hint.near || "N/A"} | rect: ${hint.rect} | text: ${hint.text}`,
        );
      }
    }

    const isConforme = nonConformeElements.length === 0;
    testResults.push({
      criterion: "5.3",
      test: "Linéarisation cohérente et rôle presentation",
      status: isConforme ? "conforme" : "non-conforme",
      details: `${conformeCount}/${count} tableaux de mise en forme avec ordre d'accès cohérent et rôle approprié`,
      elements: nonConformeElements,
    });

    expect
      .soft(
        nonConformeElements.length,
        `5.3: tableaux de mise en forme à corriger: ${nonConformeElements.join(
          " | ",
        )}`,
      )
      .toBe(0);
  });
});
