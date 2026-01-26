import {Injectable} from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class HtmlService {

    constructor() {
    }

    stripHtml(html: string): string {
        if (!html) {
            return '';
        }

        // On utilise le parser natif du navigateur
        const parser = new DOMParser();
        const doc = parser.parseFromString(html, 'text/html');

        // textContent récupère le texte de tous les nœuds enfants (p, li, blockquote...)
        // et les concatène.
        let text = doc.body.textContent || '';

        // Nettoyage final :
        // 1. On remplace les suites d'espaces blancs (espaces, tab, sauts de ligne) par un seul espace
        // 2. On trim les bords
        return text.replace(/\s+/g, ' ').trim();
    }
}
