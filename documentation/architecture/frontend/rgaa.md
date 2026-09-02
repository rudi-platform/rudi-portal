# RGAA — Checklist accessibilité projet RUDI

> **Référentiel Général d'Amélioration de l'Accessibilité (RGAA) v4.1.2**
> Source officielle : <https://accessibilite.numerique.gouv.fr/methode/criteres-et-tests/>
>
> Ce document synthétise les critères RGAA les plus impactants pour le front Angular de RUDI, avec des recommandations concrètes adaptées à la stack technique (Angular 19 + Angular Material).

---

## 1. Images (Thématique 1)

| Critère | Règle | Application RUDI |
|---------|-------|------------------|
| 1.1 | Toute image porteuse d'information a une alternative textuelle | Ajouter `alt="..."` sur les `<img>`, `aria-label` sur les `<mat-icon>` significatives |
| 1.2 | Les images de décoration sont ignorées par les technologies d'assistance | `alt=""` ou `aria-hidden="true"` sur les icônes purement décoratives |
| 1.8 | Remplacer les images texte par du texte stylé quand c'est possible | Préférer du CSS/HTML au lieu d'images contenant du texte |

**Exemple — icône décorative :**
```html
<!-- ❌ Avant -->
<mat-icon>search</mat-icon>

<!-- ✅ Après -->
<mat-icon aria-hidden="true">search</mat-icon>
```

**Exemple — icône porteuse d'information :**
```html
<!-- ❌ Avant -->
<mat-icon>delete</mat-icon>

<!-- ✅ Après -->
<mat-icon aria-label="Supprimer">delete</mat-icon>
```

---

## 2. Couleurs et contrastes (Thématique 3)

| Critère | Règle | Application RUDI |
|---------|-------|------------------|
| 3.1 | L'information ne doit pas être donnée uniquement par la couleur | Ajouter un pictogramme, texte ou motif en complément de la couleur (ex : statuts, erreurs) |
| 3.2 | Contraste texte/fond ≥ 4.5:1 (texte normal) ou ≥ 3:1 (grand texte ≥ 24px ou 18.5px gras) | Vérifier avec DevTools ou un outil comme Colour Contrast Analyser |
| 3.3 | Contraste des composants d'interface ≥ 3:1 | Bordures de champs, boutons, icônes interactives |

**Outils de vérification :**
- Chrome DevTools > Inspect > contraste affiché dans le color picker
- Extension [axe DevTools](https://www.deque.com/axe/devtools/)
- [Colour Contrast Analyser (CCA)](https://www.tpgi.com/color-contrast-checker/)

---

## 3. Liens (Thématique 6)

| Critère | Règle | Application RUDI |
|---------|-------|------------------|
| 6.1 | Chaque lien est explicite (hors cas particuliers) | Éviter « cliquez ici », préférer « Accéder au jeu de données X » |
| 6.2 | Chaque lien a un intitulé | Liens image : `aria-label` ou `alt` sur l'image du lien |

---

## 4. Scripts et composants interactifs (Thématique 7)

| Critère | Règle | Application RUDI |
|---------|-------|------------------|
| 7.1 | Chaque composant interactif est compatible avec les technologies d'assistance | Utiliser les rôles ARIA appropriés, les composants Angular Material ont un bon support natif |
| 7.3 | Contrôlable au clavier et au pointeur | Tout élément cliquable doit être atteignable par Tab et activable par Entrée/Espace |
| 7.5 | Les messages de statut sont restitués par les technologies d'assistance | Utiliser `aria-live="polite"` ou le CDK `LiveAnnouncer` pour les notifications |

**Exemple — notification accessible :**
```typescript
import { LiveAnnouncer } from '@angular/cdk/a11y';

constructor(private liveAnnouncer: LiveAnnouncer) {}

onSave(): void {
    // ...
    this.liveAnnouncer.announce('Projet enregistré avec succès');
}
```

---

## 5. Éléments obligatoires (Thématique 8)

| Critère | Règle | Application RUDI |
|---------|-------|------------------|
| 8.1 | Doctype HTML5 | `<!DOCTYPE html>` dans `index.html` ✅ |
| 8.3–8.4 | Langue par défaut déclarée et pertinente | `<html lang="fr">` dans `index.html` |
| 8.5–8.6 | Titre de page présent et pertinent | Chaque route doit définir un `<title>` dynamique via `PageTitleService` |
| 8.9 | Pas de balises utilisées uniquement à des fins de présentation | Ne pas utiliser `<table>` pour la mise en page, `<br>` pour l'espacement |

---

## 6. Structure de l'information (Thématique 9)

| Critère | Règle | Application RUDI |
|---------|-------|------------------|
| 9.1 | Structure par titres (`h1`–`h6`) correcte et hiérarchique | Un seul `<h1>` par page, pas de saut de niveau (`h2` → `h4`) |
| 9.2 | Structure du document cohérente | Utiliser `<main>`, `<nav>`, `<header>`, `<footer>`, `<section>` |
| 9.3 | Listes correctement structurées | Utiliser `<ul>`, `<ol>`, `<dl>` pour les listes |

---

## 7. Présentation (Thématique 10)

| Critère | Règle | Application RUDI |
|---------|-------|------------------|
| 10.7 | Prise de focus visible | Ne jamais supprimer `outline` sans fournir un style de focus alternatif. Vérifier `:focus-visible` |
| 10.8 | Contenus cachés ignorés par les AT | `display: none` ou `visibility: hidden` sont bien ignorés. `opacity: 0` seul ne suffit pas |
| 10.11 | Pas de scroll horizontal à 320px de large | Responsive design, tester en viewport 320px |

**Anti-pattern :**
```css
/* ❌ Ne jamais faire */
*:focus { outline: none; }

/* ✅ Style de focus personnalisé */
*:focus-visible {
    outline: 2px solid #005fcc;
    outline-offset: 2px;
}
```

---

## 8. Formulaires (Thématique 11) ⚠️ Très impactant pour RUDI

| Critère | Règle | Application RUDI |
|---------|-------|------------------|
| 11.1 | Chaque champ a une étiquette | `<mat-label>` dans `<mat-form-field>`, ou `aria-label` sur les inputs sans label visible |
| 11.2 | L'étiquette est pertinente | Le label décrit clairement le champ |
| 11.5–11.6 | Champs de même nature regroupés avec légende | `<fieldset>` + `<legend>`, ou `role="group"` + `aria-labelledby` |
| 11.9 | Intitulé de chaque bouton pertinent | Pas de bouton vide ou « OK », préférer « Enregistrer le projet » |
| 11.10 | Contrôle de saisie pertinent | Messages d'erreur associés au champ via `<mat-error>` (déjà lié par `aria-describedby` avec Angular Material) |
| 11.13 | Autocomplete sur les champs utilisateur | `autocomplete="email"`, `autocomplete="name"`, etc. |

**Exemple — champ de formulaire accessible :**
```html
<!-- ✅ Angular Material gère le lien label/input -->
<mat-form-field>
    <mat-label>Adresse e-mail de contact</mat-label>
    <input matInput formControlName="contactEmail" autocomplete="email">
    <mat-error>L'adresse e-mail est requise</mat-error>
</mat-form-field>
```

---

## 9. Navigation (Thématique 12)

| Critère | Règle | Application RUDI |
|---------|-------|------------------|
| 12.7 | Lien d'évitement vers le contenu principal | Ajouter un lien « Aller au contenu » en haut de page, visible au focus |
| 12.8 | Ordre de tabulation cohérent | Vérifier que `tabindex` ne casse pas l'ordre logique |
| 12.9 | Pas de piège au clavier | Vérifier que les modales, popups et overlays peuvent être fermées au clavier (Échap) |

**Exemple — lien d'évitement :**
```html
<!-- Dans app.component.html, tout en haut -->
<a class="skip-link" href="#main-content">Aller au contenu principal</a>
<!-- ... -->
<main id="main-content">...</main>
```
```css
.skip-link {
    position: absolute;
    top: -40px;
    left: 0;
    z-index: 1000;
    padding: 8px 16px;
    background: #005fcc;
    color: white;
}
.skip-link:focus {
    top: 0;
}
```

---

## 10. Consultation (Thématique 13)

| Critère | Règle | Application RUDI |
|---------|-------|------------------|
| 13.1 | Contrôle des limites de temps | Si une session expire, prévenir l'utilisateur et permettre de prolonger |
| 13.2 | Pas d'ouverture de fenêtre sans action utilisateur | Pas de `window.open()` automatique |

---

## Outils recommandés

| Outil | Usage |
|-------|-------|
| [axe DevTools](https://www.deque.com/axe/devtools/) | Audit automatisé dans le navigateur |
| [WAVE](https://wave.webaim.org/) | Évaluation visuelle de l'accessibilité |
| [Lighthouse](https://developer.chrome.com/docs/lighthouse/) | Audit intégré à Chrome DevTools (onglet Accessibility) |
| [Pa11y](https://pa11y.org/) | Tests automatisés en CLI, intégrable à la CI |
| Navigation clavier (Tab, Shift+Tab, Entrée, Échap) | Test manuel indispensable |
| Lecteur d'écran (NVDA gratuit, VoiceOver sur Mac) | Vérification de la restitution vocale |

---

## Ressources

- [RGAA 4.1.2 — Critères et tests (source officielle)](https://accessibilite.numerique.gouv.fr/methode/criteres-et-tests/)
- [RGAA 4.1.2 — Glossaire](https://accessibilite.numerique.gouv.fr/methode/glossaire/)
- [Angular CDK Accessibility (a11y)](https://material.angular.io/cdk/a11y/overview)
- [WAI-ARIA Authoring Practices](https://www.w3.org/WAI/ARIA/apd/)
- [WCAG 2.1 (norme internationale sous-jacente au RGAA)](https://www.w3.org/TR/WCAG21/)

> **Note :** Le RGAA v5 est prévu fin 2026. Les travaux de mise en conformité actuels restent pleinement pertinents et ne doivent pas être reportés.
