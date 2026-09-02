/**
 * Règle ESLint custom : heading-hierarchy
 *
 * RGAA 9.1 — La hiérarchie des titres est-elle cohérente ?
 * Dans un template, les niveaux de titres ne doivent pas sauter de niveau
 * (ex: passer de h1 à h3 sans h2 intermédiaire).
 *
 * ✅ <h1>…</h1> puis <h2>…</h2> puis <h3>…</h3>
 * ✅ <h2>…</h2> puis <h2>…</h2>          (même niveau, OK)
 * ✅ <h3>…</h3> puis <h2>…</h2>          (remontée, OK)
 * ❌ <h1>…</h1> puis <h3>…</h3>          (saut de h1 à h3)
 * ❌ <h2>…</h2> puis <h4>…</h4>          (saut de h2 à h4)
 *
 * Note : cette règle détecte les sauts au sein d'un même template.
 * Le niveau de départ n'est pas vérifié car le template peut être un composant enfant.
 */

'use strict';

const HEADING_REGEX = /^h([1-6])$/;

/** @type {import('@angular-eslint/utils').TemplateRuleModule} */
module.exports = {
    meta: {
        type: 'suggestion',
        docs: {
            description: 'Les niveaux de titres (h1-h6) ne doivent pas sauter de niveau (RGAA 9.1).',
            recommended: true,
        },
        schema: [],
        messages: {
            skippedLevel:
                'RGAA 9.1 : Saut de niveau de titre de <h{{ previous }}> à <h{{ current }}>. ' +
                'Les niveaux intermédiaires ne doivent pas être omis.',
        },
    },

    create(context) {
        const headings = [];

        return {
            'Element$1'(node) { collectHeading(node, headings); },
            'Element'(node) { collectHeading(node, headings); },
            'Program:exit'() { checkHierarchy(context, headings); },
        };
    },
};

function collectHeading(node, headings) {
    const match = node.name && HEADING_REGEX.exec(node.name);
    if (match) {
        headings.push({
            node,
            level: parseInt(match[1], 10),
        });
    }
}

function checkHierarchy(context, headings) {
    if (headings.length < 2) return;

    // Trier par position dans le source (ligne, colonne)
    headings.sort((a, b) => {
        const aLoc = a.node.sourceSpan || a.node.loc;
        const bLoc = b.node.sourceSpan || b.node.loc;
        if (!aLoc || !bLoc) return 0;
        const aLine = aLoc.start ? aLoc.start.line : (aLoc.startLine || 0);
        const bLine = bLoc.start ? bLoc.start.line : (bLoc.startLine || 0);
        if (aLine !== bLine) return aLine - bLine;
        const aCol = aLoc.start ? aLoc.start.col : (aLoc.startCol || 0);
        const bCol = bLoc.start ? bLoc.start.col : (bLoc.startCol || 0);
        return aCol - bCol;
    });

    for (let i = 1; i < headings.length; i++) {
        const prev = headings[i - 1].level;
        const curr = headings[i].level;
        // Un saut se produit quand on descend de plus d'un niveau
        if (curr > prev + 1) {
            context.report({
                node: headings[i].node,
                messageId: 'skippedLevel',
                data: {
                    previous: String(prev),
                    current: String(curr),
                },
            });
        }
    }
}
