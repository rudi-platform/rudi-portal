/* global document, sessionStorage, __dirname */
/**
 * Helpers RGAA pour les tests Playwright
 *
 * Fonctions utilitaires partagées entre les différentes thématiques de tests RGAA.
 * Permet de vérifier la conformité des pages aux critères du référentiel RGAA 4.1.2.
 */

import { Locator, Page } from '@playwright/test';
import fs from 'fs';
import path from 'path';

// ─── Auth ─────────────────────────────────────────────────────────────────────

const AUTH_TOKENS_PATH = path.join(__dirname, '../../test-results/.auth/tokens.json');

/**
 * Injecte les tokens JWT dans le sessionStorage du navigateur.
 * Doit être appelé APRÈS un premier `page.goto()` (le sessionStorage n'existe
 * qu'une fois qu'une page est chargée dans le contexte de l'origine).
 *
 * @param page - la page Playwright
 * @returns true si les tokens ont été injectés, false si le fichier n'existe pas
 */
export async function injectAuthTokens(page: Page): Promise<boolean> {
  if (!fs.existsSync(AUTH_TOKENS_PATH)) {
    return false;
  }
  const tokens = JSON.parse(fs.readFileSync(AUTH_TOKENS_PATH, 'utf-8'));
  await page.evaluate(({ jwt, xtoken }) => {
    sessionStorage.setItem('_jwt', jwt);
    if (xtoken) {
      sessionStorage.setItem('_xtoken', xtoken);
    }
    sessionStorage.setItem('authenticationState', 'USER');
  }, tokens);
  return true;
}

/**
 * Navigue vers une URL en injectant les tokens d'authentification
 * AVANT le chargement de la page (via addInitScript).
 *
 * Le sessionStorage est peuplé avant le bootstrap Angular,
 * ce qui permet au AuthGuard de reconnaître l'utilisateur.
 *
 * @param page - la page Playwright
 * @param targetUrl - l'URL cible (relative, ex: '/projets/soumettre-un-projet')
 */
export async function gotoAuthenticated(page: Page, targetUrl: string): Promise<void> {
  if (!fs.existsSync(AUTH_TOKENS_PATH)) {
    throw new Error(
      `Auth tokens not found at ${AUTH_TOKENS_PATH}. ` +
      'Run with --project=chromium (not chromium-noauth) to execute the auth setup first.'
    );
  }
  const tokens = JSON.parse(fs.readFileSync(AUTH_TOKENS_PATH, 'utf-8'));

  // Injecter les tokens dans le sessionStorage AVANT le chargement de la page
  await page.addInitScript((t: { jwt: string; xtoken: string | null }) => {
    sessionStorage.setItem('_jwt', t.jwt);
    if (t.xtoken) {
      sessionStorage.setItem('_xtoken', t.xtoken);
    }
    sessionStorage.setItem('authenticationState', 'USER');
  }, tokens);

  // Naviguer vers la page cible — Angular lit les tokens au bootstrap
  await page.goto(targetUrl);
  // Attendre que le contenu Angular soit rendu
  await page.waitForLoadState('networkidle');
}

// ─── Types ────────────────────────────────────────────────────────────────────

export interface RGAATestResult {
  criterion: string;
  test: string;
  status: 'conforme' | 'non-conforme' | 'non-applicable';
  details?: string;
  elements?: string[];
}

export interface LabelInfo {
  hasLabel: boolean;
  value: string;
  source: 'aria-label' | 'aria-labelledby' | 'label-for' | 'label-wrapping' | 'title' | 'placeholder' | 'none';
}

export interface FieldsetInfo {
  hasGroup: boolean;
  hasLegend: boolean;
  legendText: string;
}

export interface ErrorAssociationInfo {
  hasErrorAssoc: boolean;
  method: 'aria-describedby' | 'aria-errormessage' | 'role-alert' | 'none';
}

export interface InputHelpInfo {
  hasHelp: boolean;
  sources: string[];
}

export interface TabOrderInfo {
  isValid: boolean;
  issues: string[];
}

export interface ButtonLabelSources {
  'aria-label'?: string;
  'aria-labelledby'?: string;
  value?: string;
  textContent?: string;
  alt?: string;
  title?: string;
}

// ─── Sélecteurs ───────────────────────────────────────────────────────────────

export const RGAA_SELECTORS = {
  formFields: 'input:not([type="hidden"]):not([type="submit"]):not([type="reset"]):not([type="button"]):not([type="image"]), select, textarea',
  headings: 'h1, h2, h3, h4, h5, h6',
  images: 'img, [role="img"], svg[role="img"]',
  links: 'a[href], [role=link]',
  buttons: 'button, input[type="submit"], input[type="reset"], input[type="button"], input[type="image"], [role="button"]',
};

// ─── Fonctions d'inspection ───────────────────────────────────────────────────

/**
 * Construit une description riche d'un champ de formulaire pour les rapports RGAA.
 * Combine tag, type, id, name, placeholder, formControlName et le sélecteur CSS.
 *
 * Exemple de sortie :
 *   `<input type="text" id="mat-input-0" formControlName="title" placeholder="Titre"> — css: mat-form-field:nth-of-type(1) input`
 */
export async function describeField(field: Locator, _index: number): Promise<string> {
  const info = await field.evaluate((el: HTMLElement) => {
    const tag = el.tagName.toLowerCase();
    const type = el.getAttribute('type');
    const id = el.getAttribute('id');
    const name = el.getAttribute('name');
    const placeholder = el.getAttribute('placeholder');
    const formControlName = el.getAttribute('formcontrolname') || el.getAttribute('ng-reflect-name');
    const ariaLabel = el.getAttribute('aria-label');
    const classes = Array.from(el.classList).join('.');

    // Build a minimal CSS selector for locating the element
    let selector = tag;
    if (id) {
      selector = `#${id}`;
    } else if (name) {
      selector = `${tag}[name="${name}"]`;
    } else if (formControlName) {
      selector = `${tag}[formcontrolname="${formControlName}"]`;
    } else if (type) {
      // For inputs without id/name, find position among siblings of same type
      const parent = el.parentElement;
      if (parent) {
        const siblings = Array.from(parent.querySelectorAll(`${tag}[type="${type}"]`));
        const pos = siblings.indexOf(el as HTMLInputElement);
        selector = `${tag}[type="${type}"]:nth(${pos})`;
      }
    }

    return { tag, type, id, name, placeholder, formControlName, ariaLabel, classes, selector };
  });

  // Build human-readable description
  const parts: string[] = [`<${info.tag}`];
  if (info.type) parts.push(`type="${info.type}"`);
  if (info.id) parts.push(`id="${info.id}"`);
  if (info.name) parts.push(`name="${info.name}"`);
  if (info.formControlName) parts.push(`formControlName="${info.formControlName}"`);
  if (info.placeholder) parts.push(`placeholder="${info.placeholder}"`);
  if (info.ariaLabel) parts.push(`aria-label="${info.ariaLabel}"`);
  parts.push('>');

  return `${parts.join(' ')} — css: ${info.selector}`;
}

/**
 * Vérifie si un champ de formulaire possède une étiquette associée.
 */
export async function checkFieldLabel(field: Locator): Promise<LabelInfo> {
  // aria-label
  const ariaLabel = await field.getAttribute('aria-label');
  if (ariaLabel && ariaLabel.trim().length > 0) {
    return { hasLabel: true, value: ariaLabel.trim(), source: 'aria-label' };
  }

  // aria-labelledby
  const ariaLabelledby = await field.getAttribute('aria-labelledby');
  if (ariaLabelledby) {
    const text = await field.evaluate((el, labelledbyId) => {
      const ids = labelledbyId.split(/\s+/);
      return ids
        .map((id) => document.getElementById(id)?.textContent?.trim() || '')
        .filter((t) => t.length > 0)
        .join(' ');
    }, ariaLabelledby);
    if (text.length > 0) {
      return { hasLabel: true, value: text, source: 'aria-labelledby' };
    }
  }

  // label[for="id"]
  const id = await field.getAttribute('id');
  if (id) {
    const labelText = await field.evaluate((el, fieldId) => {
      const label = document.querySelector(`label[for="${fieldId}"]`);
      return label?.textContent?.trim() || '';
    }, id);
    if (labelText.length > 0) {
      return { hasLabel: true, value: labelText, source: 'label-for' };
    }
  }

  // label englobant
  const wrappingLabel = await field.evaluate((el) => {
    const label = el.closest('label');
    return label?.textContent?.trim() || '';
  });
  if (wrappingLabel.length > 0) {
    return { hasLabel: true, value: wrappingLabel, source: 'label-wrapping' };
  }

  // title
  const title = await field.getAttribute('title');
  if (title && title.trim().length > 0) {
    return { hasLabel: true, value: title.trim(), source: 'title' };
  }

  // placeholder (dernier recours, pas recommandé comme seul label)
  const placeholder = await field.getAttribute('placeholder');
  if (placeholder && placeholder.trim().length > 0) {
    return { hasLabel: true, value: placeholder.trim(), source: 'placeholder' };
  }

  return { hasLabel: false, value: '', source: 'none' };
}

/**
 * Vérifie si un champ obligatoire a une indication visuelle.
 */
export async function checkRequiredIndication(field: Locator): Promise<boolean> {
  return field.evaluate((el) => {
    // aria-required
    if (el.getAttribute('aria-required') === 'true') return true;
    // attribut required
    if (el.hasAttribute('required')) {
      // Vérifier qu'il y a une indication visuelle (astérisque, texte, etc.)
      const label = el.closest('label') || (el.id ? document.querySelector(`label[for="${el.id}"]`) : null);
      if (label) {
        const text = label.textContent || '';
        return text.includes('*') || /obligatoire|requis|required/i.test(text);
      }
    }
    return false;
  });
}

/**
 * Récupère les textes référencés par aria-describedby.
 */
export async function getDescribedByTexts(field: Locator): Promise<string[]> {
  return field.evaluate((el) => {
    const describedBy = el.getAttribute('aria-describedby');
    if (!describedBy) return [];
    return describedBy
      .split(/\s+/)
      .map((id) => document.getElementById(id)?.textContent?.trim() || '')
      .filter((t) => t.length > 0);
  });
}

/**
 * Vérifie la présence d'un fieldset/legend ou d'un groupe ARIA pour un champ radio/checkbox.
 */
export async function checkFieldsetLegend(field: Locator): Promise<FieldsetInfo> {
  return field.evaluate((el) => {
    const fieldset = el.closest('fieldset');
    const ariaGroup = el.closest('[role="group"], [role="radiogroup"]');

    if (fieldset) {
      const legend = fieldset.querySelector('legend');
      return {
        hasGroup: true,
        hasLegend: !!legend && (legend.textContent?.trim().length || 0) > 0,
        legendText: legend?.textContent?.trim() || '',
      };
    }

    if (ariaGroup) {
      const labelledBy = ariaGroup.getAttribute('aria-labelledby');
      const ariaLabel = ariaGroup.getAttribute('aria-label');
      const legendText = ariaLabel || (labelledBy ? (document.getElementById(labelledBy)?.textContent?.trim() || '') : '');
      return {
        hasGroup: true,
        hasLegend: legendText.length > 0,
        legendText,
      };
    }

    return { hasGroup: false, hasLegend: false, legendText: '' };
  });
}

/**
 * Vérifie l'association d'un message d'erreur à un champ invalide.
 */
export async function checkErrorAssociation(field: Locator): Promise<ErrorAssociationInfo> {
  return field.evaluate((el) => {
    // aria-describedby pointant vers un message d'erreur
    const describedBy = el.getAttribute('aria-describedby');
    if (describedBy) {
      const ids = describedBy.split(/\s+/);
      for (const id of ids) {
        const target = document.getElementById(id);
        if (target && (target.textContent?.trim().length || 0) > 0) {
          return { hasErrorAssoc: true, method: 'aria-describedby' as const };
        }
      }
    }

    // aria-errormessage
    const errMsg = el.getAttribute('aria-errormessage');
    if (errMsg) {
      const target = document.getElementById(errMsg);
      if (target && (target.textContent?.trim().length || 0) > 0) {
        return { hasErrorAssoc: true, method: 'aria-errormessage' as const };
      }
    }

    // role="alert" dans le voisinage
    const parent = el.parentElement;
    if (parent) {
      const alert = parent.querySelector('[role="alert"]');
      if (alert && (alert.textContent?.trim().length || 0) > 0) {
        return { hasErrorAssoc: true, method: 'role-alert' as const };
      }
    }

    return { hasErrorAssoc: false, method: 'none' as const };
  });
}

/**
 * Vérifie si un champ a une aide à la saisie.
 */
export async function checkInputHelp(field: Locator): Promise<InputHelpInfo> {
  const sources: string[] = [];

  const described = await getDescribedByTexts(field);
  if (described.length > 0) sources.push('aria-describedby');

  const placeholder = await field.getAttribute('placeholder');
  if (placeholder && placeholder.trim().length > 0) sources.push('placeholder');

  const title = await field.getAttribute('title');
  if (title && title.trim().length > 0) sources.push('title');

  return { hasHelp: sources.length > 0, sources };
}

/**
 * Vérifie l'ordre de tabulation (pas de tabindex positif).
 */
export async function checkTabOrder(page: Page): Promise<TabOrderInfo> {
  const issues: string[] = [];

  const positiveTabindex = await page.evaluate(() => {
    const elements = document.querySelectorAll('[tabindex]');
    const problems: string[] = [];
    elements.forEach((el) => {
      const val = parseInt(el.getAttribute('tabindex') || '0', 10);
      if (val > 0) {
        const tag = el.tagName.toLowerCase();
        const id = el.id || el.getAttribute('name') || '';
        problems.push(`${tag}${id ? '#' + id : ''} tabindex=${val}`);
      }
    });
    return problems;
  });

  issues.push(...positiveTabindex);

  return { isValid: issues.length === 0, issues };
}

/**
 * Récupère le nom accessible d'un élément via le navigateur.
 */
export async function getAccessibleName(locator: Locator): Promise<string> {
  return locator.evaluate((el) => {
    // aria-label
    const ariaLabel = el.getAttribute('aria-label');
    if (ariaLabel) return ariaLabel.trim();

    // aria-labelledby
    const labelledBy = el.getAttribute('aria-labelledby');
    if (labelledBy) {
      const text = labelledBy
        .split(/\s+/)
        .map((id) => document.getElementById(id)?.textContent?.trim() || '')
        .filter((t) => t.length > 0)
        .join(' ');
      if (text) return text;
    }

    // textContent pour les boutons
    const text = el.textContent?.trim();
    if (text) return text;

    // title
    const title = el.getAttribute('title');
    if (title) return title.trim();

    // alt pour les images
    const alt = el.getAttribute('alt');
    if (alt) return alt.trim();

    // value pour input
    const value = el.getAttribute('value');
    if (value) return value.trim();

    return '';
  });
}

/**
 * Récupère toutes les sources d'intitulé d'un bouton.
 */
export async function getButtonLabelSources(button: Locator): Promise<ButtonLabelSources> {
  return button.evaluate((el) => {
    const sources: Record<string, string | undefined> = {};
    sources['aria-label'] = el.getAttribute('aria-label')?.trim() || undefined;

    const labelledBy = el.getAttribute('aria-labelledby');
    if (labelledBy) {
      sources['aria-labelledby'] = labelledBy
        .split(/\s+/)
        .map((id) => document.getElementById(id)?.textContent?.trim() || '')
        .filter((t) => t.length > 0)
        .join(' ') || undefined;
    }

    sources.value = el.getAttribute('value')?.trim() || undefined;
    sources.textContent = el.textContent?.trim() || undefined;
    sources.alt = el.getAttribute('alt')?.trim() || undefined;
    sources.title = el.getAttribute('title')?.trim() || undefined;

    return sources as ButtonLabelSources;
  });
}

/**
 * Récupère l'intitulé visible d'un bouton (texte rendu, sans les éléments cachés).
 */
export async function getButtonVisibleLabel(button: Locator): Promise<string> {
  return button.evaluate((el) => {
    // Collecter le texte visible (pas aria-hidden, pas sr-only)
    const walker = document.createTreeWalker(el, NodeFilter.SHOW_TEXT);
    const parts: string[] = [];
    let node: Node | null;
    while ((node = walker.nextNode())) {
      const parent = node.parentElement;
      if (parent && (parent.getAttribute('aria-hidden') === 'true' || parent.classList.contains('sr-only') || parent.classList.contains('cdk-visually-hidden'))) {
        continue;
      }
      const text = node.textContent?.trim();
      if (text) parts.push(text);
    }
    return parts.join(' ');
  });
}

/**
 * Vérifie si une étiquette est pertinente (pas générique).
 */
export function isLabelPertinent(label: string): boolean {
  if (!label || label.trim().length < 2) return false;
  const lower = label.toLowerCase().trim();
  const nonPertinent = [
    'champ', 'input', 'saisir', 'entrer', 'cliquer',
    'field', 'enter', 'click', 'type here',
    '...', 'xxx', 'zzz',
  ];
  return !nonPertinent.some((np) => lower === np);
}

/**
 * Vérifie si une étiquette de select est pertinente.
 */
export function isSelectLabelPertinent(label: string): boolean {
  if (!isLabelPertinent(label)) return false;
  const lower = label.toLowerCase().trim();
  // Un select devrait avoir un label indiquant ce qu'on sélectionne
  const tooGeneric = ['sélectionner', 'choisir', 'select', 'choose'];
  // "Sélectionner" seul n'est pas pertinent, mais "Sélectionner une catégorie" l'est
  return !tooGeneric.some((g) => lower === g);
}

// ─── Rapport ──────────────────────────────────────────────────────────────────

/**
 * Génère un rapport texte à partir des résultats de tests RGAA.
 */
export function generateRGAAReport(results: RGAATestResult[]): string {
  const lines: string[] = [];
  lines.push('╔══════════════════════════════════════════════════════╗');
  lines.push('║          RAPPORT DE CONFORMITÉ RGAA 4.1.2          ║');
  lines.push('╚══════════════════════════════════════════════════════╝');
  lines.push('');

  const conforme = results.filter((r) => r.status === 'conforme').length;
  const nonConforme = results.filter((r) => r.status === 'non-conforme').length;
  const na = results.filter((r) => r.status === 'non-applicable').length;

  lines.push(`  ✅ Conforme:        ${conforme}`);
  lines.push(`  ❌ Non conforme:    ${nonConforme}`);
  lines.push(`  ⚪ Non applicable:  ${na}`);
  lines.push(`  📊 Total:           ${results.length}`);
  lines.push('');
  lines.push('──────────────────────────────────────────────────────');

  for (const r of results) {
    const icon = r.status === 'conforme' ? '✅' : r.status === 'non-conforme' ? '❌' : '⚪';
    lines.push(`${icon} ${r.criterion} - ${r.test}: ${r.status}`);
    if (r.details) lines.push(`   ${r.details}`);
    if (r.elements && r.elements.length > 0) {
      lines.push(`   Éléments: ${r.elements.slice(0, 10).join(', ')}${r.elements.length > 10 ? ` (+${r.elements.length - 10})` : ''}`);
    }
  }

  lines.push('');
  lines.push('══════════════════════════════════════════════════════');
  return lines.join('\n');
}

// ─── Images ───────────────────────────────────────────────────────────────────

export interface ImageAlternativeResult {
  hasAlternative: boolean;
  value: string;
  method: string;
}

/**
 * Vérifie si une image (img, svg, [role="img"], canvas…) possède une alternative textuelle.
 * Retourne la méthode utilisée et la valeur trouvée.
 */
export async function checkImageAlternative(img: Locator): Promise<ImageAlternativeResult> {
  return img.evaluate((el: Element) => {
    const ariaLabel = el.getAttribute('aria-label');
    if (ariaLabel && ariaLabel.trim().length > 0) {
      return { hasAlternative: true, value: ariaLabel.trim(), method: 'aria-label' };
    }

    const ariaLabelledby = el.getAttribute('aria-labelledby');
    if (ariaLabelledby) {
      const ids = ariaLabelledby.split(/\s+/);
      const texts = ids.map(id => document.getElementById(id)?.textContent?.trim() || '').filter(Boolean);
      if (texts.length > 0) {
        return { hasAlternative: true, value: texts.join(' '), method: 'aria-labelledby' };
      }
    }

    const alt = el.getAttribute('alt');
    if (alt !== null && alt.trim().length > 0) {
      return { hasAlternative: true, value: alt.trim(), method: 'alt' };
    }

    // SVG: check <title> child
    if (el.tagName.toLowerCase() === 'svg') {
      const titleEl = el.querySelector('title');
      if (titleEl && titleEl.textContent && titleEl.textContent.trim().length > 0) {
        return { hasAlternative: true, value: titleEl.textContent.trim(), method: 'svg-title' };
      }
    }

    const title = el.getAttribute('title');
    if (title && title.trim().length > 0) {
      return { hasAlternative: true, value: title.trim(), method: 'title' };
    }

    return { hasAlternative: false, value: '', method: 'none' };
  });
}

/**
 * Retourne un identifiant lisible pour un locator (src, class, id, tag).
 */
export async function getLocatorHint(locator: Locator): Promise<string> {
  return locator.evaluate((el: Element) => {
    const tag = el.tagName.toLowerCase();
    const id = el.id ? `#${el.id}` : '';
    const src = el.getAttribute('src') || el.getAttribute('data-src') || '';
    const cls = el.className && typeof el.className === 'string'
      ? `.${el.className.trim().split(/\s+/).slice(0, 2).join('.')}`
      : '';
    if (src) return `<${tag}${id} src="${src}">`;
    return `<${tag}${id}${cls}>`;
  });
}

/**
 * Vérifie si un élément est une exception RGAA pour un critère donné.
 * Pour le critère "1.2", retourne true si l'image est décorative.
 */
export async function isRGAAException(locator: Locator, criterion: string): Promise<boolean> {
  if (criterion === '1.2') {
    return locator.evaluate((el: Element) => {
      const alt = el.getAttribute('alt');
      const ariaHidden = el.getAttribute('aria-hidden');
      const role = el.getAttribute('role');
      return alt === '' || ariaHidden === 'true' || role === 'presentation' || role === 'none';
    });
  }
  return false;
}

export interface ElementAuditDetails {
  index: number;
  tag: string;
  src: string;
  alt: string | null;
  ariaLabel: string | null;
  ariaLabelledby: string | null;
  svgTitle: string | null;
  title: string | null;
  role: string | null;
  alternativeMethod: string;
  alternativeValue: string;
  nearHeading: string | null;
  status?: string;
  issue?: string;
  correction?: string;
}

/**
 * Récupère les informations détaillées d'audit pour un élément image.
 */
export async function getElementAuditDetails(img: Locator, index: number): Promise<ElementAuditDetails> {
  const details = await img.evaluate((el: Element) => {
    const tag = el.tagName.toLowerCase();
    const src = el.getAttribute('src') || el.getAttribute('data-src') || '';
    const alt = el.getAttribute('alt');
    const ariaLabel = el.getAttribute('aria-label');
    const ariaLabelledby = el.getAttribute('aria-labelledby');
    const title = el.getAttribute('title');
    const role = el.getAttribute('role');
    let svgTitle: string | null = null;
    if (tag === 'svg') {
      const titleEl = el.querySelector('title');
      svgTitle = titleEl?.textContent?.trim() || null;
    }

    // Determine alternative method
    let alternativeMethod = 'none';
    let alternativeValue = '';
    if (ariaLabel && ariaLabel.trim()) { alternativeMethod = 'aria-label'; alternativeValue = ariaLabel.trim(); }
    else if (ariaLabelledby) {
      const ids = ariaLabelledby.split(/\s+/);
      const texts = ids.map(id => document.getElementById(id)?.textContent?.trim() || '').filter(Boolean);
      if (texts.length > 0) { alternativeMethod = 'aria-labelledby'; alternativeValue = texts.join(' '); }
    }
    else if (alt !== null && alt.trim()) { alternativeMethod = 'alt'; alternativeValue = alt.trim(); }
    else if (svgTitle) { alternativeMethod = 'svg-title'; alternativeValue = svgTitle; }
    else if (title && title.trim()) { alternativeMethod = 'title'; alternativeValue = title.trim(); }

    // Find nearest heading for context
    let nearHeading: string | null = null;
    let sibling: Element | null = el.previousElementSibling;
    for (let i = 0; i < 5 && sibling; i++) {
      if (/^H[1-6]$/.test(sibling.tagName)) {
        nearHeading = sibling.textContent?.trim() || null;
        break;
      }
      sibling = sibling.previousElementSibling;
    }
    if (!nearHeading) {
      const parent = el.closest('section, article, div');
      const heading = parent?.querySelector('h1, h2, h3, h4, h5, h6');
      nearHeading = heading?.textContent?.trim() || null;
    }

    return { tag, src, alt, ariaLabel, ariaLabelledby, svgTitle, title, role, alternativeMethod, alternativeValue, nearHeading };
  });

  return { index, ...details };
}

// ─── Audit Report ─────────────────────────────────────────────────────────────

export interface AuditCriterion {
  id: string;
  title: string;
  description: string;
  status: string;
  conformeCount: number;
  nonConformeCount: number;
  totalCount: number;
  elements: ElementAuditDetails[];
}

export interface AuditReport {
  meta: {
    date: string;
    url: string;
    browser: string;
    referentiel: string;
    thematique: string;
  };
  summary: {
    totalCriteria: number;
    conforme: number;
    nonConforme: number;
    nonApplicable: number;
    conformityRate: number;
  };
  criteria: AuditCriterion[];
}

/**
 * Génère un rapport HTML à partir d'un AuditReport.
 */
export function generateAuditHTML(report: AuditReport): string {
  const statusIcon = (s: string) => s === 'conforme' ? '✅' : s === 'non-conforme' ? '❌' : '⚪';
  const statusClass = (s: string) => s === 'conforme' ? 'conforme' : s === 'non-conforme' ? 'non-conforme' : 'na';

  const criteriaHTML = report.criteria.map(c => `
    <div class="criterion ${statusClass(c.status)}">
      <h3>${statusIcon(c.status)} ${c.id} — ${c.title}</h3>
      <p class="description">${c.description}</p>
      <p><strong>Résultat :</strong> ${c.status} (${c.conformeCount} conforme / ${c.nonConformeCount} non conforme / ${c.totalCount} total)</p>
      ${c.elements.filter(e => e.status === 'non-conforme').length > 0 ? `
      <details>
        <summary>${c.elements.filter(e => e.status === 'non-conforme').length} élément(s) non conforme(s)</summary>
        <ul>
          ${c.elements.filter(e => e.status === 'non-conforme').map(e => `
            <li>
              <code>&lt;${e.tag} src="${e.src}"&gt;</code>
              ${e.issue ? `<br/><em>Problème :</em> ${e.issue}` : ''}
              ${e.correction ? `<br/><em>Correction :</em> ${e.correction}` : ''}
            </li>
          `).join('')}
        </ul>
      </details>` : ''}
    </div>
  `).join('\n');

  return `<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <title>Audit RGAA — ${report.meta.thematique}</title>
  <style>
    body { font-family: system-ui, sans-serif; max-width: 900px; margin: 2em auto; padding: 0 1em; color: #333; }
    h1 { color: #1a237e; }
    .meta { background: #f5f5f5; padding: 1em; border-radius: 4px; margin-bottom: 2em; }
    .summary { display: flex; gap: 2em; margin-bottom: 2em; padding: 1em; background: #e8eaf6; border-radius: 4px; }
    .summary div { text-align: center; }
    .summary .rate { font-size: 2em; font-weight: bold; }
    .criterion { border: 1px solid #ddd; border-radius: 4px; padding: 1em; margin-bottom: 1em; }
    .criterion.conforme { border-left: 4px solid #4caf50; }
    .criterion.non-conforme { border-left: 4px solid #f44336; }
    .criterion.na { border-left: 4px solid #9e9e9e; }
    .description { color: #666; font-style: italic; }
    details { margin-top: 0.5em; }
    code { background: #f5f5f5; padding: 2px 4px; border-radius: 2px; font-size: 0.9em; }
    ul { list-style: none; padding-left: 0; }
    li { padding: 0.5em; border-bottom: 1px solid #eee; }
  </style>
</head>
<body>
  <h1>Audit RGAA 4.1.2 — ${report.meta.thematique}</h1>
  <div class="meta">
    <p><strong>Date :</strong> ${report.meta.date} | <strong>URL :</strong> ${report.meta.url} | <strong>Navigateur :</strong> ${report.meta.browser}</p>
  </div>
  <div class="summary">
    <div><span class="rate">${report.summary.conformityRate}%</span><br/>Taux de conformité</div>
    <div>✅ ${report.summary.conforme} conforme</div>
    <div>❌ ${report.summary.nonConforme} non conforme</div>
    <div>⚪ ${report.summary.nonApplicable} non applicable</div>
  </div>
  <h2>Détail par critère</h2>
  ${criteriaHTML}
</body>
</html>`;
}

// ─── Focusable ────────────────────────────────────────────────────────────────

/**
 * Vérifie si un élément est focusable (nativement ou via tabindex).
 */
export async function isFocusable(locator: Locator): Promise<boolean> {
  return locator.evaluate((el: Element) => {
    const tag = el.tagName.toLowerCase();
    const tabindex = el.getAttribute('tabindex');

    // Éléments nativement focusables
    const nativeFocusable = ['a', 'button', 'input', 'select', 'textarea', 'details', 'summary'];
    if (nativeFocusable.includes(tag)) {
      if (tag === 'a' && !el.hasAttribute('href')) return tabindex !== null && tabindex !== '-1';
      if ((el as HTMLInputElement).disabled) return false;
      return tabindex !== '-1';
    }

    // Éléments avec tabindex >= 0
    if (tabindex !== null) {
      return parseInt(tabindex, 10) >= 0;
    }

    // Éléments avec contenteditable
    if (el.hasAttribute('contenteditable') && el.getAttribute('contenteditable') !== 'false') {
      return true;
    }

    return false;
  });
}
