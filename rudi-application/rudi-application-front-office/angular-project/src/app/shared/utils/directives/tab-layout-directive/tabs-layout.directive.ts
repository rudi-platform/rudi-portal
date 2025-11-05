import {Directive} from '@angular/core';

/**
 * Utilisé pour personnaliser l'affichage du contenu des onglets.
 * @see TabsComponent
 */
@Directive({
    selector: '[appTabsLayout]',
    standalone: false
})
export class TabsLayoutDirective {
}
