import {Component, Input} from '@angular/core';

import {ContactButtonComponent} from '@shared/business/contacts/contact-button/contact-button.component';
import {CopiedButtonComponent} from '@shared/core/common/copied-button/copied-button.component';

@Component({
    selector: 'app-contact-card',
    templateUrl: './contact-card.component.html',
    imports: [ContactButtonComponent, CopiedButtonComponent]
})
export class ContactCardComponent {

    /**
     * Variable à copier
     */
    @Input() toCopy: string;

    /**
     * affiche l'oeil de visibilité si true ( input de type paswd )
     */
    @Input() masked: boolean;

    /**
     * l'email à copier
     */
    @Input() email: string;

    /**
     * copied-button ou contact-button ?
     */
    @Input() copiedButton: boolean;
}
