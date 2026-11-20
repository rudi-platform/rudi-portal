/* global process */
/**
 * Audit axe-core (WCAG 2.2 A/AA) des pages du portail avec cliquet anti-régression.
 *
 * - Une règle axe absente de la baseline de la page fait échouer le test (régression).
 * - Une hausse du nombre de nœuds sur une règle déjà connue est signalée ; bloquante si AXE_STRICT_COUNTS=1
 *   (désactivé par défaut car le nombre de nœuds dépend des données du backend).
 * - Sans baseline, la page doit être exempte de violation.
 * - `--update-snapshots` (ou `npm run test:axe:baseline`) réécrit la baseline des pages exécutées.
 *
 * Variables d'environnement :
 *   AXE_PAGES          — liste de pages séparées par des virgules ; préfixe `auth:` pour une page authentifiée
 *                        (ex : AXE_PAGES=/catalogue,auth:/personal-space/my-account)
 *   AXE_STRICT_COUNTS  — `1` pour rendre bloquante la hausse du nombre de nœuds
 */
import {expect, test} from '@playwright/test';
import {
    compareWithBaseline,
    countViolationsByRule,
    formatAxeSummary,
    readAxeBaseline,
    runAxeAudit,
    writeAxeBaseline,
} from '../utils/axe-helpers';
import {gotoAuthenticated} from '../utils/rgaa-helpers';

interface AxePageEntry {
    path: string;
    auth: boolean;
}

const AUTHENTICATED_PROJECT = 'axe';

const DEFAULT_PAGES: AxePageEntry[] = [
    {path: '/home', auth: false},
    {path: '/catalogue', auth: false},
    {path: '/projets', auth: false},
    {path: '/organization', auth: false},
    {path: '/login', auth: false},
    {path: '/login/sign-up', auth: false},
    {path: '/login/forgot-password', auth: false},
    {path: '/projets/soumettre-un-projet', auth: true},
    {path: '/personal-space/my-account', auth: true},
    {path: '/personal-space/my-notifications', auth: true},
    {path: '/personal-space/my-activity', auth: true},
];

function getPages(): AxePageEntry[] {
    const customPages = process.env.AXE_PAGES;
    if (!customPages) {
        return DEFAULT_PAGES;
    }
    return customPages.split(',')
        .map((entry) => entry.trim())
        .filter((entry) => entry.length > 0)
        .map((entry) => entry.startsWith('auth:')
            ? {path: entry.substring('auth:'.length), auth: true}
            : {path: entry, auth: false});
}

test.describe('axe-core - WCAG 2.2 A/AA', () => {
    for (const entry of getPages()) {
        test(`${entry.auth ? '[auth] ' : ''}${entry.path}`, async ({page}, testInfo) => {
            test.skip(entry.auth !== (testInfo.project.name === AUTHENTICATED_PROJECT),
                'Page exécutée uniquement dans le projet correspondant à son mode d\'authentification');

            if (entry.auth) {
                await gotoAuthenticated(page, entry.path);
            } else {
                await page.goto(entry.path);
                await page.waitForLoadState('networkidle');
            }

            const results = await runAxeAudit(page, testInfo);
            const current = countViolationsByRule(results.violations);
            console.log(formatAxeSummary(entry.path, results.violations));

            const updateMode = testInfo.config.updateSnapshots;
            if (updateMode === 'all' || updateMode === 'changed') {
                writeAxeBaseline(entry.path, current);
                testInfo.annotations.push({type: 'baseline', description: `Baseline axe mise à jour pour ${entry.path}`});
                return;
            }

            const baseline = readAxeBaseline(entry.path);
            if (baseline === null) {
                expect(Object.keys(current), `Aucune baseline pour ${entry.path} : la page doit être sans violation axe`)
                    .toEqual([]);
                return;
            }

            const comparison = compareWithBaseline(current, baseline);
            if (comparison.improvedRules.length > 0) {
                testInfo.annotations.push({
                    type: 'amélioration',
                    description: `${comparison.improvedRules.join(' ; ')} — lancer npm run test:axe:baseline pour resserrer le cliquet`,
                });
            }
            if (comparison.increasedRules.length > 0) {
                testInfo.annotations.push({type: 'hausse', description: comparison.increasedRules.join(' ; ')});
            }

            expect(comparison.newRules, `Nouvelles règles axe en échec sur ${entry.path} (régression)`).toEqual([]);
            if (process.env.AXE_STRICT_COUNTS === '1') {
                expect(comparison.increasedRules, `Hausse du nombre de nœuds en échec sur ${entry.path}`).toEqual([]);
            }
        });
    }
});
