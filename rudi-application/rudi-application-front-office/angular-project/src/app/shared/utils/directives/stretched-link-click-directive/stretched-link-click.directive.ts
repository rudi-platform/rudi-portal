import {Directive, HostListener} from '@angular/core';

/**
 * Le lien invisible ".stretched-link" (qui recouvre toute la card pour la rendre cliquable
 * dans son ensemble) a `pointer-events: none` afin de ne jamais bloquer le survol des éléments
 * affichés au-dessus (textes tronqués avec tooltip). Il ne reçoit donc plus aucun clic natif, ni
 * clic droit (menu contextuel "ouvrir dans un nouvel onglet") : cette directive relaie donc
 * manuellement tout clic sur son élément hôte (hors boutons/liens propres) vers ce lien, en gérant
 * elle-même l'ouverture dans un nouvel onglet pour Ctrl/Cmd+clic et le clic molette (le navigateur
 * ne peut pas le faire nativement puisque le lien réel n'est jamais la cible de l'évènement).
 *
 * A appliquer sur l'élément conteneur de la card (ex: <mat-card appStretchedLinkClick>) qui
 * contient un <a class="stretched-link">.
 */
@Directive({
    selector: '[appStretchedLinkClick]',
    standalone: true
})
export class StretchedLinkClickDirective {

    @HostListener('click', ['$event'])
    onClick(event: MouseEvent): void {
        if (event.defaultPrevented || event.button !== 0) {
            return;
        }
        const target = event.target as HTMLElement;
        if (target.closest('button, a, input, [role="button"]')) {
            return;
        }
        const stretchedLink = this.getStretchedLink(event.currentTarget as HTMLElement);
        if (!stretchedLink) {
            return;
        }
        if (event.ctrlKey || event.metaKey || event.shiftKey) {
            // Le navigateur ne peut pas gérer nativement l'ouverture dans un nouvel onglet
            // (pointer-events: none sur le lien réel) : on la reproduit manuellement.
            window.open(stretchedLink.href, '_blank', 'noopener');
            return;
        }
        stretchedLink.click();
    }

    /** Clic molette : ouvre également dans un nouvel onglet, pour la même raison que ci-dessus. */
    @HostListener('auxclick', ['$event'])
    onAuxClick(event: MouseEvent): void {
        if (event.defaultPrevented || event.button !== 1) {
            return;
        }
        const target = event.target as HTMLElement;
        if (target.closest('button, a, input, [role="button"]')) {
            return;
        }
        const stretchedLink = this.getStretchedLink(event.currentTarget as HTMLElement);
        if (stretchedLink) {
            event.preventDefault();
            window.open(stretchedLink.href, '_blank', 'noopener');
        }
    }

    private getStretchedLink(host: HTMLElement): HTMLAnchorElement | null {
        return host?.querySelector<HTMLAnchorElement>('a.stretched-link') ?? null;
    }
}
