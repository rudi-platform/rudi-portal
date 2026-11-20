/* global __dirname */
/**
 * Helpers axe-core pour les tests Playwright d'accessibilité.
 *
 * - Lance axe-core sur les règles WCAG 2.0 / 2.1 / 2.2 niveaux A et AA (socle de RGAA 4.1.2).
 * - Rattache chaque règle axe au(x) critère(s) RGAA correspondant(s).
 * - Gère une baseline par page (cliquet anti-régression) versionnée dans e2e/axe/baseline/.
 */

import AxeBuilder from '@axe-core/playwright';
import type {Page, TestInfo} from '@playwright/test';
import type {AxeResults, Result} from 'axe-core';
import fs from 'node:fs';
import path from 'node:path';

export const AXE_WCAG_TAGS = ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22a', 'wcag22aa'];

const AXE_BASELINE_DIR = path.join(__dirname, '../axe/baseline');

/** Correspondance règle axe → critère(s) RGAA 4.1.2 (« WCAG 2.2 » = critère hors RGAA 4.1.2). */
export const AXE_RULE_TO_RGAA: Record<string, string> = {
    'area-alt': '1.1',
    'image-alt': '1.1',
    'input-image-alt': '1.1',
    'object-alt': '1.1',
    'role-img-alt': '1.1',
    'svg-img-alt': '1.1',
    'frame-title': '2.1',
    'color-contrast': '3.2',
    'td-headers-attr': '5.7',
    'th-has-data-cells': '5.7',
    'label-content-name-mismatch': '6.1, 11.9',
    'link-name': '6.2',
    'aria-allowed-attr': '7.1',
    'aria-allowed-role': '7.1',
    'aria-command-name': '7.1',
    'aria-conditional-attr': '7.1',
    'aria-deprecated-role': '7.1',
    'aria-hidden-body': '7.1',
    'aria-hidden-focus': '7.1, 10.8',
    'aria-input-field-name': '7.1, 11.1',
    'aria-meter-name': '7.1',
    'aria-progressbar-name': '7.1',
    'aria-prohibited-attr': '7.1',
    'aria-required-attr': '7.1',
    'aria-required-children': '7.1',
    'aria-required-parent': '7.1',
    'aria-roles': '7.1',
    'aria-toggle-field-name': '7.1, 11.1',
    'aria-tooltip-name': '7.1',
    'aria-valid-attr': '7.1',
    'aria-valid-attr-value': '7.1',
    'button-name': '7.1, 11.9',
    'nested-interactive': '7.1',
    'scrollable-region-focusable': '7.3',
    'html-has-lang': '8.3',
    'html-lang-valid': '8.4',
    'html-xml-lang-mismatch': '8.4',
    'document-title': '8.5',
    'valid-lang': '8.8',
    'definition-list': '9.3',
    'dlitem': '9.3',
    'list': '9.3',
    'listitem': '9.3',
    'meta-viewport': '10.4',
    'link-in-text-block': '10.6',
    'avoid-inline-spacing': '10.12',
    'label': '11.1',
    'select-name': '11.1',
    'form-field-multiple-labels': '11.2',
    'autocomplete-valid': '11.13',
    'bypass': '12.7',
    'meta-refresh': '13.1',
    'blink': '13.8',
    'marquee': '13.8',
    'css-orientation-lock': '13.9',
    'target-size': 'WCAG 2.2 - 2.5.8',
};

export type AxeRuleCounts = Record<string, number>;

export interface AxeBaselineComparison {
    /** Règles absentes de la baseline : régression bloquante. */
    newRules: string[];
    /** Règles déjà en baseline mais avec plus de nœuds en échec (peut dépendre des données). */
    increasedRules: string[];
    /** Règles corrigées ou en baisse : la baseline peut être resserrée. */
    improvedRules: string[];
}

export async function runAxeAudit(page: Page, testInfo: TestInfo, excludedSelectors: string[] = []): Promise<AxeResults> {
    let builder = new AxeBuilder({page}).withTags(AXE_WCAG_TAGS);
    for (const selector of excludedSelectors) {
        builder = builder.exclude(selector);
    }
    const results = await builder.analyze();
    await testInfo.attach('axe-violations.json', {
        body: JSON.stringify(sanitizeViolations(results.violations), null, 2),
        contentType: 'application/json',
    });
    return results;
}

/** Retire le HTML des nœuds : il peut contenir des données personnelles du compte de test. */
function sanitizeViolations(violations: Result[]): object[] {
    return violations.map((violation) => ({
        id: violation.id,
        rgaa: getRgaaCriterion(violation.id),
        impact: violation.impact,
        help: violation.help,
        helpUrl: violation.helpUrl,
        nodes: violation.nodes.map((node) => ({target: node.target, failureSummary: node.failureSummary})),
    }));
}

export function getRgaaCriterion(ruleId: string): string {
    return AXE_RULE_TO_RGAA[ruleId] || 'à qualifier';
}

export function countViolationsByRule(violations: Result[]): AxeRuleCounts {
    const counts: AxeRuleCounts = {};
    for (const violation of [...violations].sort((a, b) => a.id.localeCompare(b.id))) {
        counts[violation.id] = violation.nodes.length;
    }
    return counts;
}

export function formatAxeSummary(pagePath: string, violations: Result[]): string {
    if (violations.length === 0) {
        return `[axe] ${pagePath} : aucune violation WCAG A/AA détectée`;
    }
    const lines = [`[axe] ${pagePath} : ${violations.length} règle(s) en échec`];
    for (const violation of violations) {
        const targets = violation.nodes.slice(0, 3).map((node) => node.target.join(' ')).join(' | ');
        lines.push(
            `  - ${violation.id} [${violation.impact}] RGAA ${getRgaaCriterion(violation.id)} : ` +
            `${violation.nodes.length} nœud(s) - ${violation.help}`
        );
        lines.push(`      ${targets}${violation.nodes.length > 3 ? ' | …' : ''}`);
    }
    return lines.join('\n');
}

function getBaselinePath(pagePath: string): string {
    const slug = pagePath.replace(/^\/+/, '').replace(/[^a-zA-Z0-9-]+/g, '_') || 'root';
    return path.join(AXE_BASELINE_DIR, `${slug}.json`);
}

export function readAxeBaseline(pagePath: string): AxeRuleCounts | null {
    const baselinePath = getBaselinePath(pagePath);
    if (!fs.existsSync(baselinePath)) {
        return null;
    }
    return JSON.parse(fs.readFileSync(baselinePath, 'utf-8')) as AxeRuleCounts;
}

export function writeAxeBaseline(pagePath: string, counts: AxeRuleCounts): void {
    fs.mkdirSync(AXE_BASELINE_DIR, {recursive: true});
    fs.writeFileSync(getBaselinePath(pagePath), `${JSON.stringify(counts, null, 2)}\n`);
}

export function compareWithBaseline(current: AxeRuleCounts, baseline: AxeRuleCounts): AxeBaselineComparison {
    const newRules: string[] = [];
    const increasedRules: string[] = [];
    const improvedRules: string[] = [];
    for (const [ruleId, count] of Object.entries(current)) {
        const baselineCount = baseline[ruleId];
        if (baselineCount === undefined) {
            newRules.push(`${ruleId} (RGAA ${getRgaaCriterion(ruleId)}) : ${count} nœud(s)`);
        } else if (count > baselineCount) {
            increasedRules.push(`${ruleId} (RGAA ${getRgaaCriterion(ruleId)}) : ${baselineCount} → ${count}`);
        } else if (count < baselineCount) {
            improvedRules.push(`${ruleId} : ${baselineCount} → ${count}`);
        }
    }
    for (const [ruleId, baselineCount] of Object.entries(baseline)) {
        if (current[ruleId] === undefined) {
            improvedRules.push(`${ruleId} : ${baselineCount} → 0`);
        }
    }
    return {newRules, increasedRules, improvedRules};
}
